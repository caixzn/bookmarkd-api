package bookmarkd.api.resource.dto;

import bookmarkd.api.entity.User;

public record UserSummaryDto(Long id, String username) {
    public static UserSummaryDto from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(user.id, user.username);
    }
}
