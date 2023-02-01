package chapter7.java.v1;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Request {

    private final String body;
    private final Map<String, List<String>> queryParams;

    public Request(String body) {
        this(Map.of(), body);
    }

    public Request(Map<String, List<String>> queryParams) {
        this(queryParams, "");
    }

    public Request(Map<String, List<String>> queryParams, String body) {
        this.queryParams = queryParams;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (!Objects.equals(body, request.body)) return false;
        return Objects.equals(queryParams, request.queryParams);
    }

    @Override
    public int hashCode() {
        int result = body != null ? body.hashCode() : 0;
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        return result;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }
}
