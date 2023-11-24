package quarkus.social.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<MyException> {
    @Override
    public Response toResponse(MyException myException) {
        return Response.status(myException.getStatus())
                .entity(myException.getObjMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
