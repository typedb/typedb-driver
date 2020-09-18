package grakn.client.rpc.response;

public interface ResponseCollector {
    /**
     * Callback function called when a response is received. The return value determines whether this ResponseCollector
     * is done and should be cleaned up.
     * @param response The response.
     * @return 'true' if this ResponseCollector is done receiving responses, otherwise, 'false'.
     */
    boolean onResponse(Response response);
}
