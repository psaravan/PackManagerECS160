package com.ecs160.packmanager.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.Transaction;
import com.ecs160.packmanager.utils.User;

import org.json.JSONException;

public class AddNewPackageActivity extends Activity {

    private EditText mPackageName;
    private EditText mReceiverUsername;
    private EditText mIntermediaryUsername;
    private EditText mLocation;
    private EditText mTimestamp;
    private Button mDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_package);

        mPackageName = (EditText) findViewById(R.id.package_name);
        mReceiverUsername = (EditText) findViewById(R.id.receiver_username);
        mIntermediaryUsername = (EditText) findViewById(R.id.intermediary_username);
        mLocation = (EditText) findViewById(R.id.location);
        mTimestamp = (EditText) findViewById(R.id.meeting_time);
        mDoneButton = (Button) findViewById(R.id.new_package_done_button);

        if (getIntent().hasExtra("receiver")) {
            mReceiverUsername.setText(getIntent().getStringExtra("receiver"));
        }

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInfo();
            }
        });

    }

    private void validateInfo() {
        String packageName = mPackageName.getText().toString();
        String receiver = mReceiverUsername.getText().toString();
        String location = mLocation.getText().toString();
        String timestamp = mTimestamp.getText().toString();
        String intermediary = mIntermediaryUsername.getText().toString();
        String sender = App.getCurrentSession().getLoggedInUser().getUsername();

        if (packageName==null || packageName.isEmpty()) {
            Toast.makeText(App.getContext(), "Package name cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        if (receiver==null || receiver.isEmpty()) {
            Toast.makeText(App.getContext(), "Receiver cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        if (location==null || location.isEmpty()) {
            Toast.makeText(App.getContext(), "Location cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        if (timestamp==null || timestamp.isEmpty()) {
            Toast.makeText(App.getContext(), "Meeting time cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        App.getDBAccessHelper().createTransaction(packageName, sender, receiver, intermediary, location, timestamp, null, null);

        User senderObject = App.getDBAccessHelper().getUserFromUsername(sender);
        User receiverObject = App.getDBAccessHelper().getUserFromUsername(receiver);

        Transaction transaction = new Transaction(packageName, senderObject, receiverObject);
        transaction.setLocation(location);
        transaction.setStatus(Transaction.TransactionStatus.IN_TRANSIT);
        transaction.setTimeStamp(timestamp);
        try {
            App.sendPackage(transaction);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast.makeText(App.getContext(), "Package request sent to " + receiver, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_package, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFriendRequestDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.new_friend_request);
        builder.setMessage(username + " " + getString(R.string.wants_to_be_friends));
        builder.setPositiveButton(R.string.accept, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                App.getDBAccessHelper().addFriend(username);
                Toast.makeText(App.getContext(), getString(R.string.you_are_friends_with) + " " + username + ".", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.deny, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    private void showSendPackageRequestDialog(final String username, final String phoneNumber, final String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.send_package_request);
        builder.setMessage(username + " " + getString(R.string.wants_to_send_you) + " " + packageName + ".");
        builder.setPositiveButton(R.string.accept, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                App.getDBAccessHelper().createTransaction(packageName, username, App.getCurrentSession().getLoggedInUser().getUsername(), "", "", "", null, null);
                PackagesFragment.updateListView();
            }
        });

        builder.setNegativeButton(R.string.deny, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    App.denyPackage(phoneNumber, App.getCurrentSession().getLoggedInUser().getPhoneNumber(), packageName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.create().show();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getExtras().getString("type");

            if (type.equals(App.ADD_FRIEND_INTENT_FILTER)) {
                String username = intent.getStringExtra("username");
                showFriendRequestDialog(username);
            } else if (type.equals(App.SEND_PACKAGE_INTENT_FILTER)) {
                String username = intent.getStringExtra("sender");
                String packageName = intent.getStringExtra("name");
                String phoneNumber = App.getDBAccessHelper().getPhoneNumberForUsername(username);
                showSendPackageRequestDialog(username, phoneNumber, packageName);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(App.UI_UPDATE));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();

    }

}
