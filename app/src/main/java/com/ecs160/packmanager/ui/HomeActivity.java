package com.ecs160.packmanager.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.db.DBAccessHelper;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.views.SlidingTabLayout;

import org.json.JSONException;


public class HomeActivity extends ActionBarActivity {

    private ViewPager mViewPager;
    private PagerAdapter mViewPagerAdapter;
    private Toolbar mToolbar;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        setSupportActionBar(mToolbar);

        mViewPagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }

        });

        mSlidingTabLayout.setViewPager(mViewPager);

        String currentUsername = App.getCurrentSession().getLoggedInUser().getUsername();
        Toast.makeText(App.getContext(), getString(R.string.logged_in_as) + " " + currentUsername, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.yes, logoutYesClickListener);
        builder.setNegativeButton(R.string.no, logoutNoClickListener);
        builder.create().show();

    }

    private DialogInterface.OnClickListener logoutYesClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            logout();
        }
    };

    private DialogInterface.OnClickListener logoutNoClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    /**
     * Logs the user out.
     */
    private void logout() {
        App.getSharedPreferences().edit().remove(DBAccessHelper.USERNAME).commit();
        finish();
    }

    public static class PagerAdapter extends FragmentPagerAdapter {

        private Context mContext;
        private static int NUM_ITEMS = 2;

        public PagerAdapter(Context context, FragmentManager fragmentManager) {
            super(fragmentManager);
            mContext = context;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for the page.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FriendsFragment.newInstance();
                case 1:
                    return PackagesFragment.newInstance();
                default:
                    return new Fragment();
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getResources().getString(R.string.friends);
                case 1:
                    return mContext.getResources().getString(R.string.packages);
                default:
                    return null;
            }
        }
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
