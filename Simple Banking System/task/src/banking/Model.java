package banking;

import java.util.Optional;

public class Model {

    // Singleton
    private static Model instance;
    private final Database database;

    private Model() {
        this.database = SQLiteDatabase.getInstance();
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void requestNewCard() {
        Card card;
        boolean duplicateCardNumberCheck;

        do {

            card = Card.createCard();
            duplicateCardNumberCheck = database.contains(card.getCardNumber());

        } while (duplicateCardNumberCheck);

        this.database.save(card);
    }

    public Optional<Card> findCard(String cardNumber) {
        return database.find(cardNumber);
    }

    public boolean login(Optional<Card> card, String pin) {
        return card.map(optCard -> optCard.login(pin)).orElse(false);
    }

    public int deposit(String cardNumber, int deposit) {
        return database.deposit(cardNumber, deposit);
    }

    public boolean contains(String cardNumber) {
        return database.contains(cardNumber);
    }

    public boolean transfer(String payFromCardNumber, String payToCardNumber, int amount) {
        return database.transfer(payFromCardNumber, payToCardNumber, amount);
    }

    public boolean delete(String cardNumber) {
        return database.delete(cardNumber);
    }

}
