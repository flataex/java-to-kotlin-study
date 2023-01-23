package travelator.ch5

class KotlinApplication(
    private var preferences: UserPreferences
) {
    fun showWelcome() {
        KotlinWelcomeView(preferences).show()
    }

    fun editPreferences() {
        preferences = KotlinPreferencesView(preferences).showModal()
    }
}