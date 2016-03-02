package com.droidzepp.droidzepp.uiclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.droidzepp.droidzepp.R;
import com.droidzepp.droidzepp.alarm.AlarmObject;

public class AlarmFrequencyDialogFragment extends DialogFragment {
    Context context;
    AlarmObject newAlarm;

    public AlarmFrequencyDialogFragment(Context context, AlarmObject newAlarm) {
        this.context = context;
        this.newAlarm = newAlarm;
    }
    public interface AlarmFrequencyDialogListener {
        void onFrequencyItemClick(DialogInterface dialog, AlarmObject newAlarm);
    }

    AlarmFrequencyDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String frequencies[] = {"Every day", "Every 3 days", "Every 5 days", "Every week"};
        builder.setTitle(R.string.choose_frequency)
                .setItems(frequencies, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        newAlarm.setFrequency(which);
                        mListener.onFrequencyItemClick(dialog, newAlarm);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AlarmFrequencyDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }
}
