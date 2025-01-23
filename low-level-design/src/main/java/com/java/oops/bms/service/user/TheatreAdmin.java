package com.java.oops.bms.service.user;

import com.java.oops.bms.service.movie.Movie;
import com.java.oops.bms.service.movie.Show;
import com.java.oops.bms.service.movie.Theatre;

public class TheatreAdmin extends User {

    public TheatreAdmin(int id, String name, String email, String mobile) {
        super(id, name, email, mobile);
    }

    public void addMovie(Movie movie, Theatre theatre) {
        theatre.addMovie(movie);
    }

    public void addShow(Theatre theatre, Show show) {
        theatre.addShow(show);
    }
}
