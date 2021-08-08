package com.crypto.base

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * base activity define
 *
 * **此类无封装通用业务逻辑，业务层不建议直接使用**
 *
 * @author guilicheng
 * @date 2021-08-08
 */
abstract class BaseActivity : AppCompatActivity() {
    // region companion

    companion object {
        private const val TAG = "BaseActivity"
    }

    // endregion

    // region fields


    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val transactionDelegate: FragmentTransactionDelegate by lazy { FragmentTransactionDelegate(this.supportFragmentManager) }

    // endregion

    // region lift cycle

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        this.fixAndroidOBug()
        super.onCreate(savedInstanceState)
    }

    @CallSuper
    override fun onPause() {
        this.handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    // endregion

    // region interface

    // region fragment

    /**
     * 装载一个根[fragment]，[containerId]用于指定承载[fragment]的容器
     */
    fun loadRoot(@IdRes containerId: Int, fragment: Fragment) {
        this.transactionDelegate.loadRoot(containerId, fragment)
    }

    /**
     * show 一个 fragment, hide 指定或其他同栈所有 fragment
     */
    fun showHideFragment(showFragment: Fragment, hideFragment: Fragment? = null) {
        this.transactionDelegate.showHideFragment(showFragment, hideFragment)
    }

    /**
     * add 一个 fragment 到指定的容器里面[containerId]，可以指定动画效果
     */
    fun addFragment(@IdRes containerId: Int, fragment: Fragment, enter: Int = 0, exit: Int = 0, popEnter: Int = 0, popExit: Int = 0) {
        this.transactionDelegate.addFragment(containerId, fragment, enter, exit, popEnter, popExit)
    }

    /**
     * pop 最顶层的 fragment
     */
    fun popFragment() {
        this.transactionDelegate.popFragment()
    }

    // endregion

    // region post

    protected fun post(action: Runnable) {
        this.handler.post(action)
    }

    protected fun postDelayed(action: Runnable, delayMillis: Long) {
        this.handler.postDelayed(action, delayMillis)
    }

    protected fun removeCallbacks(action: Runnable) {
        this.handler.removeCallbacks(action)
    }

    // endregion

    // endregion

    // region fix Android O bug

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && this.isTranslucentOrFloating()) {
            return
        }

        super.setRequestedOrientation(requestedOrientation)
    }

    private fun fixAndroidOBug() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            this.fixOrientation()
        }
    }

    private fun isTranslucentOrFloating(): Boolean {
        var isTranslucentOrFloating = false
        try {
            val styleableRes = Class.forName("com.android.internal.R\$styleable").getField("Window").get(null) as IntArray
            val ta = this.obtainStyledAttributes(styleableRes)
            val method = ActivityInfo::class.java.getMethod("isTranslucentOrFloating", TypedArray::class.java)
            method.isAccessible = true
            isTranslucentOrFloating = method.invoke(null, ta) as Boolean
            method.isAccessible = false
        } catch (e: Exception) {
            Log.w(TAG, "isTranslucentOrFloating -> has exception.", e)
        }

        return isTranslucentOrFloating
    }

    private fun fixOrientation(): Boolean {
        try {
            val field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val o = field.get(this) as ActivityInfo
            o.screenOrientation = -1
            field.isAccessible = false
            return true
        } catch (e: Exception) {
            Log.w(TAG, "fixOrientation -> has exception.", e)
        }

        return false
    }

    // endregion

}