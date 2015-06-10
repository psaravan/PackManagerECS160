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
import android.widget.ListView;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.User;
import com.ecs160.packmanager.views.AddFriendsListAdapter;

import org.json.JSONException;

import java.util.ArrayList;

public class AddFriendActivity extends Activity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mListView = (ListView) findViewById(R.id.add_friend_list);
        ArrayList<User> list = App.getDBAccessHelper().getAllUsers();
        AddFriendsListAdapter adapter = new AddFriendsListAdapter(this, R.layout.row_layout_add_friend, list);
        mListView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_friend, menu);
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
