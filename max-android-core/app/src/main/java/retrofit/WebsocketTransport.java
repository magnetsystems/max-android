/*
 * Copyright (c) 2015 Magnet Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.ws.WebSocketRequest;
import com.magnet.max.android.ws.WebsocketStatus;
import com.magnet.max.android.ws.mcp.MCPAck;
import com.magnet.max.android.ws.mcp.MCPCommandGsonAdapter;
import com.magnet.max.android.ws.mcp.commands.ExecuteApiCommand;
import com.magnet.max.android.ws.mcp.commands.ExecuteApiResCommand;
import com.magnet.max.android.ws.mcp.MCPCommand;
import com.magnet.max.android.ws.mcp.MCPEnvelope;
import com.magnet.max.android.ws.mcp.MCPPriority;
import com.magnet.max.android.ws.mcp.MCPRequest;
import com.magnet.max.android.ws.mcp.MCPResponse;
import com.squareup.okhttp.*;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import okio.Buffer;
import okio.BufferedSource;

public class WebsocketTransport implements Closeable {
  private static final String TAG = "WebsocketTransport";

  private static final int MAX_PENDING_REQUEST = 64;
  private static final int MAX_CONTINUS_FAILURE = 3;

  private static final int maxRequests = 64;
  private static final int maxRequestsPerHost = 5;

  /** Executes calls. Created lazily. */
  private ExecutorService executorService;

  final HttpUrl endpoint;
  final Converter converter;

  private final OkHttpClient client;

  private WebSocket webSocket;

  private Gson gson;
  private JsonParser jsonParser;

  private final Deque<MCPRequest> readyCalls = new ArrayDeque();
  private final Map<String, MCPRequest> runningCalls = new ConcurrentHashMap<String, MCPRequest>();
  private final Deque<MCPRequest> executedCalls = new ArrayDeque();

  private AtomicInteger continusFailureCounter = new AtomicInteger(0);

  private boolean isNewConnectionPending;
  private boolean isReadyQueueRunning;
  private String wsSessionId;

  private boolean toShutDown;

  public WebsocketTransport(HttpUrl endpoint, Converter converter, OkHttpClient client) {
    this.endpoint = endpoint;
    this.converter = converter;
    this.client = client;

    this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(new MCPCommandGsonAdapter())
            .registerTypeAdapter(ExecuteApiResCommand.HTTPResponsePayload.class, new ExecuteApiResCommand.HTTPResponsePayload.HTTPResponsePayloadDeserializer())
            .create();
    this.jsonParser = new JsonParser();

    Log.w(TAG, "---------------->establish the connection when WebsocketTransport is created.");
    establishConnection();
  }

  public WebSocketRequest enqueue(final Request request, com.squareup.okhttp.Callback callback) {
    if(toShutDown) {
      throw new IllegalStateException("WebsocketTransport was shut down.");
    }

    WebSocketRequest webSocketRequest = new WebSocketRequest(request, callback, this);
    ExecuteApiCommand command = new ExecuteApiCommand.Builder().request(webSocketRequest, endpoint).build();
    MCPRequest mcpRequest = new MCPRequest.Builder().command(command)
            .executionType(MCPRequest.MCPExecutionType.PARALLEL)
            .priority(MCPPriority.MEDIUM)
            .sender("Magnet Android SDK").build();

    webSocketRequest.setRequestId(mcpRequest.getId());
    webSocketRequest.setCommandId(command.getCid());

    readyCalls.add(mcpRequest);
    Log.d(TAG, "---------------->Adding message to ready queue, queue size : " + readyCalls.size());
    if(null == webSocket) {
      Log.w(TAG, "---------------->Websocket is not initialized, trying to establish the connection");
      establishConnection();
    } else {
      sendReadyQueue();
    }

    return webSocketRequest;
  }

  public void close() throws IOException {
    shutDown(false);
  }

  public void shutDown(boolean gracefully) {
    if(gracefully) {
      if(readyCalls.size() > 0 || runningCalls.size() > 0) {
        toShutDown = true;
        return;
      }
    } else {
      readyCalls.clear();
      runningCalls.clear();
    }

    doShutDown();
  }

  private void doShutDown() {
    getExecutorService().shutdown();
    try {
      webSocket.close(1000, "client shutdown");
    } catch (Exception e) {
      //e.printStackTrace();
    }

    webSocket = null;
  }

  //public CancelStatus cancel(String requestId, String commandId) {
  //  if(null != runningCalls.get(requestId)) {
  //    return CancelStatus.ALREADY_STARTED;
  //    //throw new IllegalStateException("MCPRequest " + requestId + " is running. Can't be cancelled.");
  //  }
  //
  //  MCPRequest request = null;
  //  MCPCommand command = null;
  //  for(MCPRequest r : readyCalls) {
  //    if(r.getId().equals(requestId)) {
  //      for(MCPCommand c : r.getCommands()) {
  //        if(c.getCid().equals(commandId)) {
  //          command = c;
  //          request = r;
  //          break;
  //        }
  //      }
  //    }
  //
  //    if(null != request) {
  //      break;
  //    }
  //  }
  //
  //  if(null == command) {
  //    return CancelStatus.UNKNOWN;
  //  }
  //
  //  command.setStatus(WebsocketStatus.CANCELLED);
  //  if(request.getCommands().size() == 1) {
  //    request.setStatus(WebsocketStatus.CANCELLED);
  //  }
  //
  //  return CancelStatus.CANCELLED;
  //}

  private void sendCommand(final MCPEnvelope envelope) throws IOException {
    if(null != webSocket) {
      getExecutorService().execute(new Runnable() {
        @Override
        public void run() {
          try {
            // Check if it's cancelled
            if (envelope instanceof MCPRequest) {
              MCPRequest request = (MCPRequest) envelope;
              if (request.isCancelled()) {
                readyCalls.remove(envelope);
                return;
              } else {
                Iterator<MCPCommand> it = request.getCommands().iterator();
                while(it.hasNext()) {
                  MCPCommand command = it.next();
                  if(command.getStatus() == WebsocketStatus.CANCELLED) {
                    it.remove();
                  }
                }
              }
            }
            // set/update session id
            envelope.setSid(wsSessionId);
            Log.i(TAG, "---------------->Sending message : " + envelope.getId());
            webSocket.sendMessage(WebSocket.PayloadType.TEXT, new Buffer().writeUtf8(gson.toJson(envelope)));
            readyCalls.remove(envelope);
            if (envelope.getOp().equals(MCPEnvelope.MCPOperationType.REQUEST)) {
              MCPRequest mcpRequest = (MCPRequest) envelope;
              mcpRequest.setStatus(WebsocketStatus.SENT);
              runningCalls.put(mcpRequest.getId(), mcpRequest);
            }
            continusFailureCounter.set(0);
          } catch (IOException e) {
            Log.e(TAG, "---------------->Faile to send message due to : " + e.getMessage());
            if (continusFailureCounter.incrementAndGet() == MAX_CONTINUS_FAILURE) {
              Log.w(TAG, "---------------->Reach max continues sending failure(" + continusFailureCounter.get() + "), trying to re-establish the connection");
              establishConnection();
            }
          }
        }
      });
    }
  }

  private synchronized void establishConnection() {
    if(null != webSocket || isNewConnectionPending) {
      Log.i(TAG, "---------------->New connection is pending...");
      return;
    }
    webSocket = null;
    wsSessionId = null;

    isNewConnectionPending = true;

    Log.i(TAG, "---------------->establishing new connection...");

    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(endpoint.url());
    requestBuilder.get();
    WebSocketCall call =  WebSocketCall.create(client, requestBuilder.build());

    final AtomicReference<IOException> failureRef = new AtomicReference<>();
    call.enqueue(new WebSocketListener() {
      @Override
      public void onOpen(WebSocket webSocket, com.squareup.okhttp.Response response) {
        WebsocketTransport.this.webSocket = webSocket;
        isNewConnectionPending = false;
        Log.i(TAG, "<----------------New connection established...");
        //sendReadyQueue();
      }

      @Override
      public void onMessage(BufferedSource payload, WebSocket.PayloadType type)
              throws IOException {
        if (type == WebSocket.PayloadType.TEXT) {
          String payloadStr = payload.readUtf8();
          payload.close();
          if(MagnetUtils.isStringNotEmpty(payloadStr)) {
            Log.i(TAG, "onMessage \n" + payloadStr);
            JsonElement jsonElement = jsonParser.parse(payloadStr);
            JsonElement opElement = jsonElement.getAsJsonObject().get("op");
            String operationStr = null != opElement ? opElement.getAsString() : null;
            if (null != operationStr) {
              if (MCPEnvelope.MCPOperationType.ACK_CONNECTED.name().equalsIgnoreCase(operationStr) ||
                  MCPEnvelope.MCPOperationType.ACK_RECEIVED.name().equalsIgnoreCase(operationStr)) {
                handleAck(gson.fromJson(jsonElement, MCPAck.class));
              } else if (MCPEnvelope.MCPOperationType.RESPONSE.name().equalsIgnoreCase(operationStr)) {
                MCPResponse response = gson.fromJson(jsonElement, MCPResponse.class);
                Log.i(TAG, "<----------------Received Response for message : " + response.getId());
                handleResponse(response);
              } else {
                Log.e(TAG, "<----------------onMessage : can't regonize command");
              }
            }
          } else {
            Log.i(TAG, "<----------------onMessage : Empty message");
          }
        } else {
          Log.i(TAG, "<----------------onMessage : Received binary message");
          payload.close();
        }

        sendReadyQueue();
      }

      @Override
      public void onPong(Buffer payload) {
        Log.i(TAG, "onPong : " + payload.readUtf8());

        payload.close();

        sendReadyQueue();
      }

      @Override
      public void onClose(int code, String reason) {
        Log.i(TAG, "<----------------Connection closed due to : " + reason);
        try {
          webSocket.close(0, "server closed");
        } catch (IOException e) {
          e.printStackTrace();
        }

        webSocket = null;
        wsSessionId = null;
      }

      @Override
      public void onFailure(IOException e, com.squareup.okhttp.Response response) {
        isNewConnectionPending = false;
        Log.e(TAG, "<----------------Failure : " + (null != e && null != e.getMessage() ? e.getMessage() : "No error message"));
        failureRef.set(e);
      }
    });
  }

  private void handleResponse(MCPResponse response) {
    Map<String, ExecuteApiResCommand> commandMap = new HashMap<String, ExecuteApiResCommand>();
    for (MCPCommand command : response.getCommands()) {
      // TODO : only handle executeApi command response for now
      if (command.getName().equalsIgnoreCase(ExecuteApiResCommand.COMMAND_NAME)) {
        commandMap.put(command.getCid(), (ExecuteApiResCommand) command);
      }
    }
    //Find the WebsocketRequest
    MCPRequest mcpRequest = runningCalls.get(response.getId());
    if (null != mcpRequest) {
      int respondedCommandCounter = 0;
      for (MCPCommand command : mcpRequest.getCommands()) {
        if (command.getStatus() == WebsocketStatus.RESPONDED) {
          respondedCommandCounter++;
        }
        if (command.getName().equalsIgnoreCase(ExecuteApiCommand.COMMAND_NAME)) {
          // call back
          if (command.getStatus() == WebsocketStatus.SENT && commandMap.containsKey(command.getCid())) {
            ExecuteApiCommand executeApiCommand = (ExecuteApiCommand) command;
            if (null != executeApiCommand.getWebSocketRequest().getCallback()) {
              try {
                executeApiCommand.getWebSocketRequest().getCallback().onResponse(commandMap.get(command.getCid()).getResponse(executeApiCommand.getWebSocketRequest().getRequest()));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
            command.setStatus(WebsocketStatus.RESPONDED);

            respondedCommandCounter++;
          }
        }
      }

      // remove the request if all commonds are responded
      if (respondedCommandCounter == mcpRequest.getCommands().size()) {
        runningCalls.remove(mcpRequest.getId());
      }
    }

    if(toShutDown) {
      shutDown(true);
    }
  }

  private void handleAck(MCPAck ack) {
    if(ack.getOp().equals(MCPEnvelope.MCPOperationType.ACK_CONNECTED)) {
      // ack for new session
      wsSessionId = ack.getSid();
      Log.i(TAG, "<----------------Received ACK for new session : " + ack.getSid());
    } else {
      Log.i(TAG, "<----------------Received ACK for message : " + ack.getId());
    }
  }

  private void sendReadyQueue() {
    Log.d(TAG, "---------------->Sending message from ready queue, queue size : " + readyCalls.size()
            + " runningCalls size : " + runningCalls.size()
            + ", isReadyQueueRunning : " + isReadyQueueRunning);
    if(!readyCalls.isEmpty() && !isReadyQueueRunning) {
      isReadyQueueRunning = true;

      if(null == wsSessionId) {
        // seesion not established yet
        Log.w(TAG, "won't send ready queue because seesion is not established yet.");
        return;
      }

      Iterator i = readyCalls.iterator();
      do {
        if(!i.hasNext()) {
          break;
        }

        if(runningCalls.size() < MAX_PENDING_REQUEST) {
          try {
            MCPRequest request = (MCPRequest) i.next();
            if(!request.isCancelled()) {
              sendCommand(request);
            } else {
              i.remove();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      } while(runningCalls.size() < MAX_PENDING_REQUEST);

      isReadyQueueRunning = false;
    }
  }

  private synchronized ExecutorService getExecutorService() {
    if (executorService == null) {
      executorService = new ThreadPoolExecutor(0, MAX_PENDING_REQUEST, 60, TimeUnit.SECONDS,
              new SynchronousQueue<Runnable>(), Util.threadFactory("Magnet Websocket Dispatcher", false));
    }
    return executorService;
  }

  /**
   * Trim the url path to for the web socket hand shake request
   * @param urlStr
   * @return
   */
  private String trimUrlPath(String urlStr) {
    try {
      URI uri = new URI(urlStr);
      return uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return urlStr;
  }

}
