package com.droidzepp.droidzepp.uiclasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.droidzepp.droidzepp.R;

import java.util.ArrayList;

public class ActionsListViewArrayAdapter extends ArrayAdapter<RecordedActionListElement> {
    private Context mContext;
    private ArrayList<RecordedActionListElement> actionList;

    public ActionsListViewArrayAdapter(Context context, int textViewResourceID, ArrayList<RecordedActionListElement> actionList){
        super(context, textViewResourceID, actionList);
        this.mContext = context;
        this.actionList = new ArrayList<>();
        this.actionList.addAll(actionList);
    }

    public void updateContent(ArrayList<RecordedActionListElement> actionList) {
        this.actionList.clear();
        this.actionList.addAll(actionList);
        this.notifyDataSetChanged();
    }

    public class ViewHolder{
        TextView actionName;
        TextView lId;

        public String getActionName() {
            return actionName.getText().toString();
        }

        public TextView getlId() {
            return lId;
        }
    }


    @Override
    public int getCount() {
        return (null != actionList ? actionList.size() : 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.actions_list_item, null);

            holder = new ViewHolder();
            holder.actionName = (TextView) convertView.findViewById(R.id.action_title);
            holder.lId = (TextView) convertView.findViewById(R.id.action_lId);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        RecordedActionListElement recordedAction = actionList.get(position);
        holder.actionName.setText(recordedAction.getActionName());
        holder.lId.setText(String.valueOf(recordedAction.getlId()));
        return convertView;
    }
}
