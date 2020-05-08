package one.trueorigin.migrator;

public class DatabaseFieldType {

    private String fieldName;
    private String type;

    public DatabaseFieldType(String fieldName, String type) {
        this.fieldName = fieldName;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return "DatabaseFieldType{" +
                "fieldName='" + fieldName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
