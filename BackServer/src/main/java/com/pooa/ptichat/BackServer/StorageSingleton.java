package com.pooa.ptichat.BackServer;

import com.pooa.ptichat.BackServer.NativeSocketServer.ConnectionsManager;
import com.pooa.ptichat.BackServer.Storage.IStorage;
//import com.pooa.ptichat.BackServer.Storage.MemoryStorage;
import com.pooa.ptichat.BackServer.Storage.SqliteStorage;

/** Singleton Pattern, Holder method, as described here:
 * http://thecodersbreakfast.net/index.php?post/2008/02/25/26-de-la-bonne-implementation-du-singleton-en-java
 */
public class StorageSingleton {

    private int mPlopCount;
    private IStorage mStorage = new SqliteStorage();
    private ConnectionsManager mConnectionsManager = new ConnectionsManager();

    /** Private constructor */
    private StorageSingleton() { }

    public synchronized int getNextPlop() {
        mPlopCount++;
        return mPlopCount;
    }

    public IStorage getStorage() {
        return mStorage;
    }

    public ConnectionsManager getConnectionsManager() {
        return mConnectionsManager;
    }

    /** Holder */
    private static class SingletonHolder {
        /** Unique instance, not pre-initialized  */
        private final static StorageSingleton instance = new StorageSingleton();
    }

    /** Access point for the unique instance */
    public static StorageSingleton getInstance() {
        return SingletonHolder.instance;
    }
}
