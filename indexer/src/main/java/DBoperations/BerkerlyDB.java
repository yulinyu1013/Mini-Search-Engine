package DBoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class BerkerlyDB {
        
        private String directory; //check the logic of directory
        private Environment myEnv;
        private EntityStore store;
        private File homedir;
        

        
        public BerkerlyDB(String directory) {
            if (!Files.exists(Paths.get(directory))) {
                try {
                    Files.createDirectory(Paths.get(directory));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            this.directory = directory;
            this.homedir = new File(this.directory);
        }
        
        public void setUpEnv() {
            this.createEnvironment();
//            this.openAccessors();
        }
        
        private void createEnvironment() {
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();
            
            myEnvConfig.setAllowCreate(true);
            myEnvConfig.setTransactional(true);
            
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(true);
            
            //create env & store
            myEnv = new Environment(this.homedir, myEnvConfig);
            store = new EntityStore(myEnv, "EntityStore", storeConfig); 
        }
        
//        private void openAccessors() {
//            userByName = store.getPrimaryIndex(String.class, UserInfo.class);
//            docByURL = store.getPrimaryIndex(String.class, Documents.class);
//            contentSeenByhash = store.getPrimaryIndex(String.class, ContentSeen.class);
//        }
        
        public void close() {
            if (store != null) {
                try {
                    store.close();
                } catch(DatabaseException dbe) {
                    System.err.println("Error closing store: " + 
                                        dbe.toString());
                }
            }

            if (myEnv != null) {
                try {
                    myEnv.close();
                } catch(DatabaseException dbe) {
                    System.err.println("Error closing MyDbEnv: " + 
                                        dbe.toString());
                }
            }
        }
        
        public EntityStore getEntityStore() {
            return store;
        }

        public Environment getEnv() {
            return myEnv;
        }


}
