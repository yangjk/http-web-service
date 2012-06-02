package ws.web;

/**
 * @author: jkyang
 */
public class ErrorInfo extends WsResult {

    public ErrorInfo() {
        super.setStatus(ResultStatus.FAIL);
    }

    private String exception;

    private String stackTrace;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }


}