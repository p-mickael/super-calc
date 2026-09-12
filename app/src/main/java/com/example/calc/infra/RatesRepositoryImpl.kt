package com.example.calc.infra

import com.example.calc.domain.conversion.CurrencyRates
import com.example.calc.domain.conversion.RatesRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Instant

private val defaultRatesResponse = RatesResponse(
    date = "2026-06-19",
    rates = mapOf(
        "AUD" to 1.6348,
        "BRL" to 5.9173,
        "CAD" to 1.6228,
        "CHF" to 0.9248,
        "CNY" to 7.7624,
        "CZK" to 24.227,
        "DKK" to 7.4746,
        "GBP" to 0.86653,
        "HKD" to 8.9887,
        "HUF" to 352.68,
        "IDR" to 20420.55,
        "ILS" to 3.3968,
        "INR" to 108.1675,
        "ISK" to 144.0,
        "JPY" to 184.88,
        "KRW" to 1757.1,
        "MXN" to 19.8796,
        "MYR" to 4.7439,
        "NOK" to 11.1045,
        "NZD" to 1.9967,
        "PHP" to 69.645,
        "PLN" to 4.2615,
        "RON" to 5.2396,
        "SEK" to 10.974,
        "SGD" to 1.4804,
        "THB" to 37.681,
        "TRY" to 53.2587,
        "USD" to 1.1467,
        "ZAR" to 18.8907
    )
)

private val defaultRates = CurrencyRates.fromMap(
    defaultRatesResponse.rates.mapValues { (_, value) -> value.toBigDecimal() },
    Instant.DISTANT_PAST
)

class RatesRepositoryImpl(private val ratesStore: RatesStore) : RatesRepository {
    override suspend fun getRates(): CurrencyRates {
        val storedRates = ratesStore.read().first()

        if (storedRates != null && storedRates.isFresh)
            return storedRates

        val response = try {
            fetchLatestRates()
        } catch (_: Exception) {
            // Pas de réseau (ou erreur inattendue) : on ne touche pas au cache existant et on ne
            // fait surtout pas passer les taux par défaut pour "à jour" — sinon plus aucune
            // tentative de mise à jour n'aurait lieu avant le prochain seuil de fraîcheur.
            return storedRates ?: defaultRates
        }

        val fetchedRates = CurrencyRates.fromMap(
            response.rates.mapValues { (_, value) -> value.toBigDecimal() },
            Clock.System.now()
        )

        ratesStore.write(fetchedRates)
        return fetchedRates
    }
}