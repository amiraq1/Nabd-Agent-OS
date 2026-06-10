#include "LlamaRuntimeContext.hpp"
#include <android/log.h>
#include <thread>

#define TAG "LlamaRuntime"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Helper to add a token to a batch
static void batch_add(llama_batch & batch, llama_token id, llama_pos pos, const std::vector<llama_seq_id> & seq_ids, bool logits) {
    batch.token[batch.n_tokens] = id;
    batch.pos[batch.n_tokens]   = pos;
    batch.n_seq_id[batch.n_tokens] = seq_ids.size();
    for (size_t i = 0; i < seq_ids.size(); ++i) {
        batch.seq_id[batch.n_tokens][i] = seq_ids[i];
    }
    batch.logits[batch.n_tokens] = logits;
    batch.n_tokens++;
}

LlamaRuntimeContext::LlamaRuntimeContext() 
    : is_loaded(false), is_generating(false), stop_requested(false) {
    llama_backend_init();
}

LlamaRuntimeContext::~LlamaRuntimeContext() {
    unloadModel();
    llama_backend_free();
}

bool LlamaRuntimeContext::loadModel(const std::string& path) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if (is_loaded) return false;

    LOGI("Loading model: %s", path.c_str());

    llama_model_params model_params = llama_model_default_params();
    model = llama_model_load_from_file(path.c_str(), model_params);
    if (!model) {
        LOGE("Failed to load model from %s", path.c_str());
        return false;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048; 
    ctx_params.n_threads = std::thread::hardware_concurrency();

    ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        LOGE("Failed to create context for model");
        llama_model_free(model);
        model = nullptr;
        return false;
    }

    batch = llama_batch_init(512, 0, 1);
    is_loaded = true;
    return true;
}

void LlamaRuntimeContext::generate(JNIEnv* env, const std::string& prompt, const std::string& grammar, jobject callback) {
    if (!is_loaded || is_generating) return;

    is_generating = true;
    stop_requested = false;

    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");
    jmethodID onCompletionMethod = env->GetMethodID(callbackClass, "onCompletion", "()V");
    jmethodID onErrorMethod = env->GetMethodID(callbackClass, "onError", "(Ljava/lang/String;)V");

    // Tokenize
    std::vector<llama_token> tokens_list;
    tokens_list.resize(prompt.size() + 2);
    int n_tokens = llama_tokenize(model, prompt.c_str(), prompt.size(), tokens_list.data(), tokens_list.size(), true, true);
    if (n_tokens < 0) {
        env->CallVoidMethod(callback, onErrorMethod, env->NewStringUTF("Tokenization failed"));
        is_generating = false;
        return;
    }
    tokens_list.resize(n_tokens);

    // Prepare batch for prompt
    batch.n_tokens = 0;
    for (int i = 0; i < tokens_list.size(); ++i) {
        batch_add(batch, tokens_list[i], i, { 0 }, i == tokens_list.size() - 1);
    }

    int n_cur = tokens_list.size();

    // Sampler initialization
    struct llama_sampler * smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_greedy());
    
    if (!grammar.empty()) {
        const llama_vocab* vocab = llama_model_get_vocab(model);
        struct llama_sampler* grammar_sampler = llama_sampler_init_grammar(vocab, grammar.c_str(), "root");
        if (grammar_sampler) {
            llama_sampler_chain_add(smpl, grammar_sampler);
        } else {
            LOGE("Failed to parse grammar");
            env->CallVoidMethod(callback, onErrorMethod, env->NewStringUTF("Failed to parse grammar"));
            llama_sampler_free(smpl);
            is_generating = false;
            return;
        }
    }

    try {
        while (n_cur < llama_n_ctx(ctx)) {
            if (stop_requested) break;

            if (llama_decode(ctx, batch)) {
                LOGE("Failed to decode batch");
                env->CallVoidMethod(callback, onErrorMethod, env->NewStringUTF("Decode failed"));
                break;
            }

            // Sample next token
            const llama_token new_token_id = llama_sampler_sample(smpl, ctx, batch.n_tokens - 1);

            if (llama_token_is_eog(model, new_token_id)) {
                break;
            }

            // Convert token to string
            char buf[128];
            int n = llama_token_to_piece(model, new_token_id, buf, sizeof(buf), 0, true);
            if (n > 0) {
                std::string token_str(buf, n);
                jstring jtoken = env->NewStringUTF(token_str.c_str());
                env->CallVoidMethod(callback, onTokenMethod, jtoken);
                env->DeleteLocalRef(jtoken);
            }

            // Prepare next batch (single token)
            batch.n_tokens = 0;
            batch_add(batch, new_token_id, n_cur, { 0 }, true);
            n_cur++;
        }
    } catch (const std::exception& e) {
        LOGE("Generation exception: %s", e.what());
        env->CallVoidMethod(callback, onErrorMethod, env->NewStringUTF(e.what()));
    } catch (...) {
        LOGE("Unknown generation exception");
        env->CallVoidMethod(callback, onErrorMethod, env->NewStringUTF("Unknown generation exception"));
    }

    llama_sampler_free(smpl);
    env->CallVoidMethod(callback, onCompletionMethod);
    is_generating = false;
}

void LlamaRuntimeContext::stopGeneration() {
    stop_requested = true;
}

void LlamaRuntimeContext::unloadModel() {
    std::lock_guard<std::mutex> lock(engine_mutex);
    release_resources();
    is_loaded = false;
    is_generating = false;
}

void LlamaRuntimeContext::release_resources() {
    if (ctx) {
        llama_free(ctx);
        ctx = nullptr;
    }
    if (model) {
        llama_model_free(model);
        model = nullptr;
    }
    if (batch.token) {
        llama_batch_free(batch);
        batch.token = nullptr;
    }
}
