/*
 * Copyright (c) 2017 CMPUT301F17T15. This project is distributed under the MIT license.
 */

package com.cmput301.cia.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cmput301.cia.R;
import com.cmput301.cia.activities.events.HabitEventViewActivity;
import com.cmput301.cia.activities.events.HistoryActivity;
import com.cmput301.cia.activities.habits.SingleStatisticViewActivity;
import com.cmput301.cia.activities.habits.StatisticViewActivity;
import com.cmput301.cia.activities.templates.LocationRequestingActivity;
import com.cmput301.cia.controller.TimedAdapterViewClickListener;
import com.cmput301.cia.controller.TimedClickListener;
import com.cmput301.cia.models.CompletedEventDisplay;
import com.cmput301.cia.models.Habit;
import com.cmput301.cia.models.Profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adil Malik
 * @version 2
 * Date: Nov 30 2017
 *
 * This activity displays all of the people a viewer is following
 */

public class ViewFollowedUsersActivity extends LocationRequestingActivity {

    // Intent identifiers for passed in profile
    public static final String ID_VIEWED = "Profile";

    // Activity result codes
    private static final int VIEW_PROFILE = 0;

    // the profile being displayed
    private Profile displayed;

    // list of followed users
    private ListView followedList;
    private ArrayAdapter<Profile> followedListAdapter;
    private List<Profile> followed;

    // list of followed users' habits
    private ListView habitsList;
    private ArrayAdapter<String> habitsListAdapter;
    private List<Habit> habitsDisplayed;

    // list of followed users' events
    private ListView eventsList;
    private ArrayAdapter<CompletedEventDisplay> eventsListAdapter;

    private ImageView historyImage;
    private ImageView profileImage;
    private ViewSwitcher profileHistorySwitcher;

