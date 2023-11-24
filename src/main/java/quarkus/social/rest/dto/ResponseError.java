package quarkus.social.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.Response;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
public class ResponseError {
    public static final Integer UNPROCESSABLE_ENTITY = 422;
    private String message;
    private Collection<FieldError> errors;

    public ResponseError(String message, List<FieldError> errors) {
        this.message = message;
        this.errors = errors;
    }

    public static <T> ResponseError createFromValidation(Set<ConstraintViolation<T>> violations) {
        List<FieldError> errors = violations
                .stream()
                .map(cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        return new ResponseError("Validation error", errors);
    }

    public Response withStatusCode(int status) {
        return Response.status(status).entity(this).build();
    }
}
