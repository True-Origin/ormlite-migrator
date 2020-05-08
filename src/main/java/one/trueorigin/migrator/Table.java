package one.trueorigin.migrator;

import com.j256.ormlite.table.DatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Table {
    private Database database;
    private String tableName;
    private DatabaseTable databaseTable;
    private List<IncomingDatabaseFieldType> databaseField;

    public Table(Database database, String tableName, DatabaseTable databaseTable, List<IncomingDatabaseFieldType> databaseField) {
        this.database = database;
        this.tableName = tableName;
        this.databaseTable = databaseTable;
        this.databaseField = databaseField;
    }

    public List<IncomingDatabaseFieldType> getDatabaseField() {
        return databaseField;
    }

    public DatabaseTable getDatabaseTable() {
        return databaseTable;
    }

    public String getTableName() {
        return tableName;
    }

    public String create() {

        List<String> collect = this.databaseField.stream().map(IncomingDatabaseFieldType::generateStatement).collect(Collectors.toList());

        List<IncomingDatabaseFieldType> primaryKeys = this.databaseField.stream().filter(i -> {
            if (i.getDatabaseField() != null) {
                return i.getDatabaseField().id();
            }
            return false;
        }).collect(Collectors.toList());

        String primaryKey = "";

        if (primaryKeys.size() > 0) {
            List<String> pk = primaryKeys.stream().map(IncomingDatabaseFieldType::getFieldName).collect(Collectors.toList());
            primaryKey = ", PRIMARY KEY(" + String.join(",", pk) + ")";

        }

        return "CREATE TABLE IF NOT EXISTS " + this.tableName + " (" +
                String.join(",", collect) + " " + primaryKey +
                ");";
    }

    public String createIndexes() {

        String script = "";
        //Filter index with one field
        script = script + createIndexWithOneField();

        //unique indexes with one field
        script = script + createUniqueIndex();

        //composite indexes
        script = script + createCompositeIndex();

        //composite unqiue indexes
        script = script + createCompositeUniqueIndexes();

        return script;

    }

    private String createCompositeUniqueIndexes() {
        Map<String, List<IncomingDatabaseFieldType>> compositeUniqueIndexes = this.databaseField.
                stream().
                filter(i -> i.getDatabaseField() != null && !i.getDatabaseField().uniqueIndexName().isEmpty()).
                collect(Collectors.groupingBy(o -> o.getDatabaseField().uniqueIndexName()));

        return compositeUniqueIndexes.keySet().stream().map(p -> {
            List<IncomingDatabaseFieldType> compositeDatabaseFieldTypes = compositeUniqueIndexes.get(p);

            List<String> finalUniqueIndexList = compositeDatabaseFieldTypes.stream().
                    map(IncomingDatabaseFieldType::getFieldName).collect(Collectors.toList());

                return "CREATE UNIQUE INDEX " + p + " ON " + this.tableName + "(" + String.join(",", finalUniqueIndexList) + ");";
        }).collect(Collectors.joining());
    }

    private String createCompositeIndex() {
        Map<String, List<IncomingDatabaseFieldType>> collect = this.databaseField.
                stream().
                filter(i -> i.getDatabaseField() != null && !i.getDatabaseField().indexName().isEmpty()).
                collect(Collectors.groupingBy(o -> o.getDatabaseField().indexName()));

        return collect.keySet().stream().map(k -> {
            List<IncomingDatabaseFieldType> incomingDatabaseFieldTypes = collect.get(k);

            List<String> finalList = incomingDatabaseFieldTypes.stream().
                    map(IncomingDatabaseFieldType::getFieldName).collect(Collectors.toList());

            return "CREATE INDEX " + k + " ON " + this.tableName + "(" + String.join(",", finalList) + ");";
        }).collect(Collectors.joining());
    }

    private String createUniqueIndex() {
        return this.databaseField.stream().
                filter(i -> i.getDatabaseField() != null).
                filter(i -> i.getDatabaseField().unique()).
                map(i -> "CREATE UNIQUE INDEX " + i.getFieldName() + "_idx ON " + this.tableName + "(" + i.getFieldName() + ");").
                collect(Collectors.joining());
    }

    private String createIndexWithOneField() {
        return this.databaseField.stream().
                filter(i -> i.getDatabaseField() != null).
                filter(i -> i.getDatabaseField().index()).
                map(i -> "CREATE INDEX " + i.getFieldName() + "_idx ON " + this.tableName + "(" + i.getFieldName() + ");").
                collect(Collectors.joining());
    }

    public Database getDatabase() {
        return database;
    }
}
