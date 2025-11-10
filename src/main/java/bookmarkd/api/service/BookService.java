package bookmarkd.api.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import bookmarkd.api.client.OpenLibraryClient;
import bookmarkd.api.client.OpenLibraryClient.OpenLibraryDoc;
import bookmarkd.api.entity.Book;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BookService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Inject
    @RestClient
    OpenLibraryClient openLibraryClient;

    @CacheResult(cacheName = "open-library-search")
    @Transactional
    public List<OpenLibraryDoc> searchBooks(String query, Integer page, Integer limit) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }

        var response = openLibraryClient.search(query, page, limit,
                "key,title,author_name,first_publish_year,author_key");

        var docs = response.docs();
        if (docs != null) {
            docs.stream()
                    .filter(doc -> doc != null && doc.key() != null)
                    .forEach(this::persistBookFromDoc);
        }

        return docs == null ? List.of() : List.copyOf(docs);
    }

    @CacheResult(cacheName = "open-library-docs")
    @Transactional
    public OpenLibraryDoc getBookByKey(String key) {
        var book = openLibraryClient.getBookByKey(key);
        
        if (book == null) {
            throw new NotFoundException("Book not found for key: " + key);
        }
        
        persistBookFromDoc(book);
        return book;
    }

    private void persistBookFromDoc(OpenLibraryDoc doc) {
        if (doc == null || doc.key() == null) {
            return;
        }

        Book persistent = Book.find("openLibraryKey", doc.key()).firstResult();
        if (persistent == null) {
            persistent = new Book();
            persistent.openLibraryKey = doc.key();
        }

        persistent.title = doc.title();
        persistent.author = doc.authorName() != null && !doc.authorName().isEmpty()
                ? String.join(", ", doc.authorName())
                : null;
        persistent.publishedYear = doc.firstPublishYear() != null ? String.valueOf(doc.firstPublishYear()) : null;
        persistent.openLibraryAuthorKey = doc.authorKeys() != null && !doc.authorKeys().isEmpty()
                ? String.join(", ", doc.authorKeys())
                : null;

        if (!persistent.isPersistent()) {
            persistent.persist();
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Book> listPersistedBooks(Integer page, Integer size) {
        return Book.findAll(Sort.by("title"))
                .page(resolvePage(page, size))
                .list();
    }

    private Page resolvePage(Integer page, Integer size) {
        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return Page.of(pageNumber - 1, pageSize);
    }
}
