package com.alextherapeutics.diga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigaUtilsTest {

    @Test
    void testIkNumberWithoutPrefixContainingIk() {
        var testIK = "IK123456789";
        var expected = "123456789";
        assertEquals(expected, DigaUtils.ikNumberWithoutPrefix(testIK));
    }
    @Test
    void testIkNumberWithoutPrefixNotContainingIk() {
        var testIK = "123456789";
        var expected = "123456789";
        assertEquals(expected, DigaUtils.ikNumberWithoutPrefix(testIK));
    }
    @Test
    void testIkNumberWithPrefixContainingIk() {
        var testIK = "IK123456789";
        var expected = "IK123456789";
        assertEquals(expected, DigaUtils.ikNumberWithPrefix(testIK));
    }
    @Test
    void testIkNumberWithPrefixNotContainingIk() {
        var testIK = "123456789";
        var expected = "IK123456789";
        assertEquals(expected, DigaUtils.ikNumberWithPrefix(testIK));
    }
    @Test
    void testIsDigaTestCode() {
        assertTrue(DigaUtils.isDigaTestCode("77AAAAAAAAAAADEV"));
        assertFalse(DigaUtils.isDigaTestCode("BHAAAAAAAAAAADEV"));
    }
    @Test
    void testBuildPostDigaEndpoint() {
        var url = "diga.bitmarck-daten.de";
        var expected = "https://diga.bitmarck-daten.de/diga";
        assertEquals(expected, DigaUtils.buildPostDigaEndpoint(url));
    }
}