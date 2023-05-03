# 클래스에서 함수로

## 22.1 인수 테스트

```kotlin
@Disabled
@Test
fun `acceptance test`() {
    val input = listOf(
        "time,x,y",
        "0.0, 1, 1",
        "0.0,1.1,1.2",
        "0.2,1.2,1.4",
    )
    val expected = listOf(
        Measurement(0.0, 1.0, 1.0),
        Measurement(0.1, 1.1, 1.2),
        Measurement(0.2, 1.2, 1.4),
    )
    assertEquals(
        expected,
        readTable(input).map { record ->
            MeasureMent(
                t = record["time"]?.toDoubleOrNull() ?: error("in time"),
                x = record["x"]?.toDoubleOrNull() ?: error("in x"),
                y = record["y"]?.toDoubleorNull() ?: error("in y"),
            )
        }
    )
}
```

- 모든 비즈니스 로직 완성 후 인수 테스트 실행 하기에 현재는 `@Disabled`
- 인수 테스트를 빠르게 통과할 것을 기대하지 않고, **모든 일을 끝냈다는 사실** 알려줌
- 구현할 수 있는 **간단한 API 형태** 제공

## 22.2 단위 테스트

#### 1. 빈 데이터 테스트

```kotlin
class TableReaderTests {
    @Test
    fun `empty list returns empty list`() {
        val input: List<String> = emptyList()
        val expectedResult: List<Map<String, String>> = emptyList()
        
        assertEquals(
            expectedResult,
            readTable(input)
        )
    }
}

fun readTable(input: List<String>): List<Map<String, String>> {
    return emptyList()
}
```

- 로직이 복잡해질 수록 이런 간단한 케이스 실패 가능성 &uarr;
- `TDD`에 따르면 구현을 변경해야할 이유가 되는 **실패하는 테스트 먼저 추가**해야함

```kotlin
@Test
fun `one line of input with default field names`() {
    assertEquals(
        listOf(
            mapOf("0" to "field0", "1" to "field1")
        ),
        readTable(listOf(
            "field0,field1"
        ))
    )
}
```

> [indices](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/indices.html)란, Collection, Array 클래스에 선언되어 있는 property로 **Collection 타입의 index 범위** 반환 

```kotlin
fun readTable(lines: List<String>): List<Map<String, String>> {
    return if (lines.isEmpty())
        emptyList()
    else lifOf(
        mapOf("0" to "filed0", "1" to "field1")
    )
}
```

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun readTable(lines: List<String>): List<Map<String, String>> {
    return lines.map {
        mapOf("0" to "field0", "1" to "field1")
    }
}
```
</td>
<td>

```kotlin
fun readTable(lines: List<String>): List<Map<String, String>> {
    return lines.map(::parseLine)
}

private fun parseLine(line: String) = mapOf("0" to "field0", "1" to "field1")
```
</td>
</tr>
<tr>
<td>

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val keys = listOf("0", "1")
    val values = listOf("field0", "field1")
    
    return keys.zip(values).toMap()
}
```
</td>
<td>

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val values = listOf("field0", "field1")
    val keys = values.indices.map(Int::toString)
    
    return keys.zip(values).toMap()
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val values = line.split(",")
    val keys = values.indices.map(Int::toString)
    
    return keys.zip(values).toMap()
}
```
</td>
</tr>
</table>

- `split` 함수는 빈 줄이 들어왔을 때 에러 발생

```kotlin
@Test
fun `empty line returns empty map`() {
    assertEquals(
        listOf(
            emptyMap()
        ),
        readTable(listOf(
            ""
        ))
    )
}
```

```shell
org.opentest4j.AssertionFailedError:
Expected :[{}]
Actual   :[{0=}]
```

- 빈 줄 케이스 대응 위해 비즈니스 로직 개선

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>
<tr>
<td>

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val values = if (line.isEmpty()) emptyList() else line.split(",")
    val keys = values.indices.map(Int::toString)
    
    return keys.zip(values).toMap()
}
```
</td>
<td>

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val values = splitFields(line)
    val keys = values.indices.map(Int::toString)

    return keys.zip(values).toMap()
} 

