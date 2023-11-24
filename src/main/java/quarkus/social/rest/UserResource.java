package quarkus.social.rest;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import quarkus.social.domain.model.User;
import quarkus.social.domain.repository.UserRepository;
import quarkus.social.exception.ErrorMessages;
import quarkus.social.exception.MyException;
import quarkus.social.exception.MyExceptionMapper;
import quarkus.social.rest.dto.CreateUserRequest;
import quarkus.social.rest.dto.ResponseError;

import java.sql.Date;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserRepository repository;
    private final Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);

        if(!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY);
        }

        User user = new User();
        user.setAge(Date.valueOf(userRequest.getAge()));
        user.setName(userRequest.getName());

        repository.persist(user);

        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    public Response listAllUsers() {
        PanacheQuery<User> query = repository.findAll();
        return Response.ok(query.list()).build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            User user = this.findUserById(id);

            repository.delete(user);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }

    @PUT
    @Transactional
    @Path("{id}")
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData) {
        try {
            User user = this.findUserById(id);

            user.setName(userData.getName());
            user.setAge(Date.valueOf(userData.getAge()));

            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }

    public User findUserById(Long id) throws MyException {
        User user = repository.findById(id);
        if (user != null) {
            return user;
        }

        throw new MyException(ErrorMessages.USER_NOT_FOUND.toString(), Response.Status.NOT_FOUND);
    }
}
