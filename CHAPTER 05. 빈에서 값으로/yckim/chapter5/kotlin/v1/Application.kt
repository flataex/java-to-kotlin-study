package chapter5.kotlin.v1

import chapter5.java.PreferencesView
import chapter5.java.UserPreferences
import chapter5.java.WelcomeView

class Application(private val preferences: UserPreferences) {
    fun showWelcome() {
        WelcomeView(preferences).show()
    }

    fun editPreferences() {
        PreferencesView(preferences).show()
    }
}