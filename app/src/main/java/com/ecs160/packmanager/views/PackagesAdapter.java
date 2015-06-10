package com.ecs160.packmanager.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.Transaction;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * @author Saravan Pantham
 */
public class PackagesAdapter extends ArrayAdapter<Transaction> {

    private ArrayList<Transaction> mDataList;

    public PackagesAdapter(Context context, int resource, ArrayList<Transaction> dataList) {
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
        Transaction transaction = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_layout_friend, parent, false);

            viewHolder.mAvatar = (CircleView) convertView.findViewById(R.id.friend_avatar);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.friend_name);
            viewHolder.mUsername = (TextView) convertView.findViewById(R.id.friend_username);
            viewHolder.mReliability = (TextView) convertView.findViewById(R.id.friend_reliability_text);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        try {
            int color = Color.YELLOW;
            if (transaction.getStatus()== Transaction.TransactionStatus.DELIVERED) {
                color = Color.GREEN;
            } else if (transaction.getStatus()== Transaction.TransactionStatus.DELAYED) {
                color = 0xFFFF0000;
            } else {
                color = Color.YELLOW;
            }

            viewHolder.mAvatar.setBackgroundColor(color);
            viewHolder.mAvatar.setFillColor(color);
            viewHolder.mAvatar.setStrokeColor(color);
            viewHolder.mAvatar.setTitleText("");
            viewHolder.mAvatar.setTag(R.string.status, transaction.getStatus());
            viewHolder.mAvatar.setTag(R.string.app_name, transaction.getPackageName());
            viewHolder.mAvatar.setTag(R.string.phone_number, transaction.getReceiver().getPhoneNumber());
            viewHolder.mName.setText(transaction.getPackageName());
            viewHolder.mUsername.setText(transaction.getSender().getUsername() + " to " + transaction.getReceiver().getUsername());
            viewHolder.mReliability.setVisibility(View.INVISIBLE);

            viewHolder.mAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String packageName = (String) v.getTag(R.string.app_name);
                    String phoneNumber = (String) v.getTag(R.string.phone_number);
                    if ((Transaction.TransactionStatus) v.getTag(R.string.status)== Transaction.TransactionStatus.IN_TRANSIT) {
                        v.setTag(R.string.status, Transaction.TransactionStatus.DELIVERED);
                        ((CircleView) v).setBackgroundColor(Color.GREEN);
                        ((CircleView) v).setFillColor(Color.GREEN);
                        ((CircleView) v).setStrokeColor(Color.GREEN);
                        try {
                            App.sendPackageUpdate(Transaction.TransactionStatus.DELIVERED, packageName, phoneNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(App.getContext(), R.string.package_delivered, Toast.LENGTH_SHORT).show();

                    } else if ((Transaction.TransactionStatus) v.getTag(R.string.status)== Transaction.TransactionStatus.DELIVERED) {
                        v.setTag(R.string.status, Transaction.TransactionStatus.DELAYED);
                        ((CircleView) v).setBackgroundColor(Color.RED);
                        ((CircleView) v).setFillColor(Color.RED);
                        ((CircleView) v).setStrokeColor(Color.RED);
                        try {
                            App.sendPackageUpdate(Transaction.TransactionStatus.DELAYED, packageName, phoneNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(App.getContext(), R.string.package_delayed, Toast.LENGTH_SHORT).show();

                    } else {
                        v.setTag(R.string.status, Transaction.TransactionStatus.IN_TRANSIT);
                        ((CircleView) v).setBackgroundColor(Color.YELLOW);
                        ((CircleView) v).setFillColor(Color.YELLOW);
                        ((CircleView) v).setStrokeColor(Color.YELLOW);
                        try {
                            App.sendPackageUpdate(Transaction.TransactionStatus.IN_TRANSIT, packageName, phoneNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(App.getContext(), R.string.package_in_transit, Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Transaction getItem(int position) {
        return mDataList.get(position);
    }

    static class ViewHolder {
        public CircleView mAvatar;
        public TextView mName;
        public TextView mUsername;
        public TextView mReliability;
    }
}
