java_library(
    name = "cucumber-core",
    exports = [
        "//external:jar/io/cucumber/cucumber-core"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apiguardian:apiguardian-api",
        ":cucumber-expressions",
        ":datatable",
        ":gherkin",
        ":tag-expressions"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "cucumber-expressions",
    exports = [
        "//external:jar/io/cucumber/cucumber-expressions"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apiguardian:apiguardian-api",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-core",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "cucumber-java",
    exports = [
        "//external:jar/io/cucumber/cucumber-java"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apiguardian:apiguardian-api",
        ":cucumber-core"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "cucumber-junit",
    exports = [
        "//external:jar/io/cucumber/cucumber-junit"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/org/apiguardian:apiguardian-api",
        ":cucumber-core"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "datatable",
    exports = [
        "//external:jar/io/cucumber/datatable"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apiguardian:apiguardian-api",
        ":datatable-dependencies"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "datatable-dependencies",
    exports = [
        "//external:jar/io/cucumber/datatable-dependencies"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "gherkin",
    exports = [
        "//external:jar/io/cucumber/gherkin"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "tag-expressions",
    exports = [
        "//external:jar/io/cucumber/tag-expressions"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


