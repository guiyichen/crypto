package com.crypto.view

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.crypto.R
import com.crypto.model.BalanceItemInfo
import kotlinx.android.synthetic.main.model_crypto_balance_item.*

/**
 * item epoxy model
 *
 * @author guilicheng
 * @date 2021-08-08
 */
@EpoxyModelClass(layout = R.layout.model_crypto_balance_item)
abstract class CryptoBalanceItemModel : EpoxyModelWithHolder<BaseEpoxyHolder>() {

    @EpoxyAttribute
    lateinit var item: BalanceItemInfo

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var clickListener: View.OnClickListener

    override fun bind(holder: BaseEpoxyHolder) {
        super.bind(holder)
        holder.txt_name.text = item.name
        holder.txt_account.text = item.amount
        holder.txt_value.text = item.value
        Glide.with(holder.img_currency.context).load(item.picUrl).into(holder.img_currency)
        holder.layout_item.setOnClickListener(clickListener)
    }
}