package mars.ring.domain.model.beacontag;

/**
 * Created by developer on 10/01/18.
 */

public class HttpException extends Exception {
    private Integer statusCode;

    public HttpException(Integer statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
