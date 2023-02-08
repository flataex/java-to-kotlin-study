# 함수에서 확장 함수로

## 10.1 함수와 메서드

```kotlin
data class Customer(
    val id: String,
    val givenName: String,
    val familyName: String
) {
    val fullName get() = "$givenName $familyName"

    fun nameForMarketing() = "${familyName.uppercase()}, $givenName"
    
    fun nameForMarketing(customer: Customer) = 
        "${customer.familyName.uppercase()}, ${customer.givenName}"
}
```

## 10.2 확장 함수

```kotlin
fun Customer.nameForMarketing() = "${familyName.uppercase()}, $givenName"

val s = customer.nameForMarketing()
```

## 10.3 확장 함수의 타입과 함수의 타입

```kotlin
val methodReference: (Customer.() -> String) =
    Customer::fullName

val extensionFunctionReference: (Customer.() -> String) =
    Customer::nameForMarketing

val methodAsFunctionReference: (Customer) -> String =
    methodReference

val extensionAsFunctionReference: (Customer) -> String =
    extensionFunctionReference

customer.methodReference()
customer.extensionFunctionReference()

methodAsFunctionReference(customer)
extensionAsFunctionReference(customer)

customer.methodAsFunctionReference() // Unresolved reference error
customer.extensionAsFunctionReference() // Unresolved reference error
```

## 10.4 확장 프로퍼티

```kotlin
val Customer.nameForMarketing get() = "${familyName.uppercase()}, $givenName"
```

## 10.5 변환

```kotlin
var customer = nodeToCustomer(node) // 가독성 x

var customer = createCustomer(node) // node와 customer 관계 힌트 x

var customer = toCustomer(node) // Customer를 만들기 위해 node가 필요하지만 영어 흐름 x

var customer = customerFrom(node) // good

var customer = customerFor(node) // also good
```

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>

<td>

```kotlin
var customer = customerFrom(node)
var marketingName = nameForMarketing(customer)

var marketingLength = nameForMarketing(customerFrom(node)).length() // 읽기 어려움
```
</td>

<td>

```kotlin
fun JsonNode.toCustomer(): Customer = ...

val marketingLength = jsonNode.toCustomer().nameForMarketing().legnth
```
</td>
</tr>
</table>

## 10.6 널이 될 수 있는 파라미터

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>

<td>

```kotlin
val customer: Customer? = loggedInCustomer()
val greeting: String? = when (customer) {
    null -> null
    else -> greetingForCustomer(customer)
}
```
</td>

<td>

```kotlin
val customer: Customer? = loggedInCustomer()
val greeting: String? = customer?.let {greetingForCustomer(it)}
```
</td>
</tr>
</table>

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>

<tr>

<td>

```kotlin
val reminder: String? = customer?.let {
    nextTripForCustomer(it)?.let {
        timeUntilDepartureOfTrip(it, currentTime)?.let {
            durattionToUserFriendlyText(it) + " until your next trip!"
        }
    }
}
```
</td>

<td>

```kotlin
val reminder: String? = customer
    ?.let { nextTripForCustomer(it) }
    ?.let { timeUntilDepartureOfTrip(it, currentTime) }
    ?.let { durationToUserFriendlyText(it) }
    ?.let { it + " until your next trip!" }
```
</td>

<td>

```kotlin
val reminder: String? = customer
    ?.nextTrip()
    ?.timeUntilDeparture(currentTime)
    ?.toUserFriendlyText()
    ?.plus(" until your next trip!")
```
</td>
</tr>
</table>

## 10.7 널이 될 수 있는 수신 객체

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>

<td>

```kotlin
import java.time.ZonedDateTime

fun Trip?.reminderAt(currentTime: ZonedDateTime): String? =        // 항상 null 가능한 String? 타입
    this?.timeUntilDeparture(currentTime)
        ?.toUserFriendlyText()
        ?.plus(" until your next trip!")

val reminder: String? = customer.nextTrip().reminderAt(currentTime) // 타입 검사는 통과하지만 Null 가능성 확인 어려움

val trip: Trip = ...
val reminder: String = trip.reminderAt(currentTime) ?: error("Should never happen") // 항상 null이 가능하여 체크 필요
```

</td>

<td>

```kotlin
import java.time.ZonedDateTime

fun Trip?.reminderAt(currentTime: ZonedDateTime): String =
    this?.timeUntilDeparture(currentTime)
        ?.toUserFriendlyText()
        ?.plus(" until your next trip!")
        ?: "Start planning your next trip. The world's your oyster!"
```
</td>
</tr>
</table>

