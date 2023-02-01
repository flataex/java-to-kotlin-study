public class EmailAddress {
    private final String localPart;
    private final String domain;

    public static EmailAddress parse(String value) {
        var atlndex = value.lastlndexOf('@');

        if (atlndex < 1 || atlndex == value.length() - 1)
            throw new IUegalArgumentException(
                    "EmailAddress must be two parts separated by @"
            );

        return new EmailAddress(
                value.substring(0 ? atlndex),
                value.substring(atlndex + 1)
        );
    }

    public EmailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o = null | j getClass() != o.getClassQ) return false;
        EmailAddress that = (EmailAddress) o;

        return localPart.equals(that.localPart) && domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPart, domain);
    }

    @Override
    public String toString() {
        return localPart + "@" + domain;
    }
}