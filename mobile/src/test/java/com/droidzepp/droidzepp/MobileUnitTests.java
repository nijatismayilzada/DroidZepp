package com.droidzepp.droidzepp;

import com.droidzepp.droidzepp.datacollection.XYZ;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MobileUnitTests {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void xyz_isCorrect() throws Exception {
        XYZ myXYZ = new XYZ(4, 5, 6, 7);

        assertEquals(5, myXYZ.getId());
    }
}