package quarkus.social.rest;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import quarkus.social.domain.model.Post;
import quarkus.social.domain.model.User;
import quarkus.social.domain.repository.FollowerRepository;
import quarkus.social.domain.repository.PostRepository;
import quarkus.social.exception.ErrorMessages;
import quarkus.social.exception.MyException;
import quarkus.social.exception.MyExceptionMapper;
import quarkus.social.rest.dto.CreatePostRequest;
import quarkus.social.rest.dto.PostResponse;

import java.util.List;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostsResource {
    private final UserResource userResource;
    private final PostRepository postRepository;
    private final FollowerRepository followerRepository;

    @Inject
    public PostsResource(UserResource userResource, PostRepository postRepository, FollowerRepository followerRepository) {
        this.userResource = userResource;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long id, CreatePostRequest request) {
        try {
            User user = userResource.findUserById(id);
            Post post = new Post();
            post.setText(request.getText());
            post.setUser(user);

            postRepository.persist(post);

            return Response.status(Response.Status.CREATED).entity(post).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }

    @GET
    public Response listPost(@PathParam("userId") Long id, @HeaderParam("followerId") Long followerId) {
        try {
            if(followerId == null) {
                throw new MyException(ErrorMessages.HEADER_ERROR.toString(), Response.Status.BAD_REQUEST);
            }

            User user = userResource.findUserById(id);
            User follower = userResource.findUserById(followerId);

            Boolean follows = followerRepository.follows(follower, user);

            if(!follows) throw new MyException(ErrorMessages.FORBIDDEN.toString(), Response.Status.FORBIDDEN);

            PanacheQuery<Post> query = postRepository
                    .find("user", Sort.by("dateTime", Sort.Direction.Descending), user);

            List<PostResponse> posts = query
                    .list()
                    .stream()
                    .map(PostResponse::fromEntity)
                    .toList();

            return Response.ok(posts).build();
        } catch (MyException e) {
            return new MyExceptionMapper().toResponse(e);
        }
    }
}
