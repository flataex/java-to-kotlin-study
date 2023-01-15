# 간단한 값 타입
클래스는 자바에서 코드를 조직하는 기본 단위입니다.

자바 클래스를 어떻게 코틀린으로 변환할 수 있고 코틀린 클래스와 자바 클래스가 있을 때 그들의 차이는 무엇일까요?

## 코틀린으로 변환할 자바 클래스 만들기

다음과 같이 이메일 주소를 담는 VO가 있다고 가정해봅시다.
```java
public class EmailAddress {

    // ? 1. 값은 불변이기 때문에 필드를 final로 선언
    private final String localPart;
    private final String domain;

    // ? 2. 문자열을 파싱해 EmailAddress를 만드는 parse라는 정적 팩터리 메서드
    public static EmailAddress parse(String value) {
        var atIndex = value.lastIndexOf('@');
        if (atIndex < 1 || atIndex == value.length() - 1) {
            throw new IllegalArgumentException("EmailAddress must be two parts separated by @");
        }
        return new EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
        );
    }

    // ? 3.필드는 생성자에서 초기화
    public EmailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

    // ? 4. 클래스의 프로퍼티를 구성하는 접근자 메서드는 자바빈의 명명 규칙으로 사용
    public String getLocalPart() {
        return localPart;
    }

    public String getDomain() {
        return domain;
    }

    // ? 5. equals와 hashcode를 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return Objects.equals(localPart, that.localPart) && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPart, domain);
    }

    // ? 6. 전자 우편 형식으로 반환
    @Override
    public String toString() {
        return localPart + "@" + domain;
    }
}
```

위의 코드에서는 @Nullable 어노테이션이나 Null을 검사하는 구문이 존재하지 않으므로 널 가능성이 존재합니다.

이로인해 null로 인해 발생할 수 있는 다양한 문제들로 프로그래머가 혼란을 겪을 수 있습니다.

## 코틀린으로 코드를 변경해보기

인텔리제이에서 자바 코드를 코틀린으로 자동변경하는 기능을 제공합니다.

IntelliJ 기준으로 자바 파일을 오른쪽으로 클릭하고 `Convert Java File to Kotlin File` 버튼을 선택하면 다음과 같이 변경됩니다.

```kotlin
class EmailAddress(val localPart: String, val domain: String) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
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
        fun parse(value: String): EmailAddress {
            val atIndex = value.lastIndexOf('@')
            require(!(atIndex < 1 || atIndex == value.length - 1)) { "EmailAddress must be two parts separated by @" }
            return EmailAddress(
                    value.substring(0, atIndex),
                    value.substring(atIndex + 1)
            )
        }
    }
}
```

### 자바와 코틀린의 코드 비교
위의 코드를 보면 코틀린 클래스는 주 생성자 안에서 프로퍼티를 선언하기 때문에 한눈에 알 수 잇을 정도로 더 간결합니다.
```kotlin
class EmailAddress(val localPart: String, val domain: String) {
```

파라미터 앞에 val이 붙어있으면 프로퍼티로 취급되며 다음과 같은 자바 코드에 해당합니다.
```java
private final String localPart;
private final String domain;

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
```

표준 자바 코딩 컨벤션을 지키는 자바 클래스들은 다음과 같은 순서를 지키면 작성합니다.
- 클래스 이름, 상위 클래스, 인터페이스
- 클래스 본문
  - 필드
  - 생성자
  - 메서드

자바에 익숙한 프로그래머는 이런 순서를 통해 클래스를 대충보더라도 원하는 특징을 빠르게 찾을 수 있게 됩니다.

이에 반해 자바를 오래 사용한 프로그래머가 코틀린 클래스에서 원하는 부분을 찾는 것이 어려울 수 있습니다.

코틀린 클래스는 다음과 같은 순서로 정의됩니다.
- 클래스 이름
- 주 생성자(이 안에는 파라미터와 프로퍼티 정의가 있을 수 있습니다.)
- 상위 클래스(상위 클래스 뒤에 괄호를 붙여서 상위 클래스 생성자를 호출할 수도 있습니다.)
- 인터페이스가 들어가 있는 헤더 부분
- 클래스 본문
  - 프로퍼티
  - 생성자
  - 메서드
  - 동반 객체

