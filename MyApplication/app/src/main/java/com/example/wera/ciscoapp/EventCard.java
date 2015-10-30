package com.example.wera.ciscoapp;

import android.os.Parcel;
import android.os.Parcelable;

public class EventCard implements Parcelable {

    public static final String KEY = "EVENT_CARD";

    // Displayed variables
    public String title;
    public String distance;

    // Hidden variables - used for background tasks
    public double lat;
    public double lng;

    public EventCard() {

    }

    public EventCard(Parcel in) {
        title = in.readString();
        distance = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(distance);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public EventCard createFromParcel(Parcel in) {
            return new EventCard(in);
        }

        @Override
        public EventCard[] newArray(int size) {
            return new EventCard[size];
        }
    };
}
