package com.example.practisejdbl69.service.resource;


import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.domain.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
//    @NotBlank
//    private String userId;
    @Min(value = 0, message = "Rating should be greater than 0")
    @Max(value = 5, message = "Rating should be less than or equal to 5")
    private Double rating;
    @NotBlank(message = "Description cannot be blank")
    private String comment;
    @Min(value = 0, message = "bookId cannot be less than 0")
    private Integer bookId;

    public Review toReview(){
        return Review.builder().book(Book.builder().id(bookId).build())
                .comment(this.comment).rating(this.rating).build();
    }
}
