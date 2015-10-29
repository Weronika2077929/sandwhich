package com.which.sand.facebooktest;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
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
import com.facebook.Profile;
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


                Profile profile = Profile.getCurrentProfile();
                ((TextView) findViewById(R.id.person_name)).setText(profile.getName());

                final ImageView profilePic = (ImageView) findViewById(R.id.profile_pic);

                new AsyncTask<String, Void, Bitmap>(){

                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            URL image_value = new URL("https://graph.facebook.com/" + params[0] + "/picture?type=large");
                            Bitmap bmp = null;
                            try {
                                bmp = BitmapFactory.decodeStream(image_value.openConnection().getInputStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            return bmp;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if(bitmap == null) {
                            Log.i("Facebook Message:", "bitmap's null though");
                            return;
                        }

                        Log.i("Facebook Message:", "bitmap's bein' rendered");
                        profilePic.setImageBitmap(bitmap);
                        ((TextView) findViewById(R.id.main_text)).setText("hello you ;)");
                    }
                }.execute(profile.getId());

//                PackageInstaller.Session.openActiveSession(this, true, new PackageInstaller.Session.StatusCallback() {
//
//                    @Override
//                    public void call(PackageInstaller.Session session, SessionState state,
//                                     Exception exception) {
//                        if (session.isOpened()) {
//                            // make request to the /me API
//                            Request.executeMeRequestAsync(session,
//                                    new Request.GraphUserCallback() {
//                                        @Override
//                                        public void onCompleted(GraphUser user,
//                                                                Response response) {
//                                            if (user != null) {
//                                                try {
//                                                    URL image_value = new URL("http://graph.facebook.com/" + user.getId() + "/picture?type=large");
//                                                    Bitmap bmp = null;
//                                                    try {
//                                                        bmp = BitmapFactory.decodeStream(image_value.openConnection().getInputStream());
//                                                    } catch (IOException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                    profile_pic.setImageBitmap(bmp);
//                                                } catch (MalformedURLException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }
//                                    });
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error...",
//                                    Toast.LENGTH_LONG);
//                        }
//                    }
//                });
//
//                        new GraphRequest(
//                                AccessToken.getCurrentAccessToken(),
//                                "/me/likes",
//                                null,
//                                HttpMethod.GET,
//                                new GraphRequest.Callback() {
//                                    @Override
//                                    public void onCompleted(GraphResponse graphResponse) {
//                                        TextView myView = (TextView) findViewById(R.id.main_text);
//                                        Log.i("Facebook Message:", String.valueOf(graphResponse.getJSONObject()));
//                                        myView.setText("hello you ;)");
//                                    }
//                                }
//                        ).executeAsync();
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
