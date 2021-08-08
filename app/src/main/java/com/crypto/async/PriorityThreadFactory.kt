package com.crypto.async

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 可以自定线程优先级的线程池线程创建工厂
 *
 * @author guilicheng
 * @date 2021-08-08
 */
internal class PriorityThreadFactory(
        /**
         * 线程名
         */
        private val name: String,
        /**
         * 线程优先级
         */
        private val priority: Int
) : ThreadFactory {
    // region fields

    /**
     * 队列序号
     */
    private val number = AtomicInteger()

    // endregion

    // region ThreadFactory

    /**
     * 创建线程
     *
     * @param action 需要执行的逻辑块
     * @return 线程实例
     */
    override fun newThread(action: Runnable): Thread {
        return object : Thread(action, "${this.name}'-'${this.number.getAndIncrement()}") {
            override fun run() {
                android.os.Process.setThreadPriority(priority)
                super.run()
            }
        }.apply { this.priority = this@PriorityThreadFactory.priority }
    }

    // endregion
}