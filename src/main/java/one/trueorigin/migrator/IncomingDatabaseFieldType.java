package one.trueorigin.migrator;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import one.trueorigin.migrator.exception.DataTypeInvalidException;

import java.util.Date;

public class IncomingDatabaseFieldType {
    private String fieldName;
    private DatabaseField databaseField;
    private Class type;

    public DatabaseField getDatabaseField() {
        return databaseField;
    }

    public String getFieldName() {
        return fieldName;
    }

    public IncomingDatabaseFieldType(String fieldName, DatabaseField databaseField, Class tClass) {
        this.fieldName = fieldName;
        this.databaseField = databaseField;
        this.type = tClass;
    }

    public String generateStatement() {
        String field = "";
        String dataType = "";

        if (databaseField == null) {
            return "";
        }


        if (databaseField.dataType() == DataType.UNKNOWN) {
            if (this.getFieldClass().equals(String.class)) {
                field = "VARCHAR(255)";
            } else if (this.getFieldClass().equals(Integer.class)) {
                field = "BIGINT(20)";
            } else if (this.getFieldClass().equals(Boolean.class)) {
                field = "TINYINT(1)";
            } else if (this.getFieldClass().equals(Date.class)) {
                field = "DATETIME";
            } else {
                try {
                    throw new DataTypeInvalidException();
                } catch (DataTypeInvalidException e) {
                    e.printStackTrace();
                }

                field = "VARCHAR(255)";
            }
        } else if (databaseField.dataType() == DataType.LONG_STRING) {
            field = "TEXT";
        } else {
            field = databaseField.dataType().getDataPersister().getSqlOtherType();
        }

        boolean isNotNull = false;
        boolean isAutoIncrementing = false;

        if (databaseField != null) {
            if (!databaseField.canBeNull()) {
                isNotNull = true;
            }

            if (databaseField.generatedId()) {
                isAutoIncrementing = true;
            }

        }

        String fieldCommand = this.getFieldName() + " " + field;

        if (isNotNull) {
            fieldCommand = fieldCommand + " NOT NULL";
        }

        if (isAutoIncrementing) {
            fieldCommand = fieldCommand + " AUTO_INCREMENT";
        }

        return fieldCommand;
    }

    @Override
    public String toString() {
        return "IncomingDatabaseFieldType{" +
                "fieldName='" + fieldName + '\'' +
                ", databaseField=" + databaseField +
                '}';
    }

    public Class getFieldClass() {
        return type;
    }
}
