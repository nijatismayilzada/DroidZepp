package com.droidzepp.droidzepp;

/**
 * Created by nijat on 07/02/16.
 */
public class RecordedActionListElement {

    private String actionName;
    private long lId;

    public RecordedActionListElement(String actionName, long lId) {
        this.actionName = actionName;
        this.lId = lId;
    }

    public String getActionName() {
        return actionName;
    }

    public long getlId() {
        return lId;
    }
}
