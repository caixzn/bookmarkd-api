package bookmarkd.api.resource.dto;

import java.time.LocalDateTime;
import bookmarkd.api.entity.Review;

public record ReviewSummaryDto(Long id,
        String rating,
        String content,
        LocalDateTime createdAt,
        UserSummaryDto author,
        int likeCount) {

    public static ReviewSummaryDto from(Review review) {
        if (review == null) {
            return null;
        }
        int likes = review.likedBy == null ? 0 : review.likedBy.size();
        return new ReviewSummaryDto(
                review.id,
                review.rating != null ? review.rating.name().toLowerCase() : null,
                review.content,
                review.createdAt,
                UserSummaryDto.from(review.author),
                likes);
    }
}
