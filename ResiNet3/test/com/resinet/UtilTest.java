package com.resinet;

import com.resinet.util.Util;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void testTextIsNotProbability() throws Exception {
        assertFalse(Util.textIsNotProbability("0"));
        assertFalse(Util.textIsNotProbability("0.000"));
        assertFalse(Util.textIsNotProbability("0.0"));
        assertFalse(Util.textIsNotProbability("1"));
        assertFalse(Util.textIsNotProbability("1.00000"));
        assertFalse(Util.textIsNotProbability("0.0000001"));
        assertFalse(Util.textIsNotProbability("0.932133"));
        assertFalse(Util.textIsNotProbability("0.99999999"));

        assertTrue(Util.textIsNotProbability("1.1"));
        assertTrue(Util.textIsNotProbability("1.000001"));
        assertTrue(Util.textIsNotProbability("-1"));
        assertTrue(Util.textIsNotProbability("-0.000"));
        assertTrue(Util.textIsNotProbability("-0"));
        assertTrue(Util.textIsNotProbability(".000"));
        assertTrue(Util.textIsNotProbability(".."));
        assertTrue(Util.textIsNotProbability("10"));
        assertTrue(Util.textIsNotProbability("2.0"));
        assertTrue(Util.textIsNotProbability("2"));
    }
    @Test
    public void testShit() {
        String text = "String\nadasdasdaa\nasdfd";


        assertTrue(text.matches("(.*(\r\n|\r|\n).*)+"));

    }
}