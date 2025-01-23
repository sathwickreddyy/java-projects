package com.java.oops.bms.service.movie;

import com.java.oops.bms.service.user.TheatreAdmin;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Movie {
    private final  int movieId;
    private final  String movieName;
    private final  String movieDesc;
    private final  String genre;
    private final  String language;
    private final  String releaseDate;
    private final  TheatreAdmin movieAddedBy;
    private List<Show> shows = new ArrayList<>();
}
