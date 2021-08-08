package com.crypto

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crypto.async.async
import com.crypto.base.AbsBusinessActivity
import com.crypto.base.BaseViewModel
import com.crypto.model.*
import com.crypto.model.mock.CryptoMockModel

/**
 * crypto 页
 * @author guilicheng
 * @data 2019-08-08
 */
// TODO: guilicheng 2021/8/8 使用ARouter 解偶
class MainActivity : AbsBusinessActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun initView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        loadRoot(R.id.activity_load_fragment, WalletFragment.createInstance())
    }

    private val vm by lazy { createViewModel(CryptoActivityViewModel::class.java) }

    override fun initData() {
        vm.queryWalletList()
        vm.queryCurrencyRate()
        vm.queryCurrencyList()
    }

    class CryptoActivityViewModel : BaseViewModel() {

        private val cryptoVerticalModel: ICryptoModel by lazy { CryptoMockModel() }
        // TODO: guilicheng 2021/8/8 使用ARouter 解偶 ,真实网络数据用CryptoModel

        //region
        /**
         * 钱包数字货币余额列表
         */
        private val _walletBalanceList = MutableLiveData<List<WalletBalance>>()

        /**
         * 网络查询钱包列表
         */
        fun queryWalletList() {
            async {
                val basicInfoResponse = await {
                    cryptoVerticalModel.queryWalletBalance(ICryptoModel.QueryWalletBalanceParam("guilicheng@qq.com"))
                }
                when (basicInfoResponse) {
                    is ICryptoModel.QueryWalletBalanceResult.Success -> {
                        _walletBalanceList.value = basicInfoResponse.walletBalanceList
                        Log.d(TAG, "queryWalletBalance: data = ${basicInfoResponse.walletBalanceList}")

                        calculateAccountList()
                    }
                    is ICryptoModel.QueryWalletBalanceResult.Failed -> {
                        _walletBalanceList.value = listOf()
                        Log.d(TAG, "queryWalletBalance: error msg = ${basicInfoResponse.tips}")
                    }
                }
            }
        }
        // endregion

        //region
        /**
         * 数字货币描述列表
         */
        private val _currencyList = MutableLiveData<List<Currency>>()

        /**
         * 网络查询数字货币描述列表
         */
        fun queryCurrencyList() {
            async {
                val basicInfoResponse = await {
                    cryptoVerticalModel.queryCurrencyList(ICryptoModel.QueryCurrencyParam("guilicheng@qq.com"))
                }
                when (basicInfoResponse) {
                    is ICryptoModel.QueryCurrencyResult.Success -> {
                        _currencyList.value = basicInfoResponse.currencyList
                        Log.d(TAG, "queryCurrencyList: data = ${basicInfoResponse.currencyList}")

                        calculateAccountList()
                    }
                    is ICryptoModel.QueryCurrencyResult.Failed -> {
                        _currencyList.value = listOf()
                        Log.d(TAG, "queryCurrencyList: error msg = ${basicInfoResponse.tips}")
                    }
                }
            }
        }
        // endregion

        //region
        /**
         * 货币所对应的汇率列表
         */
        private val _currencyRateList = MutableLiveData<List<CurrencyRate>>()

        /**
         * 查询货币所对应的汇率列表
         */
        fun queryCurrencyRate() {
            async {
                val basicInfoResponse = await {
                    cryptoVerticalModel.queryCurrencyRateList(ICryptoModel.QueryCurrencyRateParam("USD"))
                }
                when (basicInfoResponse) {
                    is ICryptoModel.QueryCurrencyRateResult.Success -> {
                        _currencyRateList.value = basicInfoResponse.currencyRateList
                        Log.d(TAG, "queryCurrencyRate: data = ${basicInfoResponse.currencyRateList}")

                        calculateAccountList()
                    }
                    is ICryptoModel.QueryCurrencyRateResult.Failed -> {
                        _currencyRateList.value = listOf()
                        Log.d(TAG, "queryCurrencyRate: error msg = ${basicInfoResponse.tips}")
                    }
                }
            }
        }
        // endregion

        /**
         * 保存总金额数据
         */
        private val _accountTotalInfo = MutableLiveData<WalletVariant.AccountTotalInfo>()
        val accountTotalInfo: LiveData<WalletVariant.AccountTotalInfo> get() = this._accountTotalInfo

        /**
         * 保存余额列表所对应当前货币值
         */
        private val _accountList = MutableLiveData<List<BalanceItemInfo>>()
        val accountList: LiveData<List<BalanceItemInfo>> get() = this._accountList

        /**
         * 计算账户列表各币种对应当前货币值
         */
        private fun calculateAccountList() {
            if (_walletBalanceList.value?.isNotEmpty() == true
                && _currencyRateList.value?.isNotEmpty() == true) {
                val walletList = _walletBalanceList.value
                val currentRateList = _currencyRateList.value
                val accountList = walletList?.mapNotNull { walletItem ->
                    toAccountModel(walletItem, currentRateList)
                }
                accountList?.let { list ->
                    _accountList.value = list
                }
                calculateAccountTotal()

                if(_currencyList.value?.isNotEmpty() == true) {
                    mergeAccountList()
                }
            }
        }

        /**
         * 字符串转float
         */
        private fun getDoubleValue(str: String?): Double = try {
            str?.toDouble() ?: 0.0
        } catch (e: NumberFormatException) {
            0.0
        }

        /**
         * 生成只有对应汇率的数值
         */
        private fun toAccountModel(walletItem: WalletBalance, rateList: List<CurrencyRate>?): BalanceItemInfo {
            fun calculateAccount(rateItem: CurrencyRate?, walletItem: WalletBalance): Double {
                return (walletItem.amount ?: 1.0) * getDoubleValue(rateItem?.rates)
            }

            val rateItem = rateList?.find { it.fromCurrency == walletItem.currency }
            return BalanceItemInfo(walletItem.currency.orEmpty(), "", "$", rateItem?.toCurrency.orEmpty(), walletItem.amount.toString() ,calculateAccount(rateItem, walletItem).toString(), "")
        }

        /**
         * 计算账户总金额
         */
        private fun calculateAccountTotal() {
            var total = 0.0
            if (_accountList.value?.isNotEmpty() == true) {
                _accountList.value?.forEach {  balanceItemInfo ->
                    total+= getDoubleValue(balanceItemInfo.value)
                }
            }
            val totalInfo = WalletVariant.AccountTotalInfo(total.toString(),
                _accountList.value?.first()?.symbol.orEmpty(), accountList.value?.first()?.currency.orEmpty())
            _accountTotalInfo.value = totalInfo
        }

        /**
         * 合并全数据列表
         */
        private fun toCurrencyModel(currencyList: List<Currency>?, balanceItemInfo: BalanceItemInfo): BalanceItemInfo {

            fun findItem(currencyList: List<Currency>?, balanceItemInfo: BalanceItemInfo): Currency? {
                return currencyList?.first { it.coinId == balanceItemInfo.coinId }
            }

            val rateItem = findItem(currencyList, balanceItemInfo)

            return BalanceItemInfo(rateItem?.coinId.orEmpty(), rateItem?.name.orEmpty(), rateItem?.symbol.orEmpty(),
                balanceItemInfo.currency, balanceItemInfo.amount , balanceItemInfo.value, rateItem?.colorfulImageUrl.orEmpty())
        }

        /**
         * 合并列表数据
         */
        private fun mergeAccountList() {
            val accountList = _accountList.value?.mapNotNull { itemInfo ->
                toCurrencyModel(_currencyList.value, itemInfo)
            }

            accountList?.let { list ->
                _accountList.value = list
            }
        }
    }

}