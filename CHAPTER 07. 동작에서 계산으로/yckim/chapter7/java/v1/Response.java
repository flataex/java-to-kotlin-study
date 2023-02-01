package chapter7.java.v1;

import java.util.Objects;
import java.util.StringJoiner;

public class Response {

    private final int status;

    private final String body;

    public Response(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public Response(int status) {
        this(status, "");
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Response.class.getSimpleName() + "[", "]")
                .add("status=" + status)
                .add("body='" + body + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return status == response.status && Objects.equals(body, response.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, body);
    }
}
