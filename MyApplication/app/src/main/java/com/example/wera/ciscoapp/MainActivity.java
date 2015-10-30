package com.example.wera.ciscoapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<ListItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        itemList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ListItem item = new ListItem();
            item.time = "TIME " + i;
            item.eventList = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                EventCard event = new EventCard();
                event.title = "RESTAURANT " + j;
                event.distance = "DIST " + j;
                item.eventList.add(event);
            }
            itemList.add(item);
        }

        adapter = new RecyclerViewAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
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
            ListItem item = items.get(position);
            viewHolder.time.setText(item.time);

            viewHolder.llayout.removeAllViews();
            for (EventCard event : item.eventList) {
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
}
