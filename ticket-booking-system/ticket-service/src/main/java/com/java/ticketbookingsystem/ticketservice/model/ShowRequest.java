package com.java.ticketbookingsystem.ticketservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Table(name = "shows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"theatreId", "movieId", "hallId", "timing", "date"})
})
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String showId;
    @NotBlank
    private String theatreId;
    @NotBlank
    private String movieId;
    @NotBlank
    private String hallId;
    private LocalTime timing;
    private LocalDate date;
}
