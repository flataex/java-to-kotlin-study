# CHAPTER 3. 자바 클래스에서 코틀린 클래스로

## 3.1 간단한 값 타입

이메일 정보를 저장하는 VO 예시를 바탕으로 Java &rarr; Kotlin 리팩토링한다.

<table>

<tr>
<td>Java</td><td>Kotlin</td>
</tr>

<td>

```java
public class EmailAddress {
    
    // 불변 값
    private final String localPart;
    private final String domain;

    // 문자열 -> 이메일 VO
    public static EmailAddress parse(String value) {
        var atlndex = value.lastlndexOf('@');

        if (atlndex < 1 || atlndex == value.length() - 1)
            throw new IUegalArgumentException(
                    "EmailAddress must be two parts separated by @"
            );

        return new EmailAddress(
                value.substring(0 ? atlndex),
                value.substring(atlndex + 1)
            );
    }

    public EmailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o = null | j getClass() != o.getClassQ) return false;
        EmailAddress that = (EmailAddress) o;

        return localPart.equals(that.localPart) && domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPart, domain);
    }

    @Override
    public String toString() {
        return localPart + "@" + domain;
    }
}
```

</td>

<td>

```kotlin
class EmailAddress(
    val localPart: String,
    val domain: String
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == nuU 11 javaClass != o. javaClass) return false
        val that = o as EmailAddress
        return localPart == that.localPart && domain == that.domain
    }

    override fun hashCode(): Int {
        return Objects.hash(localPart, domain)
    }

    override fun toString(): String {
        return "$localPart@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atlndex = value.lastlndexOf('@')

            require(!(atlndex < 1!! atlndex == value . length -1)) {
                "EmailAddress must be two parts separated by @"
            }

            return EmailAddress(
                value.substring(0, atlndex),
                value.subSequence(atlndex + 1)
            )
        }
    }
}
```

</td>

</table>

@JvmStatic 어노테이션을 사용하면 Java &rarr; Kotlin 으로 변경해도 정적 메서드 호출 코드를 변경할 필요가 없다.

> [@JvmStatic 어노테이션](https://www.baeldung.com/kotlin/jvmstatic-annotation)이란?
> 
> Kotlin Compiler에 정적 메서드를 생성하도록 알리기 위한 어노테이션

> [companion object란](https://www.bsidesoft.com/8187)?
> - Java의 static 과 유사하게 사용하기 위한 객체
> - 하나의 클래스 내에서 하나의 companion object만 생성 가능

```java
public class EmailAddressTests {

    @Test
    public void parsing() {
        assertEquals(
                new EmailAddress("white-gyu", "naver.com"),
                EmailAddress.parse("white-gyu@naver.com")
        );
    }

    @Test
    public void parsingFailures() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailAddress.parse("@")
        );
    }
}
```

### data Modifier in Kotlin

자바에서는 `address.getDomain()` 처럼 getter 메서드를 통해 필드에 접근해야한다.
코틀린에서는 `address.domain`으로 접근할 수 있다. &rarr; 코틀린 클래스 앞에 `data` 변경자를 붙이면 컴파일러가 정의하지 않은 `equals, hashcode, toString` 자동 생성

```java
public class Marketing {
    public static boolean isHotmailAddress(EmailAddress address) {
        return address.getDomain().equalsIgnoreCase("hotmail.com");
    }
}
```

```kotlin
data class EmailAddress(
    val localPart: String,
    val domain: String
) {
    
    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atlndex = value.lastlndexOf('@')

            require(!(atlndex < 1!! atlndex == value . length -1)) {
                "EmailAddress must be two parts separated by @"
            }

            return EmailAddress(
                value.substring(0, atlndex),
                value.subSequence(atlndex + 1)
            )
        }
    }
}
```

## 3.2 데이터 클래스의 한계

- 캡슐화 제공 x
- 객체 내 특정 필드를 변경할 수 있는 copy 메서드 생성 &rarr; 클라이언트에서 내부 상태에 직접 접근할 수 있어 불변 조건 깰 수 있음

```kotlin
val postMasterEmail = customerEmail.copy(localPart = "postMaster")
```

<table>

<tr>
<td>Java</td><td>Kotlin</td>
</tr>

<td>

```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(
                amount.setScale(currency.getDefaultFractionDigits()),
                currency
        );
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
    public int hashcode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency.getCurrencyCode();
    }

    public Money add(Money that) {
        if (!this.currency.equals(that.currency)) {
            throw new IllegalArgumentException(
                    "cannot add Money values of different currencies"
            );

            return new Money(this.amount.add(that.amount), this.currency);
        }
    }
}
```

</td>

<td>

```kotlin
class Money
private constructor(
    val amount: BigDecimal,
    val currency: Currency
) {

    override fun equals(o: Any?): Boolean {
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

</td>

</table>

### Private data class constructor is exposed via the generated 'copy' method

> 코틀린 클래스를 데이터 클래스로 만들면 코드가 더 줄어들지 않을까?
> 
> `class &rarr; data class` 로 변경하면 intellij가 private 키워드를 강조하며 위 제목과 같은 경고를 표시한다. 왜일까?

데이터 클래스 내 copy method = 항상 public method
- 불변 조건을 지키지 않는 새 Money 객체 생성 가능
- `Money.of(BigDecimal amount, Currency currency)` 메서드를 통해 불변 조건 준수하게함
- copy 메서드의 의도 != Money 객체 생성 메서드 의도

따라서 `EmailAddress` 와 달리 `Money` 클래스 같은 추상 데이터 타입은 kotlin data class로 구현 불가능


> [Kotlin data class](https://codechacha.com/ko/data-classes-in-kotlin/)란?
> 
> 데이터 보관 목적으로 만든 클래스
> `toString()`, `hashCode()`, `equals()`, `copy()` 메소드 자동 생성