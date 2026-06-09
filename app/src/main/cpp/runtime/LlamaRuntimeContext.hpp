#pragma once

#include <string>
#include <vector>
#include <atomic>
#include <mutex>
#include <jni.h>
#include "../third_party/llama.cpp/include/llama.h"

/**
 * LlamaRuntimeContext: Manages the native lifecycle of the llama.cpp engine.
 * Encapsulates model data, generation state, and thread safety.
 */
class LlamaRuntimeContext {
public:
    LlamaRuntimeContext();
    ~LlamaRuntimeContext();

    bool loadModel(const std::string& path);
    void generate(JNIEnv* env, const std::string& prompt, const std::string& grammar, jobject callback);
    void stopGeneration();
    void unloadModel();

private:
    std::mutex engine_mutex;
    
    llama_model* model = nullptr;
    llama_context* ctx = nullptr;
    llama_batch batch;

    std::atomic<bool> is_loaded;
    std::atomic<bool> is_generating;
    std::atomic<bool> stop_requested;

    void release_resources();
};
