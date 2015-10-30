package com.example.wera.ciscoapp;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// GCALNEDAR
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;


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


    // GOOGLE CALENDAR

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

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


        //GCalendar
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));


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


        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mOutputText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }



    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                mOutputText.setText("No network connection available.");
            }
        }
    }


    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }






    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());

            Date startLunchTime = new Date();
            startLunchTime.setHours(10); startLunchTime.setMinutes(0); startLunchTime.setSeconds(0);
            DateTime startLunchTime1 = new DateTime(startLunchTime);
            DateTime minTime;
            if (now.toString().compareTo(startLunchTime1.toString()) >0 ){
                minTime = now;
            } else{
                minTime = startLunchTime1;
            }

            Date endLunchTime = new Date();
            endLunchTime.setHours(23); endLunchTime.setMinutes(59); endLunchTime.setSeconds(59);
            DateTime endLunchTime1 = new DateTime(endLunchTime);

            List<String> eventStrings = new ArrayList<String>();

            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(minTime)
                    .setTimeMax(endLunchTime1)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();


            for( int i = 0; i < items.size()-1; i++){
                if ( items.get(i).getEnd().getDateTime() == null ){
                    continue;
                }
                else if ( items.get(i+1).getStart().getDateTime() == null ){
                    continue;
                }
                long start1 = items.get(i).getEnd().getDateTime().getValue();
                long start2 = items.get(i+1).getStart().getDateTime().getValue();
                long difference = (start2 - start1) /60000;
                String location = items.get(i).getLocation();
                eventStrings.add(
                        String.format("%s\n %s\n %s\n FREE TIME (%d) min at (%s)\n", items.get(i).getSummary(),items.get(i+1).getSummary(), location, difference, items.get(i).getEnd().getDateTime()));
            }

            Event event = new Event()
                    .setSummary("Google I/O 2015")
                    .setLocation("800 Howard St., San Francisco, CA 94103");

            DateTime startDateTime = new DateTime("2015-11-01T09:00:00-07:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);
            event.setStart(start);

            DateTime endDateTime = new DateTime("2015-11-01T17:00:00-07:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);
            event.setEnd(end);

            String calendarId = "primary";
            event = mService.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());

            return eventStrings;

        }



        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Calendar API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }



}
