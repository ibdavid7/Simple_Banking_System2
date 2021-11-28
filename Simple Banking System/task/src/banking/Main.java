package banking;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {

        Optional<String> dbName = Optional.ofNullable(getDBName(args));

        dbName.ifPresent(dbn -> new App(dbn).run());
    }

    private static String getDBName(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-fileName") && i + 1 < args.length) {
                return args[i + 1];
            }
        }
        return null;
    }


}