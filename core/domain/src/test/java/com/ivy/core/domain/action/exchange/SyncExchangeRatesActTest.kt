package com.ivy.core.domain.action.exchange

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.ivy.core.persistence.entity.exchange.ExchangeRateEntity
import com.ivy.data.exchange.ExchangeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SyncExchangeRatesActTest {

    private lateinit var syncExchangeRatesAct: SyncExchangeRatesAct
    private lateinit var exchangeProviderFake: RemoteExchangeProviderFake
    private lateinit var exchangeRateDaoFake: ExchangeRateDaoFake

    @BeforeEach
    fun setUp() {
        exchangeProviderFake = RemoteExchangeProviderFake()
        exchangeRateDaoFake = ExchangeRateDaoFake()
        syncExchangeRatesAct = SyncExchangeRatesAct(
            exchangeProvider = exchangeProviderFake,
            exchangeRateDao = exchangeRateDaoFake
        )
    }

    @Test
    fun `Test sync exchange rates, negative values ignored`() = runBlocking {
        syncExchangeRatesAct("USD")

        val usdRates = exchangeRateDaoFake
            .findAllByBaseCurrency("USD")
            .first { it.isNotEmpty() }
        val cadRate = usdRates.find { it.currency == "CAD" }

        assertThat(cadRate).isNull()
    }

    @Test
    fun `Test sync exchange rates, valid values saved`() = runBlocking<Unit> {
        syncExchangeRatesAct("USD")

        val usdRates = exchangeRateDaoFake
            .findAllByBaseCurrency("USD")
            .first { it.isNotEmpty() }
        val eurRate = usdRates.find { it.currency == "EUR" }
        val audRate = usdRates.find { it.currency == "AUD" }

        assertThat(eurRate).isNotNull()
        assertThat(audRate).isNotNull()
    }

    @Test
    fun `findAllByBaseCurrency should collect for each save`() = runBlocking {
        val dao = ExchangeRateDaoFake()
        var saveCount = 0

        CoroutineScope(Dispatchers.IO).launch {
            dao.findAllByBaseCurrency("USD")
                .collect {
                    println(it)
                    saveCount++
                }
        }

        dao.save(
            listOf(
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    currency = "EUR",
                    rate = 0.91,
                    provider = ExchangeProvider.Fawazahmed0
                ),
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    currency = "AUD",
                    rate = 1.49,
                    provider = ExchangeProvider.Fawazahmed0
                ),
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    currency = "CAD",
                    rate = -3.0,
                    provider = ExchangeProvider.Fawazahmed0
                ),
                ExchangeRateEntity(
                    baseCurrency = "EUR",
                    currency = "EUR",
                    rate = 1.08,
                    provider = ExchangeProvider.Fawazahmed0
                ),
                ExchangeRateEntity(
                    baseCurrency = "EUR",
                    currency = "AUD",
                    rate = 1.62,
                    provider = ExchangeProvider.Fawazahmed0
                ),
                ExchangeRateEntity(
                    baseCurrency = "EUR",
                    currency = "CAD",
                    rate = 1.43,
                    provider = ExchangeProvider.Fawazahmed0
                ),
            )
        )

        dao.save(
            listOf(
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    currency = "EUR",
                    rate = 100.91,
                    provider = ExchangeProvider.Fawazahmed0
                ),
            )
        )

        delay(100)
        assertThat(saveCount).isEqualTo(3)
    }
}
