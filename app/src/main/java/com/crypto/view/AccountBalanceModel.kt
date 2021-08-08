package com.crypto.view

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.crypto.R
import com.crypto.model.WalletVariant
import kotlinx.android.synthetic.main.layout_account_balance.*

/**
 * 账户总余额
 */
@EpoxyModelClass(layout= R.layout.layout_account_balance)
abstract class AccountBalanceModel() : EpoxyModelWithHolder<BaseEpoxyHolder>() {

    @EpoxyAttribute
    lateinit var totalInfo: WalletVariant.AccountTotalInfo

    override fun bind(holder: BaseEpoxyHolder) {
        super.bind(holder)
        holder.txt_account.text = totalInfo.account
        holder.txt_currency.text = totalInfo.currency
        holder.txt_symbol.text = totalInfo.symbol
    }

}