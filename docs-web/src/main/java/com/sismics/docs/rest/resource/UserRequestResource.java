package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRequestDao;
import com.sismics.docs.core.dao.dto.UserRequestDto;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.docs.core.util.authentication.AuthenticationUtil;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * User request REST resources.
 *

 */
@Path("/user/request")
public class UserRequestResource extends BaseResource {

    /**
     * Creates a new user request.
     *
     * @param username Username
     * @param password Password
     * @param email Email
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {

        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");

        // Check if the username is already taken
        UserRequestDao userRequestDao = new UserRequestDao();
        if (userRequestDao.isUsernameExists(username)) {
            throw new ClientException("AlreadyExistingUsername", "Login already used");
        }

        // Create the user request
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setPassword(password);
        userRequest.setEmail(email);

        try {
            userRequestDao.create(userRequest);
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error creating user request", e);
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all user requests.
     *
     * @return Response
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        UserRequestDao userRequestDao = new UserRequestDao();
        List<UserRequestDto> userRequestDtoList = userRequestDao.findAll();

        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (UserRequestDto userRequestDto : userRequestDtoList) {
            requests.add(Json.createObjectBuilder()
                    .add("id", userRequestDto.getId())
                    .add("username", userRequestDto.getUsername())
                    .add("email", userRequestDto.getEmail())
                    .add("create_date", userRequestDto.getCreateDate().getTime())
                    .add("status", userRequestDto.getStatus()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Approves a user request.
     *
     * @param id User request ID
     * @return Response
     */
    /**
     * Approves a user request.
     *
     * @param id User request ID
     * @return Response
     */
    @POST
    @Path("approve/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approve(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the user request
        UserRequestDao userRequestDao = new UserRequestDao();
        UserRequest userRequest = userRequestDao.findById(id);
        if (userRequest == null) {
            throw new ClientException("UserRequestNotFound", "The user request does not exist");
        }

        // Update the request status
        userRequestDao.updateStatus(id, "APPROVED");

        // Create the user
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword()); // The create method will hash it correctly
        user.setEmail(userRequest.getEmail());
        user.setRoleId("user");
        user.setStorageQuota(10_000_000_000L); // Default 10GB
        user.setOnboarding(true);

        // Create the user - this method hashes the password
        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId());
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error creating user", e);
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Rejects a user request.
     *
     * @param id User request ID
     * @return Response
     */
    @POST
    @Path("reject/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reject(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the user request
        UserRequestDao userRequestDao = new UserRequestDao();
        UserRequest userRequest = userRequestDao.findById(id);
        if (userRequest == null) {
            throw new ClientException("UserRequestNotFound", "The user request does not exist");
        }

        // Update the request status
        userRequestDao.updateStatus(id, "REJECTED");

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}