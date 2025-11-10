package bookmarkd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import bookmarkd.api.entity.Review;
import bookmarkd.api.resource.dto.ReviewDto;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ReviewServiceTest {

    @Inject
    ReviewService reviewService;

    @Test
    @Transactional
    void createReview_persistsWithDefaultTimestamp() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var book = TestDataUtil.persistBook("Review Book", "Author", "2019");

        ReviewDto review = reviewService.createReview(book.id, author.id, "three_stars", "Solid read", null);

        assertNotNull(review.id());
        assertEquals("three_stars", review.rating());
        assertNotNull(review.createdAt());
        assertNotNull(Review.findById(review.id()));
    }

    @Test
    @Transactional
    void createReview_unknownRatingThrows() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var book = TestDataUtil.persistBook("Invalid Rating", "Author", "2018");

        assertThrows(BadRequestException.class,
                () -> reviewService.createReview(book.id, author.id, "not_a_rating", "Oops", null));
    }

    @Test
    @Transactional
    void createReview_missingBookThrowsNotFound() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");

        assertThrows(NotFoundException.class,
                () -> reviewService.createReview(999L, author.id, "one_star", "Missing book", null));
    }

    @Test
    @Transactional
    void listReviews_filtersByBookAndRating() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var book = TestDataUtil.persistBook("Filter Book", "Author", "2017");
        var otherBook = TestDataUtil.persistBook("Other Book", "Author", "2016");

        reviewService.createReview(book.id, author.id, "four_stars", "Great", null);
        reviewService.createReview(book.id, author.id, "five_stars", "Excellent", null);
        reviewService.createReview(otherBook.id, author.id, "five_stars", "Also excellent", null);

        List<ReviewDto> results = reviewService.listReviews(book.id, null, "five_stars", null, null);

        assertEquals(1, results.size());
        assertEquals("five_stars", results.get(0).rating());
        assertEquals(book.id, results.get(0).book().id());
    }

    @Test
    @Transactional
    void listReviews_paginatesResults() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var book = TestDataUtil.persistBook("Paged Book", "Author", "2022");

        reviewService.createReview(book.id, author.id, "one_star", "Not great", null);
        reviewService.createReview(book.id, author.id, "two_stars", "Okay", null);
        reviewService.createReview(book.id, author.id, "three_stars", "Fine", null);

        List<ReviewDto> firstPage = reviewService.listReviews(book.id, null, null, 1, 2);
        List<ReviewDto> secondPage = reviewService.listReviews(book.id, null, null, 2, 2);

        assertEquals(2, firstPage.size());
        assertEquals(1, secondPage.size());
    }

    @Test
    @Transactional
    void likeReview_addsUserOnce() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var liker = TestDataUtil.persistUser("fan");
        var book = TestDataUtil.persistBook("Liked Book", "Author", "2020");

        ReviewDto review = reviewService.createReview(book.id, author.id, "five_stars", "Great book", null);

        ReviewDto liked = reviewService.likeReview(review.id(), liker.id);
        assertEquals(1, liked.likedBy().size());
        assertTrue(liked.likedBy().stream().anyMatch(user -> user.id().equals(liker.id)));

        ReviewDto likedAgain = reviewService.likeReview(review.id(), liker.id);
        assertEquals(1, likedAgain.likedBy().size());
    }

    @Test
    @Transactional
    void unlikeReview_removesUserLike() {
        TestDataUtil.clearDatabase();
        var author = TestDataUtil.persistUser("author");
        var liker = TestDataUtil.persistUser("fan");
        var book = TestDataUtil.persistBook("Unliked Book", "Author", "2021");

        ReviewDto review = reviewService.createReview(book.id, author.id, "four_stars", "Good", null);
        reviewService.likeReview(review.id(), liker.id);

        ReviewDto updated = reviewService.unlikeReview(review.id(), liker.id);
        assertTrue(updated.likedBy().stream().noneMatch(user -> user.id().equals(liker.id)));

        ReviewDto idempotent = reviewService.unlikeReview(review.id(), liker.id);
        assertFalse(idempotent.likedBy().stream().anyMatch(user -> user.id().equals(liker.id)));
    }
}
