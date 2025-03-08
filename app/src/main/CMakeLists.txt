cmake_minimum_required(VERSION 3.31.6)

project(bangcalendar)

add_library( bangcalendar
             SHARED
             cpp/security.cpp cpp/eaes.cpp cpp/base64.cpp)

find_library( log-lib
              log )

add_library( openssl1
             STATIC
             IMPORTED)
set_target_properties( openssl1
                       PROPERTIES IMPORTED_LOCATION
                       ${PROJECT_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libcrypto.so )

add_library( openssl2
             STATIC
             IMPORTED)
set_target_properties( openssl2
                       PROPERTIES IMPORTED_LOCATION
                       ${PROJECT_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libssl.so )

include_directories(cpp)

target_link_libraries( bangcalendar
                       ${log-lib} openssl1 openssl2 )

