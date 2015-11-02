//package com.magnet.max.android.tests;
//
//import com.magnet.persistence.EntityContext;
//import com.magnet.persistence.EntityContextType;
//import com.magnet.persistence.android.AndroidDataClient;
//import com.magnet.persistence.android.AndroidDataStoreHelper;
//import com.magnet.persistence.android.EntityCollection;
//import com.magnet.persistence.android.PersistenceAsyncCallback;
//import com.magnet.persistence.model.Entity;
//import com.magnet.persistence.model.Id;
//import com.magnet.persistence.model.Property;
//
//import java.util.List;
//import java.util.UUID;
//
///**
// * Created by pshah on 5/13/15.
// */
//public class UserTestCase extends AsynchronousTestCase {
//
//    public void testCreate() throws Exception {
//        try {
//            AndroidDataClient androidDataClient = new AndroidDataClient(getContext(), this.getClass().getClassLoader().getResourceAsStream("META-INF/persistence/persistence-metadata.xml"));
//            AndroidDataStoreHelper.deleteLocalDataStore(getContext(), "MyDataStore"); // delete existing database
//            androidDataClient.initOrOpenLocalDataStore("MyDataStore", false);
//            EntityContext entityContext = androidDataClient.getEntityContextDefinition().createContext(EntityContextType.READ_WRITE);
//            EntityCollection<User> userEntityCollection = new EntityCollection<User>(entityContext.getCollection(User.class, "Users"));
//
//
//            User user = new UserBuilder()
//                    .name("Pritesh")
//                    .build();
//
//            entityContext.add(user);
//            entityContext.flush();
//
//            List<User> users = userEntityCollection.executeList();
//            for (User u : users) {
//                assertEquals("Pritesh", u.getName());
//            }
//
//            userEntityCollection.executeListAsync(new PersistenceAsyncCallback<List<User>>() {
//                @Override
//                public void onSuccess(List<User> users, Object o) {
//                    assertEquals("Pritesh", users.get(0).getName());
//                    countDown();
//                }
//
//                @Override
//                public void onError(Exception e, Object o) {
//
//                }
//
//                @Override
//                public void onCancel(Object o) {
//
//                }
//            });
//
//            await();
//
//        } catch (Exception e) {
//            fail(e.getLocalizedMessage());
//        }
//
//    }
//
//    /**
//     * Created by pshah on 5/12/15.
//     */
//    @Entity
//    public interface User {
//
//        @Id
//        @Property(generated = true)
//        UUID getId();
//
//        @Property(maxLength = 100)
//        String getName();
//    }
//}
