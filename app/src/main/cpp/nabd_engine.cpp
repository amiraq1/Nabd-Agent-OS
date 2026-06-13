#include <jni.h>
#include <string>
#include "runtime/LlamaRuntimeContext.hpp"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeInit(JNIEnv* env, jobject thiz) {
    auto* context = new LlamaRuntimeContext();
    return reinterpret_cast<jlong>(context);
}

JNIEXPORT jboolean JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeLoadModel(JNIEnv* env, jobject thiz, jlong handle, jstring path) {
    auto* context = reinterpret_cast<LlamaRuntimeContext*>(handle);
    if (!context) return JNI_FALSE;

    const char* nativePath = env->GetStringUTFChars(path, nullptr);
    bool success = context->loadModel(std::string(nativePath));
    env->ReleaseStringUTFChars(path, nativePath);

    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeGenerate(JNIEnv* env, jobject thiz, jlong handle, jstring prompt, jstring grammar, jobject callback) {
    try {
        auto* context = reinterpret_cast<LlamaRuntimeContext*>(handle);
        if (!context) {
            jclass Exception = env->FindClass("java/lang/IllegalStateException");
            env->ThrowNew(Exception, "Native context is null");
            return;
        }

        const char* nativePrompt = env->GetStringUTFChars(prompt, nullptr);
        const char* nativeGrammar = env->GetStringUTFChars(grammar, nullptr);
        
        context->generate(env, std::string(nativePrompt), std::string(nativeGrammar), callback);
        
        env->ReleaseStringUTFChars(prompt, nativePrompt);
        env->ReleaseStringUTFChars(grammar, nativeGrammar);
    } catch (const std::exception& e) {
        jclass Exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(Exception, e.what());
    } catch (...) {
        jclass Exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(Exception, "Unknown native error during generation");
    }
}

JNIEXPORT void JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeAbortGeneration(JNIEnv* env, jobject thiz, jlong handle) {
    auto* context = reinterpret_cast<LlamaRuntimeContext*>(handle);
    if (context) {
        context->stopGeneration();
    }
}

JNIEXPORT void JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeUnloadModel(JNIEnv* env, jobject thiz, jlong handle) {
    auto* context = reinterpret_cast<LlamaRuntimeContext*>(handle);
    if (context) {
        context->unloadModel();
    }
}

JNIEXPORT void JNICALL
Java_com_nabd_ai_local_engine_LlamaEngine_nativeRelease(JNIEnv* env, jobject thiz, jlong handle) {
    auto* context = reinterpret_cast<LlamaRuntimeContext*>(handle);
    delete context;
}

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/nabd/ai/local/engine/LlamaEngine");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    static const JNINativeMethod methods[] = {
        {"nativeInit", "()J", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeInit},
        {"nativeLoadModel", "(JLjava/lang/String;)Z", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeLoadModel},
        {"nativeGenerate", "(JLjava/lang/String;Ljava/lang/String;Lcom/nabd/ai/local/engine/LlamaEngine$TokenCallback;)V", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeGenerate},
        {"nativeAbortGeneration", "(J)V", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeAbortGeneration},
        {"nativeUnloadModel", "(J)V", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeUnloadModel},
        {"nativeRelease", "(J)V", (void*)Java_com_nabd_ai_local_engine_LlamaEngine_nativeRelease}
    };

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

} // extern "C"
