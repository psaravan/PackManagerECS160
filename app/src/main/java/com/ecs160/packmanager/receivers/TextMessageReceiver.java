package com.ecs160.packmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.ecs160.packmanager.utils.App;

import org.json.JSONException;

/**
 * Created by pantham1-a on 6/8/15.
 */
public class TextMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);

        try {
            if (messages.getMessageBody().startsWith("{\"friendRequest")) {
                App.parseNewFriend(messages.getMessageBody());
            } else if (messages.getMessageBody().startsWith("{\"addNewUser")) {
                App.parseNewUser(messages.getMessageBody());
            } else if (messages.getMessageBody().startsWith("{\"packageReq")) {
                App.parseSendPackage(messages.getMessageBody());
            } else if (messages.getMessageBody().startsWith("{\"denyPackage")) {
                App.parsePackageDenial(messages.getMessageBody());
            } else if (messages.getMessageBody().startsWith("{\"")) {
                App.parsePackageUpdate(messages.getMessageBody());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}