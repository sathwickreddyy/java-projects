package com.example.practisejdbl69.repository;

import com.example.practisejdbl69.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    public List<Review> findByBookId(Integer bookId);
}
