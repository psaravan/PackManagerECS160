package com.ecs160.packmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.db.DBAccessHelper;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.Session;
import com.ecs160.packmanager.utils.User;


public class LoginActivity extends Activity {

    private RelativeLayout mRootLayout;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private ImageView mBackgroundImage;
    private Button mLoginButton;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRootLayout = (RelativeLayout) findViewById(R.id.login_root);
        mUsernameEditText = (EditText) findViewById(R.id.username_edittext);
        mPasswordEditText = (EditText) findViewById(R.id.password_edittext);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mBackgroundImage = (ImageView) findViewById(R.id.login_background_image);

        // Resample the background image for better performance.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.login_background, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        mBackgroundImage.setImageBitmap(App.decodeSampledBitmapFromResource(getResources(),
                R.drawable.login_background, imageWidth , imageHeight));

        //Picasso.with(App.getContext()).load(R.drawable.login_background).into(mBackgroundImage);

        // Check if the user is already signed in.
        if (App.getSharedPreferences().getString(DBAccessHelper.USERNAME, null)!=null) {
            User user = new User();
            user.setUsername(App.getSharedPreferences().getString(DBAccessHelper.USERNAME, null));

            App.setCurrentSession(new Session(user));
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();

        }

        mLoginButton.setOnClickListener(loginClickListener);
        mSignUpButton.setOnClickListener(signUpClickListener);

        animateActivity();
    }

    public void animateActivity() {
        TranslateAnimation slideUp = new TranslateAnimation(0.0f, 0.0f, -50.0f, 0.0f);
        slideUp.setInterpolator(new DecelerateInterpolator(2.0f));
        slideUp.setDuration(2000);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator(2.0f));
        fadeIn.setDuration(2000);

        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(slideUp);
        animSet.addAnimation(fadeIn);

        animSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mRootLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRootLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animSet.start();
    }

    private View.OnClickListener loginClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String username = mUsernameEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();

            if (username==null || username.isEmpty()) {
                Toast.makeText(App.getContext(), R.string.login_failure_toast, Toast.LENGTH_LONG).show();
                return;
            }

            if (password==null || password.isEmpty()) {
                Toast.makeText(App.getContext(), R.string.login_failure_toast, Toast.LENGTH_LONG).show();
                return;
            }

            login(username, password);
        }
    };

    private View.OnClickListener signUpClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(App.getContext(), SignUpActivity.class);
            startActivity(intent);
        }
    };

    /**
     * Logs the user into the system and launches {@link HomeActivity};
     */
    private void login(String username, String password) {
        // Validate the username and password.
        String storedPassword = App.getDBAccessHelper().getUserPassword(username);
        if (storedPassword!=null && storedPassword.equals(password)) {
            // Login success! Grab the user's details and set the global session object.
            App.getSharedPreferences().edit().putString(DBAccessHelper.USERNAME, username).commit();
            App.setCurrentSession(App.getDBAccessHelper().getUserSessionObject(username));

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            // Login failure.
            Toast.makeText(App.getContext(), R.string.login_failure_toast, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
    public void onResume() {
        super.onResume();
        mUsernameEditText.setText("");
        mPasswordEditText.setText("");
    }

}
