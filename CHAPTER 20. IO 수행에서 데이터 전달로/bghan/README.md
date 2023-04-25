# I/O 수행에서 데이터 전달로

## 20.2 I/O에서 데이터로

- 매개 변수를 통해 할당 받는 구조로 변경<br>
&rArr; input을 받기 위한 객체인 `Reader` 에서 input인 `List<String>` 으로 변경

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
@Throws(IOException::class)
fun generate(reader: Reader, writer: Writer) {
    val valuableCustomers = reader
        .readLines()
        .toValuableCustomers()
        .sortedBy(CustomerData::score)
    
    writer.appendLine("ID\tName\tSpend")
    
    for (customerData in valuableCustomers) {
        writer.appendLine(customerData.outputLine)
    }
    
    writer.append(valuableCustomers.summarised())
}
```
</td>
<td>

```kotlin
@Throws(IOException::class)
fun generate(writer: Writer, lines: List<String>) {
    val valuableCustomers = lines
        .toValuableCustomers()
        .sortedBy(CustomerData::score)
    
    writer.appendLine("ID\tName\tSpend")
    
    for (customerData in valuableCustomers) {
        writer.appendLine(customerData.outputLine)
    }
    
    writer.append(valuableCustomers.summarised())
}
```
</td>
</tr>
<tr>
<td>

```kotlin
import java.io.StringWriter

private fun check(
    inputLines: List<String>,
    expectedLines: List<String>
) {
    val output = StringWriter()
    val reader = StringReader(inputLines.joinToString("\n"))
    
    generate(reader, output)
    
    val outputLines = output.toString().lines()
    
    assertEquals(expectedLines, outputLines)
}
```
</td>
<td>

```kotlin
import java.io.StringWriter

private fun check(
    inputLines: List<String>,
    expectedLines: List<String>
) {
    val output = StringWriter()
    
    generate(output, inputLines)
    
    val outputLines = output.toString().lines()
    
    assertEquals(expectedLines, outputLines)
}
```
</td>
</tr>
</table>

- `Writer` 객체의 상태를 변경하는 로직에서 `List<String>` 을 반환하는 로직으로 변경

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
writer.appendLine("ID\tName\tSpend")

for (customerData in valuableCustomers) {
    writer.appendLine(customerData.outputLine)
}

writer.append(valuableCusomers.summarised())
```
</td>
<td>

```kotlin
val resultLines = listOf("ID\tName\tSpend") +
    valuableCustomers.map(CustomerData::outputLine) +
    valuableCustomers.summarised()
```
</td>
</tr>
<tr>
<td>

```kotlin
import java.io.StringWriter

private fun check(
    inputLines: List<String>,
    expectedLines: List<String>
) {
    val output = StringWriter()
    output.append(generate(inputLines).joinToString("\n"))
    
    val outputLines = output.toString().lines()
    assertEquals(expectedLines, outputLines)
}
```
</td>
<td>

```kotlin
private fun check(
    inputLines: List<String>,
    expectedLines: List<String>
) {
    assertEquals(expectedLines, generate(inputLines))
}
```
</td>
</tr>
</table>

## 20.3 효율적인 쓰기

- `generate` 메서드가 `Reader` 대신 `List<String>` 를 받게 됐을 때 아래와 같이 리팩토링됨

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
import java.io.InputStreamReader
import java.io.OutputStreamWriter

fun main() {
    InputStreamReader(System.`in`).use { reader ->
        OutputStreamWriter(System.out).use { writer ->
            generate(reader, writer)
        }
    }
}
```
</td>
<td>

```kotlin
fun main() {
    System.`in`.reader().use { reader ->
        System.out.writer().use { writer ->
            writer.append(
                generate(
                    reader.readLines()
                ).joinToString("\n")
            )
        }
    }
}
```
</td>
</tr>
</table>

> "출력의 각 줄과 이들을 합친 문자열이 동시에 메모리를 차지한다." &rarr; 왜..?<br>
> 이를 위해 List &rarr; Sequence 리팩토링

- `Iterable`, `Sequence` 모두 `joinToString` 함수 정의되어 있어 `main` 메서드 변경 필요 x

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun generate(lines: List<String>): List<String> {
    val valuableCustomers = lines
        .withoutHeader()
        .map(String::toCustomerData)
        .filter { it.score >= 10 }
        .sortedBy(CustomerData::score)
    
    return listOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) + 
            valuableCustomers.summarised()
}
```
</td>
<td>

```kotlin
fun generate(lines: List<String>): List<String> {
    val valuableCustomers = lines
        .withoutHeader()
        .map(String::toCustomerData)
        .filter { it.score >= 10 }
        .sortedBy(CustomerData::score)
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) + 
            valuableCustomers.summarised()
}
```
</td>
</tr>
</table>

- 효율적인 메모리 사용을 위해, 효율적인 문자열 계산을 위해 `joinToString` 메서드 리팩토링

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun main() {
    System.`in`.reader().use { reader ->
        System.out.writer().use { writer ->
            generate(
                reader.readLines()
            ).forEach { line ->
                writer.appendLine(line)
            }
        }
    }
}
```
</td>
<td>

```kotlin
fun main() {
    System.`in`.reader().use { reader ->
        System.out.writer().use { writer ->
            writer.appendLines(
                generate(reader.readLines())
            )
        }
    }
}

fun Writer.appendLines(lines: Sequence<CharSequence>): Writer {
    return this.also {
        lines.forEach(this::appendLine)
    }
}
```
</td>
</tr>
</table>

## 20.4 효과적인 읽기

- `Sequence` 객체를 통해 stream 중간 연산 메모리 개선

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun generate(lines: List<String>): List<String> {
    val valuableCustomers = lines
        .withoutHeader()
        .map(String::toCustomerData)
        .filter { it.score >= 10 }
        .sortedBy(CustomerData::score)
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) + 
            valuableCustomers.summarised()
}
```
</td>
<td>

```kotlin
val valuableCustomers: List<CustomerData> = lines
    .asSequence()
    .constrainOnce()
    .withoutHeader()
    .map(String::toCustomerData)
    .filter { it.score >= 10 }
    .sortedBy(CustomerData::score)
    .toList()
```
</td>
</tr>
<tr>
<td>

```kotlin
fun main() {
    System.`in`.reader().use { reader ->
        System.out.writer().use { writer ->
            writer.appendLines(
                generate(
                    reader.readLines().asSequence().constrainOnce()
                )
            )
        }
    }
}

val valuableCustomers: List<CustomerData> = lines
    .withoutHeader()
    .map(String::toCustomerData)
    .filter { it.score >= 10 }
    .sortedBy(CustomerData::score)
    .toList()
```
</td>
<td>

```kotlin
fun main() {
    System.`in`.reader().use { reader ->
        System.out.writer().use { writer ->
            writer.appendLines(
                generate(
                    reader.buffered().lineSequence()
                )
            )
        }
    }
}
```
</td>
</tr>
</table>

#### [constrainOnce()](https://runebook.dev/ko/docs/kotlin/api/latest/jvm/stdlib/kotlin.sequences/constrain-once) 란?

> 한번만 동작할 수 있도록 보장

#### [buffered()](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/java.io.-reader/buffered.html) 란?

> `BufferedReader` 객체 반환

[왜 BufferedReader가 Reader보다 문자열 처리 속도가 빠를까?](https://soopeach.tistory.com/257)

> Buffer가 가득 차거나 줄 바꿈이 나타날 때 Buffer flush 하기 때문