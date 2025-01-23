package com.java.oops.bms.service.user;


import com.java.oops.bms.service.booking.Booking;

import java.util.List;

public class Customer extends User {

    private List<Booking> currentBookings;

    public Customer(int id, String name, String email, String mobile) {
        super(id, name, email, mobile);
    }

    // Make Booking
    public boolean makeBooking(Booking booking) {
        // if success return true and add to list of bookings
        // if failed then false
        return false;
    }

    // Cancel Booking
    public boolean cancelBooking(Booking booking) {
        return false;
    }

    // List all Bookings
    public List<Booking> getBookings() {
        return currentBookings;
    }
}
