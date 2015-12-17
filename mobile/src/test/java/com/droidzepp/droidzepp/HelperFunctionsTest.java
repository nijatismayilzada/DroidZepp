package com.droidzepp.droidzepp;

import org.junit.Test;

/**
 * Created by nijat on 16/12/15.
 */
public class HelperFunctionsTest {
    @Test
    public void shouldBeAbleToGetCurrentDateAndTime(){
        String simpleDate = HelperFunctions.getDateTime();
        assert simpleDate != null && !simpleDate.equals("");
    }
}
