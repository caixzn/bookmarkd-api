package bookmarkd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import bookmarkd.api.client.OpenLibraryClient;
import bookmarkd.api.client.OpenLibraryClient.OpenLibraryDoc;
import bookmarkd.api.client.OpenLibraryClient.OpenLibrarySearchResponse;
import bookmarkd.api.entity.Book;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@QuarkusTest
class BookServiceTest {

    @Inject
    BookService bookService;

    @Inject
    @InjectMock
    @RestClient
    OpenLibraryClient openLibraryClient;

    @Test
    @Transactional
    void searchBooks_persistsResults() {
        TestDataUtil.clearDatabase();
        var doc = new OpenLibraryDoc("/works/OL123W", "Sample Title", List.of("Author One", "Author Two"), 1999,
                List.of("OL1A", "OL2A"));
        var response = new OpenLibrarySearchResponse(1, 0, List.of(doc));

        when(openLibraryClient.search(eq("sample"), eq(1), eq(10),
                eq("key,title,author_name,first_publish_year,author_key"))).thenReturn(response);

        List<OpenLibraryDoc> docs = bookService.searchBooks("sample", null, null);

        assertEquals(1, docs.size());
        Book persisted = Book.find("openLibraryKey", doc.key()).firstResult();
        assertNotNull(persisted);
        assertEquals("Sample Title", persisted.title);
        assertEquals("Author One, Author Two", persisted.author);
        assertEquals("OL1A, OL2A", persisted.openLibraryAuthorKey);
        assertEquals("1999", persisted.publishedYear);
    }

    @Test
    @Transactional
    void getBookByKey_persistsBook() {
        TestDataUtil.clearDatabase();
        var doc = new OpenLibraryDoc("/works/OL999W", "Another Title", List.of("Author"), 2005, List.of("OL1A"));

        when(openLibraryClient.getBookByKey("/works/OL999W")).thenReturn(doc);

        OpenLibraryDoc result = bookService.getBookByKey("/works/OL999W");

        assertEquals(doc, result);
        Book persisted = Book.find("openLibraryKey", "/works/OL999W").firstResult();
        assertNotNull(persisted);
        assertEquals("Another Title", persisted.title);
    }

    @Test
    @Transactional
    void listPersistedBooks_returnsSortedResults() {
        TestDataUtil.clearDatabase();

        TestDataUtil.persistBook("Beta Title", "Author Two", "2001");
        TestDataUtil.persistBook("Alpha Title", "Author One", "1999");

        List<Book> books = bookService.listPersistedBooks(null, null);

        assertEquals(2, books.size());
        assertEquals("Alpha Title", books.get(0).title);
        assertEquals("Beta Title", books.get(1).title);
    }

    @Test
    @Transactional
    void listPersistedBooks_honorsPagination() {
        TestDataUtil.clearDatabase();

        TestDataUtil.persistBook("Alpha Title", "Author One", "1999");
        TestDataUtil.persistBook("Beta Title", "Author Two", "2001");
        TestDataUtil.persistBook("Gamma Title", "Author Three", "2003");

        List<Book> firstPage = bookService.listPersistedBooks(1, 2);
        List<Book> secondPage = bookService.listPersistedBooks(2, 2);

        assertEquals(2, firstPage.size());
        assertEquals("Alpha Title", firstPage.get(0).title);
        assertEquals("Beta Title", firstPage.get(1).title);

        assertEquals(1, secondPage.size());
        assertEquals("Gamma Title", secondPage.get(0).title);
    }
}
