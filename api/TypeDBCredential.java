package com.vaticle.typedb.client.api;

import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.Nullable;
import java.nio.file.Path;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_ROOT_CA_SUPPLIED_WHEN_TLS_DISABLED;

public class TypeDBCredential {
    private final boolean tlsEnabled;
    @Nullable
    private final Path tlsRootCA;

    public TypeDBCredential(boolean tlsEnabled) {
        this(tlsEnabled, null);
    }

    public TypeDBCredential(boolean tlsEnabled, @Nullable Path tlsRootCA) {
        if (!tlsEnabled && tlsRootCA != null) throw new TypeDBClientException(CLUSTER_ROOT_CA_SUPPLIED_WHEN_TLS_DISABLED);
        this.tlsEnabled = tlsEnabled;
        this.tlsRootCA = tlsRootCA;
    }

    public boolean tlsEnabled() {
        return tlsEnabled;
    }

    @Nullable
    public Path tlsRootCA() {
        return tlsRootCA;
    }
}
