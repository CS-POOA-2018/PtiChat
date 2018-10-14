package com.pooa.ptichat.BackServer.Storage;

import java.io.File;

public class SqliteStorageTests extends StorageTests {

    private static final String TEST_DB_PATH = "/tmp/MemoryStorageTests.sqlite";

    @Override
    public IStorage createInstance() {
        boolean deleted = new File(TEST_DB_PATH).delete();
        System.out.println("Deletion of " + TEST_DB_PATH + " returned " + deleted);

        return new SqliteStorage("jdbc:sqlite:" + TEST_DB_PATH);
    }
}
