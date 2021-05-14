package grakn.client.common.rpc;

import grakn.client.common.exception.GraknClientException;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.nio.file.Path;

public interface ManagedChannelFactory {
    ManagedChannel forAddress(String address);

    class PlainText implements ManagedChannelFactory {
        @Override
        public ManagedChannel forAddress(String address) {
            return NettyChannelBuilder.forTarget(address)
                    .usePlaintext()
                    .build();
        }
    }

    class TLS implements ManagedChannelFactory {
        @Nullable
        private final Path tlsRootCA;

        public TLS() {
            this.tlsRootCA = null;
        }

        public TLS(Path tlsRootCA) {
            this.tlsRootCA = tlsRootCA;
        }

        @Override
        public ManagedChannel forAddress(String address) {
            try {
                SslContext sslContext;
                if (tlsRootCA != null) {
                    sslContext = GrpcSslContexts.forClient().trustManager(tlsRootCA.toFile()).build();
                } else {
                    sslContext = GrpcSslContexts.forClient().build();
                }
                return NettyChannelBuilder.forTarget(address).useTransportSecurity().sslContext(sslContext).build();
            } catch (SSLException e) {
                throw new GraknClientException(e.getMessage(), e);
            }
        }
    }
}
