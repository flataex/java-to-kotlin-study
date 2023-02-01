package chapter5.kotlin.v2

import chapter5.java.WelcomeView

class Application(private var preferences: UserPreferences) {
    fun showWelcome() {
        WelcomeView(preferences).show()
    }

    fun editPreferences() {
        preferences = PreferencesView().showModal(preferences)
    }
}