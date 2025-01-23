package com.java.oops.bms.service.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Location {
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
