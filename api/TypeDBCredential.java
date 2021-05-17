package com.vaticle.typedb.client.api;

import javax.annotation.Nullable;
import java.nio.file.Path;

public class TypeDBCredential {
    private final boolean tlsEnabled;
    @Nullable
    private final Path tlsRootCA;

    private TypeDBCredential(boolean tlsEnabled, @Nullable Path tlsRootCA) {
        if (!tlsEnabled && tlsRootCA != null) assert false;

        this.tlsEnabled = tlsEnabled;
        this.tlsRootCA = tlsRootCA;
    }

    public static TypeDBCredential plainText() {
        return new TypeDBCredential(false, null);
    }

    public static TypeDBCredential tls() {
        return new TypeDBCredential(true, null);
    }

    public static TypeDBCredential tls(Path rootCA) {
        return new TypeDBCredential(true, rootCA);
    }

    public boolean tlsEnabled() {
        return tlsEnabled;
    }

    @Nullable
    public Path tlsRootCA() {
        return tlsRootCA;
    }
}
