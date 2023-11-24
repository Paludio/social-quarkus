package quarkus.social.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MyException extends RuntimeException{
    @Getter
    private final Response.Status status;
    private final String message;

    public MyException(String message, Response.Status status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    public Map<String, String> getObjMessage() {
        HashMap<String, String> objMessage = new HashMap<>();
        objMessage.put("message", this.message);

        return objMessage;
    }
}
