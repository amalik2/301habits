/*
 * Copyright (c) 2017 CMPUT301F17T15. This project is distributed under the MIT license.
 */

package com.cmput301.cia.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.cmput301.cia.R;
import com.cmput301.cia.activities.events.CreateHabitEventActivity;
import com.cmput301.cia.activities.events.HistoryActivity;
import com.cmput301.cia.activities.habits.CreateHabitActivity;
import com.cmput301.cia.activities.habits.HabitViewActivity;
import com.cmput301.cia.activities.habits.StatisticActivity;
import com.cmput301.cia.activities.templates.LocationRequestingActivity;
import com.cmput301.cia.activities.users.FollowRequestsFragment;
import com.cmput301.cia.activities.users.RankingsActivity;
import com.cmput301.cia.activities.users.SearchUsersFragment;
import com.cmput301.cia.activities.users.UserProfileActivity;
import com.cmput301.cia.activities.users.UserProfileFragment;
import com.cmput301.cia.activities.users.ViewEventsMapActivity;
import com.cmput301.cia.activities.users.ViewFollowedUsersActivity;
import com.cmput301.cia.models.AddHabitEvent;
import com.cmput301.cia.models.CompletedEventDisplay;
import com.cmput301.cia.models.Habit;
import com.cmput301.cia.models.HabitEvent;
import com.cmput301.cia.models.OfflineEvent;
import com.cmput301.cia.models.Profile;
import com.cmput301.cia.utilities.DateUtilities;
import com.cmput301.cia.utilities.LocationUtilities;
import com.cmput301.cia.utilities.NetworkUtilities;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;
import com.roughike.bottombar.TabSelectionInterceptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.OnCompleteListener;

/**
 * @author Adil Malik, Shipin Guan, Jessica Prieto
 * @version 7
 * Date: Nov 12 2017
 *
 * The landing activity after the user logs in to the app
 * used to be HomePageActivity
 */

public class HomeTabbedActivity extends LocationRequestingActivity {

    public static final int CREATE_EVENT = 1, CREATE_HABIT = 2, VIEW_HABIT = 3, VIEW_HABIT_HISTORY = 4, VIEW_PROFILE = 5;

    public static final String ID_PROFILE = "User";

    // Profile of the signed in user
    private Profile user;

    private BottomBar bottomBar;

    @Override
    public void onBackPressed() {
        confirmLogOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tabbed);

