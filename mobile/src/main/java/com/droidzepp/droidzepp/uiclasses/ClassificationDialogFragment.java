package com.droidzepp.droidzepp.uiclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.droidzepp.droidzepp.R;

public class ClassificationDialogFragment extends DialogFragment {

    Context context;
    EditText labelName;
    String classificationResult = "";

    public ClassificationDialogFragment(Context context) {
        this.context = context;
    }

    public ClassificationDialogFragment(Context context, String classificationResult) {
        this.context = context;
        this.classificationResult = classificationResult;
    }

    public interface ClassificationDialogListener {
        void onClassificationDialogPositiveClick(DialogFragment dialog, String addedLabel);
        void onClassificationDialogNeutralClick(DialogFragment dialog);
        void onClassificationDialogNegativeClick(DialogFragment dialog);
    }

    ClassificationDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_action, null);
        labelName = (EditText) dialogView.findViewById(R.id.action_label);
        labelName.setText(classificationResult);
        builder.setView(dialogView)
                .setMessage(R.string.dialog_save_name_of_action)
                .setPositiveButton(R.string.save_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String addedLabel = labelName.getText().toString().trim();
                        mListener.onClassificationDialogPositiveClick(ClassificationDialogFragment.this, addedLabel);
                    }
                })
                .setNeutralButton(R.string.classify_the_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onClassificationDialogNeutralClick(ClassificationDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onClassificationDialogNegativeClick(ClassificationDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ClassificationDialogListener) activity;
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
