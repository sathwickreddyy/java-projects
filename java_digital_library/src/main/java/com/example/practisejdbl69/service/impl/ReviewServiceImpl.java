package com.example.practisejdbl69.service.impl;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.domain.Review;
import com.example.practisejdbl69.repository.BookRepository;
import com.example.practisejdbl69.repository.ReviewRepository;
import com.example.practisejdbl69.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookRepository bookRepository;

    @Override
    public void addReview(Review review) {
        Optional<Book> book = bookRepository.findById(review.getBook().getId());
        if(book.isPresent()){
            review.setBook(book.get()); // update book with actual book
            reviewRepository.save(review);
        }
        else{
            throw new IllegalArgumentException("Book with book Id "+review.getBook().getId()+" Not found!");
        }

    }

    @Override
    public Review getReview(Integer id) {
        Optional<Review> review = reviewRepository.findById(id);
        return review.orElse(null);
    }
}