        loadCurrentUser();
        initializeBottomBar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //user app tour if first time login
        if (user.getFirstTimeUse()){
            userAppTour();
        }
    }

    /**
     * Home page functionality showcasing
     * For first time user only
     * Disabled after complete showcasing
     */
    private void userAppTour() {
        FancyShowCaseQueue showCaseQueue = new FancyShowCaseQueue();


        final FancyShowCaseView showCaseExpandable = new FancyShowCaseView.Builder(this)
                .title("Habits will be sorted by their category here")
                .focusOn(getDashboardFragment().getTabView(0))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .build();

        final FancyShowCaseView showCaseDaily = new FancyShowCaseView.Builder(this)
                .title("Today's habits will be displayed here")
                .focusOn(getDashboardFragment().getTabView(1))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .build();

        final FancyShowCaseView showCaseSearch = new FancyShowCaseView.Builder(this)
                .title("Search for new users here")
                .focusOn(bottomBar.getTabWithId(R.id.tab_search))
                .build();

        final FancyShowCaseView showCaseRequest = new FancyShowCaseView.Builder(this)
                .title("View follow requests that you have received here")
                .focusOn(bottomBar.getTabWithId(R.id.tab_followRequests))
                .build();

        final FancyShowCaseView showCaseProfile = new FancyShowCaseView.Builder(this)
                .title("Access your unique profile here")
                .focusOn(bottomBar.getTabWithId(R.id.tab_profile))
                .build();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int phoneWidth = displayMetrics.widthPixels;

        int offsetX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        int offsetY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, getResources().getDisplayMetrics());
        int radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        final FancyShowCaseView showCaseMenu = new FancyShowCaseView.Builder(this)
                .title("More navigation is available here")
                .focusCircleAtPosition(phoneWidth - offsetX, offsetY, radius)
                .build();



        final FancyShowCaseView showCaseHabit = new FancyShowCaseView.Builder(this)
                .title("Create your first habit here")
                .focusOn(bottomBar.getTabWithId(R.id.tab_addHabit))
                .build();

        showCaseQueue.add(showCaseExpandable);
        showCaseQueue.add(showCaseDaily);


        showCaseQueue.add(showCaseSearch);
        showCaseQueue.add(showCaseHabit);
        showCaseQueue.add(showCaseRequest);
        showCaseQueue.add(showCaseProfile);
        showCaseQueue.add(showCaseMenu);

        showCaseQueue.show();

        showCaseQueue.setCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                user.setFirstTimeUse(false);
                user.save();
            }
        });
    }

    @Override
    protected void handleLocationGranted() {

        Location location = LocationUtilities.getLocation(this);
        if (location == null) {
            if (!isFinishing())
                Toast.makeText(this, "Could not receive GPS location", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ViewEventsMapActivity.class);
        intent.putExtra(ViewEventsMapActivity.ID_EVENTS, (Serializable) user.getNearbyEvents(location));
        startActivity(intent);
    }

    /**
     * returns the BottomBar
     * @param id
     * @return BottomBar
     */
    public BottomBarTab getBottomBarTabFromId(@IdRes int id) {
        return bottomBar.getTabWithId(id);
    }

    /**
     * loads the current user that just logged in
     */
    private void loadCurrentUser() {
        Intent intent = getIntent();
        user = (Profile) intent.getSerializableExtra(ID_PROFILE);
        //if (user.hasValidId())
        //    user.load();

        user.synchronize();
        //System.out.println("EVENTS-> " + user.getHabits().get(0).getEvents().get(0).getComment());
        updateAllHabits(user);

        // handle any habits that may have been missed since the user's last login
        Date currentDate = new Date();
        if (user.getLastLogin() != null && !DateUtilities.isSameDay(user.getLastLogin(), currentDate)) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(user.getLastLogin());

            // go through each date between the user's last login and the current date
            while (!DateUtilities.isSameDay(calendar.getTime(), currentDate)){
                // update all events at the end of that date, to make sure they are marked as missed if they weren't completed
                // on that day
                user.onDayEnd(calendar.getTime());
                calendar.add(Calendar.DATE, 1);
            }
        }

        user.setLastLogin(currentDate);
        user.save();
    }

    /**
     * set up the navigation tabs at the bottom of the screen
     * navigation items:
     *      dashboard
     *      search
     *      add new habit
     *      follow requests
     *      profile
     */
    private void initializeBottomBar() {
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_dashboard:
                        onDashboardClicked();
                        break;
                    case R.id.tab_search:
                        onSearchClicked();
                        break;
                    case R.id.tab_addHabit:
                        break;
                    case R.id.tab_followRequests:
                        onFollowRequestClicked();
                        break;
                    case R.id.tab_profile:
                        onProfileClicked();
                        break;
                }
            }
        });

        bottomBar.setTabSelectionInterceptor(new TabSelectionInterceptor() {
            @Override
            public boolean shouldInterceptTabSelection(@IdRes int oldTabId, @IdRes int newTabId) {
                if (newTabId == R.id.tab_addHabit) {
                    onAddHabitClicked();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * goes to the Dashboard fragment and loads it in the activity
     */
    private void onDashboardClicked() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFragmentContainer, DashboardFragment.create(user));
        ft.commit();
    }

    /**
     * goes to the SearchActivity fragment and loads it in this activity
     */
    private void onSearchClicked() {
        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        // Replace the contents of the container with the new fragment
        ft.replace(R.id.homeFragmentContainer, SearchUsersFragment.create(user));

        // Complete the changes added above
        ft.commit();
    }

    /**
     * goes to the CreateHabitActivity
     */
    private void onAddHabitClicked() {
        Intent intent = new Intent(this, CreateHabitActivity.class);

        if (user.getHabitCategories() != null) {
            List<String> types = new ArrayList<>();
            types.addAll(user.getHabitCategories());
            intent.putStringArrayListExtra("Categories", (ArrayList<String>) types);
        }

        startActivityForResult(intent, HomeTabbedActivity.VIEW_HABIT);
    }

    /**
     * goes to the FollowRequestActivity fragment and loads it in this activity
     */
    private void onFollowRequestClicked() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFragmentContainer, FollowRequestsFragment.create(user));
        ft.commit();
    }

    /**
     * goes to the UserProfileFragment and loads it in this activity
     */
    private void onProfileClicked() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFragmentContainer, UserProfileFragment.create(user,user));
        ft.commit();
    }

    /**
     *  this method is for when a task is marked completed;
     *  it launches CreateHabitEventActivity
     *
     *  @param completions the name of the habit
     *  @param habitId the ID of the habit
     */
    public void onHabitClicked(String completions, String habitId) {
        Intent intent = new Intent(this, CreateHabitEventActivity.class);
        intent.putExtra(CreateHabitEventActivity.ID_HABIT_NAME, completions);
        intent.putExtra(CreateHabitEventActivity.ID_HABIT_HASH, habitId);
        startActivityForResult(intent, CREATE_EVENT);
    }

    //Create the menu object
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
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
        if (requestCode == CREATE_EVENT) {
            if (resultCode == RESULT_OK) {

                // successful event creation
                HabitEvent event = (HabitEvent) data.getSerializableExtra(CreateHabitEventActivity.RETURNED_HABIT);
                String habitId = data.getStringExtra(CreateHabitEventActivity.ID_HABIT_HASH);
                OfflineEvent addEvent = new AddHabitEvent(habitId, event);
                user.tryHabitEvent(addEvent);

                // workaround for offline events not being assigned an ID, so that they can still be modified/deleted while offline
                if (!event.hasValidId()){
                    long temporaryId = 0;
                    for (CompletedEventDisplay completedEventDisplay : user.getHabitHistory()){
                        if (!completedEventDisplay.getEvent().hasValidId() && completedEventDisplay.getEvent().getId() != null){
                            temporaryId = Long.valueOf(completedEventDisplay.getEvent().getId()) + 1;
                        }
                    }
                    event.setId(String.valueOf(temporaryId));
                }

                if (NetworkUtilities.hasInternetConnection(getApplicationContext())) {
                    user.save();
                }

                // update the today's tasks list
                updateAllHabits();
            }
        }
        //Read result from create habit activity
        else if(requestCode == CREATE_HABIT) {
            if(resultCode == RESULT_OK) {
                // add a new habit
                Habit habit = (Habit) data.getSerializableExtra("Habit");

                // save the habit so that it has a valid ID
                // this is necessary for habit events, since they need to refer to the habit's ID
                if (NetworkUtilities.hasInternetConnection(getApplicationContext()) && habit.save()){
                    user.addHabit(habit);
                    user.save();
                    updateAllHabits();
                } else {
                    Toast.makeText(this, "There was an error connecting to the database. Habit was not saved.", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else if (requestCode == HomeTabbedActivity.VIEW_HABIT) {
            if (resultCode == RESULT_OK){

                // whether the habit was deleted or updated
                boolean deleted = data.getBooleanExtra("Deleted", false);
                if (deleted) {
                    String id = data.getStringExtra("HabitID");
                    user.removeHabit(user.getHabitById(id));
                } else {
                    // update the habit
                    Habit habit = (Habit) data.getSerializableExtra("Habit");

                    if (user.getHabits().contains(habit)) {
                        for (Habit h : user.getHabits()){
                            if (h.equals(habit)){
                                h.copyFrom(habit);
                                break;
                            }
                        }
                    } else {
                        user.addHabit(habit);
                    }

                }

                updateAllHabits();
                user.save();
            }

        }
        else if (requestCode == VIEW_HABIT_HISTORY){
            if (resultCode == RESULT_OK) {
                user.copyFrom((Profile) data.getSerializableExtra(HistoryActivity.ID_PROFILE), true);
                if (NetworkUtilities.hasInternetConnection(getApplicationContext()))
                    user.save();
                else
                    user.savePendingEvents();

                updateAllHabits();
            }
        } else if (requestCode == VIEW_PROFILE){
            if (resultCode == RESULT_OK){
                user.copyFrom((Profile) data.getSerializableExtra(UserProfileActivity.RESULT_PROFILE_ID), false);
                if (NetworkUtilities.hasInternetConnection(getApplicationContext()))
                    user.save();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * a helper method to make sure that the habits data is up to date in the dashboard activity
     * @param newUser the user to set in the dashboard fragment
     */
    public void updateAllHabits(Profile newUser) {
        Fragment f = getFragmentForCurrentTab();
        if (f instanceof DashboardFragment) {
            ((DashboardFragment) f).updateHabits(newUser);
        }
    }

    /**
     * a helper method to make sure that the habits data is up to date in the dashboard activity
     */
    public void updateAllHabits() {
        Fragment f = getFragmentForCurrentTab();
        if (f instanceof DashboardFragment) {
            ((DashboardFragment) f).updateHabits();
        }
    }

    /**
     * @return the active fragment that's currently displayed in the screen
     */
    public Fragment getFragmentForCurrentTab() {
        return getSupportFragmentManager().findFragmentById(R.id.homeFragmentContainer);
    }

    /**
     * @return the dashboard fragment
     */
    public DashboardFragment getDashboardFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.homeFragmentContainer);
        if (f instanceof DashboardFragment) {
            return ((DashboardFragment) f);
        }
        return null;
    }

    //Menu item onclick bridge to specific activity.
    //use startActivityForResult instead of startActivity for return value or refresh home page.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_Statistic:
                Intent intent_Statistic = new Intent(this, StatisticActivity.class);
                intent_Statistic.putExtra(StatisticActivity.ID_USER, user);
                startActivity(intent_Statistic);
                return true;
            case R.id.menu_button_Habit_History:
                Intent intent_Habit_History = new Intent(this, HistoryActivity.class);
                intent_Habit_History.putExtra(HistoryActivity.ID_PROFILE, user);
                startActivityForResult(intent_Habit_History, VIEW_HABIT_HISTORY);
                return true;
            case R.id.menu_button_My_Following:
                Intent intent_My_Following = new Intent(this, ViewFollowedUsersActivity.class);
                intent_My_Following.putExtra(ViewFollowedUsersActivity.ID_VIEWED, user);
                startActivity(intent_My_Following);
                return true;
            case R.id.menu_button_PowerRankings:
                Intent intentPR = new Intent(this, RankingsActivity.class);
                startActivity(intentPR);
                return true;
            case R.id.menu_button_nearbyEvents:
                requestLocationPermissions();
                return true;
            case R.id.menu_button_logOut:
                confirmLogOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogOut() {
        /**
         * Reference: https://developer.android.com/guide/topics/ui/dialogs.html
         */
        // Ask the user for confirmation before a habit is deleted
        AlertDialog.Builder dialog = new AlertDialog.Builder(HomeTabbedActivity.this);
        dialog.setTitle("Log out " + user.getName() + "?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isFinishing())
                    return;
                finish();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        if (!isFinishing())
            dialog.show();
    }

    public void onStartHabitDetails(Habit habit, ArrayList<String> types) {
        Intent intent = new Intent(this, HabitViewActivity.class);
        intent.putExtra("Habit", habit);
        intent.putExtra("Categories", types);

        startActivityForResult(intent, HomeTabbedActivity.VIEW_HABIT);
    }
}
