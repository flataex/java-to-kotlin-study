# ch3. 자바 클래스에서 코틀린 클래스로

## 간단한 값 타입
```java
public class EmailAddress {
    private final String localPart; // 불변인 필드
    private final String domain; // 불변인 필드

    public static EmailAddress parse(String value) { // 정적 팩터리 메서드
        var atIndex = value.lastIndexOf('@');
        if (atIndex < 1 || atIndex == value.length() - 1) 
        throw new IllegalArgumentException(
            "EmailAddress must be two parts separated by @"
        );
        return new EmailAddress(
            value.substring(0, atIndex),
            value.substring(atIndex + 1)
        );
    }

    public EmailAddress(String localPart, String domain) { // 생성자
        this.localPart = localPart;
        this.domain = domain;
    
    }

    public static String getLocalPart() {
        return localPart;
    }

    public static String getDomain() {
        return domain;
    }

    // 모든 필드가 같을 때 두 EmailAddress 객체가 같다고 판정하도록 하기 위해
    // equals(), hashCode() 메서드 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;

        return localPart.equals(that.localPart) &&
                domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPart, domain);
    }

    @Override
    public String toString() { // 표준 이메일 형식을 반환하는 메서드
        return localPart + "@" + domain;
    }
}

```
* 자바 코드에서는 @Nullable 애너테이션이 빠져있어 널 가능성 관련 관습을 찾아볼 수 없다.

* 자바에서는 혼란을 일으킬 수 있는 버그를 피하기 위해 equals()와 hashCode() 메서드를 다시 생성해 줘야 한다.

* 인텔리제이에서는 자바 코드를 코틀린 코드로 변환해주는 기능을 제공한다.

```kotlin
class EmailAddress (
    val localPart: String,
    val domain: String
) {
    override fun equals(o: Any?) : Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as EmailAddress

        return localPart == that.localPart && domain == that.domain
    }

    override fun hashcode(): Int {
        return Objects.hash(localPart, domain)
    }

    override fun toString(): String {
        return "$localPart@$domain";
    }

    companion object {
        @JvmStatic
        fun parse(value: String) EmailAddress {
            val atIndex = value.lastIndexOf('@')
            require(!(atIndex < 1 || atIndex = value.length - 1)) {
                "EmailAddress must be two parts separated by @"
            }
            return EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
            )
        }
    }
}
```
* 코틀린 클래스는 주생성자 안에 프로퍼티를 선언하므로 간결하다. 파라미터 앞에 val이 붙어 있으면 프로퍼티로 취급된다.
* 주생성자는 자바 표준 코딩 관습처럼 클래스 이름, 사위클래스, 인터페이스, 클래스본문(필드, 생성자 메서드)순으로 요소를 배열한다.

* 동반 객체(companion object)와 @JvmStatic 애너테이션을 사용하면 클래스를 코틀린으로 바꿔도 정적 메서드 호출 코드를 변경할 필요가 없다.

## 데이터 클래스의 한계
데이터 클래스는 equals(), hashCode(), toString 메서드뿐만 아니라 원하면 일부를 다른 값으로 대치할 수 있는 copy() 메서드도 생성한다. 문제는 copy 메서드가 내부표현을 추상화하거나 프로퍼티사이에 어떤 불변조건을 유지해야 하는 경우에도 불변 조건을 깨버릴 수 있다는 것이다.

```java
public class Money {
    private final BigDeciaml amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) &&
                currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() { // 표준 이메일 형식을 반환하는 메서드
        return amount.toString() + " " + currency.getCurrencyCode();
    }

    public Money add(Money that) {
        if (!this.currency.equals(that.currency)) {
            throw new IllegalArgumentException(
                "cannot add Money values of different currencies");
        }

        return new Money(this.amount.add(that.amount), this.currency);
    }
}
```
위 자바 코드를 코틀린 코드로 변환하면 다음과 같다.

```kotlin
class Money
private constructor (
    val amount: BigDecimal,
    val currency: Currency
) {
    override fun equals(o: Any?) : Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val money = o as Money
        return amount == money.amount && currency == money.currency
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, currency)
    }

    override fun toString(): String {
        return amount.toString() + " " + currency.currencyCode
    }

    fun add(that: Money): Money {
        require(currency == that.currency) {
             "cannot add Money values of different currencies"
        }
        return Money(amount.add(that.amount), currency)
    }

    companion object {
        @JvmStatic
        fun of(amount: BigDecimal, currency: Currency): Money {
            return Money(
                amount.setScale(currency.defaultFractionDigits),
                currency
            )
        }
    }
}
```

위 코틀린 코드에서 class를 data class로 변경하면 "Private data class constructor is exposed via genearted 'copy' method."라는 경고를 표시한다.

데이터 클래스에 있는 copy() 메서드는 항상 공개 메서드 이기 때문에 불변 조건을 지키지 않는 새 Money 값을 만들 수 있다. 따라서 프로퍼티 사이에 불변 조건을 유지해야 하는 값 타입을 데이터를 사용해 정의해서는 안 된다. 하지만 나중에 다른 장(12장)에서 이런 클래스도 더 깔끔하고 편하게 정의할 수 있다.

## 다음으로 나아가기
대부분 경우, 자바 클래스를 코틀린으로 변환하는 것이 쉽다. 데이터 클래스를 사용해 위의 EmailAddress와 같은 간단한 클래스에서 성가신 준비 코드를 많이 줄일 수 있다.

아직도 EmailAddress와 Money는 자바스러워 보이는 상태이며, 앞으로 코틀린 숙어를 사용해 더 간결하고, type-safe하며, 다른 코드를 추가힉도 쉬운 코드로 변경하는 방법을 알아볼 것이다.
