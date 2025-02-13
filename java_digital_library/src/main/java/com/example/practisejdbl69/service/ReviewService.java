package com.example.practisejdbl69.service;

import com.example.practisejdbl69.domain.Review;

public interface ReviewService {
    void addReview(Review review);
    Review getReview(Integer id);
}
