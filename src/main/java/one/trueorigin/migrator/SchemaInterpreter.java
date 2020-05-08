package one.trueorigin.migrator;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static one.trueorigin.migrator.DatabaseConnectionManager.determineFieldsToBeCreated;


public class SchemaInterpreter {
    private static final Logger logger = LoggerFactory.getLogger(SchemaInterpreter.class.getSimpleName());

    private one.trueorigin.migrator.Database database;
    private List<Class> models;

    public SchemaInterpreter(Database database) {
        this.database = database;
        this.models = new ArrayList<>();
    }

    public SchemaInterpreter(Database database, List<Class> models) {
        this.database = database;
        this.models = models;
    }

    private List<DatabaseFieldType> getSchema(String tableName) throws SQLException {

        List<DatabaseFieldType> fields = new ArrayList<>();
        String query = "describe " + tableName;

        logger.info(query);

        try (Statement stmt = database.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                fields.add(new DatabaseFieldType(rs.getString(1), rs.getString(2)));
            }
        }

        return fields;
    }

    private List<Indexes> getIndexes(String tableName) throws SQLException {

        Map<String, Indexes> indexes = new HashMap<>();
        String query = "show indexes from " + tableName;

        logger.info(query);

        try (Statement stmt = database.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {

                //TODO this is specific to mysql
                Indexes index = indexes.
                        getOrDefault(
                                rs.getString("Key_name"),
                                new Indexes(rs.getString("Key_name")));

                index.getColumns().add(rs.getString("Column_name"));
                if (rs.getString("Non_unique").equals("1")) {
                    index.setUnqinue(false);
                } else {
                    index.setUnqinue(true);
                }

                indexes.put(rs.getString("Key_name"), index);
            }
        }

        return new ArrayList<>(indexes.values());
    }

    public <T> SchemaInterpreter model(Class<T> databaseClass) {
        this.models.add(databaseClass);
        return this;
    }

    public String generate() throws ClassNotFoundException, SQLException, NoFieldDefinedException, TableAnnotationNotFound {

        String script = "";

        for (Class model : this.models) {

            Table table = this.fetchTable(model);

            List<DatabaseFieldType> schemaFoundInDatabase = getDatabaseFieldTypes(table);
            List<Indexes> indexesFound = getIndexes(table);

            if (schemaFoundInDatabase.size() == 0) {
                if (indexesFound.size() != 0) {
                    for (Indexes i : indexesFound) {
                        this.execute("drop indexes " + i.getName());
                    }
                }

                System.out.println("now generating table");
                script = script + "\n" + table.create();
                System.out.println("now creating indexes");
                script = script + "\n" + table.createIndexes();

            } else {
                List<IncomingDatabaseFieldType> incomingDatabaseFieldTypes =
                        determineFieldsToBeCreated(
                                table.getDatabaseField(),
                                schemaFoundInDatabase
                        );

                if (incomingDatabaseFieldTypes.size() == 0) {
                    logger.info("No new fields found");
                } else {
                    logger.info("total new fields found - " + incomingDatabaseFieldTypes.size());
                    script = script + this.alterTable(table, incomingDatabaseFieldTypes);
                }
                //to do update the indexes
            }
        }

        return script;

}

    public void migrate() throws ClassNotFoundException, SQLException, NoFieldDefinedException, TableAnnotationNotFound {

        String generate = this.generate();

        if(generate.isEmpty()) {
            logger.info("No overall change found");
            return;
        }

        System.out.println(generate);

        Arrays.asList(generate.split(";")).forEach(st -> {
            try (Statement stmt = database.getConnection().createStatement()) {
                stmt.execute(st);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
    }

    private List<Indexes> getIndexes(Table table) {
        List<Indexes> indexesFound = new ArrayList<>();
        try {
            indexesFound = this.getIndexes(table.getTableName());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return indexesFound;
    }

    private List<DatabaseFieldType> getDatabaseFieldTypes(Table table) {
        List<DatabaseFieldType> schemaFoundInDatabase = new ArrayList<>();
        try {
            schemaFoundInDatabase = this.getSchema(table.getTableName());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return schemaFoundInDatabase;
    }

    private Table fetchTable(Class model) throws ClassNotFoundException, NoFieldDefinedException, TableAnnotationNotFound {

        Class c = Class.forName(model.getName());

        DatabaseTable dTable = (DatabaseTable) c.getAnnotation(DatabaseTable.class);

        if (dTable == null) {
            throw new TableAnnotationNotFound();
        }
        String tableName = dTable.tableName();

        if (tableName.isEmpty()) {
            tableName = c.getSimpleName();
        }

        Field[] fields = c.getDeclaredFields();

        List<IncomingDatabaseFieldType> incomingFields = Arrays.
                stream(fields).
                filter(f -> f.getAnnotation(DatabaseField.class) != null).
                map(f -> new IncomingDatabaseFieldType(f.getName(), f.getAnnotation(DatabaseField.class), f.getType())).
                collect(Collectors.toList());


        if (incomingFields.size() == 0) {
            throw new NoFieldDefinedException();
        }
        return new Table(this.database, tableName, dTable, incomingFields);
    }

    private void execute(String query) throws SQLException {
        try (Statement stmt = database.getConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    private String alterTable(Table table, List<IncomingDatabaseFieldType> incomingDatabaseFields) throws SQLException {

        List<String> collect = incomingDatabaseFields.stream().
                map(IncomingDatabaseFieldType::generateStatement).
                collect(Collectors.toList());
        return "ALTER TABLE " + table.getTableName() + " ADD (" + String.join(",", collect) + ");";
    }
}
