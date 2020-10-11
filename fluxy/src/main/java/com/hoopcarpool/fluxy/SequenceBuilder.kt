package com.hoopcarpool.fluxy

/**
 * Utility builder to handle sequential flow for on-shot operation
 */
class SequenceBuilder {

    private val decisionBlockList = mutableListOf<DecisionBlock<*>>()
    private var finallyBlock: () -> Unit = {}

    fun <T : Any> next(
        origin: suspend () -> ConcludedResult<T>,
        onSuccess: (ConcludedResult.Success<T>) -> Unit = {},
        onFailure: (ConcludedResult.Failure<T>) -> Unit = {},
        haltOnFailure: Boolean = true
    ): SequenceBuilder {
        decisionBlockList.add(DecisionBlock(origin, onSuccess, onFailure, haltOnFailure))
        return this
    }

    fun onFinally(block: () -> Unit): SequenceBuilder {
        finallyBlock = block
        return this
    }

    suspend fun build() {
        for (decisionBlock in decisionBlockList) {
            if (!decisionBlock.compute() && decisionBlock.haltOnFailure)
                break
        }
        finallyBlock()
    }

    private data class DecisionBlock<T : Any>(
        inline val origin: suspend () -> ConcludedResult<T>,
        inline val onSuccess: ((ConcludedResult.Success<T>) -> Unit)?,
        inline val onFailure: ((ConcludedResult.Failure<T>) -> Unit)?,
        val haltOnFailure: Boolean = true
    ) {

        suspend fun compute(): Boolean {
            return when (val result = origin()) {
                is ConcludedResult.Success -> {
                    onSuccess?.invoke(result)
                    true
                }
                is ConcludedResult.Failure -> {
                    onFailure?.invoke(result)
                    false
                }
            }
        }
    }
}

