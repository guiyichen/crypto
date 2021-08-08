package com.crypto.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * fragment 操作代理
 *
 * 封装fragment的装载，显示和隐藏，减少业务方的重复代码
 *
 * @author guilicheng
 * @date 2021-08-08
 */
internal class FragmentTransactionDelegate(private val fragmentManager: FragmentManager) {
    // region const

    companion object {
        private const val TAG = "FragmentTransactionDelegate"

        const val FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container"

        fun getBackStackTopFragment(fragmentManager: FragmentManager, containerId: Int): BaseFragment? {
            val count = fragmentManager.backStackEntryCount

            for (i in count - 1 downTo 0) {
                val entry = fragmentManager.getBackStackEntryAt(i)
                val fragment = fragmentManager.findFragmentByTag(entry.name)
                if (fragment is BaseFragment) {
                    if (containerId == 0) {
                        return fragment
                    }

                    if (containerId == fragment.containerId) {
                        return fragment
                    }
                }
            }

            fragmentManager.fragments.forEach { fragment ->
                if (fragment is BaseFragment) {
                    if (containerId == 0) {
                        return fragment
                    }

                    if (containerId == fragment.containerId) {
                        return fragment
                    }
                }
            }

            return null
        }
    }

    // endregion

    // region interface

    @SuppressLint("LongLogTag")
    fun loadRoot(containerId: Int, root: Fragment) {
        if (!this.checkCanDoOP()) {
            Log.i(TAG, "can not do op, the reason can be is fm is detach!")
            return
        }

        this.bindContainerId(containerId, root)
        this.doStart(root)
    }

    @SuppressLint("LongLogTag")
    fun loadRoot(containerId: Int, showPosition: Int, tos: Array<out Fragment>) {
        if (!this.checkCanDoOP()) {
            Log.i(TAG, "can not do op, the reason can be is fm is detach!")
            return
        }

        if (tos.isNotEmpty()) {
            val ft = this.fragmentManager.beginTransaction()

            for (i in tos.indices) {
                val to = tos[i]

                this.bindContainerId(containerId, to)

                ft.add(containerId, to, to.javaClass.name)

                if (i != showPosition) {
                    ft.hide(to)
                }
            }

            ft.commitAllowingStateLoss()
        }
    }

    fun showHideFragment(showFragment: Fragment, hideFragment: Fragment? = null) {
        if (!this.checkCanDoOP()) {
            return
        }

        if (showFragment === hideFragment) {
            return
        }

        val ft = this.fragmentManager.beginTransaction().show(showFragment)

        hideFragment?.let {
            ft.hide(it)
        } ?: run {
            fragmentManager.fragments.forEach { fragment ->
                if (fragment != null && fragment != showFragment) {
                    ft.hide(fragment)
                }
            }
        }

        ft.commitAllowingStateLoss()
    }

    fun addFragment(@IdRes containerId: Int, fragment: Fragment, enter: Int, exit: Int, popEnter: Int, popExit: Int) {
        if (!this.checkCanDoOP()) {
            return
        }

        val tag = fragment.javaClass.name
        val ft = fragmentManager.beginTransaction().apply { this.setCustomAnimations(enter, exit, popEnter, popExit) }
        ft.add(containerId, fragment, tag)
        ft.addToBackStack(tag)
        ft.commitAllowingStateLoss()
    }

    fun popFragment() {
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        }
    }

    // endregion

    // region helper

    @SuppressLint("LongLogTag")
    private fun checkCanDoOP(): Boolean {
        if (this.fragmentManager.isDestroyed) {
            Log.w(TAG, "FragmentManager is destroyed, skip the action!")
            return false
        }

        return true
    }

    private fun getArguments(fragment: Fragment): Bundle {
        var bundle: Bundle? = fragment.arguments
        if (bundle == null) {
            bundle = Bundle()
            fragment.arguments = bundle
        }

        return bundle
    }

    private fun bindContainerId(containerId: Int, to: Fragment) {
        val args = this.getArguments(to)
        args.putInt(FRAGMENTATION_ARG_CONTAINER, containerId)
    }

    private fun doStart(to: Fragment) {
        val ft = fragmentManager.beginTransaction()
        val tag = to.javaClass.name
        val args = this.getArguments(to)

        val from = getBackStackTopFragment(fragmentManager, args.getInt(FRAGMENTATION_ARG_CONTAINER))

        if (from == null) {
            ft.replace(args.getInt(FRAGMENTATION_ARG_CONTAINER), to, tag)
        } else {
            ft.add(from.containerId, to, tag)
            ft.hide(from)
        }

        ft.commitAllowingStateLoss()
    }

    // endregion
}