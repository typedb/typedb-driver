cmake_minimum_required(VERSION 3.20)
if(NOT DEFINED TYPEDB_ASSEMBLY)
    message(FATAL_ERROR "TYPEDB_ASSEMBLY is not set")
ENDIF()

project (TestAssembly)

set(CMAKE_CXX_STANDARD 17)
link_directories(${TYPEDB_ASSEMBLY}/lib)
include_directories(${TYPEDB_ASSEMBLY}/include)

add_executable(test_assembly test.cpp)
IF (WIN32)
    target_link_libraries(test_assembly typedb-driver-cpp.if.lib)
ELSE()
    target_link_libraries(test_assembly typedb-driver-cpp)
ENDIF()
