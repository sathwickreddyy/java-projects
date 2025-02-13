package com.example.practisejdbl69.service.impl;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.repository.BookRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceImplTest {

    @Test
    void testGetAllBooks_EmptyList() {
        BookServiceImpl bookService = new BookServiceImpl();
        //mocking
        BookRepository bookRepository = mock(BookRepository.class);
        bookService.setBookRepository(bookRepository);
        //stubbing
        List<Book> bookList = new ArrayList<>();
//        bookList.add(new Book());
        when(bookRepository.findAll()).thenReturn(bookList);
        assertThrows(IllegalArgumentException.class, () -> bookService.getAllBooks());

//        bookService.getAllBooks();
    }

    @Test
    void getAllBooks(){
        BookServiceImpl bookService = new BookServiceImpl();
        //mocking
        BookRepository bookRepository = mock(BookRepository.class);
        bookService.setBookRepository(bookRepository);
//        bookService.getAllBooks();

        //stubbing
        List<Book> bookList = new ArrayList<>();
        bookList.add(new Book());
        when(bookRepository.findAll()).thenReturn(bookList);
        bookService.getAllBooks();
    }


    @Test
    void testAddBook_InvalidBookWithSpy(){
        // check beuldang for test cases.
    }
}


/*
  Unit testing -> Testing of the code blocks a developer has written. We mock mostly in UT using mocito library
               : Mock -> Mimic or mock the object in the test case.
                   -ex : AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
                   the dependency bean is also handled in the mock.
               : Stub -> We mock the behavior of the object.
                   -ex : for bookService, the mocked bookRepository, get a custom output of a specific method.
               : Spy  -> To help stubbing when the object is not mocked.
                   -ex : For non-mocked objects, we can spy for them to call a method which are stubbed and give
                   stubbed output to them.
  It covers all the scenarios of the developer.
  Code Coverage -> Coverage of the build is determined by the number of lines covered by test cases
               -> 100 % code coverage is ideal.
  Functional Testing -> Testing of the functionality of the application. Where the entire operation of the entity or feature
  is tested.
 */