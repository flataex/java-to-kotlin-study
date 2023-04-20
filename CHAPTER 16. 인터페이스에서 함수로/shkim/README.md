> 자바에서는 기능을 정의하는 코드와 기능이 필요한 코드 사이에 계약을 지정하기 위해 인터페이스를 사용한다. <br>
> 인터페이스는 계약의 두 당사자를 서로 결합시키기 때문에 소프트웨어가 더 유지 보수하기 어려워진다. <br> 
> 함수 타입이 이 문제를 어떻게 해결할 수 있을까?

<br>
<hr>

```kt
data class Email(
    val to: EmailAddress, 
    val from: EmailAddress, 
    val subject: String, 
    val body: String
)
```

```kt
fun sendEmail( 
    email: Email,
    serverAddress: InetAddress, 
    username: String,
    password: String
) { 
    
}
```

만약 이메일을 보내는 코드를 작성한다면 이런식일 것이다.

다만 클라이언트 입장에서는 이메일을 보내는 곳에서 송신 서버의 호스트, 보안 credential 같은 요소를 알아야 하고, 직접 넘겨야 해서 불편하다.

<br>

## 객체 지향 캡슐화

객체 지향 언어에서는 이를 위해 데이터를 캡슐화 할 수 있을 것이다.

여기서 fun interface를 정의하면, 메서드가 하나뿐인 객체를 선언하는 대신 람다를 사용해 인터페이스의 유일한 연산을 정의할 수 있다.

```kt
fun interface ISendEmail { 
    fun send(email: Email)
}

fun createEmailSender( 
    serverAddress: InetAddress, 
    username: String,
    password: String
) = ISendEmail { email -> 
    sendEmail(
        email, 
        serverAddress, 
        username, 
        password
    ) 
}
```

<br>

## 함수형 캡슐화

아래와 같은 함수를 얻으려고 시도했지만
```kt
fun send(email: Email) {
    ...
}
```

실제로는 메시지를 보내기 위해선 다른 정보들도 필요하다.
```kt
fun sendEmail( 
    email: Email,
    serverAddress: InetAddress, 
    username: String,
    password: String
) {
    ...
}
```

<br>

함수형 용어로 이를 **부분 적용**에 속한다. <br>
함수의 인자 중 일부를 고정시키면서 인자가 더 적은 새 함수를 만들어내는 기법이다.

우리는 설정 정보를 파라미터로 받아서 전자 우편을 보내는 방법을 알고 있는 함수를 반환하는 함수를 원한다.

```kt
fun createEmailSender( 
    serverAddress: InetAddress, 
    username: String,
    password: String
): (Email) -> Unit = 
    { email ->
        sendEmail( email,
            serverAddress, username, password
        ) 
    }
```

createEmailSender는 sendEmail을 호출하는 람다를 반환하는 함수이다. <br>
이 람다는 email을 유일한 인자로 받고, 다른 설정 정보를 createEmailSender의 파라미터로부터 가져 와서 sendEmail에 설정한다.

<br>

```kt
// 설정을 알 수 있는 곳
val sender: (Email) -> Unit = createEmailSender( 
    inetAddress("smtp.travelator.com"), 
    "username",
    "password"
)

// 메시지를 보내는 곳
fun sendThanks() {
    sender(
        Email(
            to = parse("support@internationalrescue.org"),
            from = parse("support@travelator.com"),
            subject = "Thanks for your help",
            body = "..."
        )
    )
}
```

<br>

## 자바의 함수 타입

자바는 함수 타입을 가리키기 위해 원하는 시그니처의 **단일 추상 메서드 (Single Abstract Method)** 인터페이스를 사용한다. <br>
자바 람다는 SAM 인터페이스를 구현하기 위해 허용되는 특별한 문법이다. <br>
자바 런타임에는 Consumer, Supplier. Function. BiFunction, Predicate 등 역할에 따라 정의된 여러 SAM 인터페이스가 있다.

```java
// 설정을 알 수 있는 곳
Consumer<Email> sender = createEmailSender(
        inetAddress("example.com"),
        "username",
        "password"
);

// 메시지를 보내는 곳
public void sendThanks() { 
    sender.accept(
        new Email(
            parse("support@internationalrescue.org"), 
            parse("support@travelator.com"),
            "Thanks for your help",
        ) 
    );
}
```

createEmailSender를 람다를 사용해 구현할 수도 있다.

```java
static Consumer<Email> createEmailSender(
    InetAddress serverAddress,
    String username.
    String password
) {
    return email -> sendEmail(
        email, 
        serverAddress, 
        username, 
        password
    );
}
```

다만 createEmailSender가 반환하는 (Email) -> Unit 타입의 결과를 Consumer<Email> 타입의 변수에 대입할 수는 없다. <br>
자바와 코틀린을 함수 수준에서 혼합해 사용하기 위해선 한쪽을 감싸야 한다. 

<br>

## 믹스 앤드 매치

객체 지향의 경우 필드가 설정을 운반하고 클라이언트가 메서드를 호출한다. <br>
함수형의 경우 함수가 설정을 클로저에 포획하고, 클라이언트는 함수를 호출한다.

이런 접근 방법을 하나로 합쳐, 함수를 받는 클라이언트에 객체 지향 구현을 넘기거나 <br>
객체 지향 구현을 받는 클라이언트에게 함수를 넘길 수 있다.

```kt
class EmailSender(
    ...
) : ISendEmail,
    (Email) -> Unit {
    override operator fun invoke(email: Email) =
        send(email)

    override fun send(email: Email) {
        sendEmail(
            email, 
            serverAddress, 
            username, 
            password
        )
    }
}
```

이렇게 하면 함수가 필요한 위치에서 클래스 기반의 송신기를 사용할 수 있다.

```kt
// 설정을 알 수 있는 곳
val sender: (Email) -> Unit = createEmailSender( 
    inetAddress("smtp.travelator.com"), 
    "username",
    "password"
)

// 메시지를 보내는 곳
fun sendThanks() {
    sender(
        Email(
            to = parse("support@internationalrescue.org"),
            from = parse("support@travelator.com"),
            subject = "Thanks for your help",
            body = "..."
        )
    )
}
```

여기서 ISendEmail은 (Email) -> Unit 타입의 함수와 동등하다. <br>
ISendEmail이 하는 일은 이 함수를 호출할 때 send라는 이름을 부여하는 것 뿐이다. <br>

타입 별명을 사용해 다음과 같이 사용할 수 있다.

```kt
typealias EmailSenderFunction = (Email) -> Unit

class EmailSender(
    ...
) : EmailSenderFunction {
    override operator fun invoke(email: Email) =
        send(email)

    override fun send(email: Email) {
        sendEmail(
            email,
            serverAddress,
            username,
            password
        )
    }
}
```

<br>

## 결합

의존 관계를 ISendEmail의 구현으로 표현하는 방법과 (Email) -> Unit 타입의 함수 구현으로 표현하는 것 사이의 차이는 클라이언트와 구현 사이의 결합에 있다.

ISendEmail은 어딘가에 정의되어 있어야 한다. <br>
구현하는 쪽은 이 인터페이스에 의존해야 하기에 클라이언트가 이 인터페이스를 정의할 수 없고, <br>
클라이언트는 구현에도 의존해야 하기에 순환적인 의존성이 생긴다.

클라이언트와 구현이 인터페이스에 따라 결합되기 때문에 시스템에 대한 리팩토링과 추론이 힘들어진다.








