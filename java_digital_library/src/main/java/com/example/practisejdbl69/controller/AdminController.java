package com.example.practisejdbl69.controller;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.service.BookService;
import com.example.practisejdbl69.service.resource.BookRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminController {

    private final BookService bookService;

    public AdminController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/admin/books")
    public ResponseEntity<Book> addBooks(@RequestBody @Valid List<BookRequest> bookRequests){
        bookRequests.forEach(bookRequest -> bookService.addBook(bookRequest.getBook()));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/admin/book")
    public ResponseEntity<Book> createBook(@RequestBody @Valid BookRequest book){
        bookService.addBook(book.getBook());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/admin/book")
    public ResponseEntity<Book> updateBook(@RequestParam("bookId") Integer id, @RequestBody Book book){
        return new ResponseEntity<>(bookService.updateBook(id, book), HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/book")
    public ResponseEntity<String> deleteBook(@RequestParam("bookId") Integer id){
        bookService.deleteBook(id);
        return new ResponseEntity<>("Book " +id+ " Deleted", HttpStatus.NO_CONTENT);
    }
}
