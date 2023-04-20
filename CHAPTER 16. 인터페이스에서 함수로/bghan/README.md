# 인터페이스에서 함수로

```kotlin
import java.net.InetAddress

fun sendEmail(
    email: Email,
    serverAddress: InetAddress,
    username: String,
    password: String
) {
    ...
}
```

- 클라이언트 입장에서 이메일 보내기 위한 설정을 알아야 하는 단점 존재

## 16.1 객체 지향 캡슐화

```kotlin

import java.net.InetAddress

class EmailSender(
    private val serverAddress: InetAddress,
    private val username: String,
    private val password: String
) {
    fun send(email: Email) {
        sendEmail(
            email,
            serverAddress,
            username,
            password
        )
    }
}
```

- 이메일 보내기 위해 EmailSender 접근 필요<br>
&rArr; 이메일 보내기 위한 세부 사항 저장하고 있어 전달 필요 x

```kotlin
// 설정 알 수 있는 곳
val sender: EmailSender = EmailSender(
    inetAddress("smtp.travelator.com"),
    "username",
    "password"
)

// 메세지 보내는 곳
fun sendThanks() {
    sender.send(
        Email(
            to = parse("support@internationalrescue.org"),
            from = parse("support@travelator.com"),
            subject = "Thanks for your help",
            body = "..."
        )
    )
}
```

### 16.2 함수형 캡슐화

<table>
<tr>
<td align="center">function</td><td align="center">lambda</td>
</tr>
<tr>
<td>

```kotlin
fun createEmailSender(
    serverAddress: InetAddress,
    username: String,
    password: String
): (Email) -> Unit {
    fun result(email: Email) {
        sendEmail(
            email,
            serverAddress,
            username,
            password
        )
    }
    
    return ::result
}
```
</td>
<td>

```kotlin
fun createEmailSender(
    serverAddress: InetAddress,
    username: String,
    password: String
): (Email) -> Unit = 
        { email -> 
            sendEmail(
                email,
                serverAddress,
                username,
                password
            )
        }
```
</td>
</tr>
</table>

- 메일 전송 설정 정보를 받는 기능 vs 메일 보내는 기능 나누기

### 16.4 믹스 앤드 매치

<table>
<tr>
<td align="center">Java</td><td align="center">Kotlin</td>
</tr>
<tr>
<td>

```java
public class EmailSender implements ISendEmail, Consumer<Email> {
    
    @Override
    public void accept(Email email) {
        send(email);
    }
    
    @Override
    public void send(Email email) {
        sendEmail(email, serverAddress, username, password);
    }
}
```
</td>
<td>

```kotlin
class EmailSender (
    ...
) : ISendEmail,
    (Email) -> Unit {
        override operator fun invoke(email: Email) = 
            send(email)
    
        override fun send(email: Email) = 
            sendEmail(
                email,
                serverAddress,
                username,
                password
            )
    }
```
</td>
</tr>
</table>

- `typealias` 를 통해 의도 표현

```kotlin
typealias EmailSenderFunction = (Email) -> Unit

class EmailSender(
    ...
) : EmailSenderFunction {
    override fun invoke(email: Email) {
        sendEmail(
            email,
            serverAddress,
            username,
            password
        )
    }
}
```

- EmailSender instance 사용 방법

<table>
<tr>
<td align="center">function</td><td align="center">lambda</td>
</tr>
<tr>
<td>

```kotlin
val instance = EmailSender(
    inetAddress("smtp.travelrator.com"),
    "username",
    "password"
)

val sender: (Email) -> Unit = { instance.send(it) }
```
</td>
<td>

```kotlin
val sender: (Email) -> Unit = instance::send
```
</td>
</tr>
</table>

## 16.6 결합

> 함수형 프로그래밍에선 런타임이 모든 함수 타입 정의<br>
> &rArr; 클라이언트와 구현사이 컴파일 시점 의존성 x

## 16.7 객체 지향인가 함수형인가?

> 클라이언트의 의존성만 만족할 수 있도록 구현

```kotlin
interface EmailSystem {
    fun send(email: Email)
    fun delete(email: Email)
    fun list(folder: Folder): List<Email>
    fun move(email: Email, to: Folder)
}
```

1. only 이메일 목록 표시 기능

> 함수 타입만으로 충분한 경우 인터페이스와 결합 x

```kotlin
class Organiser(
    private val listing: (Folder) -> List<Email>
) {
    fun subjectsin(folder: Folder): List<String> {
        return listing(folder).map { it.subject }
    }
}

val emailSystem: EmailSystem = ...
val organiser = Organiser(emailSystem::list)
```

2. 이메일 목록 표시 기능 & 이메일 삭제 기능

