package banking;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class SQLiteDatabase implements Database {

    // Singleton
    private static SQLiteDatabase sqliteDatabase;

    // SQL JDBC property of Singleton
    private final SQLiteDataSource sqLiteDataSource;


    private SQLiteDatabase(String dbName) {
        // Initilize DataSource
        sqLiteDataSource = new SQLiteDataSource();
        // Update Database URL
        String urlRoot = "jdbc:sqlite:";
        String url = urlRoot + dbName;
        // Set DataSource url
        sqLiteDataSource.setUrl(url);
        // Initialize Table if not already exists
        instatiateDBTable(this.sqLiteDataSource);
    }

    // Instatiate Singleton
    public static void instantiate(String dbName) {
        SQLiteDatabase.sqliteDatabase = new SQLiteDatabase(dbName);
    }

    // DataSource Singleton Getter
    public static Database getInstance() {
        return sqliteDatabase;
    }

    private void instatiateDBTable(DataSource dataSource) {

        String createCardTableExpression = "CREATE TABLE IF NOT EXISTS card(" +
                "id INTEGER," +
                "number TEXT," +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0" +
                ");";
        // Create Connection
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement
            try (Statement statement = connection.createStatement()) {
                int i = statement.executeUpdate(createCardTableExpression);
//                System.out.println(i);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Optional<Card> find(String cardNumber) {
        String sql = "SELECT * FROM card where number=?";

        try (Connection connection = sqLiteDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, cardNumber);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return Optional.ofNullable(
                            new Card.CardBuilder(resultSet.getString("number"))
                                    .pin(resultSet.getString("pin"))
                                    .balace(resultSet.getInt("balance"))
                                    .build()
                    );
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean contains(String cardNumber) {
        String sql = "SELECT number FROM card WHERE number = ?";

        try (Connection connection = sqLiteDataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, cardNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int save(Card card) {

        String sql = "INSERT INTO card (number, pin, balance) VALUES (?, ?, ?)";

        try (Connection connection = sqLiteDataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, card.getCardNumber());
                preparedStatement.setString(2, card.getPin());
                preparedStatement.setInt(3, card.getBalance());

                int i = preparedStatement.executeUpdate();
                return i;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int deposit(String cardNumber, int deposit) {

        String sql = "UPDATE card SET balance = balance + ? WHERE number=?";
        int i = 0;

        try (Connection connection = sqLiteDataSource.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // Create SavePoint for RollBack
                Savepoint savepoint0 = connection.setSavepoint();

                // Set Placeholders in Prepared Statement
                preparedStatement.setInt(1, deposit);
                preparedStatement.setString(2, cardNumber);
                i = preparedStatement.executeUpdate();

                // Check that only 1 row is affected, otherwise rollback
                if (i != 1) {
                    connection.rollback(savepoint0);
                }

                // Commit Transaction
                connection.commit();

            } catch (SQLException e) {
                // Catch block to rollback transaction altogether
                e.printStackTrace();
                try {
                    System.out.println("Transaction is being rolled back!");
                    connection.rollback();
                } catch (SQLException exc) {
                    exc.printStackTrace();
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return i;
    }

    @Override
    public boolean transfer(String payFromCardNumber, String payToCardNumber, int amount) {

        if (amount <= 0) return false;

        String sql = "UPDATE card SET balance = balance + ? WHERE number= ?";

        try (Connection connection = sqLiteDataSource.getConnection()) {
            // Transaction so turn off autocommit
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                Savepoint savepoint = connection.setSavepoint();

                // Pay From
                preparedStatement.setInt(1, -amount);
                preparedStatement.setString(2, payFromCardNumber);
                int i = preparedStatement.executeUpdate();

                // i is number of rows affected, if not 1, rollback transaction to savepoint
                if (i != 1) {
                    connection.rollback(savepoint);
                }

                // Pay To
                preparedStatement.setInt(1, amount);
                preparedStatement.setString(2, payToCardNumber);
                i = preparedStatement.executeUpdate();

                if (i != 1) {
                    connection.rollback(savepoint);
                }

                // Commit transaction
                connection.commit();

                // Return true boolean
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                try (connection) {
                    System.out.println("Transaction is being rolled back!");
                    connection.rollback();
                } catch (SQLException exc) {
                    exc.printStackTrace();
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(String cardNumber) {
        if (!contains(cardNumber)) return false;

        String sql = "DELETE FROM card WHERE number = ?";

        try (Connection connection = sqLiteDataSource.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                Savepoint savepoint = connection.setSavepoint();

                preparedStatement.setString(1, cardNumber);
                int i = preparedStatement.executeUpdate();

                if (i != 1) {
                    connection.rollback(savepoint);
                }

                connection.commit();

                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    System.out.printf("Transaction is being rolled back!");
                    connection.rollback();
                } catch (SQLException exc) {
                    exc.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
