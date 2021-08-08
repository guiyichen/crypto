package com.crypto.model


/***
 * 钱包模块接口定义
 *
 * @author guilicheng
 * @date 2021-08-09
 */
interface ICryptoModel {

    // endregion

    // region
    /**
     * 查询支持的货币
     *
     * @param userId 用户ID
     */
    data class QueryCurrencyParam(val userId: String?)

    /**
     * 查询支持的货币
     * 成功返回[Success]，并携带相应的列表
     * 失败返回[Failed]，并提供失败的提示信息，可能为空
     */
    sealed class QueryCurrencyResult {
        data class Success(val currencyList: List<Currency>) : QueryCurrencyResult()
        data class Failed(val tips: String? = null) : QueryCurrencyResult()
    }

    /**
     * 查询支持的货币
     * @param param 查询参数
     * @return 查询结果，详情参看[QueryCurrencyResult]
     */
    fun queryCurrencyList(param: QueryCurrencyParam): QueryCurrencyResult
    // endregion


    // region
    /**
     * 查询货币对美元的汇率
     *
     * @param currency 当前选择的货币
     */
    data class QueryCurrencyRateParam(val currency: String?)

    /**
     * 查询支持的货币
     * 成功返回[Success]，并携带相应的列表
     * 失败返回[Failed]，并提供失败的提示信息，可能为空
     */
    sealed class QueryCurrencyRateResult {
        data class Success(val currencyRateList: List<CurrencyRate>) : QueryCurrencyRateResult()
        data class Failed(val tips: String? = null) : QueryCurrencyRateResult()
    }

    /**
     * 查询支持的货币
     * @param param 查询参数
     * @return 查询结果，详情参看[QueryCurrencyRateResult]
     */
    fun queryCurrencyRateList(param: QueryCurrencyRateParam): QueryCurrencyRateResult
    // endregion


    // region
    /**
     * 查询钱包的货币余额 请求参数
     *
     * @param userId 用户ID
     */
    data class QueryWalletBalanceParam(val userId: String?)

    /**
     * 查询钱包的货币余额
     * 成功返回[Success]，并携带相应的列表
     * 失败返回[Failed]，并提供失败的提示信息，可能为空
     */
    sealed class QueryWalletBalanceResult {
        data class Success(val walletBalanceList: List<WalletBalance>) : QueryWalletBalanceResult()
        data class Failed(val tips: String? = null) : QueryWalletBalanceResult()
    }

    /**
     * 查询支持的货币
     * @param param 查询参数
     * @return 查询结果，详情参看[QueryWalletBalanceResult]
     */
    fun queryWalletBalance(param: QueryWalletBalanceParam): QueryWalletBalanceResult
    // endregion

}