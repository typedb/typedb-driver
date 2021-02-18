package grakn.client.rpc;

import io.grpc.stub.StreamObserver;

public class SynchronizedStreamObserver<T> {
    private final StreamObserver<T> s;

    SynchronizedStreamObserver(StreamObserver<T> stream) {
        s = stream;
    }

    synchronized void onNext(T value) {
        s.onNext(value);
    }

    synchronized void onCompleted() {
        s.onCompleted();
    }
}
