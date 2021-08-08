package com.crypto.view

import androidx.fragment.app.Fragment
import com.airbnb.epoxy.EpoxyController
import com.crypto.model.BalanceItemInfo
import com.crypto.model.WalletVariant

/**
 * 钱包页 epoxy model controller
 *
 * @author guilicheng
 * @date 2021-08-08
 */
class CryptoWalletController(val fragment: Fragment) : EpoxyController() {
    companion object {
        private const val TAG = "CryptoWalletController"
    }

    private lateinit var balanceInfoList: List<WalletVariant>

    /**
     * item点击跳转详情页
     */
    var itemClickListener: ((BalanceItemInfo) -> Unit)? = null


    fun init(balanceInfoList: List<WalletVariant>) {
        this.balanceInfoList = balanceInfoList
        requestModelBuild()
    }

    override fun buildModels() {
        balanceInfoList.forEach { info ->
            when (info) {
                is WalletVariant.AccountTotalInfo -> addAccountTotalInfo(info)
                is WalletVariant.AccountBalanceList -> addAccountBalanceList(info)
            }
        }
    }


    /**
     * 账户总余额
     */
    private fun addAccountTotalInfo(info: WalletVariant.AccountTotalInfo) {
        info.let {
            accountBalance {
                id("addAccountTotalInfo")
                totalInfo(it)
            }
        }
    }

    /**
     * 账户各数字货币列表
     */
    private fun addAccountBalanceList(info: WalletVariant.AccountBalanceList) {
        if (info.balanceInfoList.isNotEmpty()) {
            cryptoBalanceList {
                id("addAccountBalanceList")
                balanceList(info.balanceInfoList)
                itemClickListener { item ->
                    itemClickListener?.invoke(item)
                }
            }
        }
    }

}