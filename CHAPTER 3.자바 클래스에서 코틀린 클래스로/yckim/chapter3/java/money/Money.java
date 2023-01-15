package chapter3.java.money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    // ? 비공개 생성자로 정적 팩토리 메서드 of를 호출해 Money를 생성하게 만든다.
    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(
                amount.setScale(currency.getDefaultFractionDigits()),
                currency
        );
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency.getCurrencyCode();
    }

    // ? Money는 통화 값을 더할 수 있는 연산을 제공
    public Money add(Money that) {
        if (!this.currency.equals(that.currency)) {
            throw new IllegalArgumentException("cannot add Money values of difference currencies");
        }
        return new Money(this.amount.add(that.amount), this.currency);
    }
}
