package banking;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String cardNumber;
    private String pin;
    private int balance;

    private Card() {

        this.cardNumber = Card.generateCardNumber();
        this.pin = IntStream.range(0, 4)
                .map(i -> (int) Math.floor(Math.random() * 10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
        this.balance = 0;

        System.out.printf("Your card has been created\n" +
                "Your card number:\n" +
                "%s\n" +
                "Your card PIN:\n" +
                "%s\n", this.cardNumber, this.pin);
    }

    private Card(CardBuilder cardBuilder) {

        this.cardNumber = cardBuilder.cardNumber;
        this.pin = cardBuilder.pin;
        this.balance = cardBuilder.balance;

    }

    public static Card createCard() {
        return new Card();
    }

    private static String generateCardNumber() {

        long result;

        do {
            final long BIN = 400000;

            final long customerNumber = 100_000_000L +
                    (long) Math.floor(Math.random() * (999_999_999L - 100_000_000L + 1));

            final long luhnSum = getLuhnSum(Long.parseLong(new StringBuffer()
                    .append(BIN)
                    .append(customerNumber)
                    .toString()));

            final long checkSum = 10L - (long) (luhnSum % 10L);
//            System.out.println(luhnSum);
//            System.out.println((long) (luhnSum % 10L));
//            System.out.println(10L - (long) (luhnSum % 10L));

            result = Long.parseLong(String.valueOf(BIN) +
                    customerNumber +
                    checkSum);

//            System.out.println(getLuhnSum(result));

        } while (!checkLuhn(result));

        return Long.toString(result);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public boolean login(String pin) {
        return this.pin.equals(pin);
    }

    public int getBalance() {
        return this.balance;
    }

    public String getPin() {
        return pin;
    }

    static boolean checkLuhn(long cardNumber) {

        int luhnSum = getLuhnSum(cardNumber);

        return luhnSum % 10 == 0;

    }

    static int getLuhnSum(long cardNumber) {
        AtomicBoolean counter = new AtomicBoolean(true);
        return String.valueOf(cardNumber)
                .chars()
                .map(Character::getNumericValue)
                .map(i -> {
                    return counter.getAndSet(!counter.get()) ? i * 2 : i;
                })
                .map(i -> i > 9 ? i - 9 : i)
                .sum();
    }

    // Builder pattern to generate Card object instance from database
    public static class CardBuilder {

        private final String cardNumber;
        private String pin;
        private int balance;

        public CardBuilder(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public CardBuilder pin(String pin) {
            this.pin = pin;
            return this;
        }

        public CardBuilder balace(int balance) {
            this.balance = balance;
            return this;
        }

        public Card build() {
            return new Card(this);
        }


    }

}
