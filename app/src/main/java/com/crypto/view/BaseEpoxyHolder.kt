package com.crypto.view

import android.view.View
import androidx.annotation.CallSuper
import com.airbnb.epoxy.EpoxyHolder
import kotlinx.android.extensions.LayoutContainer

/**
 * 统一做bind view初始化，减少样板代码。
 *
 * @author guilicheng
 * @date 2021-08-08
 */
open class BaseEpoxyHolder : EpoxyHolder(), LayoutContainer {
    override lateinit var containerView: View

    @CallSuper
    override fun bindView(itemView: View) {
        containerView = itemView
    }
}