package com.example.practisejdbl69.service;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.domain.Review;

import java.util.List;

public interface BookService {
    void addBook(Book book);
    List<Book> getAllBooks();
    Book getBook(Integer id);

    void deleteBook(Integer id);
    Book updateBook(Integer id, Book book);
    void addReview(String bookId, Review review);
}