## 10.8 제네릭스

```kotlin
fun <T> T.printed(): T = this.also(::println)
```

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>

<td>

```kotlin
val customer = jsonNode.toCustomer()
println(customer)

val marketingLength = customer.nameForMarketing().length
```
</td>

<td>

```kotlin
val marketingLength = jsonNode.toCustomer().printed().nameForMarketing().length
```
</td>
</tr>
</table>

## 10.9 확장 함수를 메서드로 정의하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>

<td>

```kotlin
class JsonWriter(
    private val objectMapper: ObjectMapper
) {
    fun Customer.toJson(): JsonNode = objectMapper.valueToTree(this)
}
```
</td>

<td>

```kotlin
fun Customer.toJson(): JsonNode =
    this@JsonWriter.objectMapper.valueToTree(this@toJson)
```
</td>
</tr>
</table>

## 10.10 확장 함수로 리팩터링하기

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>

<td>

```kotlin
private fun lineFor(customer: CustomerData): String {
    return customer.id + "\t" + marketingNameFor(customer) + "\t" +
            formatMoney(customer.spend)
}

private fun marketingNameFor(customer: CustomerData): String {
    return customer.familyName.toUpperCase() + ", " + customer.givenName
}
```
</td>

<td>

```kotlin
private fun CustomerData.marketingNameFor(): String {
    return familyNane.toUpperCase() + ", " + givenName
}
```
</td>
</tr>

<tr>

<td>

```kotlin
private fun lineFor(customer: CustomerData): String =
    "${customer.id}\t${customer.marketingName}\t${formatMoney(customer.spend)}"

private fun formatMoney(money: Double): String {
    return String.format("$#.2f", money)
}

private val CustomerData.marketingName: String
    get() = "${familyName.toUpperCase()}, $givenName"
```
</td>

<td>

```kotlin
private fun lineFor(customer: CustomerData): String =
    "${customer.id}\t${customer.marketingName}\t${customer.spend.toMoneyString()}"

private fun Double.toMoneyString() = String.format("$#.2f", this)
```
</td>
</tr>

<tr>
<td colspan="2">

```kotlin
private fun Double.toMoneyString() = this.formattedAs("$#.2f")

private fun Double.formattedAs(format: String) = String.format(format, this)
```
</td>
</tr>
</table>

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>
<td>

```kotlin
fun customerDataFrom(line: String): CustomerData {
    val parts = line.split("\t".toRegex()).toTypedArray()
    val spend: Double = if (parts.size == 4) 0.0 else parts[4].toDouble()
    
    return CustomerData(
        parts[0],
        parts[1],
        parts[2],
        parts[3].toInt(),
        spend
    )
}
```
</td>
<td>

```kotlin
fun customerDataFrom(line: String): CustomerData {
    val parts = line.split("\t")
    
    return CustomerData(
        id = parts[0],
        givenName = parts[1],
        familyName = parts[2],
        score = parts[3].toInt(),
        spend = if (parts.size == 4) 0.0 else parts[4].toDouble()
    )
}
```
</td>
</tr>
<tr>

<td>

```kotlin
fun customerDataFrom(line: String): CustomerData =
    line.split("\t").let { parts ->
        CustomerData(
            id = parts[0],
            givenName = parts[1],
            familyName = parts[2],
            score = parts[3].toInt(),
            spend = if (parts.size == 4) 0.0 else parts[4].toDouble()
        )
    }
```
</td>
<td>

```kotlin
fun String.customerDataFrom(line: String): CustomerData =
    split("\t").let { parts ->
        CustomerData(
            id = parts[0],
            givenName = parts[1],
            familyName = parts[2],
            score = parts[3].toInt(),
            spend = if (parts.size == 4) 0.0 else parts[4].toDouble()
        )
    }
```
</td>
</tr>
</table>

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
private fun summaryFor(valuableCustomers: List<CustomerData>): String {
    val total = valuableCustomers
        .stream()
        .mapToDouble{ (_, _, _, _, spend) -> spend }
        .sum()
    
    return "\tTOTAL\t" + total.toMoneyString()
}
```
</td>
<td>

```kotlin
private fun summaryFor(valuableCustomers: List<CustomerData>): String {
    val total = valuableCustomers.sumByDouble { it.spend }
    
    return "\tTOTAL\t${total.toMoneyString()}"
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
private fun List<CustomerData>.summarized(): String = 
    sumByDouble { it.spend }.let { total -> 
        "\tTOTAL\t${total.toMoneyString()}"
    }
```
</td>
</tr>
</table>