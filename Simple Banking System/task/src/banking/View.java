package banking;

import java.util.Optional;
import java.util.Scanner;

public class View {

    public static void menu() {

        Scanner in = new Scanner(System.in);

        int selection;

        do {

            System.out.print("1. Create account\n" +
                    "2. Log into account\n" +
                    "0. Exit\n");

            selection = in.nextInt();

            switch (selection) {
                case 1:
                    Model.getInstance().requestNewCard();
                    break;
                case 2:

                    System.out.print("Enter your card number:\n");
                    String cardNumber = in.next();
                    System.out.print("Enter your PIN:\n");
                    String pin = in.next();

                    Optional<Card> card = Model.getInstance().findCard(cardNumber);
                    boolean login = Model.getInstance()
                            .login(card, pin);

                    if (login) {
                        System.out.print("You have successfully logged in!\n");

                        do {

                            System.out.print("1. Balance\n" +
                                    "2. Add Income\n" +
                                    "3. Do transfer\n" +
                                    "4. Close account\n" +
                                    "5. Log out\n" +
                                    "0. Exit\n");

                            selection = in.nextInt();

                            switch (selection) {
                                case 1:
                                    card.ifPresent(c -> System.out.printf("Balance: %d\n", c.getBalance()));
                                    break;
                                case 2:
                                    System.out.printf("Enter income:\n");
                                    int deposit = in.nextInt();
                                    card.ifPresent(c -> {
                                        int i = Model.getInstance().deposit(c.getCardNumber(), deposit);
                                        if (i == 1) {
                                            System.out.println("Income was added!");
                                        } else {
                                            System.out.println("Transaction failed!");
                                        }
                                    });
                                    break;
                                case 3:
                                    System.out.printf("Transfer\n");
                                    System.out.printf("Enter card number:\n");
                                    String payToCardNumber = in.next();
                                    // Validate payToCardNumber a) Lun algo, b) exists
                                    if (!Card.checkLuhn(Long.parseLong(payToCardNumber))) {
                                        System.out.printf("Probably you made a mistake in the card number. Please try" +
                                                " again!\n");
                                    } else if (!Model.getInstance().contains(payToCardNumber)) {
                                        System.out.printf("Such a card does not exist.\n");
                                    } else {
                                        System.out.printf("Enter how much money you want to transfer:\n");
                                        int amount = in.nextInt();
                                        card.ifPresent(c -> {
                                            if (Model.getInstance().findCard(c.getCardNumber()).get().getBalance() < amount) {
                                                System.out.printf("Not enough money!\n");
                                            } else {
                                                boolean success = Model.getInstance().transfer(
                                                        c.getCardNumber(),
                                                        payToCardNumber,
                                                        amount);
                                                if (success) {
                                                    System.out.printf("Success!\n");
                                                } else {
                                                    System.out.printf("Transaction failed!\n");
                                                }

                                            }
                                        });
                                    }
                                    break;
                                case 4:
                                    card.ifPresent(c -> {
                                        boolean success = Model.getInstance().delete(c.getCardNumber());
                                        if (success) {
                                            System.out.printf("The has has been closed!\n");
                                        } else {
                                            System.out.printf("The account has not been closed");
                                        }
                                    });
                                case 5:
                                    card = null;
                                    System.out.println("You have successfully logged out!");
                                    break;
                                case 0:
                                    card = null;
                                    return;
                                default:
                                    break;
                            }

                        } while (selection != 5);

                    } else {
                        System.out.print("Wrong card number or PIN!!\n");
                    }
                    break;
                default:
                    break;
            }

        } while (selection != 0);
    }
}
