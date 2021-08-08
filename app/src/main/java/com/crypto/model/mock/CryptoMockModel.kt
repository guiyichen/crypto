package com.crypto.model.mock

import com.crypto.model.Currency
import com.crypto.model.CurrencyRate
import com.crypto.model.ICryptoModel
import com.crypto.model.WalletBalance

/**
 * mock 数据
 * @author guilicheng
 * @date 2021/8/8
 *
 **/
class CryptoMockModel: ICryptoModel {

    override fun queryCurrencyList(param: ICryptoModel.QueryCurrencyParam): ICryptoModel.QueryCurrencyResult {
        return ICryptoModel.QueryCurrencyResult.Success(
            listOf(
                Currency("BTC","Bitcoin", "BTC", "https://s3-ap-southeast-1.amazonaws.com/monaco-cointrack-production/uploads/coin/colorful_logo/5c1246f55568a400e48ac233/bitcoin.png"),
                Currency("ETH","Ethereum", "ETH", "https://s3-ap-southeast-1.amazonaws.com/monaco-cointrack-production/uploads/coin/colorful_logo/5c12487d5568a4017c20a993/ethereum.png"),
                Currency("CRO","Crypto.com Coin", "CRO", "https://s3-ap-southeast-1.amazonaws.com/monaco-cointrack-production/uploads/coin/colorful_logo/5c1248c15568a4017c20aa87/cro.png"),
                Currency("USDT","Tether", "USDT", "https://s3-ap-southeast-1.amazonaws.com/monaco-cointrack-production/uploads/coin/colorful_logo/5c12487f5568a4017c20a999/tether.png"),
                Currency("DAI","dai03", "DAI", "https://s3-ap-southeast-1.amazonaws.com/monaco-cointrack-production/uploads/coin/colorful_logo/5e01c4cd49cde700adb27b0d/4943__1_.png"),
            )
        )
    }

    override fun queryCurrencyRateList(param: ICryptoModel.QueryCurrencyRateParam): ICryptoModel.QueryCurrencyRateResult {
        return ICryptoModel.QueryCurrencyRateResult.Success(
         listOf(
             CurrencyRate("USDT", "USD", "1.000727", 1000),
             CurrencyRate("XRP", "USD", "0.248040", 1000),
             CurrencyRate("KAVA", "USD", "1.798794", 1000),
             CurrencyRate("MCO", "USD", "4.042767", 1000),
             CurrencyRate("CRV", "USD", "0.469000", 1000),
             CurrencyRate("FET", "USD", "0.045014", 1000),
             CurrencyRate("STAKE", "USD", "11.117285", 1000),
             CurrencyRate("OGN", "USD", "0.167389", 1000),
             CurrencyRate("NEST", "USD", "0.053808", 1000),
             CurrencyRate("CRO", "USD", "0.147268", 1000),
             CurrencyRate("ETH", "USD", "340.210000", 1000),
             CurrencyRate("BTC", "USD", "10603.900000", 1000),
             CurrencyRate("DAI", "USD", "1.010000", 1000),
         )
        )
    }

    override fun queryWalletBalance(param: ICryptoModel.QueryWalletBalanceParam): ICryptoModel.QueryWalletBalanceResult {
        return ICryptoModel.QueryWalletBalanceResult.Success(
            listOf(
                WalletBalance("USDT", 1245.0),
                WalletBalance("BTC", 1.4),
                WalletBalance("ETH", 20.3),
                WalletBalance("CRO", 259.1),
                WalletBalance("DAI", 854.0)
               )
        )
    }

}