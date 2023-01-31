# 빈에서 값으로

## 5.2 값

> 값 = value = variable, parameter, field &rarr; 불변 데이터 조각으로 정의<br>
> 값 타입 = 불변 데이터 조각의 동작을 정의하는 타입

## 5.3 값을 선호해야만 하는 이유는 무엇인가?

- 맵의 키나 집합 원소로 불변 객체 할당 가능
- 불변 객체의 불변 컬렉션에 대해 반복하는 경우 원소가 달라질 우려 필요 x
- 초기 상태를 deep copy 하지 않고도 다양한 시나리오 가능
- thread safe

## 5.4 빈을 값으로 리팩터링하기

#### [@JvmOverloads란,](https://holika.tistory.com/entry/%EB%82%B4-%EB%A7%98%EB%8C%80%EB%A1%9C-%EC%A0%95%EB%A6%AC%ED%95%9C-Kotlin-JvmOverloads-constructor%EB%A5%BC-%EC%9D%BC%EC%9D%BC%EC%9D%B4-%EC%83%81%EC%86%8D%EB%B0%9B%EC%95%84-%EB%A7%8C%EB%93%A4%EA%B8%B0-%EA%B7%80%EC%B0%AE%EB%8B%A4%EB%A9%B4)
> 생성자 오버로딩을 자동으로 생성해 주는 어노테이션

```java

public class PreferencesView extends View {
    
    private final UserPreferences preferences;
    private final GreetingPicker greetingPicker = new GreetingPicker();
    private final LocalePicker localePicker = new LocalPicker();
    private final CurrencyPicker currencyPicker = new CurrencyPicker();
    
    public PreferencesView(UserPreferences preferences) {
        this.preferences = preferences;
    }
    
    public void show() {
        greetingPicker.setGreeting(preferences.getGreeting());
        localePicker.setLocale(preferences.getLocale());
        curencyPicker.setCurrency(preferences.getCurrency());
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

가변 데이터 가능성 &uarr;
- `PreferencesView`, `WelcomeView` 가 둘 다 활성화된 경우 `WelcomeView`의 상태가 현재 값과 달라질 수 있음
- `UserPreferences`의 동등성과 해시 코드가 가변 프로퍼티 값에 따라 결정 &rarr; `UserPreferences`를 집합에 넣거나 맵의 키로 사용 불가능
- 읽기와 쓰기가 다른 스레드에서 발생하는 경우 설정 프로퍼티 수준에서 동기화 처리 필요

### 1. 코틀린으로 변환


```kotlin
class PreferencesView(
    private val preferences: UserPreferences
) : View() {
    private val greetingPicker = GreetingPicker()
    private val localePicker = LocalePicker()
    private val currencyPicker = CurrencyPicker()
    
    fun showModal(): UserPreferences {
        greetingPicker.greeting = preferences.greeting
        localePicker.locale = preferences.locale
        currencyPicker.currency = preferences.currency
        show()
        
        return preferences
    }

    override fun show() {
        greetingPicker.greeting = preferences.greeting
        localePicker.locale = preferences.locale
        currencyPicker.currency = preferences.currency
        super.show()
    }
}

protected fun onGreetingChange() {
    preferences.greeting = greetingPicker.greeting
}
```

```kotlin
class Application(
    private var preferences: UserPreferences
) {
    fun editPreferences() {
        preferences = PreferencesView(preferences).showModal()
    }
}
```

- `preferences` 를 `var`로 지정<br>
&rarr; UI element가 update 될 때 `preferences` 에 새로운 `UserPreferences` 객체를 지정하게 하기 위함<br>
&rarr; 리팩토링 필요!

### 2. var &rarr; val 변경

```kotlin

import java.util.Currency

class PreferencesView(
    private var preferences: UserPreferences
) : View() {
    private val greetingPicker = GreetingPicker()
    private val localePicker = LocalePicker()
    private val currencyPicker = CurrencyPicker()

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

data class UserPreferences(
    val greeting: String,
    val locale: Locale,
    val currency: Currency
)
```

- setter 호출 제거
- 생성자에서 각 객체 필드에 주입하고 절대로 변경하지 않음
- var &rarr; val 로 변경하여 디폴트 생성자 안에 삽입 &rarr; DI 개념과 유사

### 3. 생성한 객체 주입

```kotlin
class PreferencesView : View() {
    private val greetingPicker = GreetingPicker()
    private val localePicker = LocalePicker()
    private val currencyPicker = CurrencyPicker()

    fun showModal(preferences: UserPreferences): UserPreferences {
        greetingPicker.greeting = preferences.greeting,
        localePicker.locale = preferences.locale,
        currencyPicker.currency = preferences.currency 
        show()

        return UserPreferences(
            greeting = preferences.greeting,
            locale = preferences.locale,
            currency = preferences.currency
        )
    }
}
```

```kotlin
class Application(
    private var preferences: UserPreferences
) {
    fun editPreferences() {
        preferences = PreferenceView().showModal(preferences)
    }
}
```