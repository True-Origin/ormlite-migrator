package one.trueorigin.migrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionManager {

    public static Database withConnection(String connectionString) throws ConnectionStringException, ClassNotFoundException, SQLException {
        String[] split = connectionString.split(":");

        if (split.length > 1) {

            if (split[0].equals("jdbc") && split[1].equals("mysql")) {
                Class.forName(Database.MySQLDriver);
                Connection connection = DriverManager
                        .getConnection(connectionString);

                return new Database(Database.Type.MySQL, Database.MySQLDriver, connection, connectionString);
            } else if (split[0].equals("jdbc") && split[1].equals("sqlite")) {

                Class.forName(Database.MySQLDriver);
                Connection connection = DriverManager
                        .getConnection(connectionString);

                return new Database(Database.Type.MySQL, Database.MySQLDriver, connection, connectionString);


            }
        }

        throw new ConnectionStringException(connectionString + " is not valid");
    }


    static List<IncomingDatabaseFieldType> determineFieldsToBeCreated(List<IncomingDatabaseFieldType> incomingDatabaseFields, List<DatabaseFieldType> existingFields) {

        List<IncomingDatabaseFieldType> fieldsToBeCreated = new ArrayList<>();
        for(IncomingDatabaseFieldType field : incomingDatabaseFields) {
            if(field.getDatabaseField() != null){
                if(!in(field, existingFields)) {
                    fieldsToBeCreated.add(field);
                }
            }
        }

        return fieldsToBeCreated;
    }

    private static boolean in(IncomingDatabaseFieldType field, List<DatabaseFieldType> existingFields) {
        return existingFields.stream().anyMatch(f -> f.getFieldName().equals(field.getFieldName()));
    }
}
