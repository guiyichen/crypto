package com.crypto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.crypto.base.AbsViewModelFragment
import com.crypto.base.BaseViewModel
import com.crypto.model.BalanceItemInfo
import com.crypto.model.WalletVariant
import com.crypto.view.CryptoWalletController
import kotlinx.android.synthetic.main.fragment_wallet_page.*


/**
 * 钱包页首页
 *
 * @author guilicheng
 * @date 2021-08-08
 */
class WalletFragment : AbsViewModelFragment<WalletFragment.WalletFragmentViewModel>() {

    companion object {
        private const val TAG = "WalletFragment"

        fun createInstance(): WalletFragment {
            return WalletFragment()
        }
    }

    private val hostVm by lazy { activity?.let{ ViewModelProviders.of(it).get(MainActivity.CryptoActivityViewModel::class.java) }}

    private val epoxyController by lazy { CryptoWalletController(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallet_page, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        recycler_view.apply {
            recycler_view.setController(epoxyController)
            layoutManager = LinearLayoutManager(context)
            epoxyController.apply {
                itemClickListener = { item ->
                    // TODO: guilicheng 2021/8/8 跳转具体页面
                }
            }
            tag = epoxyController
        }
    }

    override fun initBinding() {
        /**
         * 数据更新后，更新UI
         */
        hostVm?.accountTotalInfo?.observe(this, Observer{ accountTotalInfo ->
            vm.addPageHead(accountTotalInfo)
        })

        hostVm?.accountList?.observe(this, Observer { accountList ->
            vm.addPageCurrencyList(accountList)
        })

        vm.pageList.observe(this, Observer { pageList ->
            epoxyController.init(pageList)
            epoxyController.requestModelBuild()
        })
    }

    override fun getViewModelClass(): Class<WalletFragmentViewModel> = WalletFragmentViewModel::class.java

    class WalletFragmentViewModel : BaseViewModel() {
        /**
         * 保存页面整体数据
         */
        private val _pageList = MutableLiveData<List<WalletVariant>>()
        val pageList: LiveData<List<WalletVariant>> get() = this._pageList

        /**
         * 添加头部数据，已保证先插入头部数据、否则应做占位更新头部数据操作
         */
        fun addPageHead(accountTotalInfo: WalletVariant.AccountTotalInfo) {
            _pageList.value = listOf(accountTotalInfo)
        }

        /**
         * 添加内容列表数据
         */
        fun addPageCurrencyList(currencyList: List<BalanceItemInfo>) {
            val list = _pageList.value
            val mergeList = mutableListOf<WalletVariant>()
            if(list?.isNotEmpty() == true) {
                mergeList.addAll(list)
            }
            if(currencyList.isNotEmpty()) {
                mergeList.add(WalletVariant.AccountBalanceList(currencyList))
            }
            _pageList.value = mergeList
        }
    }
}