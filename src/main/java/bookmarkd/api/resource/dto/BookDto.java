package bookmarkd.api.resource.dto;

import java.util.List;

import bookmarkd.api.entity.Book;

public record BookDto(Long id,
        String title,
        String author,
        String publishedYear,
        String openLibraryKey,
        String openLibraryAuthorKey,
        List<ReviewSummaryDto> reviews) {

    public static BookDto from(Book book) {
        if (book == null) {
            return null;
        }
        List<ReviewSummaryDto> reviewDtos = book.reviews == null
                ? List.of()
                : book.reviews.stream().map(ReviewSummaryDto::from).toList();
        return new BookDto(
                book.id,
                book.title,
                book.author,
                book.publishedYear,
                book.openLibraryKey,
                book.openLibraryAuthorKey,
                reviewDtos);
    }
}
