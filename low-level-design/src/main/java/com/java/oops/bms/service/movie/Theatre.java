package com.java.oops.bms.service.movie;

import com.java.oops.bms.service.location.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@Slf4j
// it is like a movie hall (oka movie hall lo 2/3 movies run avthai daily but at one show time only one movie run avudhi)
public class Theatre {
    private int theatreId;
    private TheatreCompany theatreCompany;
    private String theatreName;
    private Location location;
    private Set<Movie> moviesCurrentlyPlaying;
    private Set<Show> shows;

    public void addMovie(Movie movie) {
        moviesCurrentlyPlaying.add(movie);
    }

    public void addShow(Show show) {
        shows.add(show);
    }

    public boolean removeMovie(Movie movie) {
        if (shows.stream().noneMatch(show -> show.getMovie() == movie && !show.isCompleted())) {
            moviesCurrentlyPlaying.remove(movie);
            return true;
        }
        log.info("This movie is being scheduled at current show, cannot remove this movie from this hall");
        return false;
    }

    public boolean removeShow(Show show) {
        if(show.isCompleted())
        {
            shows.remove(show);
            return true;
        }
        log.info("There is a movie that needs to be completed before removing this show");
        return false;
    }

}
