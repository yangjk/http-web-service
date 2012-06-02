package ws.exception;

import com.wiscom.NestedRuntimeException;


public class InvalidWSRequestException extends RuntimeException {

    public InvalidWSRequestException(String msg) {
        super(msg);
    }

    public InvalidWSRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
