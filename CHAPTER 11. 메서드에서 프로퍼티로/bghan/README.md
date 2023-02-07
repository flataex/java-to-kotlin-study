# 메서드에서 프로퍼티로

## 11.1 필드, 접근자, 프로퍼티

> 코틀린은 접근자 메서드만 지원 &rarr; 필드 직접 접근 지원 x(= 필드와 접근자 별도 정의 x)

```kotlin
data class PersonWithProperties(
    val givenName: String,
    val familyName: String,
    val dateOfBirth: LocalDate
) {
    fun accessField(person: PersonWithProperties): String =
        person.givenName

    val fullName get() = "$givenName $familyName"
    
    fun age() = Period.between(dateOfBirth, LocalDate.now()).years
    
    val hash: ByteArray =
        someSlowHashOf(givenName, familyName, dateOfBirth.toString())
    
    val hash: ByteArray by lazy {
        someSlowHashOf(givenName, familyName, dateOfBirth.toString())
    }
    
    fun hash() = hash
}
```

## 11.2 어떻게 선택해야 할까?

#### [kotlin lateinit vs by lazy](https://holika.tistory.com/entry/%EB%82%B4-%EB%A7%98%EB%8C%80%EB%A1%9C-%EC%A0%95%EB%A6%AC%ED%95%9C-Kotlin-lateinit%EA%B3%BC-by-lazy%EC%9D%98-%EC%B0%A8%EC%9D%B4%EC%A0%90)

<table>
<tr>
<td align="center">lateinit</td><td align="center">by lazy</td>
</tr>

<tr>
<td align="center">var</td><td align="center">val</td>
</tr>

<tr>
<td>

```kotlin
lateinit var x : String
x = "Initialized"
println(x)
```
</td>

<td>

```kotlin
val x : String by lazy { "Initialized!" }
println(x)
```
</tr>
</table>

## 11.4 프로퍼티로 리팩터링하기

<table>

<tr>
<td>java</td><td>kotlin</td>
</tr>

<tr>
<td>

```java
public class CampSite {
    private final String id;
    private final String name;
    private final Address address;
    
    public CampSite(
            String id,
            String name,
            Address address
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCountryCode() {
        return address.getCountryCode();
    }
}
```
</td>

<td>

```kotlin
data class CampSite(
    val id: String,
    val name: String,
    val address: Address
) {
    val countryCode: String
        get() = address.countryCode

    fun region(): String {
        return address.region
    }

    // 위와 동일
    val region: String
        get() {
            return address.region
        }

    // 위와 동일
    val region: String get() = address.region
}
```
</td>
</tr>

<tr>
<td>

```java
public class CampSites {
    public static Set<CampSite> sitesInRegion(
            Set<CampSite> sites,
            String countryISO,
            String region
    ) {
        return sites
                .stream()
                .filter(campSite ->
                        campSite.getCountryCode().equals(countryISO) &&
                                campSite.region().equalsIgnoreCase(region)
                )
                .collect(toUnmodifiableSet());
    }
}
```
</td>

<td>

```kotlin
object CampSites {
    fun sitesInRegion(
        sites: Set<CampSite>,
        countryISO: String,
        region: String?
    ): Set<CampSite> {
        return sites
            .stream()
            .filter { campSite: CampSite ->
                campSite.countryCode == countryISO &&
                        campSite.region.equals(region, ignoreCase = true)
            }
            .collect(Collectors.toUnmodifiableSet())
    }
    
    fun Set<CampSite>.sitesInRegion(
        countryISO: String,
        region: String
    ): Set<CampSite> {
        return stream()
            .filter { campSite: CampSite ->
                campSite.countryCode == countryISO &&
                        campSite.region.equals(region, ignoreCase = true)
            }
            .collect(Collectors.toUnmodifiableSet())
    }
    
    fun Iterable<CampSite>.sitesInRegion(
        countryISO: String,
        region: String
    ): Set<CampSite> =
        filter { site -> 
            site.countryCode == countryISO &&
                    site.region.equals(region, ignoreCase = true)
        }.toSet()
    
    fun Iterable<CampSite>.sitesInRegion(
        countryISO: String,
        region: String
    ): Set<CampSite> =
        filter { site -> 
            site.isIn(countryISO, region)
        }.toSet()
    
    fun CampSite.isIn(countryISO: String, region: String) =
        countryCode == countryISO &&
                this.region.equals(region, ignoreCase = true)
    
    fun CampSite.isIn(countryISO: String, region: String? = null) =
        when (region) {
            null -> countryCode == countryISO
            else -> countryCode == countryISO &&
                    region.equals(this.region, ignoreCase = true)
        }
    
    fun CampSite.isIn(countryISO: String, region: String? = null) =
        countryCode == countryISO &&
                region?.equals(this.region, ignoreCase = true) ?: true
}
```
</td>

</tr>
</table>