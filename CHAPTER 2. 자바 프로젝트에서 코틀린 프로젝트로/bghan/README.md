# CHAPTER 2. 자바 프로젝트에서 코틀린 프로젝트로

## 2.2 코틀린 지원을 자바 빌드에 추가하기

> 코틀린 플러그인을 추가 &rarr; gradle build 추가 &rarr; 코틀린 컴파일 가능

<table>
<tr>
<td>Java</td><td>Kotlin</td>
</tr>
<tr>
<td>

```java
plugins {
    id（"java"） 
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetcompatibility = JavaVersion.VERSION_11
... 다른 프로젝트 설정 ...

dependencies {
    implementation "com.fasterxml.jackson.core:jackson-databind:2.10.0"
    implementation "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0" 
    implementation "com.fasterxml.jackson.datatype:jackson-datatype-jdk8：2.10.0" 
    ... 앱의 나머지 구현 의존 관계
    
    testimplementation "org.junit.jupiter：junit-jupiter-api：5.4.2" 
    testlmplementation "org.junit.jupiter：junit-jupiter-params:5.4.2" 
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.5.2" 
    testRuntimeOnly "org.junit.platform:junit-platform-launcher:1.4.2"
    ... 앱의 나머지 테스트 의존 관계
}

... 나머지 빌드 규칙
```

</td>

<td>

```kotlin
plugins {
    id 'org.jetbrains.kotlin.jvm' version "1.5.0" 
}

java.sourcecompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
... 다른 프로젝트 설정 ...

dependencies {
    implementation "org.jetbrains.kotlin：kotlin-stdlib-jdk8"
    ... 앱의 나머지 의존 관계 
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile) { 
    kotlinOptions { 
        jvmTarget = "11"
        javaParameters = true
        freeCompilerArgs = ["-Xjvm-default=aU"] 
    }
}

... 나머지 빌드 규칙
```

</td>
</tr>
</table>

```kotlin
fun main() {
    println("hello, world")
}
```

```shell
$ java -cp build/classes/kotlin/main HeeloWorldKt
hello, world
```