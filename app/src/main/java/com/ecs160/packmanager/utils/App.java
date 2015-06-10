package com.ecs160.packmanager.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.db.DBAccessHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Application context class.
 */
public class App extends Application {

    private static Context mContext;
    private static SharedPreferences mSharedPreferences;
    private static Session mCurrentSession;
    private static DBAccessHelper mDBAccessHelper;

    public static final String UI_UPDATE = "ui_update";
    public static final String ADD_FRIEND_INTENT_FILTER = "add_friend_intent_filter";
    public static final String SEND_PACKAGE_INTENT_FILTER = "send_package_intent_filter";
    public static final String IS_MASTER_ACTIVATED = "is_master_activated";

    @Override
    public void onCreate() {
        super.onCreate();
        setContext(getApplicationContext());
        mDBAccessHelper = DBAccessHelper.getInstance(getContext());
        mSharedPreferences = getContext().getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        if (mSharedPreferences.getBoolean(IS_MASTER_ACTIVATED, false)==false) {
            getDBAccessHelper().addNewUser("psaravan", "root", "Saravan Pantham", "4087184073");
            mSharedPreferences.edit().putBoolean(IS_MASTER_ACTIVATED, true).commit();
        }

    }

    /** 4086931014
     * Calculates the sample size of a bitmap to be rescaled.
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Decodes the resampled bitmap from the resources dir.
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static void sendFriendRequest(String phoneNumber) throws JSONException {
        if (phoneNumber==null) {
            return;
        }

        JSONObject friendRequestData = new JSONObject();
        friendRequestData.put("username", mCurrentSession.getLoggedInUser().getUsername());

        JSONObject friendRequestHolder = new JSONObject();
        friendRequestHolder.put("friendRequest", friendRequestData);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, friendRequestHolder.toString(), null, null);
    }

    public static void sendPackage(Transaction transaction) throws JSONException {
        JSONObject packageData = new JSONObject();
        packageData.put("name", transaction.getPackageName());
        packageData.put("id", transaction.getTransactionId());
        packageData.put("status", "waiting");
        packageData.put("from", transaction.getSender().getUsername());
        packageData.put("to", transaction.getReceiver().getUsername());

        if (transaction.getIntermediary()!=null) {
            packageData.put("i", transaction.getIntermediary().getUsername());
            packageData.put("iTime", transaction.getIntermediaryTimestamp());
            packageData.put("iPlace", transaction.getIntermediaryLocation());
        } else {
            packageData.put("i", "");
            packageData.put("iTime", 0);
            packageData.put("iPlace", "");
        }

        packageData.put("time", transaction.getTimeStamp());
        packageData.put("place", transaction.getLocation());

        JSONObject packageHolder = new JSONObject();
        packageHolder.put("packageReq", packageData);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(transaction.getReceiver().getPhoneNumber(), null, packageHolder.toString(), null, null);
    }

    public static void parseSendPackage(String message) throws JSONException {
        JSONObject packageDataHolder = new JSONObject(message);
        JSONObject packageData = packageDataHolder.getJSONObject("packageReq");

        String packageName = packageData.getString("name");
        String sender = packageData.getString("from");
        String transactionId = packageData.getString("id");
        String status = packageData.getString("status");
        String receiver = packageData.getString("to");
        String intermediary = packageData.getString("i");
        String timestamp = packageData.getString("time");
        String location = packageData.getString("place");
        String intermediaryTimestamp = packageData.getString("iPlace");
        String intermediaryLocation = packageData.getString("iTime");

        Intent resultsIntent = new Intent(UI_UPDATE);
        resultsIntent.putExtra("type", SEND_PACKAGE_INTENT_FILTER);
        resultsIntent.putExtra("sender", sender);
        resultsIntent.putExtra("name", packageName);
        resultsIntent.putExtra("id", transactionId);
        resultsIntent.putExtra("status", status);
        resultsIntent.putExtra("receiver", receiver);
        resultsIntent.putExtra("i", intermediary);
        resultsIntent.putExtra("time", timestamp);
        resultsIntent.putExtra("place", location);
        resultsIntent.putExtra("iTimestamp", intermediaryTimestamp);
        resultsIntent.putExtra("iLocation", intermediaryLocation);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(App.getContext());

        localBroadcastManager.sendBroadcast(resultsIntent);

    }

    public static void denyPackage(String phoneNumber, String receiver, String packageName) throws JSONException {
        JSONObject holder = new JSONObject();
        JSONObject contents = new JSONObject();
        contents.put("receiver", receiver);
        contents.put("name", packageName);
        holder.put("denyPackage", contents);

        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, holder.toString(), null, null);
    }

    public static void parsePackageDenial(String message) throws JSONException {
        JSONObject holder = new JSONObject(message);
        JSONObject data = holder.getJSONObject("denyPackage");
        String receiver = data.getString("receiver");
        String packageName = data.getString("packageName");
        Toast.makeText(App.getContext(), receiver + " " + getContext().getString(R.string.denied_your_package) + " (" + packageName + ").", Toast.LENGTH_SHORT).show();
    }

    public static void sendNewUserData(User newUser) throws JSONException {
        JSONObject userData = new JSONObject();
        userData.put("username", newUser.getUsername());
        userData.put("realName", newUser.getRealName());
        userData.put("phoneNumber", newUser.getPhoneNumber());
        userData.put("reliabilityIndex", newUser.getReliabilityIndex());
        userData.put("friends", newUser.getFriends());
        userData.put("transactions", newUser.getTransactions());
        userData.put("password", newUser.getPassword());

        JSONObject userHolder = new JSONObject();
        userHolder.put("addNewUser", userData);

        SmsManager sms = SmsManager.getDefault();
        ArrayList<User> list = getDBAccessHelper().getAllUsers();
        for (User user : list) {
            String phoneNumber = user.getPhoneNumber();
            if (!phoneNumber.equals(newUser.getPhoneNumber())) {
                sms.sendTextMessage(phoneNumber, null, userHolder.toString(), null, null);
            }
        }
    }

    public static void parseNewUser(String message) throws JSONException {
        JSONObject newUserHolder = new JSONObject(message);
        JSONObject userData = newUserHolder.getJSONObject("addNewUser");

        String username = userData.getString("username");
        String password = userData.getString("password");
        String realName = userData.getString("realName");
        String phoneNumber = userData.getString("phoneNumber");

        App.getDBAccessHelper().addNewUser(username, password, realName, phoneNumber);
        Toast.makeText(App.getContext(), R.string.users_list_updated, Toast.LENGTH_SHORT).show();
    }

    public static void parseNewFriend(String message) throws JSONException {
        JSONObject newFriendObject = new JSONObject(message);
        String username = newFriendObject.getJSONObject("friendRequest").getString("username");

        Intent resultsIntent = new Intent(UI_UPDATE);
        resultsIntent.putExtra("type", ADD_FRIEND_INTENT_FILTER);
        resultsIntent.putExtra("username", username);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(App.getContext());
        localBroadcastManager.sendBroadcast(resultsIntent);

    }

    public static void sendPackageUpdate(Transaction.TransactionStatus status, String packageName, String phoneNumber) throws JSONException {
        JSONObject holder = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("name", packageName);
        data.put("status", status.ordinal());
        holder.put("updatePackage", data);

        getDBAccessHelper().updatePackageStatus(packageName, status.ordinal());

        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, holder.toString(), null, null);
    }

    public static void parsePackageUpdate(String message) throws JSONException {
        JSONObject holder = new JSONObject(message);
        JSONObject data = holder.getJSONObject("updatePackage");

        String name = data.getString("name");
        int status = data.getInt("status");
        getDBAccessHelper().updatePackageStatus(name, status);
        Toast.makeText(getContext(), name + "'s status has been updated.", Toast.LENGTH_SHORT).show();
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        App.mContext = context;
    }

    public static Session getCurrentSession() {
        return mCurrentSession;
    }

    public static void setCurrentSession(Session currentSession) {
        App.mCurrentSession = currentSession;
    }

    public static DBAccessHelper getDBAccessHelper() {
        return mDBAccessHelper;
    }

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }
}
