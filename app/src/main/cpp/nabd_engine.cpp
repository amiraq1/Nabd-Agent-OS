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
Java_com_nabd_ai_local_engine_LlamaEngine_nativeStopGeneration(JNIEnv* env, jobject thiz, jlong handle) {
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

JNIEXPORT jstring JNICALL
Java_com_nabd_ai_local_engine_NabdEngine_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Nabd Native Engine Active";
    return env->NewStringUTF(hello.c_str());
}

} // extern "C"
