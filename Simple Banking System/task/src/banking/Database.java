package banking;

import java.util.Optional;

public interface Database {

    Optional<Card> find(String cardNumber);

    boolean contains(String cardNumber);

    int save(Card card);

    int deposit(String cardNumber, int deposit);

    boolean transfer(String payFromCardNumber, String payToCardNumber, int amount);

    boolean delete(String cardNumber);
}
