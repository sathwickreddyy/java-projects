package com.java.oops.bms.service.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class TheatreCompany {
    private int companyId;
    private String name;
    private String officeAddress;
    private Set<Theatre> theatreList;

    public void addTheatre(Theatre theatre) {
        theatreList.add(theatre);
    }

    public void removeTheatre(Theatre theatre) {
        theatreList.remove(theatre);
    }
}
