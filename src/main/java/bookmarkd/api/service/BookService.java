package bookmarkd.api.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import bookmarkd.api.client.OpenLibraryClient;
import bookmarkd.api.client.OpenLibraryClient.OpenLibraryDoc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookService {
    @Inject
    @RestClient
    OpenLibraryClient openLibraryClient;

    public List<OpenLibraryDoc> searchBooks(String query, Integer page, Integer limit) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }

        var response = openLibraryClient.search(query, page, limit, "key,title,author_name,first_publish_year");
        return response.docs();
    }
}
