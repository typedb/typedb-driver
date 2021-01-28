load("@graknlabs_common//test/server:rules.bzl", "grakn_java_test")

def grakn_core_and_cluster_behaviour_test(
        name,
        connection_steps_core,
        connection_steps_cluster,
        steps,
        native_grakn_artifact_core,
        native_grakn_artifact_cluster,
        runtime_deps = [],
        **kwargs):
    grakn_java_test(
        name = name + "-core",
        native_grakn_artifact = native_grakn_artifact_core,
        runtime_deps = runtime_deps + [connection_steps_core] + steps,
        **kwargs,
    )

    grakn_java_test(
        name = name + "-cluster",
        native_grakn_artifact = native_grakn_artifact_cluster,
        runtime_deps = runtime_deps + [connection_steps_cluster] + steps,
        **kwargs,
    )
