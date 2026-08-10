package com.example.calc

import android.app.Application
import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.conversion.RatesRepository
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.infra.AppPreferenceStoreImpl
import com.example.calc.infra.ExpressionHistoryStoreImpl
import com.example.calc.infra.RatesRepositoryImpl
import com.example.calc.infra.RatesStore
import com.example.calc.infra.dataStore

class CalcApplication : Application() {
    val ratesRepository: RatesRepository by lazy { RatesRepositoryImpl(RatesStore(dataStore)) }
    val appPreferenceStore: AppPreferenceStore by lazy {
        AppPreferenceStoreImpl(
            dataStore
        )
    }
    val expressionHistoryStore: ExpressionHistoryStore by lazy {
        ExpressionHistoryStoreImpl(dataStore)
    }
}