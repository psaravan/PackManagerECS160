package com.ecs160.packmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.User;

import org.json.JSONException;

public class SignUpActivity extends Activity {

    private EditText mRealNameEditText;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mPhoneNumberEditText;
    private Button mDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mRealNameEditText = (EditText) findViewById(R.id.sign_up_name);
        mUsernameEditText = (EditText) findViewById(R.id.sign_up_username);
        mPasswordEditText = (EditText) findViewById(R.id.sign_up_password);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.sign_up_confirm_password);
        mPhoneNumberEditText = (EditText) findViewById(R.id.sign_up_phone);
        mDoneButton = (Button) findViewById(R.id.sign_up_done_button);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInfo();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    private void validateInfo() {
        String realName = mRealNameEditText.getText().toString();
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmPassword = mConfirmPasswordEditText.getText().toString();
        String phoneNumber = mPhoneNumberEditText.getText().toString();

        if (realName==null || realName.isEmpty()) {
            Toast.makeText(App.getContext(), R.string.name_cannot_be_empty, Toast.LENGTH_LONG).show();
            return;
        }

        if (username==null || username.isEmpty()) {
            Toast.makeText(App.getContext(), R.string.username_cannot_be_empty, Toast.LENGTH_LONG).show();
            return;
        }

        if (phoneNumber==null || phoneNumber.isEmpty()) {
            Toast.makeText(App.getContext(), R.string.phone_number_cannot_be_empty, Toast.LENGTH_LONG).show();
            return;
        }

        if (password==null || confirmPassword==null || password.isEmpty() || confirmPassword.isEmpty() || !password.equals(confirmPassword)) {
            Toast.makeText(App.getContext(), R.string.retype_password, Toast.LENGTH_LONG).show();
            return;
        }

        signUp(realName, username, password, phoneNumber);
    }

    private void signUp(String realName, String username, String password, String phoneNumber) {
        App.getDBAccessHelper().addNewUser(username, password, realName, phoneNumber);

        User user = new User();
        user.setRealName(realName);
        user.setUsername(username);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        user.setReliabilityIndex(-1);

        // Log the user in.
        App.setCurrentSession(App.getDBAccessHelper().getUserSessionObject(username));

        try {
            App.sendNewUserData(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
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
}
