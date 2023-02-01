## 가변 POJO에서 값으로

*자바빈즈가 도입될 때 개발자들은 대부분의 객체가 가변 객체일 것이라고 생각했다.* <br>
*이 가변 객체를 불변 데이터로 리팩토링해보자.*

<br>

## 값을 선호해야만 하는 이유는 ?

- 맵의 키나 집합 원소로 불변 객체를 넣을 수 있다.
- 불변 객체의 불변 컬렉션에 대해 iteration하는 경우 원소가 달라질지 염려할 필요 없다.
- 불변 객체를 쓰면 되돌리기나 다시하기 등도 쉽게 구현할 수 있다.
- 여러 스레드에서 불변 객체를 안전하게 공유할 수 있다.

<br>

## 빈을 값으로 리팩토링하기

아래 자바 코드의 문제점은 다음과 같다.

- PreferencesView와 WelcomeView가 둘 다 활성화된 경우 WelcomeView의 상태가 현재 값과 달라질 수 있다.
- UserPreferences의 동등성과 해시코드가 가변 프로퍼티 값에 따라 결정된다. 따라서 UserPreferences를 집합에 넣거나 맵의 키로 쓸 수 없다.
- Welcomeview가 사용자 설정 정보만을 읽는다는 사실을 알려주는 표시가 없다.
- 읽기와 쓰기가 다른 스레드에서 발생하는 경우 설정 프로퍼티 수준에서 동기화를 처리해야만 한다.

```java
import java.util.Currency;
import java.util.Locale;

public class UserPreferences {
    private String greeting;
    private Locale locale;
    private Currency currency;

    public UserPreferences() {
        this("Hello", Locale.UK, Currency.getInstance(Locale.UK));
    }

    public UserPreferences(String greeting, Locale locale, Currency currency) {
        this.greeting = greeting;
        this.locale = locale;
        this.currency = currency;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
```

```java
public class Application {
    private final UserPreferences preferences;

    public Application(UserPreferences userPreferences) {
        this.preferences = userPreferences;
    }

    public void showWelcome() {
        new WelcomeView(preferences).show();
    }

    public void editPreferences() {
        new PreferencesView(preferences).show();
    }
}
```

```java
public class PreferencesView extends View {

    private final UserPreferences preferences;
    private final GreetingPicker greetingPicker = new GreetingPicker();
    private final LocalePicker localePicker = new LocalePicker();
    private final CurrencyPicker currencyPicker = new CurrencyPicker();

    public PreferencesView(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public void show() {
        greetingPicker.setGreeting(preferences.getGreeting());
        localePicker.setLocale(preferences.getLocale());
        currencyPicker.setCurrency(preferences.getCurrency());
        super.show();
    }

    protected void onGreetingChange() {
        preferences.setGreeting(greetingPicker.getGreeting());
    }

    protected void onLocaleChange() {
        preferences.setLocale(localePicker.getLocale());
    }

    protected void onCurrencyChange() {
        preferences.setCurrency(currencyPicker.getCurrency());
    }
}
```

<br>

***코틀린으로의 변환***

```kt
class Application(
    private val preferences: UserPreferences
) {
    fun showWelcome() {
        WelcomeView(preferences).show()
    }

    fun editPreferences() {
        PreferencesView(preferences).show()
    }
}
```

```kt
import java.util.*

class UserPreferences @JvmOverloads constructor(
    var greeting: String = "Hello", 
    var locale: Locale = Locale.UK, 
    var currency: Currency = Currency.getInstance(Locale.UK)
)
```

```kt
class PreferencesView(
    private val preferences: UserPreferences
) : View() {
    
    private val greetingPicker: GreetingPicker = GreetingPicker()
    private val localePicker: LocalePicker = LocalePicker()
    private val currencyPicker: CurrencyPicker = CurrencyPicker()
    
    override fun show() {
        greetingPicker.setGreeting(preferences.greeting)
        localePicker.setLocale(preferences.locale)
        currencyPicker.setCurrency(preferences.currency)
        super.show()
    }

    protected fun onGreetingChange() {
        preferences.greeting = greetingPicker.getGreeting()
    }

    protected fun onLocaleChange() {
        preferences.locale = localePicker.getLocale()
    }

    protected fun onCurrencyChange() {
        preferences.currency = currencyPicker.getCurrency()
    }
}
```

<br>

***가변 객체에 대한 불변 참조를 불변 객체에 대한 가변 참조로 바꾼다.***

> **진행 과정은 다음과 같다.** <br>
> 1. PreferencesView의 show를 showModal로 변경하고, show()가 반환되면 기존의 가변 properties 프로퍼티를 반환하도록 변경한다.
> 2. 이제는 Application.preferences가 가변 프로퍼티이며. showModal의 결과를 이 프로퍼티에 설정한다.
> 3. preference 프로퍼티에 새로운 UserPreferences 객체를 지정할 수 있게 하기 위해 PreferencesView의 preferences 프로퍼티를 가변으로 만든다.  
>  *UserPreferences에 대한 setter가 사라져서, 생성자에서 프로퍼티를 설정한 후 절대로 변경하지 않을 수 있다 !!*
> 4. 이제 UserPreferences의 프로퍼티를 val로 변경한다.
> 5. 설정에 대한 참조를 PreferencesView에서 제거하기 위해, Application에서 preferences 인자를 PreferencesView 생성자 대신 showModal에 넘기도록 변경한다.
> 6. 이제 사용자 설정이 변경될 수 있는 부분은 한 군데 뿐이다 !!

```kt
class Application(
    private var preferences: UserPreferences
) {
    fun showWelcome() {
        WelcomeView(preferences).show()
    }

    fun editPreferences() {
        preferences = PreferencesView().showModal(preferences)
    }
}
```

```kt
import java.util.*

data class UserPreferences (
    val greeting: String,
    val locale: Locale,
    val currency: Currency
)
```

```kt
class PreferencesView : View() {

    private val greetingPicker: GreetingPicker = GreetingPicker()
    private val localePicker: LocalePicker = LocalePicker()
    private val currencyPicker: CurrencyPicker = CurrencyPicker()

    fun showModal(preferences: UserPreferences): UserPreferences {
        greetingPicker.greeting = preferences.greeting
        localePicker.locale = preferences.locale
        currencyPicker.currency = preferences.currency
        show()
        return UserPreferences(
            greeting = greetingPicker.greeting,
            locale = localePicker.locale,
            currency = currencyPicker.currency
        )
    }

    protected fun onGreetingChange() {
        preferences.greeting = greetingPicker.getGreeting()
    }

    protected fun onLocaleChange() {
        preferences.locale = localePicker.getLocale()
    }

    protected fun onCurrencyChange() {
        preferences.currency = currencyPicker.getCurrency()
    }
}
```

