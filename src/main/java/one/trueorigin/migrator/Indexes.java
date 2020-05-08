package one.trueorigin.migrator;

import java.util.ArrayList;
import java.util.List;

public class Indexes {

    private String name;
    private List<String> columns;
    private Boolean isUnqinue;
    private String key_name;

    public Indexes(String name) {
        this.name = name;
        columns = new ArrayList<>();
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void setUnqinue(Boolean unqinue) {
        isUnqinue = unqinue;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Boolean getUnqinue() {
        return isUnqinue;
    }

    @Override
    public String toString() {
        return "Indexes{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                ", isUnqinue=" + isUnqinue +
                '}';
    }
}
