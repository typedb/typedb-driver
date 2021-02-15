package grakn.client.rpc.util;

import grakn.client.common.exception.GraknClientException;
import io.grpc.StatusRuntimeException;

import java.util.function.Supplier;

public class RPCUtils {

    public static <RES> RES rpcCall(Supplier<RES> req) {
        try {
            return req.get();
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }
}
