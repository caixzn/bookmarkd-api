package bookmarkd.api.resource;

import java.time.LocalDateTime;
import java.util.List;

import bookmarkd.api.entity.Log;
import bookmarkd.api.service.LogService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogResource {

    @Inject
    LogService logService;

    @POST
    @Transactional
    public Response createLog(CreateLogRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        Log log = logService.createLog(request.bookId(), request.userId(), request.action(), request.timestamp());
        return Response.status(Response.Status.CREATED).entity(log).build();
    }

    @GET
    public List<Log> listLogs(@QueryParam("bookId") Long bookId,
            @QueryParam("userId") Long userId,
            @QueryParam("action") String actionValue,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {
        return logService.listLogs(bookId, userId, actionValue, page, size);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Log updateLog(@PathParam("id") Long logId, UpdateLogRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.bookId() == null && request.userId() == null && (request.action() == null || request.action().isBlank())
                && request.timestamp() == null) {
            throw new BadRequestException("At least one field must be provided for update");
        }
        return logService.updateLog(logId, request.bookId(), request.userId(), request.action(), request.timestamp());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteLog(@PathParam("id") Long logId) {
        logService.deleteLog(logId);
        return Response.noContent().build();
    }

    public record CreateLogRequest(Long bookId, Long userId, String action, LocalDateTime timestamp) {
    }

    public record UpdateLogRequest(Long bookId, Long userId, String action, LocalDateTime timestamp) {
    }
}
