java_library(
    name = "tracing-client-java",
    srcs = glob(["*.java"]),
    deps = [
        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",
        "@graknlabs_protocol//grpc/java:protocol",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_grabl_tracing//util",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/com/google/code/findbugs:jsr305",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/io/grpc:grpc-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-stub",
        "//dependencies/maven/artifacts/io/grpc:grpc-api",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api", # TODO: Do we still need this?

        "//:client-java",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-netty",
        "//dependencies/maven/artifacts/io/netty:netty-all",
    ],
    visibility = ["//visibility:public"],
)