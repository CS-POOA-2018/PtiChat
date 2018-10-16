package com.pooa.ptichat.BackServer.Storage;


public class MemoryStorageTests extends StorageTests {

    @Override
    public IStorage createInstance() {
        return new MemoryStorage();
    }
}
