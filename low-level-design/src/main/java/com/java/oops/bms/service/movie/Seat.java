package com.java.oops.bms.service.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Setter
@Getter
@Slf4j
public class Seat {
    public enum SeatType{
        REGULAR, PREMIUM, RECLINER
    }

    private int seatId;
    private SeatType seatType;
    private boolean isReserved = false;
    private Movie correspondingMovie;
    private double price;

    public boolean isAvailable() {
        return !isReserved;
    }

    public boolean book()
    {
        if(!isReserved)
        {
            isReserved = true;
            return true;
        }
        log.info("Seat is not available");
        return false;
    }

    public void unReserve() {
        isReserved = false;
    }
}
