package com.crypto.view

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.crypto.R
import com.crypto.model.BalanceItemInfo
import kotlinx.android.synthetic.main.model_crypto_balance_list.*

/**
 * 列表epoxy model
 *
 * @author guilicheng
 * @date 2021-08-08
 */
@EpoxyModelClass(layout = R.layout.model_crypto_balance_list)
abstract class CryptoBalanceListModel : EpoxyModelWithHolder<BaseEpoxyHolder>() {

    @EpoxyAttribute
    lateinit var balanceList: List<BalanceItemInfo>

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var itemClickListener: (BalanceItemInfo) -> Unit

    override fun bind(holder: BaseEpoxyHolder) {
        super.bind(holder)
        holder.balance_list_view.withModels {
            balanceList.forEach { item ->
                cryptoBalanceItem {
                    id(item.coinId)
                    item(item)
                    clickListener(View.OnClickListener {
                        itemClickListener.invoke(item)
                    })
                }
            }
        }
        holder.balance_list_view.requestModelBuild()
    }
}