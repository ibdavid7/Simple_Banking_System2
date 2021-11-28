package banking;

public class App implements Runnable {

    private String dbName;

    public App(String dbName) {
        this.dbName = dbName;
    }

    App() {
    }

    @Override
    public void run() {
        if (dbName != null) {
            SQLiteDatabase.instantiate(dbName);
        }
        View.menu();
    }

}
