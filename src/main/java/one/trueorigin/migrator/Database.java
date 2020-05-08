package one.trueorigin.migrator;

import java.sql.Connection;

public class Database {

    public static final String MySQLDriver = "com.mysql.cj.jdbc.Driver";

    //TODO
    public static final String SQLiteDriver = "com.mysql.cj.jdbc.Driver";

    public String getConnectionString() {
        return connectionString;
    }

    enum Type {
        MySQL, Postgres, SqlLite
    }

    public Type getType() {
        return type;
    }

    public String getDriver() {
        return driver;
    }

    public Connection getConnection() {
        return connection;
    }

    private Type type;
    private String driver;
    private Connection connection;
    private String connectionString;

    public Database(Type type, String driver, Connection connection, String connectionString) {
        this.type = type;
        this.driver = driver;
        this.connection = connection;
        this.connectionString = connectionString;
    }

    public SchemaInterpreter getSchemaManager() {
        return new SchemaInterpreter(this);
    }
}
