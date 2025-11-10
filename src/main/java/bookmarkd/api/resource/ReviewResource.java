package bookmarkd.api.resource;

import java.time.LocalDateTime;
import java.util.List;

import bookmarkd.api.resource.dto.ReviewDto;
import bookmarkd.api.service.ReviewService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {

    @Inject
    ReviewService reviewService;

    @POST
    @Transactional
    public Response createReview(CreateReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        ReviewDto review = reviewService.createReview(request.bookId(), request.authorId(), request.rating(),
                request.content(), request.createdAt());
        return Response.status(Response.Status.CREATED).entity(review).build();
    }

    @GET
    public List<ReviewDto> listReviews(@QueryParam("bookId") Long bookId,
            @QueryParam("authorId") Long authorId,
            @QueryParam("rating") String ratingValue,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {
        return reviewService.listReviews(bookId, authorId, ratingValue, page, size);
    }

    @POST
    @Path("/{id}/likes")
    @Transactional
    public ReviewDto likeReview(@PathParam("id") Long reviewId, LikeReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.userId() == null) {
            throw new BadRequestException("userId is required");
        }
        return reviewService.likeReview(reviewId, request.userId());
    }

    @DELETE
    @Path("/{id}/likes/{userId}")
    @Transactional
    public ReviewDto unlikeReview(@PathParam("id") Long reviewId, @PathParam("userId") Long userId) {
        return reviewService.unlikeReview(reviewId, userId);
    }

    public record CreateReviewRequest(Long bookId, Long authorId, String rating, String content,
            LocalDateTime createdAt) {
    }

    public record LikeReviewRequest(Long userId) {
    }
}
