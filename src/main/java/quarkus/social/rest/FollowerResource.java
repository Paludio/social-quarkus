package quarkus.social.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import quarkus.social.domain.model.Follower;
import quarkus.social.domain.model.User;
import quarkus.social.domain.repository.FollowerRepository;
import quarkus.social.exception.ErrorMessages;
import quarkus.social.exception.MyException;
import quarkus.social.exception.MyExceptionMapper;
import quarkus.social.rest.dto.FollowerRequest;
import quarkus.social.rest.dto.FollowerResponse;
import quarkus.social.rest.dto.FollowersPerUserResponse;

import java.util.List;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {
    private final FollowerRepository repository;
    private final UserResource userResource;

    @Inject
    public FollowerResource(FollowerRepository repository, UserResource userResource) {
        this.repository = repository;
        this.userResource = userResource;
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest request) {
        try {
            User user = userResource.findUserById(userId);
            User follower = userResource.findUserById(request.getFollowerId());

            if (userId.equals(request.getFollowerId())) {
                throw new MyException(ErrorMessages.CONFLICT.toString(), Response.Status.CONFLICT);
            }

            Boolean follows = repository.follows(follower, user);

            if (follows) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            Follower entityFollower = new Follower();
            entityFollower.setUser(user);
            entityFollower.setFollower(follower);

            repository.persist(entityFollower);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId) {
        try {
            userResource.findUserById(userId);

            List<Follower> followerByUser = repository.findByUser(userId);
            FollowersPerUserResponse followersPerUserResponse = new FollowersPerUserResponse();
            followersPerUserResponse.setFollowersCount(followerByUser.size());

            List<FollowerResponse> followersList = followerByUser.stream()
                    .map(FollowerResponse::new)
                    .toList();

            followersPerUserResponse.setContent(followersList);

            return Response.ok(followersPerUserResponse).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {
        try {
            userResource.findUserById(userId);

            repository.deleteByFollowerAndUser(userId, followerId);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }
}