이런 상황에서 기존의 자바 개발자들은 코틀린 클래스가 더 읽기 어렵다 느껴질 수 있습니다.

## 생성자 파라미터 한줄씩 배치하기
이런 상황에서 도움이 될 수 있는 방법 중 하나는 생성자 파라미터를 다음과 같이 한줄에 하나씩 배치하는 것입니다.
```kotlin
class EmailAddress(
        val localPart: String,
        val domain: String
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
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
        fun parse(value: String): EmailAddress {
            val atIndex = value.lastIndexOf('@')
            require(!(atIndex < 1 || atIndex == value.length - 1)) {
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

## Companion과 @JvmStatic을 이용하여 테스트 깨지지 않도록 하기

다음과 같은 테스트 코드가 작성되어 있었다고 가정해봅시다.
```java
class EmailAddressTest {

    @Test
    void parsing() {
        // given
        EmailAddress want = new EmailAddress("yongcheol", "example.com");

        // when
        EmailAddress got = EmailAddress.parse("yongcheol@example.com");

        // then
        assertThat(got).isEqualTo(want);
    }

    @Test
    public void parsingFailures() {
        // given
        // when

        // then
        assertThatThrownBy(() -> EmailAddress.parse("@"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

위의 테스트 코드를 실행해보면 parse를 인식하지 못하면서 컴파일 에러가 발생합니다.

이런 상황에서는 @JvmStatic 어노테이션을 사용하면 테스트코드를 변경하지 않고 실행할 수 있습니다.
```kotlin
companion object {
    @JvmStatic
		fun parse(value: String): EmailAddress {
        val atIndex = value.lastIndexOf('@')
        require(!(atIndex < 1 || atIndex == value.length - 1)) {
            "EmailAddress must be two parts separated by @"
        }
        return EmailAddress(
                value.substring(0, atIndex),
                value.substring(atIndex + 1)
        )
    }
}
```
[@JvmStatic 어노테이션은 무엇인가?](https://codechacha.com/ko/kotlin-annotations/)

## 코틀린 코드에는 접근자가 없는데 어떻게 접근이 가능한 것일까?
코틀린 코드를 보면 자바 코드일때는 존재했던 getLocalPart()와 getDomain()이 사라진 것을 볼 수 있습니다.

코틀린은 기본적으로 프로퍼티를 선언하면 코틀린 컴파일러가 private 필드와 접근자와 수정자를 생성해줍니다.

이때 프로퍼티를 val로 선언하면 접근자만 생성되게 됩니다.

그리고 접근자는 자바와 같이 get을 앞에 붙이지않고 프로퍼티 이름으로 호출하게 됩니다.

```kotlin
email.localPart
```

## 클래스에 Data 붙이기
자바 코드를 코틀린으로 변환하기만 해도 기존의 자바코드 보다는 훨씬 줄일 수 있지만 코틀린에서는 언어차원에서 값 타입을 지원합니다.

코틀린에서 클래스 앞에 data 변경자(modifier)를 붙이면 사용자가 정의하지 않은 equals, hashCode, toString과 같은 메서드들이 자동으로 붙게 됩니다.

이를 적용하면 다음과 같이 코드를 줄일 수 있습니다.
```kotlin
data class EmailAddress(
    val localPart: String,
    val domain: String
) {
		// toString은 자동으로 생성되지 않도록 직접 정의
    override fun toString(): String {
        return "$localPart@$domain"
    }

    companion object {
        @JvmStatic
        fun parse(value: String): EmailAddress {
            val atIndex = value.lastIndexOf('@')
            require(!(atIndex < 1 || atIndex == value.length - 1)) {
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

코드를 보면 훨씬 깔끔해진 것을 볼 수 있습니다.

# 데이터 클래스의 한계

## 코틀린 data 클래스의 copy 메서드
데이터 클래스를 사용하면 코드를 획기적으로 줄일 수 있지만 데이터 클래스가 캡슐화를 제공하지 않는다는 점이 단점입니다.

앞에서 컴파일러가 데이트 클래스가 equals, hashCode, toString 메서드를 생성해준다는 사실을 살펴봤습니다.

이외에도 컴파일러가 데이터 클래스 객체의 모든 프로퍼티 값을 그대로 복사한 새 객체를 생성하는 copy 메서드도 생성합니다.

copy 메서드는 원하면 일부를 다른 값으로 변경할 수도 있습니다.
```kotlin
fun main() {
    val customerEmail = EmailAddress.parse("customer@mail.com")
    val postMasterEmail = customerEmail.copy(localPart = "postMaster")
    println(customerEmail)
    println(postMasterEmail)
}
```

실행 시켜보면 다음과 같은 결과가 나온다는 것을 볼 수 있습니다.
```text
customer@mail.com
postMaster@mail.com
```

상당수의 클래스에서 이런 기능은 아주 편리할 수 있습니다.

하지만 클래스가 내부 표현을 추상화하거나 프로퍼티 사이에 어떤 불변 조건을 유지해야 하는 경우에도 copy 메서드로 인해 내부 상태에 접근할 수 있어 불변 조건이 깨질 수 있습니다.

## 자바코드
```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    // ? 비공개 생성자로 정적 팩토리 메서드 of를 호출해 Money를 생성하게 만든다.
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
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency.getCurrencyCode();
    }

    // ? Money는 통화 값을 더할 수 있는 연산을 제공
    public Money add(Money that) {
        if (!this.currency.equals(that.currency)) {
            throw new IllegalArgumentException("cannot add Money values of difference currencies");
        }
        return new Money(this.amount.add(that.amount), this.currency);
    }
}
```

### 확장 함수 사용하기
위의 코드에서 `BigDecimal.setScale` 이라는 메서드를 사용하고 있습니다.

이는 자바 Beans 규약에서 사용하는 수정자 네이밍 규칙으로 좀 더 의미에 맞도록 확장함수를 사용하여 이를 수정할 수 있습니다.
```kotlin
func BigDecimal.withScale(int scale, RoundingMode mode) = setScale(scale, mode)
```

## 코틀린 변환 코드

```kotlin
class Money
private constructor(val amount: BigDecimal, val currency: Currency) {

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
        require(currency == that.currency) { "cannot add Money values of difference currencies" }
        return Money(amount.add(that.amount), currency)
    }

    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money {
            return Money(
                amount.setScale(currency.defaultFractionDigits),
                currency
            )
        }
    }
}
```

코틀린으로 변환된 클래스를 보면 생성자 앞에 private이 붙어있는 것을 볼 수 있습니다.

현재 클래스를 data 클래스로 변경하려고 하면 다음과 같은 경고 메세지가 나오는 것을 볼 수 있습니다.
```kotlin
Private primary constructor is exposed via the generated 
'copy()' method of a 'data' class.
```

Money 클래스의 경우 of 메서드를 통해 데이터를 정제하고 값을 집어넣는데 data 클래스로 만들면 copy를 통해 임의의 값으로 생성할 수 있게 됩니다.

이렇게 프로퍼티가 불변 조건을 유지해야 하는 값 타입을 데이터 클래스에 사용해 정의하지 않아야 합니다.

# 정리
- 자바 코드를 코틀린으로 변환하면 더 깔끔하고 안전한 코드를 작성할 수 있습니다.
  - null에 안전하게 작성할 수 있다.
  - 불변 클래스를 쉽게 만들 수 있다.
- 코틀린 Data 클래스를 통해 쉽고 빠르게 생성할 수 있고 값 의미론을 가지는 클래스를 만들 수 있다.
- 값 타입이 불변 조건을 유지해야 하거나 내부 캡슐화가 필요하다면 데이터 클래스는 적합하지 않다.
