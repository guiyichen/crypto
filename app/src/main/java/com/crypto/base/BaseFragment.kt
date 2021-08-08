package com.crypto.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

/**
 * base fragment define
 *
 * @author guilicheng
 * @date 2021-08-08
 */
abstract class BaseFragment : Fragment() {
    // region fields

    internal var containerId: Int = 0
        private set

    private lateinit var transactionDelegate: FragmentTransactionDelegate

    private val visibleDelegate: VisibleDelegate = VisibleDelegate(this)
    private val handler = Handler(Looper.getMainLooper())

    // endregion

    // region life cycle

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.transactionDelegate = FragmentTransactionDelegate(this.childFragmentManager)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.classLoader = this.javaClass.classLoader
        this.containerId = this.arguments?.getInt(FragmentTransactionDelegate.FRAGMENTATION_ARG_CONTAINER) ?: this.containerId
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        this.visibleDelegate.onResume()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        this.visibleDelegate.onPause()
    }

    // region custom life cycle

    /**
     * 当 fragment 可见时调用
     * 子类复写此函数即可
     */
    open fun onVisible() {
    }

    /**
     * 当 fragment 不可见时调用
     * 子类复写此函数即可
     */
    open fun onInvisible() {
    }

    // endregion

    // region interface

    /**
     * 获取当前fragment所属的activity
     */
    fun <TActivity : BaseActivity> getHostActivity(): TActivity {
        return activity as TActivity
    }

    /**
     * 装载一个根[root]，[containerId]用于指定承载[root]的容器
     */
    fun loadChild(containerId: Int, root: BaseFragment) {
        requireActivity()
        this.transactionDelegate.loadRoot(containerId, root)
    }

    /**
     * 装载一系列[tos]fragments，[containerId]用于指定承载[tos]的容器，[showPosition]用于指定当前显示哪一个，剩余则隐藏
     */
    fun loadChild(containerId: Int, showPosition: Int, vararg tos: BaseFragment) {
        this.transactionDelegate.loadRoot(containerId, showPosition, tos)
    }

    /**
     * show 一个 fragment, hide 指定或其他同栈所有fragment（[hideFragment]为空，隐藏其他所有同栈的）
     */
    fun showHideFragment(showFragment: BaseFragment, hideFragment: BaseFragment? = null) {
        this.transactionDelegate.showHideFragment(showFragment, hideFragment)
    }

    /**
     * add 一个 fragment 到指定的容器里面[containerId]，可以指定动画效果
     */
    fun addChildFragment(@IdRes containerId: Int, fragment: Fragment, enter: Int = 0, exit: Int = 0, popEnter: Int = 0, popExit: Int = 0) {
        this.transactionDelegate.addFragment(containerId, fragment, enter, exit, popEnter, popExit)
    }

    /**
     * pop 最顶层的 child fragment
     */
    fun popChildFragment() {
        this.transactionDelegate.popFragment()
    }

    fun post(action: Runnable) {
        this.handler.post(action)
    }

    fun postDelayed(action: Runnable, delayMillis: Long) {
        this.handler.postDelayed(action, delayMillis)
    }

    fun removeCallbacks(action: Runnable) {
        this.handler.removeCallbacks(action)
    }

    // endregion

    // region VisibleDelegate

    private class VisibleDelegate(private val fragment: BaseFragment) {
        // region interface

        fun onResume() {
            // onResume 并不代表 fragment 可见
            // 如果是在 viewpager 里,就需要判断 getUserVisibleHint,不在 viewpager 时,getUserVisibleHint 默认为true
            // 如果是其它情况,就通过 isHidden 判断,因为 show/hide 时会改变 isHidden 的状态
            // 所以,只有当 fragment 原来是可见状态时,进入 onResume 就回调 onVisible
            if (this.fragment.userVisibleHint && !this.fragment.isHidden) {
                this.fragment.onVisible()
            }
        }

        fun onPause() {
            // onPause 时也需要判断,如果当前 fragment 在 viewpager 中不可见,就已经回调过了,onPause 时也就不需要再次回调 onInvisible 了
            // 所以,只有当 fragment 是可见状态时进入 onPause 才加调 onInvisible
            if (this.fragment.userVisibleHint && !this.fragment.isHidden) {
                this.fragment.onInvisible()
            }
        }
        // endregion
    }
    // endregion
}