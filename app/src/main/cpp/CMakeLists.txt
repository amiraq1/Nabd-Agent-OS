cmake_minimum_required(VERSION 3.22.1)

project("nabd_engine")

# Enable C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization flags for Android ARM64
if(CMAKE_SYSTEM_PROCESSOR STREQUAL "aarch64")
    add_compile_options(-O3 -ffast-math -fno-exceptions -fno-rtti)
endif()

# llama.cpp options
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "llama: build examples" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "llama: build tests" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "llama: build server" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "build shared libraries" FORCE)

# Add llama.cpp as a subdirectory
add_subdirectory(third_party/llama.cpp)

# Nabd Engine Sources
set(NABD_SOURCES
    runtime/LlamaRuntimeContext.cpp
    nabd_engine.cpp
)

add_library(
    nabd_engine
    SHARED
    ${NABD_SOURCES}
)

find_library(
    log-lib
    log
)

# Link with llama (the target defined in third_party/llama.cpp)
# Note: Modern llama.cpp might have targets like 'llama' or 'llama-core'
# Based on the structure, it defines 'llama' library.
target_link_libraries(
    nabd_engine
    llama
    ${log-lib}
)

# Include directories for our own runtime
target_include_directories(
    nabd_engine
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    third_party/llama.cpp/include
)

# تفعيل الاستثناءات لمكتبتنا فقط لتجاوز إعدادات llama.cpp الصارمة
target_compile_options(nabd_engine PRIVATE -fexceptions)

