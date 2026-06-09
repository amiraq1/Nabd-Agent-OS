package com.nabd.ai.local.embedding

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.TensorInfo
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.LongBuffer
import java.nio.FloatBuffer

class OnnxEmbeddingProvider(
    private val context: Context,
    private val modelPath: String
) : EmbeddingProvider {

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    
    override val dimensions: Int = 384 // all-MiniLM-L6-v2 dimensions, configurable ideally

    init {
        initModel()
    }

    private fun initModel() {
        ortEnvironment = OrtEnvironment.getEnvironment()
        val options = OrtSession.SessionOptions()
        
        // Load model from assets or file system. Assuming file system path for now.
        ortSession = ortEnvironment?.createSession(modelPath, options)
    }

    override suspend fun embed(text: String): FloatArray = withContext(Dispatchers.Default) {
        val env = ortEnvironment ?: throw IllegalStateException("Environment not initialized")
        val session = ortSession ?: throw IllegalStateException("Session not initialized")

        // VERY simplified tokenization for demonstration.
        // A production ONNX integration needs a real WordPiece or BPE tokenizer (like HF tokenizers).
        // Since we cannot easily pull in a full tokenizer library without more complex JNI,
        // we'll assume the input is pre-tokenized or we use a basic fallback.
        // NOTE: For a real production ready system, we'd use a local tokenizer.
        // Here we simulate the tensor inputs needed by all-MiniLM-L6-v2
        
        val tokens = tokenize(text)
        val inputIds = tokens.map { it.toLong() }.toLongArray()
        val attentionMask = LongArray(tokens.size) { 1L }
        val tokenTypeIds = LongArray(tokens.size) { 0L }

        val shape = longArrayOf(1, tokens.size.toLong())

        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape)
        val attentionTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)
        val typeTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape)

        val inputs = mapOf(
            "input_ids" to inputTensor,
            "attention_mask" to attentionTensor,
            "token_type_ids" to typeTensor
        )

        session.run(inputs).use { results ->
            // Extract the last hidden state and apply mean pooling
            val outputTensor = results[0] as OnnxTensor
            val floatArray = outputTensor.floatBuffer.array()
            
            // Simplified pooling (just returning first token's embeddings, normally mean pooling over attention mask)
            val result = FloatArray(dimensions)
            System.arraycopy(floatArray, 0, result, 0, dimensions)
            
            inputTensor.close()
            attentionTensor.close()
            typeTensor.close()
            
            result
        }
    }

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> {
        return texts.map { embed(it) } // Sequential for now, can be batched at tensor level
    }

    // Dummy tokenizer for structural completion. Real integration requires huggingface/tokenizers.
    private fun tokenize(text: String): List<Int> {
        return text.split(" ").map { it.hashCode() % 30000 }
    }
}
