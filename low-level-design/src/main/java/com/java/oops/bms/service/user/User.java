package com.java.oops.bms.service.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class User {
    private int id;
    private String name;
    private String email;
    private String mobile;
}
