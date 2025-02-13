package com.example.practisejdbl69.controller;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.domain.Review;
import com.example.practisejdbl69.domain.User;
import com.example.practisejdbl69.service.BookService;
import com.example.practisejdbl69.service.UserService;
import com.example.practisejdbl69.service.resource.BookRequest;
import com.example.practisejdbl69.service.resource.UserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final BookService bookService;
    private final UserService userService;

    public UserController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping("/book")
    public ResponseEntity<List<Book>> getAllBooks(){
        return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") Integer bookId){
        return new ResponseEntity<>(bookService.getBook(bookId), HttpStatus.OK);
    }


    @PostMapping("{bookId}/review")
    public ResponseEntity<Book> addReview(@PathVariable("bookId") String bookId, @RequestBody Review review){
        bookService.addReview(bookId, review);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRequest userRequest){
        userService.addUser(userRequest.toUser());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}


/***
 * Two ways to read from the Get API
 *
 * 1. Query param
 *
 * ones which are after the ? symbol on the URL and has key as variable name and value as value.
 * e.x -> book?bookId=1
 *
 * @RequestParam -> should be used in the parameters of method to map the values of URL to Java variables.
 *
 * 2. Path param
 *
 * ones which are part of the URL itself.
 *
 * E.x -> book/1 , book/43
 * where 1 is the ID of the book.
 *
 * @PathVariable -> should be used in the  part of the URL  to map the values of Java variables.
 *
 * */
