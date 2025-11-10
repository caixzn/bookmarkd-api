package bookmarkd.api.service;

import bookmarkd.api.entity.Book;
import bookmarkd.api.entity.Log;
import bookmarkd.api.entity.Review;
import bookmarkd.api.entity.User;

final class TestDataUtil {
    private TestDataUtil() {
    }

    static void clearDatabase() {
        Log.deleteAll();
        Review.deleteAll();
        Book.deleteAll();
        User.deleteAll();
    }

    static User persistUser(String username) {
        var user = new User();
        user.username = username;
        user.persist();
        return user;
    }

    static Book persistBook(String title, String author, String year) {
        var book = new Book();
        book.title = title;
        book.author = author;
        book.publishedYear = year;
        book.persist();
        return book;
    }
}
