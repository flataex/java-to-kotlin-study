> I/O는 동작이기 때문에 이에 대해 추론하거나 리팩토링하기 힘들다. <br>
> 어떻게 하면 이런 문제의 영향이 미치는 영역을 제한할 수 있을까 ?

<br>

```kt
class HighValueCustomersReportTests {
    
    @Test
    fun test() {
        check(
            inputLines = listOf(
                "ID\tFirstName\tLastNanie\tScore\tSpend",
                "l\tFred\tFlintstone\tll\tl000.00",
                "4\tBetty\tRubble\t10vt2000.00",
                "2\tBarney\tRubble\t0\t20.00",
                "3\tWilma\tFlintstone\t9\t0.00"
            ),
            expectedLines = listOf(
                "ID\tName\tSpend",
                "4\tRUBBLE, Betty\t2000.00",
                "l\tFLINTSTONE, Fred\tl000.00",
                "\tTOTAL\t3000.00"
            )
        )
    }

    private fun check(
        inputLines: List<String>,
        expectedLines: List<String>
    ) {
        val output = StringWriter() 
        generate(
            StringReader(inputLines.joinToString("\n")),
            output
        )
        assertEquals(expectedLines.joinToString("\n"), output.toString())
    }
}
```

위 테스트 코드에서 check를 살펴보면, 반환되는 값 대신 I/O에 의존하기 때문에 계산이 아니다.

<br>

```kt
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

check 메서드를 좀 바꿔보면, generate는 여전히 자신의 파라미터를 읽고 쓰는데 의존하는 동작이지만 <br>
이 동작의 부수효과가 미치는 영역을 지역변수로 제한함으로써 이를 계산으로 변환할 수 있다.

<br>
<hr>
<br>

리팩토링의 첫 단계는 generate에서 reader 파라미터를 없애는 것이다. <br>
reader 파라미터를 사용하는 부분인 reader.readLines()를 파라미터로 도입해서, generate가 list를 읽도록 바꾼다.

원래는 generate에 전달할 목적으로 StringReader를 생성했지만, 이제 Reader를 없앨 수 있다.



<table>
<th>
AS-IS
</th>
<th>
TO-BE
</th>
<tr>
<td>

```kt
@Throws(IOException::class)
fun generate(reader: Reader, writer: Writer) {
    val valuablecustomers = reader.readLines()
        .toValuableCustomers()
        .sortedBy(CustomerData::score) 
    writer.appendLine("ID\tName\tSpend")
    for (customerData in valuableCustomers) {
        writer.appendLine(customerData.outputLine) 
    }
    writer.append(valuableCustomers.summarised()) 
}
```

```kt
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

```kt
@Throws(IOException::class)
fun generate(writer: Writer, lines: List<String>) {
    val valuablecustomers = lines
        .toValuableCustomers()
        .sortedBy(CustomerData::score) 
    writer.appendLine("ID\tName\tSpend")
    for (customerData in valuableCustomers) {
        writer.appendLine(customerData.outputLine) 
    }
    writer.append(valuableCustomers.summarised()) 
}
```


```kt
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

<br>




> I/O 발생 장소를 프로그램 진입점에 가깝게 옮길수록 계산으로 처리할 수 있는 부분이 많아진다.


