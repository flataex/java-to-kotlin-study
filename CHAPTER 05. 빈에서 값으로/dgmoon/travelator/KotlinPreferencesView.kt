package travelator.ch5

data class KotlinPreferencesView(
    private var preferences: UserPreferences
) : View() {
    private val greetingPicker = GreetingPicker()
    private val localePicker = LocalePicker()
    private val currencyPicker = CurrencyPicker()
    override fun show() {
        greetingPicker.greeting = preferences.greeting
        localePicker.locale = preferences.locale
        currencyPicker.currency = preferences.currency
        super.show()
    }

    fun showModal(): UserPreferences {
        greetingPicker.greeting = preferences.greeting
        localePicker.locale = preferences.locale
        currencyPicker.currency = preferences.currency
        show()
        return preferences
    }

    protected fun onGreetingChange() {
        preferences = UserPreferences(
            greetingPicker.greeting,
            preferences.locale,
            preferences.currency
        )
    }
}