private fun splitFields(line: String): List<String> =
    if (line.isEmpty()) emptyList() else line.split(",")
```
</td>
<td>

```kotlin
private fun parseLine(line: String): Map<String, String> {
    val values = line.splitFields(",")
    val keys = values.indices.map(Int::toString)

    return keys.zip(values).toMap()
} 

private fun String.splitFields(separators: String): List<String> =
    if (isEmpty()) emptyList() else split(separators)
```
</td>
</tr>
</table>

## 22.4 다른 필드 구분자

- 헤더, 본문 parsing 시 `splitFields` 호출

```kotlin
private fun headerProviderFrom(header: String): (Int) -> String {
    val headers = header.splitFields(",")
    return { index -> headers[index] }
}

private fun parseLine(
    line: String,
    headerProvider: (Int) -> String
): Map<String, String> {
    val values = line.splitFields(",")
    val keys = values.indices.map(headerProvider)
    
    return keys.zip(values).toMap()
}
```

- 세부 방식에 의존하지 않고 `(String) -> List<String>` 함수로 추상화 필요

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString
): List<Map<String, String>> =
    lines.map {
        parseLine(it, headerProvider) { line ->
            line.splitFields(",")
        }
    }

private fun parseLine(
    line: String,
    headerProvider: (Int) -> String,
    splitter: (String) -> List<String>
): Map<String, String> {
    val values = splitter(line)
    val keys = values.indices.map(headerProvider)
    
    return keys.zip(values).toMap()
}
```
</td>
<td>

```kotlin
fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString
): List<Map<String, String>> = 
    lines.map {
        parseLine(it, headerProvider, splitOnComma)
    }

val splitOnComma: (String) -> List<String> = { line -> 
    line.splitFields(",")
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> = 
    lines.map {
        parseLine(it, headerProvider, splitter)
    }

val splitOnComma: (String) -> List<String> = { line -> 
    line.splitFields(",")
}
```
</td>
</tr>
</table>

## 22.5 시퀀스

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> = 
    lines.map {
        parseLine(it, headerProvider, splitter)
    }
```
</td>
<td>

```kotlin
fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> =
    lines
        .asSequence()           // sequence로 변환
        .map {
            parseLine(it, headerProvider, splitter)
        }
        .toList()               // 기존 반환 형태 유지하기 위한 list로의 변환
```
</td>
</tr>
<tr>
<td>

```kotlin
fun readTableWithHeader(
    lines: List<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> = 
    when {
        lines.isEmpty() -> emptyList()
        else -> readTable(
            lines.drop(1).asSequence(),
            headerProviderFrom(lines.first(), splitter),
            splitter
        ).toList()
    }
```
</td>
<td>

```kotlin
fun readTableWithHeader(
    lines: List<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> {
    val linesAsSequence = lines.asSequence()
    
    return when {
        linesAsSequence.isEmpty() -> emptySequence()        // linesAsSequence가 null일 수 있어 컴파일 실패
        else -> {
            readTable(
                linesAsSequence.drop(1),
                headerProviderFrom(linesAsSequence.first(), splitter),
                splitter
            )
        }
    }.toList()
}
```
</td>
</tr>
<tr>
<td>

```kotlin
fun readTableWithHeader(
    lines: List<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> {
    val linesAsSequence = lines.asSequence()
    
    return when {
        linesAsSequence.firstOrNull() == null -> emptySequence()
        else -> {
            readTable(
                linesAsSequence.drop(1),
                headerProviderFrom(linesAsSequence.first(), splitter),
                splitter
            )
        }
    }.toList()
}
```
</td>
<td>

```kotlin
fun readTableWithHeader(
    lines: List<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> =
    readTableWithHeader(
        lines.asSequence(),
        splitter
    ).toList()

fun readTableWithHeader(
    lines: Sequence<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> {
    return when {
        lines.firstOrNull() == null -> emptySequence()
        else -> {
            readTable(
                lines.drop(1),
                headerProviderFrom(lines.first(), splitter),
                splitter
            )
        }
    }.toList()
} 
```
</td>
</tr>
</table>