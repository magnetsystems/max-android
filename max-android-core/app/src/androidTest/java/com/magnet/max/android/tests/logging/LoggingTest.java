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
package com.magnet.max.android.tests.logging;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import com.google.gson.Gson;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.logging.DefaultLoggerOptions;
import com.magnet.max.android.logging.EventLog;
import com.magnet.max.android.logging.Logger;

import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;

import com.magnet.max.android.tests.utils.MaxHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingTest extends AndroidTestCase {
    private Logger mLogger;
    private String TAG;
    private Context testContext;
    private DefaultLoggerOptions loggerOptions;
    private File logDir;

     public void setUp() throws Exception {
         super.setUp();

         MaxHelper.initMax(getContext(), com.magnet.max.android.tests.R.raw.keys);

        testContext = getContext().getApplicationContext();
        loggerOptions = new DefaultLoggerOptions();
        loggerOptions.setRemoteLoggingEnabled(true);
        loggerOptions.setRollingFileFrequencyInMinutes(1);
        TAG = this.getName();
        mLogger = Logger.getInstance(this.getClass());
         Logger.startLogging(loggerOptions);

        logDir = new File(Environment.getDataDirectory().getPath() +
                "/data/com.magnet.max.android.tests/files/logs");
    }

    public void test01StringLog() throws Exception {
        cleanLogDir();

        for(int i = 0; i < 5; i++) {
            mLogger.d("TAG", "Debug log message number : " + i);
            Thread.sleep(1000);
        }

        for(int i = 0; i < 5; i++) {
            mLogger.i("TAG", "Information log message number : " + i);
            Thread.sleep(1000);
        }

        for(int i = 0; i < 5; i++) {
            mLogger.e("TAG", "Error log message number : " + i);
            Thread.sleep(1000);
        }

        Thread.sleep(60*1000);

        mLogger.d("TAG", "Debug log message after 1 min");

        File[] logFiles = logDir.listFiles();
        assertThat(logFiles.length).isGreaterThanOrEqualTo(1);

        File logFile = logFiles[logFiles.length - 1];
        FileReader fileReader = new FileReader(logFile);
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;

        List<Data> dataList = new ArrayList<Data>();
        while ((line = br.readLine()) != null) {
            System.out.println("^^^^ line = " + line);
            if (!line.isEmpty()) {
                Data data = new Gson().fromJson(line, Data.class);
                dataList.add(data);
            }
        }
        fileReader.close();
        br.close();

        System.out.println("^^^^^ number of data in list: " + dataList.size());
        assertThat(dataList.size()).isGreaterThanOrEqualTo(14);

        for (int i = 0; i < 5; i++) {
            assertEquals("TAG", dataList.get(i).getCategory());
            assertNotNull(dataList.get(i).getIdentifier());
            assertEquals("TAG", dataList.get(i).getName());
            assertEquals("DEBUG", dataList.get(i).getSubCategory());
            assertEquals("event", dataList.get(i).getType());
            assertEquals(Arrays.asList("DEBUG", "TAG", "SYSTEM"), dataList.get(i).getTags());
            System.out.println("**** message = " + dataList.get(i).getPayload().get("__message"));
            assertEquals("Debug log message number : " + i, dataList.get(i).getPayload().get("__message"));
        }

        for (int i = 5; i < 10; i++) {
            assertEquals("TAG", dataList.get(i).getCategory());
            assertNotNull(dataList.get(i).getIdentifier());
            assertEquals("TAG", dataList.get(i).getName());
            assertEquals("INFO", dataList.get(i).getSubCategory());
            assertEquals("event", dataList.get(i).getType());
            assertEquals(Arrays.asList("INFO","TAG","SYSTEM"), dataList.get(i).getTags());
            System.out.println("**** message = " + dataList.get(i).getPayload().get("__message"));
            assertEquals("Information log message number : " + (i-5), dataList.get(i).getPayload().get("__message"));
        }

        for (int i = 10; i < 14; i++) {
            assertEquals("TAG", dataList.get(i).getCategory());
            assertNotNull(dataList.get(i).getIdentifier());
            assertEquals("TAG", dataList.get(i).getName());
            assertEquals("ERROR", dataList.get(i).getSubCategory());
            assertEquals("event", dataList.get(i).getType());
            assertEquals(Arrays.asList("ERROR","TAG","SYSTEM"), dataList.get(i).getTags());
            System.out.println("**** message = " + dataList.get(i).getPayload().get("__message"));
            assertEquals("Error log message number : " + (i-10), dataList.get(i).getPayload().get("__message"));
        }
    }

    @Suppress
    public void test02EventLog() throws Exception {
        cleanLogDir();

        Map<String, String> payload = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            payload.put("logMessange", "Log message number " + i);
            payload.put("logAttemptCount", String.valueOf(i));
            mLogger.logEvent(new EventLog.EventBuilder().category("Event")
                .name("LogSuccess")
                .payload(payload)
                .subCategory("customSubcategory")
                .tags(Arrays.asList("CUSTOMTAG1", "CUSTOMTAG2", "CUSTOMTAG3"))
                .build());
            Thread.sleep(1000);
        }
        Thread.sleep(60 * 1000);

        File[] logFiles = logDir.listFiles();
        assertThat(logFiles.length).isGreaterThanOrEqualTo(1);

        File logFile = logFiles[logFiles.length - 1];

        FileReader fileReader = new FileReader(logFile);
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;

        List<Data> dataList = new ArrayList<Data>();
        while ((line = br.readLine()) != null) {
            System.out.println("^^^^^ line: " + line);
            if (!line.isEmpty()) {
                Data data = new Gson().fromJson(line, Data.class);
                dataList.add(data);
            }
        }

        System.out.println("^^^^^ number of data in list: " + dataList.size());
        assertTrue(dataList.size() >= 9);
        fileReader.close();
        br.close();

        for (int i = 1; i < 9; i++) {
            assertEquals("Event", dataList.get(i).getCategory());
            assertNotNull(dataList.get(i).getIdentifier());
            assertEquals("LogSuccess", dataList.get(i).getName());
            assertEquals("customSubcategory", dataList.get(i).getSubCategory());
            assertEquals("event", dataList.get(i).getType());
            assertEquals(Arrays.asList("CUSTOMTAG1", "CUSTOMTAG2", "CUSTOMTAG3"),
                dataList.get(i).getTags());
            //assertNotNull(dataList.get(i).getDate());
            System.out.println("**** message = " + dataList.get(i).getPayload().get("logMessange"));
        }
    }

    private void cleanLogDir() {
        File[] existingFiles = logDir.listFiles();
        if(null != existingFiles) {
            for (File file : logDir.listFiles()) {
                Log.d(TAG, "Log file : " + file.getName());
                //file.delete();
            }
        }
    }

}

class Data {
    String category;
    String identifier;
    String name;
    Map<String,String> payload;
    String subCategory;
    List<String> tags;
    String type;
    String date;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
