cmake_minimum_required(VERSION 3.15)
if(NOT DEFINED TYPEDB_ASSEMBLY)
    message(FATAL_ERROR "TYPEDB_ASSEMBLY is not set")
ENDIF()

project (TestAssembly C)

link_directories(${TYPEDB_ASSEMBLY}/lib)
include_directories(${TYPEDB_ASSEMBLY}/include)

add_executable(test_assembly test.c)
IF (WIN32)
    target_link_libraries(test_assembly typedb-driver-clib.dll.lib)
ELSE()
    target_link_libraries(test_assembly typedb_driver_clib)
ENDIF()
