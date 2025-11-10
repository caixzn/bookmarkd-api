package bookmarkd.api.resource.dto;

import bookmarkd.api.entity.Book;

public record BookSummaryDto(Long id, String title, String author, String openLibraryKey) {
    public static BookSummaryDto from(Book book) {
        if (book == null) {
            return null;
        }
        return new BookSummaryDto(book.id, book.title, book.author, book.openLibraryKey);
    }
}
