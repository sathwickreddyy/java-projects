package com.java.oops.bms.service.user;

import com.java.oops.bms.TicketBookingSystem;
import com.java.oops.bms.service.movie.TheatreCompany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SystemAdmin extends User {
    @Getter
    @Setter
    private TicketBookingSystem system;

    public SystemAdmin(int id, String name, String email, String mobile) {
        super(id, name, email, mobile);
    }

    public List<TheatreCompany> getTheatreCompanies() {
        return system.getTheatreCompanies();
    }

    public void removeTheatreCompanies(TheatreCompany theatreCompany) {
        system.removeClient(theatreCompany);
    }

    public void addTheatreCompany(TheatreCompany theatreCompany) {
        system.addClient(theatreCompany);
    }
}
