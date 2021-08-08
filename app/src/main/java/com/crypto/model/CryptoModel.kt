package com.crypto.model

/**
 *
 * 网络请求数据
 * @author guilicheng
 * @date 2021/8/8
 *
 **/
class CryptoModel: ICryptoModel {

    override fun queryCurrencyList(param: ICryptoModel.QueryCurrencyParam): ICryptoModel.QueryCurrencyResult {
        TODO("Not yet implemented")
        // TODO: guilicheng 2021/8/8 网络请求
    }

    override fun queryCurrencyRateList(param: ICryptoModel.QueryCurrencyRateParam): ICryptoModel.QueryCurrencyRateResult {
        TODO("Not yet implemented")
        // TODO: guilicheng 2021/8/8 网络请求
    }

    override fun queryWalletBalance(param: ICryptoModel.QueryWalletBalanceParam): ICryptoModel.QueryWalletBalanceResult {
        TODO("Not yet implemented")
        // TODO: guilicheng 2021/8/8 网络请求
    }

}