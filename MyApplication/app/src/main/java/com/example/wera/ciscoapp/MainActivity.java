package com.example.wera.ciscoapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<ListItem> itemList;

    private int count = 0;

    //The callback manager for the facebook login button
    CallbackManager facebookCallbackManager;

    //the views associated with the facebook bar at the bottom
    LoginButton loginButton;
    TextView experienceText;
    TextView personName;
    ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialising the facebook sdk and generating an access token.
        // Add code to print out the key hash
        initFacebook();

        setContentView(R.layout.activity_main);

        //setting up the login button
        setupFBLogin();

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        itemList = new ArrayList<>();
        for (int i = 12; i < 15; i++) {
            final ListItem item = new ListItem();
            item.time = i + ":00";

            String tempAddress = "52 Kersland Street, Glasgow, G12 8BT";
            LatLng latLng = EventDownloader.getLocationFromAddress(this, tempAddress);
            item.lat = latLng.latitude;
            item.lng = latLng.longitude;

            new EventDownloader(this, tempAddress, 1000) {
                @Override
                public void onLoadComplete(ArrayList<MapEntity> entities) {
                    item.eventList = new ArrayList<>();
                    for (MapEntity entity : entities) {
                        EventCard event = new EventCard();
                        event.title = entity.name;
                        event.distance = entity.time;
                        event.lat = entity.latLng.latitude;
                        event.lng = entity.latLng.longitude;
                        item.eventList.add(event);
                    }
                    itemList.add(item);

                    count++;
                    if (count == 3) {
                        adapter = new RecyclerViewAdapter(MainActivity.this, itemList);
                        recyclerView.setAdapter(adapter);
                    }
                }
            };
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView time;
        public LinearLayout llayout;

        public ViewHolder(View view, TextView time, LinearLayout llayout) {
            super(view);
            this.time = time;
            this.llayout = llayout;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Context context;
        private List<ListItem> items;

        public RecyclerViewAdapter(Context context, List<ListItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int groupType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.time_events, parent, false);

            TextView time = (TextView) view.findViewById(R.id.event_time);
            LinearLayout llayout = (LinearLayout) view.findViewById(R.id.event_llayout);

            ViewHolder vh = new ViewHolder(view, time, llayout);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            final ListItem item = items.get(position);
            viewHolder.time.setText(item.time);

            viewHolder.llayout.removeAllViews();
            for (final EventCard event : item.eventList) {
                LinearLayout itemRow = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.event_cardview, viewHolder.llayout, false);

                TextView title = (TextView) itemRow.findViewById(R.id.card_title);
                TextView time = (TextView) itemRow.findViewById(R.id.card_time);
                ImageButton imageButton = (ImageButton) itemRow.findViewById(R.id.card_imagebutton);

                title.setText(event.title);
                time.setText(event.distance);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        intent.putExtra(MapsActivity.ORIGIN_KEY_LAT, item.lat);
                        intent.putExtra(MapsActivity.ORIGIN_KEY_LNG, item.lng);
                        intent.putExtra(MapsActivity.DEST_KEY_LAT, event.lat);
                        intent.putExtra(MapsActivity.DEST_KEY_LNG, event.lng);
                        startActivity(intent);
                    }
                });

                viewHolder.llayout.addView(itemRow);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private void initFacebook() {
        Log.i("Facebook Message:", "obligatory wowza.");

        //getting an access token. search for this in the debug output.
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.wera.ciscoapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        //initialising the api
        FacebookSdk.sdkInitialize(MainActivity.this);
    }

    private void setupFBLogin() {
        //getting the login button, and assigning its callback listener
        loginButton = (LoginButton) findViewById(R.id.login_button);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        experienceText = (TextView) findViewById(R.id.experience_text);
        personName = (TextView) findViewById(R.id.person_name);

        loginButton.setReadPermissions("user_friends");

        //getting the facebook user profile and determining whether they're logged in or not.
        Profile profile = Profile.getCurrentProfile();
        if (profile == null) { //user is not logged in, so set up the event listener and everything.
            showLoggedOutFB(profile);
        } else { //user is logged in, so just display stuff about them.
            showLoggedInFB(profile);
        }

        //we set up the callback manager regardless of whether we're currently logged in or out
        facebookCallbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                showLoggedInFB(Profile.getCurrentProfile());
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

    private void showLoggedInFB(final Profile profile) {
        //creating an async task to fetch the profile picture. once it has been fetched, we put it
        //in the image view and change all of the other relevant views to visible.
        new AsyncTask<String, Void, Bitmap>() {
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
                if (bitmap == null) {
                    Log.i("Facebook Message:", "bitmap's null though");
                    return;
                }

                Log.i("Facebook Message:", "bitmap's bein' rendered");

                profilePic.setVisibility(View.VISIBLE);
                profilePic.setImageBitmap(bitmap);

                loginButton.setVisibility(View.INVISIBLE);
                experienceText.setVisibility(View.INVISIBLE);
                personName.setVisibility(View.VISIBLE);
                personName.setText(profile.getFirstName() + ", welcome to da club.");
            }
        }.execute(profile.getId());
    }

    private void showLoggedOutFB(final Profile profile) {
        //we're not logged in, so making the names and images invisible and the button and text
        //visible.

        loginButton.setVisibility(View.VISIBLE);
        experienceText.setVisibility(View.VISIBLE);

        profilePic.setVisibility(View.INVISIBLE);
        personName.setVisibility(View.INVISIBLE);
        personName.setText(" welcome to da club.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
