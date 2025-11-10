package bookmarkd.api.resource;

import java.util.List;

import bookmarkd.api.client.OpenLibraryClient.OpenLibraryDoc;
import bookmarkd.api.resource.dto.BookDto;
import bookmarkd.api.service.BookService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/books")
public class BookResource {
    @Inject
    BookService bookService;

    @GET
    public List<OpenLibraryDoc> searchBooks(@QueryParam("q") String query,
                                             @QueryParam("page") Integer page,
                                             @QueryParam("limit") Integer limit) {
        return bookService.searchBooks(query, page, limit);
    }

    @GET
    @Path("/key")
    public OpenLibraryDoc getBookByKey(@QueryParam("key") String key) {
        return bookService.getBookByKey(key);
    }

    @GET
    @Path("/persisted")
    public List<BookDto> listPersistedBooks(@QueryParam("page") Integer page,
                                            @QueryParam("size") Integer size) {
        return bookService.listPersistedBooks(page, size);
    }
}
