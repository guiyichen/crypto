package com.crypto.base

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders

/**
 * 页面通用功能ViewModel
 * @author : guilicheng
 * @date : 2021/08/08
 */
abstract class AbsBusinessActivity : BaseActivity() {

    //region activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initEvent()
        initData()
    }

    //region method for override

    /**初始化UI视图*/
    protected open fun initView(savedInstanceState: Bundle?) {}

    /**初始化数据绑定、事件监听*/
    protected open fun initEvent() {}

    /**初始化数据*/
    protected open fun initData() {}

    //endregion

    //region helper

    /**
     * 用于 MVVM 模型开发业务
     */
    protected fun <T : BaseViewModel> createViewModel(viewModelClass: Class<T>): T =
        ViewModelProviders.of(this).get(viewModelClass)

    //endregion
}