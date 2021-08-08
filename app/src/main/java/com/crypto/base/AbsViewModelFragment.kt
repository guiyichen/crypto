package com.crypto.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders

/**
 * 基于[BaseViewModel]的fragment，适用于 MVVM 模型开发业务
 *
 * 推荐使用 MVVM 模型进行业务开发
 *
 * @author guilicheng
 * @date 2021-08-08
 */
abstract class AbsViewModelFragment<TViewModel : BaseViewModel> : BaseFragment() {
    // region fields

    /**
     * ViewModel实例，用于访问业务数据和业务接口
     */
    protected val vm: TViewModel by lazy { this.createViewModel() }

    // endregion

    // region abstract

    /**
     * 获取[vm]的类型，用于构造[vm]的实例
     */
    protected abstract fun getViewModelClass(): Class<TViewModel>

    // endregion

    // region virtual

    /**
     * 初始化UI视图
     * 此函数用于UI视图的初始设定
     */
    open fun initView(savedInstanceState: Bundle?) {}

    /**
     * 初始化数据绑定
     * 此函数用于[vm]里的业务数据和UI交互逻辑建立绑定关系
     */
    open fun initBinding() {}

    /**
     * 初始化数据
     * 此函数用于初始化[vm]里的业务数据
     */
    open fun initData() {}

    // endregion

    // region life cycle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initView(savedInstanceState)
        this.initBinding()
        this.initData()
    }

    // endregion

    // region helper

    private fun createViewModel(): TViewModel = ViewModelProviders.of(this).get(this.getViewModelClass())

    // endregion
}