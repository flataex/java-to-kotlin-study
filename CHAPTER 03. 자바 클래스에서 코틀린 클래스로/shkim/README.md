### 예제 코드는 여기에

https://github.com/MALLLAG/java-to-kotlin-example/tree/main/src/main/java/com/example/demo

## 간단한 값 타입

> 아래의 코드는 email의 두 가지 부분을 저장하는 값 타입이다. <br>
> 이 클래스는 두 문자열을 감싸기만 하고 자체적인 연산을 제공하지 않는 아주 간단한 코드이지만, 아주 길다.

- 값은 불변이기에, 필드를 final로 선언한다.
- 문자열을 파싱해 EmailAddress로 만드는 static 메서드가 있다. 이 메서드는 전체 생성자를 호출한다.
- 필드는 생성자에서 초기화된다.
- 클래스의 프로퍼티를 구성하는 접근자 메서드는 자바빈의 명명 규칙을 따른다.
- equals와 hashCode를 구현해 모든 필드가 같을 때 두 EmailAddress가 같다고 판정한다.
- toString은 email 형식을 return한다.

<br>

````java
public class EmailAddress {
    private final String localPort;
    private final String domain;

    public static EmailAddress parse(String value) {
        var atIndex = value.lastIndexOf('@') ;
        if (atIndex < 1 || atIndex == value.length() - 1) {
            throw new IllegalAccessException(
                    "EmailAddress must be two parts separated by @"
            );
        }
        return new EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
        );
    }

    public EmailAddress(String localPort, String domain) {
        this.localPort = localPort;
        this.domain = domain;
    }

    public String getLocalPort() {
        return localPort;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return localPort.equals(that.localPort)
                && domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPort, domain);
    }

    @Override
    public String toString() {
        return localPort + "@" + domain;
    }
}
````

<br>

> 위 코드를 IntelliJ Convert Java File to Kotlin File로 변경하면 아래와 같다 <br>
> 코틀린 클래스는 주 생성자 안에 프로퍼티를 선언하기에 더 간결하다. <br>
> 주 생성자 구문은 편리하고, 가독성을 해치지 않는다. <br>
> getter 메서드는 사라진 것 처럼 보이지만, domail 프로퍼티를 선언하면 코틀린 컴파일러가 domain 필드와 getDomain()을 생성해 준다. 

```kt
class EmailAddress(
    val localPort: String, 
    val domain: String
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as EmailAddress
        return localPort == that.localPort && domain == that.domain
    }

    override fun hashCode(): Int {
        return Objects.hash(localPort, domain)
    }

    override fun toString(): String {
        return "$localPort@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atIndex = value.lastIndexOf('@')
            if (atIndex < 1 || atIndex == value.length - 1) {
                throw IllegalAccessException(
                    "EmailAddress must be two parts separated by @"
                )
            }
            return EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
            )
        }
    }
}
```

```java
// 다음과 같이 getDomail() 메서드 사용이 가능하다.
public class Marketing {
    public static boolean isHotmailAddress(EmailAddress address) {
        return address.getDomain().equalsIgnoreCase("hotmail.com" );
    }
}
```


<br>


> 코틀린으로 클래스를 변경함으로써 14줄을 줄일 수 있었지만, 아직 끝이 아니다. <br>
> 클래스 앞에 data를 붙이면 컴파일러가 equals, hashCode, toString 메서드를 자동으로 생성해준다.

```kt
data class EmailAddress(val localPort: String, val domain: String) {

    override fun toString(): String {
        return "$localPort@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atIndex = value.lastIndexOf('@')
            if (atIndex < 1 || atIndex == value.length - 1) {
                throw IllegalAccessException(
                    "EmailAddress must be two parts separated by @"
                )
            }
            return EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
            )
        }
    }
}
```


<br>
<hr>


## 데이터 클래스의 한계

> 데이터 클래스의 단점은 캡슐화를 제공하지 않는다는 점이다. <br>
> 컴파일러가 데이터 클래스의 copy 메서드를 생성하는데, 이 메서드는 모든 프로퍼티 값을 그대로 복사한 새 객체를 생성하되 원하면 일부를 다른 값으로 대치할 수 있다. <br>
> copy 메서드는 편리하지만, 불변 조건을 유지해야 하는 경우에도 불변 조건을 깰 수 있다.

```kt
val postmasterEmail = customerEmail.copy(localPart = "postmaster")
```

<br>

> 다음은 간단한 Money 클래스이다

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
        return amount.equals(money.amount)
                && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
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
        }
        return new Money(this.amount.add(that.amount), this.currency);
    }
}
```

- 위 클래스는 생성자가 private이다. 다른 클래스들은 Money.of를 호출해 Money 값을 얻어야 한다.
- Money 값은 자바빈 관습에 따라 amount와 currency 프로퍼티를 노출한다.
- equals와 hashCode가 구현되어 있다.
- toString 메서드는 프로퍼티 값을 표현하되, 사용자가 볼 수 있는 표현을 return한다.
- 통화 값을 계산할 수 있는 연산을 제공한다.


<br>


> Money를 코틀린으로 변환하면 아래와 같다

```kt
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

<br>

> 위 코드를 더 간결하게 만들기 위해 data class로 만든다면 <br>
> 'private 기본 생성자가 'data' 클래스의 생성된 'copy()' 메서드를 통해 노출됩니다.'의 경고문이 뜬다. <br>
> 불변 조건을 지키기 위해 private 생성자를 두고 Money.of() 메서드로만 접근하게 만들었지만 <br>
> copy 메서드는 항상 public이기 때문에 불변 조건을 어기는 새 Money 값이 만들어 질 수 있다.


<br>
<hr>


## 다음으로 나아가기

> 값 타입이 불변 조건을 유지해야 하거나 내부 표현을 캡슐화해야 한다면 데이터 클래스가 적합하지 않다. <br>
> EmailAddress와 Money는 여전히 자바스럽지만 다음 챕터에서 더 간결하고, 타입 안전하게 변경될 것이다. 
