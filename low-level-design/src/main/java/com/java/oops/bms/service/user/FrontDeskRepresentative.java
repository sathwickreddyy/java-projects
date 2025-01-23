package com.java.oops.bms.service.user;

import com.java.oops.bms.service.booking.Booking;
import lombok.Getter;
import lombok.Setter;

public class FrontDeskRepresentative extends User {
    @Getter
    @Setter
    private int theatreId;

    public FrontDeskRepresentative(int id, int theatreId, String name, String email, String mobile) {
        super(id, name, email, mobile);
        this.theatreId = theatreId;
    }

    // create booking
    public boolean createBookingAndIssueTicket(Booking booking) {
        // add logic
        return false;
    }
}
