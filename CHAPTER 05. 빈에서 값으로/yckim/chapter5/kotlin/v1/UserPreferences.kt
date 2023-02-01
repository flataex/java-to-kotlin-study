package chapter5.kotlin.v1

import java.util.*

class UserPreferences @JvmOverloads constructor(
    var greeting: String = "Hello",
    var locale: Locale = Locale.UK,
    var currency: Currency = Currency.getInstance(Locale.UK)
)