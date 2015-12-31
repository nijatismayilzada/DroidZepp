package com.droidzepp.droidzepp;

import com.droidzepp.droidzepp.classification.FeatureContainer;

import org.junit.Test;

public class FeatureContainerTest {
    @Test
    public void shouldRegisterFeatureContainer(){
        FeatureContainer dataFeatures = new FeatureContainer();
        long lId = 1;
        dataFeatures.setLid(lId);
        assert dataFeatures != null;
        assert dataFeatures.getLid() == lId;
    }

    @Test
    public void shouldSetTimeToFeatureContainer (){
        FeatureContainer dataFeatures = new FeatureContainer();
        String time = HelperFunctions.getDateTime();
        dataFeatures.setTime(time);
        assert dataFeatures.getTime() == time;
    }

    @Test
    public void shouldSetFeatureToFeatureContainer(){
        FeatureContainer dataFeatures = new FeatureContainer();
        float feature = 10;
        dataFeatures.setA(feature);
        assert dataFeatures.getA() == feature;
    }
}
