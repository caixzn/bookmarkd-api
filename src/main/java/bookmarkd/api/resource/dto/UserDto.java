package bookmarkd.api.resource.dto;

import java.util.List;

import bookmarkd.api.entity.User;

public record UserDto(Long id, String username, List<ReviewSummaryDto> reviews) {
    public static UserDto from(User user) {
        if (user == null) {
            return null;
        }
        List<ReviewSummaryDto> reviewDtos = user.reviews == null
                ? List.of()
                : user.reviews.stream().map(ReviewSummaryDto::from).toList();
        return new UserDto(user.id, user.username, reviewDtos);
    }
}
