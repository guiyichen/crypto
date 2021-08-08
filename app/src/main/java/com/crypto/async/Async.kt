package com.crypto.async

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.*

/**
 * 协程封装，调用同步化
 *
 * ex:
 *
 * private fun awaitNormal() = async {
 *      btnAwaitNormal.isEnabled = false
 *      progressBar.visibility = View.VISIBLE
 *      progressBar.isIndeterminate = true
 *      txtResult.text = "Loading..."
 *      // Release main thread and wait until text is loaded
 *      val loadedText = await(::loadText)
 *      // Loaded successfully, come back in UI thread and show result
 *      txtResult.text = loadedText + " (to be processed)"
 *      // Have to continue processing in background
 *      txtResult.text = await { processText(loadedText) }
 *      progressBar.visibility = View.INVISIBLE
 *      btnAwaitNormal.isEnabled = true
 * }
 *
 * private fun awaitWithProgress() = async {
 *      btnAwaitWithProgress.isEnabled = false
 *      progressBar.visibility = View.VISIBLE
 *      progressBar.isIndeterminate = false
 *      txtResult.text = "Loading..."
 *
 *      txtResult.text = awaitWithProgress(::loadTextWithProgress) {
 *          progressBar.progress = it
 *          progressBar.max = 100
 *      }
 *
 *      progressBar.visibility = View.INVISIBLE
 *      btnAwaitWithProgress.isEnabled = true
 * }
 *
 * @Suppress("UNREACHABLE_CODE")
 * private fun throwException() = async {
 *      btnThrowException.isEnabled = false
 *      progressBar.visibility = View.VISIBLE
 *      progressBar.isIndeterminate = true
 *      txtResult.text = "Loading..."
 *      await {
 *          throw RuntimeException("Test exception")
 *      }
 *      txtResult.text = "Should never be displayed"
 *      progressBar.visibility = View.INVISIBLE
 *      btnThrowException.isEnabled = true
 * }
 *
 * @Suppress("UNREACHABLE_CODE")
 * private fun tryCatchException() = async {
 *      btnTryCatchException.isEnabled = false
 *      progressBar.visibility = View.VISIBLE
 *      progressBar.isIndeterminate = true
 *      txtResult.text = "Loading..."
 *      try {
 *          await {
 *              throw RuntimeException("Test exception")
 *          }
 *          txtResult.text = "Should never be displayed"
 *      }
 *      catch (e: Exception) {
 *          // Exception always handled in UI thread
 *          txtResult.text = e.message
 *          btnTryCatchException.text = "Handled. see the log"
 *          Log.e(TAG, "Couldn't update text", e)
 *      }
 *      progressBar.visibility = View.INVISIBLE
 *      btnTryCatchException.isEnabled = true
 * }
 *
 * @Suppress("UNREACHABLE_CODE")
 * private fun handleExceptionInOnError() = async {
 *      btnHandleExceptionInOnError.isEnabled = false
 *      progressBar.visibility = View.VISIBLE
 *      progressBar.isIndeterminate = true
 *      txtResult.text = "Loading..."
 *
 *      await {
 *          throw RuntimeException("Test exception")
 *      }
 *
 *      txtResult.text = "Should never be displayed"
 *      progressBar.visibility = View.INVISIBLE
 *      btnHandleExceptionInOnError.isEnabled = true
 * }.onError {
 *      // Exception always handled in UI thread
 *      txtResult.text = it.message
 *      btnHandleExceptionInOnError.text = "Handled. see the log"
 *      progressBar.visibility = View.INVISIBLE
 *      btnHandleExceptionInOnError.isEnabled = true
 *      Log.e(TAG, "Couldn't update text", it)
 * }.finally {
 *      can do something，if be
 * }
 *
 */

// -----------------------------------------------我是分割线---------------------------------------------------------------------------

// region type aliases（类型别名定义）

typealias ErrorHandler = (Exception) -> Unit

typealias ProgressHandler<P> = (P) -> Unit

// endregion

// region valuable

private val coroutines = WeakHashMap<Any, ArrayList<WeakReference<AsyncController>>>()

// endregion

// region extend property

/**
 * 获取当前 target 的协程集合（用于取消）
 *
 * ex:
 * class MainActivity: Activity {
 *      override fun onDestroy() {
 *          super.onDestroy()
 *          async.cancelAll()
 *      }
 * }
 *
 */
val Any.async: Async
    get() = Async(this)

// endregion

// region extend function

/**
 * 是否是主线程
 */
fun isMainThread(): Boolean {
    return Thread.currentThread() == Looper.getMainLooper().thread
}

