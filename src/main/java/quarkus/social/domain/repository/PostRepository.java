package quarkus.social.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import quarkus.social.domain.model.Post;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {
}
