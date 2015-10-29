package com.which.sand.facebooktest;


import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.TextView;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FBActivity extends AppCompatActivity {

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_fb);


        callbackManager = CallbackManager.Factory.create();

        Log.i("testlol", "initialised facebook.");


//        View view = inflater.inflate(R.layout.splash, container, false);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setLoginBehavior(SUPPRESS_SSO);
        loginButton.setReadPermissions("user_friends");
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("Facebook Message:", "wowza success!");

                        new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "/me/likes",
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    @Override
                                    public void onCompleted(GraphResponse graphResponse) {
                                        TextView myView = (TextView) findViewById(R.id.main_text);
                                        Log.i("Facebook Message:", String.valueOf(graphResponse.getJSONObject()));
                                        myView.setText("hello you ;)");
                                    }
                                }
                        ).executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Message:", "wowza cancel!");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("Facebook Message:", "wowza, some error :/");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fb, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
