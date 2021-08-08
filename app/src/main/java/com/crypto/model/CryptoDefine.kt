package com.crypto.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 数据结构定义
 * @author guilicheng
 * @date 2021/8/8
 *
 **/


/**
 * Currency 钱包支持的货币转换
 *
 * @param coinId  代号：BTC
 */
@Parcelize
data class Currency(@SerializedName("coin_id") val coinId: String?,
                    @SerializedName("name") val name: String?,
                    @SerializedName("symbol") val symbol: String?,
                    @SerializedName("colorful_image_url") val colorfulImageUrl: String?,
    ) : Parcelable


/**
 * CurrencyRate 当前货币汇率
 *
 * @param fromCurrency  对应数字货币：BTC
 */
@Parcelize
data class CurrencyRate(@SerializedName("from_currency") val fromCurrency: String?,
                        @SerializedName("to_currency") val toCurrency: String?,
                        @SerializedName("rates") val rates: String?,
                        @SerializedName("amount") val amount: Int?,
                        ) : Parcelable


/**
 * WalletBalance 钱包余额
 *
 * @param currency  对应数字货币：BTC
 * @param amount  对应数字货币数量
 */
@Parcelize
data class WalletBalance(@SerializedName("currency") val currency: String?,
                         @SerializedName("amount") val amount: Double?
) : Parcelable



/**
 * 转化后的账户货币信息
 * @param coinId coinId
 * @param name name
 * @param symbol symbol
 * @param currency currency
 * @param amount amount
 */
@Parcelize
data class BalanceItemInfo(
    val coinId: String,
    val name: String,
    val symbol: String,
    val currency: String,
    val amount: String,
    val value: String,
    val picUrl: String,
) : Parcelable



// region
/**
 * 钱包页的各种类型定义
 *
 * 密封类定义，有限的子类来区分详情的不同维度
 */
sealed class WalletVariant : Parcelable {

    /**
     * @param account 总金额
     * @param symbol 货币符号
     * @param currency 当前货币
     */
    @Parcelize
    data class AccountTotalInfo(val account: String, val symbol: String, val currency: String) : WalletVariant(), Parcelable


    /**
     * @param balanceInfoList  转化后的账户货币列表
     */
    @Parcelize
    data class AccountBalanceList(val balanceInfoList: List<BalanceItemInfo>) : WalletVariant(), Parcelable
}
// endregion