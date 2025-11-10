package bookmarkd.api.resource;

import java.util.List;

import bookmarkd.api.resource.dto.UserDto;
import bookmarkd.api.service.UserService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Transactional
    public Response createUser(CreateUserRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        UserDto user = userService.createUser(request.username());
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    public List<UserDto> listUsers(@QueryParam("username") String username,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {
        return userService.listUsers(username, page, size);
    }

    @GET
    @Path("/{id}")
    public UserDto getUser(@PathParam("id") Long id) {
        return userService.getUser(id);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public UserDto updateUser(@PathParam("id") Long id, UpdateUserRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        return userService.updateUser(id, request.username());
    }

    public record CreateUserRequest(String username) {
    }

    public record UpdateUserRequest(String username) {
    }
}
