package com.example.practisejdbl69.service.impl;

import com.example.practisejdbl69.domain.Book;
import com.example.practisejdbl69.domain.Review;
import com.example.practisejdbl69.repository.BookRepository;
import com.example.practisejdbl69.repository.ReviewRepository;
import com.example.practisejdbl69.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookServiceImpl implements BookService {

    BookRepository bookRepository;

    // Setter injection through Test Case
    @Autowired
    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }


    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    //Field redisTemplate in com.example.practisejdbl69.service.impl.BookServiceImpl required a bean of type 'org.springframework.data.redis.core.RedisTemplate' that could not be found.
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addBook(Book book){
        /*
            redisTemplate.opsForValue().set("MyJavaKey", "myJavaValue"); // byteCode will be stored
            System.out.println(redisTemplate.opsForValue().get("MyJavaKey"));
            redisTemplate.opsForList().leftPush("listTest", "1");
            redisTemplate.opsForList().leftPush("listTest", "2");
            redisTemplate.opsForList().leftPush("listTest", "4");
            redisTemplate.opsForList().leftPush("listTest", "15");
            System.out.println(redisTemplate.opsForList().leftPop("listTest"));
            System.out.println(redisTemplate.opsForList().rightPop("listTest"));
            redisTemplate.opsForHash().put("book", book.getAuthor(), book.getCost());
            redisTemplate.opsForHash().put("book", book.getTitle(), book); // Jackson2JsonRedisSerializer
            // to create other encryption we need to create and set the CustomKeySerializer in redisTemplateBean Creation
          */
        bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks(){
//        List<Book> books = bookRepository.findAll();
//        books.forEach(book -> { Manual way of binding entities
//            List<Review> reviews = reviewRepository.findByBookId(book.getId());
////            book.setReviewList(reviews);
//        });

        List<Book> books = bookRepository.findAll();
        if(books.isEmpty()){
            throw new IllegalArgumentException("Empty List and cannot be returned");
        }
        return books; // hibernate takes care of joining and returning
    }

    @Override
    public Book getBook(Integer id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteBook(Integer id){
        bookRepository.deleteById(id);
    }

    @Override
    public Book updateBook(Integer id, Book book) {
        Optional<Book> originalBook = bookRepository.findById(id);
        if(originalBook.isPresent()){
            bookRepository.save(book);
        }
        return book;
    }

    @Override
    public void addReview(String bookId, Review review) {
//        Book book = getBook(bookId);
//        if(book != null) {
//            book.getReviewList().add(review);
//        }
    }

}
