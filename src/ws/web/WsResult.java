package ws.web;

/**
 * @author: jkyang
 * Date: 12-4-16
 */
public class WsResult {

    public WsResult() {
    }

    public WsResult(ResultStatus status) {
        this.status = status;
    }

    public WsResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    private ResultStatus status;
    
    private String message ;

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum ResultStatus {
        SUCCESS,FAIL
    }
}
