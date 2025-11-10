package bookmarkd.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bookmarkd.api.entity.Book;
import bookmarkd.api.entity.Review;
import bookmarkd.api.entity.Review.Rating;
import bookmarkd.api.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ReviewService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 50;

	public Review createReview(Long bookId, Long authorId, String ratingValue, String content, LocalDateTime createdAt) {
		if (bookId == null) {
			throw new BadRequestException("bookId is required");
		}
		if (authorId == null) {
			throw new BadRequestException("authorId is required");
		}
		if (content == null || content.isBlank()) {
			throw new BadRequestException("content is required");
		}

		Book book = Book.findById(bookId);
		if (book == null) {
			throw new NotFoundException("Book not found for id: " + bookId);
		}

		User author = User.findById(authorId);
		if (author == null) {
			throw new NotFoundException("User not found for id: " + authorId);
		}

		var rating = parseRating(ratingValue);

		var review = new Review();
		review.book = book;
		review.author = author;
		review.content = content;
		review.rating = rating;
		review.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
		review.persist();

		return review;
	}

	public List<Review> listReviews(Long bookId, Long authorId, String ratingValue, Integer page, Integer size) {
		// Assemble a flexible JPQL query based on the provided filters.
		var query = new StringBuilder();
		Map<String, Object> parameters = new HashMap<>();

		if (bookId != null) {
			appendPredicate(query, "book.id = :bookId");
			parameters.put("bookId", bookId);
		}
		if (authorId != null) {
			appendPredicate(query, "author.id = :authorId");
			parameters.put("authorId", authorId);
		}
		if (ratingValue != null && !ratingValue.isBlank()) {
			var rating = parseRating(ratingValue);
			appendPredicate(query, "rating = :rating");
			parameters.put("rating", rating);
		}

		PanacheQuery<Review> panacheQuery;
		if (query.length() == 0) {
			panacheQuery = Review.findAll();
		} else {
			panacheQuery = Review.find(query.toString(), parameters);
		}

		return panacheQuery.page(resolvePage(page, size)).list();
	}

	@Transactional
	public Review likeReview(Long reviewId, Long userId) {
		if (reviewId == null) {
			throw new BadRequestException("reviewId is required");
		}
		if (userId == null) {
			throw new BadRequestException("userId is required");
		}

		Review review = Review.findById(reviewId);
		if (review == null) {
			throw new NotFoundException("Review not found for id: " + reviewId);
		}

		User user = User.findById(userId);
		if (user == null) {
			throw new NotFoundException("User not found for id: " + userId);
		}

		boolean alreadyLiked = review.likedBy.stream()
				.anyMatch(existing -> existing.id != null && existing.id.equals(user.id));
		if (!alreadyLiked) {
			review.likedBy.add(user);
		}

		return review;
	}

	@Transactional
	public Review unlikeReview(Long reviewId, Long userId) {
		if (reviewId == null) {
			throw new BadRequestException("reviewId is required");
		}
		if (userId == null) {
			throw new BadRequestException("userId is required");
		}

		Review review = Review.findById(reviewId);
		if (review == null) {
			throw new NotFoundException("Review not found for id: " + reviewId);
		}

		User user = User.findById(userId);
		if (user == null) {
			throw new NotFoundException("User not found for id: " + userId);
		}

		review.likedBy.removeIf(existing -> existing.id != null && existing.id.equals(user.id));
		return review;
	}

	private void appendPredicate(StringBuilder query, String predicate) {
		if (query.length() > 0) {
			query.append(" and ");
		}
		query.append(predicate);
	}

	private Rating parseRating(String ratingValue) {
		if (ratingValue == null || ratingValue.isBlank()) {
			throw new BadRequestException("rating is required");
		}
		try {
			return Rating.valueOf(ratingValue.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Unknown rating: " + ratingValue);
		}
	}

	private Page resolvePage(Integer page, Integer size) {
		int pageNumber = (page == null || page < 1) ? 1 : page;
		int pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		return Page.of(pageNumber - 1, pageSize);
	}
}
