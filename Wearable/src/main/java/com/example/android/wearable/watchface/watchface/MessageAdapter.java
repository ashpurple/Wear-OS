package com.example.android.wearable.watchface.watchface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.wearable.watchface.R;

import java.util.Vector;

public class MessageAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Message message;
    public MessageAdapter( Message message, LayoutInflater layoutInflater) {
        this.message = message;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return message.size();
    }

    @Override
    public Object getItem(int position) {
        return message.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MessageAdapter.MessageHolder messageHolder;
        if (convertView == null) {
            messageHolder = new MessageAdapter.MessageHolder();
            convertView = layoutInflater.inflate(R.layout.item_beacon, parent, false);
            messageHolder.user_id = convertView.findViewById(R.id.userID);
            messageHolder.user_name = convertView.findViewById(R.id.userName);
            messageHolder.group_code = convertView.findViewById(R.id.groupCode);
            messageHolder.group_name =convertView.findViewById(R.id.groupName);
            convertView.setTag(messageHolder);
        } else {
            messageHolder = (MessageAdapter.MessageHolder)convertView.getTag();
        }
        if(messageHolder.user_id!=null) {
            messageHolder.user_id.setText("사용자 ID :" + message.get(position).getUser_id());
            messageHolder.user_name.setText("사용자 이름 :"+message.get(position).getUser_name());
            messageHolder.group_code.setText("그룹 코드 :"+message.get(position).getGroup_code());
            messageHolder.group_name.setText("그룹명 :"+message.get(position).getGroup_name());
        }
        return convertView;
    }

    private class MessageHolder {
        TextView user_id;
        TextView user_name;
        TextView group_code;
        TextView group_name;

    }
}
