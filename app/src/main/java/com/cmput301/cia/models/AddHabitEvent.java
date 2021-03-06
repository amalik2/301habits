/*
 * Copyright (c) 2017 CMPUT301F17T15. This project is distributed under the MIT license.
 */

package com.cmput301.cia.models;

/**
 * @author Adil Malik
 * @version 3
 * Date: Nov 27 2017
 * This class represents a pending event to add a new habit event to a user's habit
 */

public class AddHabitEvent extends OfflineEvent {

    private String habitId;

    /**
     * Construct a new command object that will add the specified event when executed
     * @param habitId the id of the habit containing the event
     * @param event the event to add
     */
    public AddHabitEvent(String habitId, HabitEvent event){
        super(event);
        this.habitId = habitId;
    }

    /**
     * What this event does when it is synchronized with the server
     * @param profile the user who this event is being handled for
     * @return whether the event was handled successfully or not
     */
    @Override
    public boolean handle(Profile profile) {
        profile.completeHabitEvent(habitId, event);
        return profile.save();
    }

}
