package com.ecs160.packmanager.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.User;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Saravan Pantham
 */
public class AddFriendsListAdapter extends ArrayAdapter<User> {

    private ArrayList<User> mDataList;

    public AddFriendsListAdapter(Context context, int resource, ArrayList<User> dataList) {
        super(context, resource);
        mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        User user = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_layout_add_friend, parent, false);

            viewHolder.mAvatar = (CircleView) convertView.findViewById(R.id.friend_avatar);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.friend_name);
            viewHolder.mUsername = (TextView) convertView.findViewById(R.id.friend_username);
            viewHolder.mAddFriend = (ImageButton) convertView.findViewById(R.id.add_friend_button);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        viewHolder.mAddFriend.setTag(R.string.phone_number, user.getPhoneNumber());
        viewHolder.mAvatar.setBackgroundColor(color);
        viewHolder.mAvatar.setFillColor(color);
        viewHolder.mAvatar.setStrokeColor(color);
        viewHolder.mAvatar.setTitleText(user.getRealName().toUpperCase().substring(0, 1));
        viewHolder.mName.setText(user.getRealName());
        viewHolder.mUsername.setText(user.getUsername());
        viewHolder.mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(App.getContext(), R.string.friend_request_sent, Toast.LENGTH_SHORT).show();
                    App.sendFriendRequest((String) v.getTag(R.string.phone_number));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public User getItem(int position) {
        return mDataList.get(position);
    }

    static class ViewHolder {
        public CircleView mAvatar;
        public TextView mName;
        public TextView mUsername;
        public ImageButton mAddFriend;
    }
}
