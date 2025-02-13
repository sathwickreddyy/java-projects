package com.example.practisejdbl69.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import com.example.practisejdbl69.domain.Review;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
@Entity
@Builder
public class Book {
    private String title;
    private String author;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // not part of request while adding
    @Enumerated(value = EnumType.STRING)
    private Genre genre;
    private Double rating; // not part of request while adding
    private Double cost;
    private Integer year;
    // One to many -> One entity of book can be mapped with multiple entities of other Reviews
    // Book - Review
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY) // variable name in Review entity
    @JsonIgnoreProperties("book") // breaks the infinite loop cycle. Everytime we fetch a book, it fetches review and every review has a book and this cycle repeats
    // ignores the book property from review, while serializing the result
    private List<Review> reviews;

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", id=" + id +
                ", genre=" + genre +
                ", rating=" + rating +
                ", cost=" + cost +
                ", year=" + year +
                ", reviews=" + reviews +
                '}';
    }
}

/***
 * Types of ID generation
 *
 *     TABLE -> keeping a separate table for the Id generation.
 *     SEQUENCE -> where the last sequence is identified and IDs are updated . -> postgres
 *     IDENTITY -> which is supported by the under-laying database. eg: mysql, sql server.
 *     UUID -> generate a unique identifer, randomly generated.
 *     AUTO -> which will the framwork to support the one by the under-laying data or go forward with Identity.
 */