package com.pastekit.tools.kitten;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class KittenTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public KittenTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( KittenTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws IOException {
        assertTrue(true);
    }


}
