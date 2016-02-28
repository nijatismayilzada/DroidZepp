package com.droidzepp.droidzepp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.droidzepp.droidzepp.classification.ActionsDatabase;
import com.droidzepp.droidzepp.classification.FeatureContainer;

public class PermDatabaseTest extends ApplicationTestCase<Application> {

    private Application application;

    public PermDatabaseTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        application = getApplication();
    }

    public void testShouldReturnRowNumberOfNewlyAddedAction(){
        ActionsDatabase db = new ActionsDatabase(application.getApplicationContext());
        FeatureContainer newActionFeatures = new FeatureContainer("time", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 100);
        long result = db.addFeatures(newActionFeatures);

        assertNotNull(result);
        assertTrue(result != 0);
    }

    public void testShouldReturnRowNumberOfRecentAddedLabel(){
        ActionsDatabase db = new ActionsDatabase(application.getApplicationContext());
        String label = "running";
        long result = db.addNewLabel(label);

        assertNotNull(result);
        assertTrue(result != 0);
    }

//    public void testShouldGetWholeDataSet(){
//        ActionsDatabase db = new ActionsDatabase(application.getApplicationContext());
//        double[][][] dataSet = db.getDataSet();
//
//        assertNotNull(dataSet);
//        assertTrue(dataSet[1][1][1] != 0);
//    }

//    public void testShouldGetWholeLabelSet(){
//        ActionsDatabase db = new ActionsDatabase(application.getApplicationContext());
//        // On the later stages of the project, labels will be represented as strings
//        int[] labels = db.getLabels();
//
//        assertNotNull(labels);
//        assertTrue(labels[1] != 0);
//    }

}
