package com.droidzepp.droidzepp;

import org.junit.Test;

public class HelperFunctionsTest {
    @Test
    public void shouldBeAbleToGetCurrentDateAndTime(){
        String simpleDate = HelperFunctions.getDateTime();
        assert simpleDate != null && !simpleDate.equals("");
    }
}
