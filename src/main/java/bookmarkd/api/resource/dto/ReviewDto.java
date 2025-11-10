package bookmarkd.api.resource.dto;

import java.time.LocalDateTime;
import java.util.List;

import bookmarkd.api.entity.Review;

public record ReviewDto(Long id,
        String rating,
        String content,
        LocalDateTime createdAt,
        BookSummaryDto book,
        UserSummaryDto author,
        List<UserSummaryDto> likedBy) {

    public static ReviewDto from(Review review) {
        if (review == null) {
            return null;
        }
        List<UserSummaryDto> likedUsers = review.likedBy == null
                ? List.of()
                : review.likedBy.stream().map(UserSummaryDto::from).toList();
        return new ReviewDto(
                review.id,
                review.rating != null ? review.rating.name().toLowerCase() : null,
                review.content,
                review.createdAt,
                BookSummaryDto.from(review.book),
                UserSummaryDto.from(review.author),
                likedUsers);
    }
}
