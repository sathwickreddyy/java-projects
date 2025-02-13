package com.example.practisejdbl69.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reviews")
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reviewId")
    private Integer id;
//    private String userId;
    private Double rating;
    private String comment;
    // Review-Book
    // Many to one -> Many entities of Review can be mapped with one entity of Book resource
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name="book_id") // className _ primaryKeyVariableName
    // kind of select * from books b, reviews r where b.id=r.book_id;
    private Book book;
}

/**
 * Cascade means, how the reference table should be there when the action is taken on entity: This to have ripple effect.
 *
 * If i update a book, how that change should be cascade to other tables.
 *
 * ALL -> Perform all the operations on the reference table also
 * PERSIST -> If an entitiy is persisted, persist the reference entity also
 *      if a book is created with review as data object of java and on saving book it should save the reviews also.
 * MERGE -> Incase of any update or re-entry merge the entites back.
 *              If book gets deleted, the reviews will not be deleted and will be orphan can be merged if same id is found.
 * REMOVE -> In case of delete, it deletes the references also (RISKY & DANGEROUS)
 *      If book gets deleted, the reviews will be deleted also
 *      Imagine a scenario: If One author, has multiple books, and if one of the book is deleted,
 *      the author should also be deleted. (Other books will be orphaned)
 * REFRESH ->
 *              Incase of refresh in parent or entity, the reference entity also gets refreshed
 *
 * DETACH -> (Safer)
*               Incase of delete in parent entity, the reference is detached from the parent and kept as it is.
 */

/**
 * Associations
 *
 * 1. Unidirectional -> from one entity we can fetch another entity but vice versa is not true
 * 2. Bidirectional -> from one entity we can fetch another entity
 *
 * One to many -> One entity can be mapped with multiple entities of other resource
 * Many to one -> Many entities can be mapped with one entity of other resource
 * Many to many -> Many entities can be mapped with multiple entities of other resource
 * One to One -> One entity can be mapped with one entity of other resource
 *
 * Fetch Types:
 *
 * 1. Eager
 *
 * When the entity is fetched from database, the asscoiated entities are also fetched at the same time
 *
 * 2. Lazy
 *
 * When the entity is fetched from database, the associated entities are not fetched at the same time
 *
 *
 * Default Fetch type:
 *
 * for OneToMany & ManyToMany - Lazy
 * for ManyToOne & OneToOne - Eager
 *
 */