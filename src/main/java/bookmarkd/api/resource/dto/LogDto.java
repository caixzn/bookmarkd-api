package bookmarkd.api.resource.dto;

import java.time.LocalDateTime;

import bookmarkd.api.entity.Log;

public record LogDto(Long id,
        String action,
        LocalDateTime timestamp,
        BookSummaryDto book,
        UserSummaryDto user) {

    public static LogDto from(Log log) {
        if (log == null) {
            return null;
        }
        return new LogDto(
                log.id,
                log.action != null ? log.action.name().toLowerCase() : null,
                log.timestamp,
                BookSummaryDto.from(log.book),
                UserSummaryDto.from(log.user));
    }
}