```kotlin
class Organiser(
    private val listing: (Folder) -> List<Email>,
    private val deleting: (Email) -> Unit
) {
    fun deleteInternal(folder: Folder) {
        listing(rootFolder).forEach {
            if (it.to.isInternal()) {
                deleting.invoke(it)
            }
        }
    }
}

val organiser = Organiser(
    emailSystem::list,
    emailSystem::delete
)
```

3. 원하는 기능만 지원하는 객체 주입

<table>
<tr>
<td align="center">interface</td><td align="center">class</td>
</tr>
<tr>
<td>

```kotlin
class Organiser(
    private val emails: Depedencies
) {
    interface Dependencies {
        fun delete(email: Email)
        fun list(folder: Folder): List<Email>
        fun move(email: Email, to: Folder)
    }

    fun organise() {
        emails.list(rootFolder).forEach {
            if (it.to.isInternal()) {
                emails.delete(it)
            } else {
                emails.move(it, archiveFolder)
            }
        }
    }
}

val organiser = Organiser(object : Organiser.Dependencies {
    override fun delete(email: Email) {
        emailSystem.delete(email)
    }
    
    override fun list(folder: Folder): List<Email> {
        return emailSystem.list(folder)
    }
    
    override fun move(email: Email, to: Folder) {
        emailSystem.move(email, to)
    }
})
```
</td>
<td>

```kotlin
class Organiser(
    private val emails: Depedencies
) {
    class Dependencies(
        val delete: (Email) -> Unit,
        val list: (folder: Folder) -> List<Email>,
        val move: (email: Email, to: Folder) -> Unit
    )
    
    fun organise() {
        emails.list(rootFolder).forEach {
            if (it.to.isInternal()) {
                emails.delete(it)
            } else {
                emails.move(it, archiveFolder)
            }
        }
    }
}

val organiser = Organiser(
    Organiser.Dependencies(
        delete = emailSystem::delete,
        list = emailSystem::list,
        move = emailSystem::move
    )
)
```
</td>
</tr>
</table>

## 16.10 인터페이스에서 함수로 리팩터링하기

```java
@RequiredArgsConstructor
public class Recommendations {                                      // 총 7개 중 2개의 메서드만 사용
    private final FeaturedDestinations featuredDestinations;        // 5개의 메서드
    private final DistanceCalculator distanceCalculator;            // 2개의 메서드
}
```

### 16.10.1 함수 도입하기

```kotlin
class Recommendations(
    private val featuredDestinations: FeaturedDestinations,
    private val distanceCalculator: DistanceCalculator
) {
    fun recommendationsFor(
        journey: Set<Location>
    ): List<FeaturedDestinationSuggestion> =
        journey
            .flatmap { location -> recommendationsFor(location) }
            .deduplicated()
            .sortedBy { it.distanceMeters }
    
    fun recommendationFor(
        location: Location
    ): List<FeaturedDestinationSuggestion> = 
        featuredDestinations.findCloseTo(location)
            .map { featuredDestination -> 
                FeaturedDestinationSuggestion(
                    location,
                    featuredDestination,
                    distanceCalculator.distanceInMetersBetween(
                        location,
                        featuredDestination.location
                    )
                )
            }
}
```

1. 인터페이스 메서드로 초기화한 프로퍼티 추가

> `featuredDestinations::findCloseTo` 가리키는 인터페이스

```kotlin
class Recommendations(
    private val featuredDestinations: FeaturedDestinations,
    private val distanceCalculator: DistanceCalculator
) {
    private val destinationFinder:
            (Location) -> List<FeaturedDestination> =
        featuredDestinations::findCloseTo

    fun recommendationFor(
        location: Location
    ): List<FeaturedDestinationSuggestion> =
        destinationFinder(location)                             // property로 변경
            .map { featuredDestination ->
                FeaturedDestinationSuggestion(
                    location,
                    featuredDestination,
                    distanceCalculator.distanceInMetersBetween(
                        location,
                        featuredDestination.location
                    )
                )
            }
}
```

2. 외부로부터 메서드를 주입받도록 변경

```kotlin
class Recommendations(
    private val featuredDestinations: FeaturedDestinations,
    private val distanceCalculator: DistanceCalculator,
    private val destinationFinder:
        (Location) -> List<FeaturedDestination> =
        featuredDestinations::findCloseTo
) {
}
```

3. 필요없는 객체 주입 x

```kotlin
class Recommendations(
    private val distanceCalculator: DistanceCalculator,
    private val destinationFinder: (Location) -> List<FeaturedDestination> // 더이상 FeaturedDestination을 사용하지 않아 함수로 주입
) {
}
```

4. 외부로부터 함수를 전달하여 객체에 주입

```java
private final Recommendations recommendations = new Recommendations(
    distanceCalculator,
    featuredDestinations::findCloseTo
);
```