/**
 * Run asynchronous computations based on [c] coroutine parameter.
 * Must be used in UI thread.
 *
 * Execution starts immediately within the 'async' call and it runs until
 * the first suspension point is reached ('await' call).
 * Remaining part of coroutine will be executed after awaited task is completed
 * and result is delivered into UI thread.
 *
 * @param c a coroutine representing asynchronous computations
 *
 * @return AsyncController object allowing to define optional [AsyncController.onError]
 * or [AsyncController.finally] handlers
 */
fun Any.async(
        executorService: ExecutorService = DefaultExecutor.defaultExecutorService,
        c: suspend AsyncController.() -> Unit
): AsyncController {
    val controller = AsyncController(this, executorService)
    keepCoroutineForCancelPurpose(this, controller)
    return async(c, controller)
}

/**
 * Run asynchronous computations based on [c] coroutine parameter.
 * Prevents suspended coroutine to be resumed when [Activity] is in finishing state.
 * Must be used in UI thread.
 *
 * Execution starts immediately within the 'async' call and it runs until
 * the first suspension point is reached ('await' call).
 * Remaining part of coroutine will be executed after awaited task is completed
 * and [Activity] is not in finishing state. Result is delivered into UI thread.
 *
 * @param c a coroutine representing asynchronous computations
 *
 * @return AsyncController object allowing to define optional `onError` handler
 */
fun Activity.async(
        executorService: ExecutorService = DefaultExecutor.defaultExecutorService,
        c: suspend AsyncController.() -> Unit
): AsyncController {
    val controller = AsyncController(this, executorService)
    keepCoroutineForCancelPurpose(this, controller)
    return async(c, controller)
}

/**
 * Run asynchronous computations based on [c] coroutine parameter.
 * Prevents suspended coroutine to be resumed when [Fragment] is in invalid state.
 * Must be used in UI thread.
 *
 * Execution starts immediately within the 'async' call and it runs until
 * the first suspension point is reached ('await' call).
 * Remaining part of coroutine will be executed after awaited task is completed
 * and [Fragment] has parental [Activity] instance and it's in attached state.
 * Result is delivered into UI thread.
 *
 * @param c a coroutine representing asynchronous computations
 *
 * @return AsyncController object allowing to define optional `onError` handler
 */
fun Fragment.async(
        executorService: ExecutorService = DefaultExecutor.defaultExecutorService,
        c: suspend AsyncController.() -> Unit
): AsyncController {
    val controller = AsyncController(this, executorService)
    keepCoroutineForCancelPurpose(this, controller)
    return async(c, controller)
}

// endregion

// region helper function

private fun keepCoroutineForCancelPurpose(any: Any, controller: AsyncController) {
    val list = coroutines.getOrElse(any) {
        val newList = ArrayList<WeakReference<AsyncController>>()
        coroutines[any] = newList
        newList
    }

    list.add(WeakReference(controller))
}

private fun async(
        c: suspend AsyncController.() -> Unit,
        controller: AsyncController
): AsyncController {
    c.startCoroutine(controller, completion = object : Continuation<Unit> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            result.exceptionOrNull()?.let { throw it }
        }
    })
    return controller
}

// endregion

// region class define

// region Async

class Async(private val asyncTarget: Any) {
    /**
     * 取消当前 target 下的所有协程
     */
    fun cancelAll() {
        coroutines[asyncTarget]?.forEach {
            it.get()?.cancel()
        }
    }
}

// endregion

// region AsyncController

/**
 * Controls coroutine execution and thread scheduling
 */
class AsyncController(private val target: Any, private val executorService: ExecutorService) {
    // region fields

    internal var currentTask: CancelableTask<*>? = null

    private var errorHandler: ErrorHandler? = null
    private var finallyHandler: (() -> Unit)? = null

    private lateinit var uiThreadStackTrace: Array<out StackTraceElement>

