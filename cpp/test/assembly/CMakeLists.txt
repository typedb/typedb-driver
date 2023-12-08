cmake_minimum_required(VERSION 3.20)

project (TestAssembly)

set(CMAKE_CXX_STANDARD 17)
link_directories(typedb-driver-cpp-mac-arm64/lib)
include_directories(typedb-driver-cpp-mac-arm64/include)

add_executable(test_assembly test.cpp)
target_link_libraries(test_assembly -ltypedb-driver-cpp)
