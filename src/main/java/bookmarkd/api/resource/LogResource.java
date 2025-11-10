package bookmarkd.api.resource;

import java.time.LocalDateTime;
import java.util.List;

import bookmarkd.api.entity.Log;
import bookmarkd.api.service.LogService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
            @QueryParam("action") String actionValue) {
        return logService.listLogs(bookId, userId, actionValue);
    }

    public record CreateLogRequest(Long bookId, Long userId, String action, LocalDateTime timestamp) {
    }
}
