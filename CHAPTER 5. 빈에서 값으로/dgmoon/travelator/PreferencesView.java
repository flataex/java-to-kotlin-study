package travelator.ch5;

public class PreferencesView {
    private final UserPreferences preferences;
    private final GreetingPicker greetingPicker = new GreetingPicker();
    private final LocalePicker localePicker = new LocalePicker();
    private final CurrencyPicker currencyPicker = new CurrencyPicker();
    public PreferencesView(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public void show() {
    }
}
