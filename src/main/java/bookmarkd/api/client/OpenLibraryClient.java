package bookmarkd.api.client;

import java.io.Serializable;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.*;

@RegisterRestClient(configKey = "open-library-api")
@Path("/search.json")
public interface OpenLibraryClient {
    public record OpenLibrarySearchResponse(
            Integer numFound,
            Integer start,
            List<OpenLibraryDoc> docs) implements Serializable {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
        public record OpenLibraryDoc(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorName,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
                        @JsonProperty("author_key") List<String> authorKeys) implements Serializable {
        // a lot of fields are omitted here
    }

    @GET
    OpenLibrarySearchResponse search(
            @QueryParam("q") String query,
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit,
            @QueryParam("fields") String fields);

    @GET
    @Path("{key}.json")
    OpenLibraryDoc getBookByKey(@PathParam("key") String key);
}