    private TextView noFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_followed_users);

        Intent intent = getIntent();
        displayed = (Profile) intent.getSerializableExtra(ID_VIEWED);

        followedList = (ListView)findViewById(R.id.vfuProfilesList);

        noFollowing = (TextView)findViewById(R.id.noFollowing);

        followed = new ArrayList<>();//displayed.getFollowing();

        if (followed.size() > 0) {
            noFollowing.setVisibility(View.GONE);
        }

        followedListAdapter = new ArrayAdapter<>(this, R.layout.list_item, followed);
        followedList.setAdapter(followedListAdapter);

        // view a profile when it is clicked
        followedList.setOnItemClickListener(new TimedAdapterViewClickListener() {
            @Override
            public void handleClick(View view, int index) {
                Intent profileIntent = new Intent(ViewFollowedUsersActivity.this, UserProfileActivity.class);
                profileIntent.putExtra(UserProfileActivity.PROFILE_ID, followed.get(index));
                profileIntent.putExtra(UserProfileActivity.USER_ID, displayed);
                startActivityForResult(profileIntent, VIEW_PROFILE);
            }
        });

        habitsList = (ListView)findViewById(R.id.vfuHabitsList);
        //updateHabitsAdapter();
        habitsList.setOnItemClickListener(new TimedAdapterViewClickListener() {
            @Override
            public void handleClick(View view, int index) {
                Intent intent = new Intent(ViewFollowedUsersActivity.this, SingleStatisticViewActivity.class);
                intent.putExtra(SingleStatisticViewActivity.ID_HABIT, habitsDisplayed.get(index));//displayed.getFollowedHabits().get(index));
                startActivity(intent);
            }
        });

        eventsList = (ListView)findViewById(R.id.vfuEventsList);
        /*eventsList.setOnItemClickListener(new TimedAdapterViewClickListener() {
            @Override
            public void handleClick(View view, int index) {

            }
        });*/

        historyImage = (ImageView)findViewById(R.id.vfuHistoryIcon);
        // switch to history mode
        historyImage.setOnClickListener(new TimedClickListener() {
            @Override
            public void handleClick() {
                showHistory();
            }
        });

        profileImage = (ImageView)findViewById(R.id.vfuUsersIcon);
        // switch to user mode
        profileImage.setOnClickListener(new TimedClickListener() {
            @Override
            public void handleClick() {
                showProfiles();
            }
        });

        // switch to map activity when the map image is clicked
        findViewById(R.id.vfuMapView).setOnClickListener(new TimedClickListener() {
            @Override
            public void handleClick() {
                requestLocationPermissions();
            }
        });

        final Switch eventsHistorySwitch = (Switch) findViewById(R.id.vfuHistoryEventsSwitch);
        eventsHistorySwitch.setText("Events");
        // toggle the history/habit view when the switch gets clicked
        eventsHistorySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchHistoryView();
                if (eventsHistorySwitch.getText().toString().equals("Events")){
                    eventsHistorySwitch.setText("Habits");
                } else {
                    eventsHistorySwitch.setText("Events");
                }
            }
        });

        profileHistorySwitcher = (ViewSwitcher)findViewById(R.id.vfuProfileHistorySwitcher);
        // do this because calling showProfiles() for the first time results in switching to history
        profileHistorySwitcher.showNext();
        showProfiles();
        habitsList.setVisibility(View.INVISIBLE);
    }

    /**
     * Handle the results of the request location permission being granted
     */
    @Override
    protected void handleLocationGranted() {
        Intent intent = new Intent(this, ViewEventsMapActivity.class);
        intent.putExtra(ViewEventsMapActivity.ID_EVENTS, (Serializable) displayed.getFollowedHabitHistory());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        followed = displayed.getFollowing();
        followedListAdapter.clear();
        followedListAdapter.addAll(followed);
        followedListAdapter.notifyDataSetChanged();
        if (followed.size() > 0) {
            noFollowing.setVisibility(View.GONE);
        } else
            noFollowing.setVisibility(View.VISIBLE);

        updateHabitsAdapter();

        eventsListAdapter = new ArrayAdapter<>(this, R.layout.list_item, displayed.getFollowedHabitHistory());
        eventsList.setAdapter(eventsListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*followed = displayed.getFollowing();
        followedListAdapter = new ArrayAdapter<>(this, R.layout.list_item, followed);
        followedList.setAdapter(followedListAdapter);

        updateHabitsAdapter();
        eventsListAdapter = new ArrayAdapter<>(this, R.layout.list_item, displayed.getFollowedHabitHistory());
        eventsList.setAdapter(eventsListAdapter);*/
    }

    /**
     * Handle the results of an activity that has finished
     * @param requestCode the activity's identifying code
     * @param resultCode the result status of the finished activity
     * @param data the activity's returned intent information
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // When a new habit event is created
        if (requestCode == VIEW_PROFILE) {
            if (resultCode == RESULT_OK) {
                Profile newProfile = (Profile) data.getSerializableExtra(UserProfileActivity.RESULT_PROFILE_ID);
                displayed.copyFrom(newProfile, false);
            }
        }
    }

    /**
     * Switch the view to the followed user's habits list and history
     */
    private void showHistory(){
        historyImage.setVisibility(View.INVISIBLE);
        profileImage.setVisibility(View.VISIBLE);
        profileHistorySwitcher.showNext();

        // viewing habits list
        if (habitsList.getVisibility() == View.VISIBLE){
            if (habitsList.getAdapter().getCount() == 0 && !isFinishing())
                Toast.makeText(this, "No habits found.", Toast.LENGTH_SHORT).show();
        } else {
            // viewing events list
            if (eventsList.getAdapter().getCount() == 0 && !isFinishing())
                Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Switch the view to the followed user's profiles
     */
    private void showProfiles(){
        historyImage.setVisibility(View.VISIBLE);
        profileImage.setVisibility(View.INVISIBLE);
        profileHistorySwitcher.showNext();
    }

    /**
     * Cycle between the history and event view lists
     */
    private void switchHistoryView(){
        if (habitsList.getVisibility() == View.VISIBLE){
            // switch to events view
            habitsList.setVisibility(View.INVISIBLE);
            eventsList.setVisibility(View.VISIBLE);

        } else {
            // switch to history view
            habitsList.setVisibility(View.VISIBLE);
            eventsList.setVisibility(View.INVISIBLE);
        }

        // viewing habits list
        if (habitsList.getVisibility() == View.VISIBLE){
            if (habitsList.getAdapter().getCount() == 0 && !isFinishing())
                Toast.makeText(this, "No habits found.", Toast.LENGTH_SHORT).show();
        } else {
            // viewing events list
            if (eventsList.getAdapter().getCount() == 0 && !isFinishing())
                Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get a string representation of the specified habit to display in a list
     * @param habit the habit to get a representation of
     * @param creator the user who created the habit
     * @return string representation of the habit
     */
    public static String getHabitDisplayText(Habit habit, Profile creator){
        return creator.getName() + " created " + habit.getTitle() + " (" + habit.getCompletionPercent() + " completion rate)";
    }

    /**
     * Update the adapter for the ListView displaying details about followed user's habits
     */
    private void updateHabitsAdapter(){
        // map each habit's id to their creator
        Map<String, Profile> habitCreatorMap = new HashMap<>();
        for (Profile profile : followed){
            for (Habit habit : profile.getHabits()){
                habitCreatorMap.put(habit.getId(), profile);
            }
        }

        habitsDisplayed = displayed.getFollowedHabits();
        List<String> list = new ArrayList<>();
        for (Habit habit : habitsDisplayed){
            Profile creator = habitCreatorMap.get(habit.getId());
            if (creator != null)
                list.add(getHabitDisplayText(habit, creator));
        }

        habitsListAdapter = new ArrayAdapter<>(this, R.layout.list_item, list);
        habitsList.setAdapter(habitsListAdapter);
    }

}
