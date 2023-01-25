package travelator.ch5

import java.util.*

class KotlinUserPreferences @JvmOverloads constructor(
    var greeting: String = "Hello",
    var locale: Locale = Locale.UK,
    var currency: Currency = Currency.getInstance(Locale.UK)
)