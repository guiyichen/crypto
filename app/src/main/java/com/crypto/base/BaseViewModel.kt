package com.crypto.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.crypto.async.async

/**
 * base view model define
 *
 * @author guilicheng
 * @date 2021-08-08
 */
abstract class BaseViewModel : ViewModel() {
    // region ViewModel

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        async.cancelAll()
    }

    // endregion
}