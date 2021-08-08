package com.crypto.async

import android.os.Process
import java.util.concurrent.*

/**
 * 默认的线程池
 *
 * @author guilicheng
 * @date 2021-08-08
 */
internal object DefaultExecutor {
    // region const

    /**
     * 保持活动时间 - 3 seconds
     */
    const val KEEP_ALIVE_TIME = 3L

    // endregion

    // region fields

    val defaultExecutorService: ExecutorService by lazy { this.createPool() }

    // endregion

    // region interface

    fun defaultParallelism() = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)

    // endregion

    // region helper

    private inline fun <T> Try(block: () -> T) = try {
        block()
    } catch (e: Throwable) {
        null
    }

    private fun createPool(): ExecutorService {
        if (System.getSecurityManager() != null) return createPlainPool()
        val forkJoinPoolClass = Try { Class.forName("java.util.concurrent.ForkJoinPool") }
                ?: return this.createPlainPool()

        Try { forkJoinPoolClass.getMethod("commonPool").invoke(null) as? ExecutorService }?.let { return it }

        Try { forkJoinPoolClass.getConstructor(Int::class.java).newInstance(this.defaultParallelism()) as? ExecutorService }?.let { return it }

        return this.createPlainPool()
    }

    private fun createPlainPool(): ExecutorService {
        return ThreadPoolExecutor(
                defaultParallelism(),
                Integer.MAX_VALUE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                PriorityBlockingQueue<Runnable>(),
                PriorityThreadFactory("default_thread_pool", Process.THREAD_PRIORITY_BACKGROUND)
        )
    }

    // endregion
}