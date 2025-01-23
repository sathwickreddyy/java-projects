package com.java.oops.bms.service.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class Show {
    private int showId;
    private long createdOn;
    private Movie movie;
    private Date startTime;
    private int durationInMinutes;
    private Theatre playingAtTheatre;

    public boolean isCompleted() {
        // Get the current time
        Date currentTime = new Date();

        // Calculate the end time of the show
        long endTimeMillis = startTime.getTime() + ((long) durationInMinutes * 60 * 1000);

        // Compare the current time with the end time
        return currentTime.getTime() > endTimeMillis;
    }
}