    private val uiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (isAlive()) {
                @Suppress("UNCHECKED_CAST") (msg.obj as () -> Unit)()
            }
        }
    }

    // endregion

    // region suspend interface

    /**
     * Non-blocking suspension point. Coroutine execution will proceed after [f] is finished
     * in background thread.
     *
     * @param f a function to call in background thread. The result of [f] will be delivered
     * into UI thread.
     *
     * @return the result of [f] delivered in UI thread after computation is done
     * in background thread
     */
    suspend fun <V> await(f: () -> V): V {
        keepAwaitCallerStackTrace()
        return suspendCoroutine {
            currentTask = AwaitTask(f, this, it)
            executorService.submit(currentTask)
        }
    }

    /**
     * Non-blocking suspension point. Similar to [await] but its function [f] has functional parameter
     * which can be called right in background thread in order to send progress value into
     * [onProgress] progress handler.
     *
     * @param f a function to call in background thread. Its functional parameter
     * `ProgressHandler<P>` can be called in background thread in order to send progress result.
     *
     * @param onProgress a function to handle progress result. Called in UI thread.
     *
     * @return the result of [f] delivered in UI thread after computation is done
     * in background thread
     */
    suspend fun <V, P> awaitWithProgress(
            f: (ProgressHandler<P>) -> V,
            onProgress: ProgressHandler<P>
    ): V {
        keepAwaitCallerStackTrace()
        return suspendCoroutine {
            currentTask = AwaitWithProgressTask(f, onProgress, this, it)
            executorService.submit(currentTask)
        }
    }

    // endregion

    // region interface

    /**
     * Optional error handler. Exceptions happening in a background thread and not caught within
     * try/catch in coroutine body will be delivered to this handler in UI thread.
     *
     *  @return this AsyncController object allowing to define optional [finally] handlers
     */
    fun onError(errorHandler: ErrorHandler): AsyncController {
        this.errorHandler = errorHandler
        return this
    }

    /**
     * Optional handler to be invoked after successful coroutine execution
     * or after handling exception in [onError].
     */
    fun finally(finallyHandler: () -> Unit) {
        this.finallyHandler = finallyHandler
    }

    // endregion

    // region helper

    fun cancel() {
        currentTask?.cancel()
    }

    internal fun <V> handleException(originalException: Exception, continuation: Continuation<V>) {
        runOnUi {
            currentTask = null

            try {
                continuation.resumeWithException(originalException)
            } catch (e: Exception) {
                val asyncException = AsyncException(e, refineUiThreadStackTrace())
                errorHandler?.invoke(asyncException) ?: throw asyncException
            }

            applyFinallyBlock()
        }
    }

    internal fun applyFinallyBlock() {
        if (isLastCoroutineResumeExecuted()) {
            finallyHandler?.invoke()
        }
    }

    internal fun runOnUi(block: () -> Unit) {
        uiHandler.obtainMessage(0, block).sendToTarget()
    }

    private fun isLastCoroutineResumeExecuted() = currentTask == null

    private fun isAlive(): Boolean {
        return when (target) {
            is Activity -> return !target.isFinishing
            is Fragment -> return target.activity != null && !target.isDetached
            else -> true
        }
    }

    private fun keepAwaitCallerStackTrace() {
        uiThreadStackTrace = Thread.currentThread().stackTrace
    }

    private fun refineUiThreadStackTrace(): Array<out StackTraceElement> {
        return uiThreadStackTrace.dropWhile { it.methodName != "keepAwaitCallerStackTrace" }.drop(2)
                .toTypedArray()
    }

    // endregion
}

// endregion

// region AsyncException

/**
 * 异步异常定义
 */
class AsyncException(e: Exception, stackTrace: Array<out StackTraceElement>) : RuntimeException(e) {
    init {
        this.stackTrace = stackTrace
    }
}

// endregion

// region CancelableTask

/**
 * 可以被取消的任务抽象定义
 */
internal abstract class CancelableTask<V>(@Volatile var asyncController: AsyncController?, @Volatile var continuation: Continuation<V>?) :
        Runnable {
    // region fields

    private val isCancelled = AtomicBoolean(false)

    // endregion

    // region abstract

    abstract fun obtainValue(): V

    // endregion

    // region interface

    /**
     * 取消
     */
    open fun cancel() {
        isCancelled.set(true)
        asyncController = null
        continuation = null
    }

    // endregion

    // region Runnable

    override fun run() {
        // Finish task immediately if it was cancelled while being in queue
        if (isCancelled.get()) return

        try {
            val value = obtainValue()
            if (isCancelled.get()) return
            asyncController?.apply {
                runOnUi {
                    currentTask = null
                    continuation?.resume(value)
                    applyFinallyBlock()
                }
            }
        } catch (e: Exception) {
            if (isCancelled.get()) return

            continuation?.apply {
                asyncController?.handleException(e, this)
            }
        }
    }

    // endregion
}

// endregion

// region AwaitTask

private class AwaitTask<V>(
        val f: () -> V,
        asyncController: AsyncController,
        continuation: Continuation<V>
) : CancelableTask<V>(asyncController, continuation) {
    override fun obtainValue(): V {
        return f()
    }
}

// endregion

// region AwaitWithProgressTask

private class AwaitWithProgressTask<P, V>(
        val f: (ProgressHandler<P>) -> V, @Volatile var onProgress: ProgressHandler<P>?,
        asyncController: AsyncController,
        continuation: Continuation<V>
) : CancelableTask<V>(asyncController, continuation) {
    override fun obtainValue(): V {
        return f { progressValue ->
            onProgress?.apply {
                asyncController?.runOnUi { this(progressValue) }
            }
        }
    }

    override fun cancel() {
        super.cancel()
        onProgress = null
    }
}

// endregion

// endregion