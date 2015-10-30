package com.example.wera.ciscoapp;

import java.util.List;

public class ListItem {

    public String time;
    public List<EventCard> eventList;

    // Hidden variables - used for background tasks
    public String startTime;
    public String endTime;
    public String address;
    public double lat;
    public double lng;
}
