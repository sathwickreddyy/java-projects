package com.java.oops.bms.service.booking;


import com.java.oops.bms.service.movie.Seat;
import com.java.oops.bms.service.movie.Show;
import com.java.oops.bms.service.payment.Payment;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Booking {
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, CHECKED_IN
    }
    private final String bookingID;
    private final Show show;
    private Date bookingDate;
    private BookingStatus bookingStatus;
    private final List<Seat> seats;
    private Payment payment;

    public Booking(String bookingID, Show show, List<Seat> seats) {
        this.bookingID = bookingID;
        this.show = show;
        this.bookingStatus = BookingStatus.PENDING;
        this.seats = seats;
    }

    public boolean makePayment(Payment payment) {
        // if payment succeeds then update the payment & confirm the seats
        this.payment = payment;
        // success -> seats.map(seat.book())
        // update status to CONFIRMED
        // failure
        // Mark the status as cancelled and seats.foreach(unreserver) and return false
        return false;
    }

    public boolean cancel()
    {
        if(bookingStatus != BookingStatus.CHECKED_IN)
        {
            bookingStatus = BookingStatus.CANCELLED;
            seats.forEach(Seat::unReserve);
            return true;
        }
        return false;
    }
}
