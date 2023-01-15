package chapter3.java.email;

import java.util.Objects;

public class EmailAddress {

    // ? 1. 값은 불변이기 때문에 필드를 final로 선언
    private final String localPart;
    private final String domain;

    // ? 3.필드는 생성자에서 초기화
    public EmailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

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
