package com.github.anob3it.person.uuid;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class PersonUuidTest {

    @Test
    public void testOrgnr() {
        assertEquals("00556809-9963-1000-9000-d59a20d06c1a", PersonUUID.parse("5568099963").toString());
        assertEquals("00556809-9963-1000-9000-d59a20d06c1a", PersonUUID.parse("556809-9963").toString());
    }

    @Test
    public void testPersnr() {
        assertEquals("19410617-7753-1000-9001-d59a20d06c1a", PersonUUID.parse("194106177753").toString());
        assertEquals("18150526-9272-1000-9001-d59a20d06c1a", PersonUUID.parse("18150526-9272").toString());
    }

    @Test
    public void testSamnr() {
        assertEquals("19701063-2391-1000-9002-d59a20d06c1a", PersonUUID.parse("197010632391").toString());
        assertEquals("19701063-2391-1000-9002-d59a20d06c1a", PersonUUID.parse("19701063-2391").toString());
    }

    @Test
    public void testGdnr() {
        assertEquals("00302000-2568-1000-9003-d59a20d06c1a", PersonUUID.parse("3020002568").toString());
        assertEquals("00302000-2568-1000-9003-d59a20d06c1a", PersonUUID.parse("302000-2568").toString());
    }

    @Test
    public void testIsPersonUUID() {
        assertTrue(PersonUUID.isPersonUUID(UUID.fromString("00556809-9963-1000-9000-d59a20d06c1a")));
        assertTrue(PersonUUID.isPersonUUID(UUID.fromString("19410617-7753-1000-9001-d59a20d06c1a")));
        assertTrue(PersonUUID.isPersonUUID(UUID.fromString("19701063-2391-1000-9002-d59a20d06c1a")));
        assertTrue(PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-9003-d59a20d06c1a")));
        assertFalse("Wrong MAC", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-9003-d49a20d06c1a")));
        assertFalse("Wrong MAC", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-9003-d59a20d06c1b")));
        assertFalse("Wrong version", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-2000-9003-d59a20d06c1a")));
        assertFalse("Wrong reserved bit", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-8003-d59a20d06c1a")));
        assertFalse("Wrong reserved bit", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-9103-d59a20d06c1a")));
        assertFalse("Wrong reserved bit", PersonUUID.isPersonUUID(UUID.fromString("00302000-2568-1000-9013-d59a20d06c1a")));
        assertFalse("Version 1 UUID", PersonUUID.isPersonUUID(UUID.fromString("b5097d86-e118-11e7-80c1-9a214cf093ae")));
        assertFalse("Version 4 UUID", PersonUUID.isPersonUUID(UUID.fromString("5bd4bb5a-d57d-4612-9b8f-f0ad3154cfbd")));
        assertFalse("Nil UUID", PersonUUID.isPersonUUID(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    }
}
