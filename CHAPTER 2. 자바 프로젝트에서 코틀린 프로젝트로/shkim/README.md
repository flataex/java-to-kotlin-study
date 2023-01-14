### 우리는 자바에서 함수형 접근 방법을 배웠지만, 자바의 한계에 자주 부딪힌다.

- 불변 값 타입을 구현하기 위해 필요한 장황한 코드
- 기본형과 참조형의 분리
- null 참조
- 고차 함수 지원이 부족한 스트림


<br>
<hr>


## 코틀린을 위한 준비

```gradle
plugins {
    id 'org.jetbrains.kotlin.jvm' version "1.5.0"
}

java.sourcecompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
        
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    ...
    ...
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile) {
    kotlinOptions {
        jvmTarget = "11"
        javaParameters = true
        freeCompilerArgs = ["-Xjvm-default=aU"]
    } 
}
```

