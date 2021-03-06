package one.trueorigin.migrator;

import one.trueorigin.migrator.exception.ConnectionStringException;
import one.trueorigin.migrator.exception.NoFieldDefinedException;
import org.junit.Ignore;
import org.junit.Test;
import java.sql.SQLException;
import static one.trueorigin.migrator.DatabaseConnectionManager.withConnection;

//This is an example
public class SchemaInterpreterTest {

    @Test
    @Ignore
    public void shouldCreateSchema() throws SQLException, ConnectionStringException, ClassNotFoundException, NoFieldDefinedException, TableAnnotationNotFound {
        SchemaInterpreter schemaInterpreter = new SchemaInterpreter(withConnection("jdbc:mysql://root@localhost:3306/trueorigin"));

        //TODO
//        schemaInterpreter.
//                model(Admin.class).
//                model(UserWorkflow.class).
//                model(User.class).
//                model(Tenant.class);

        schemaInterpreter.migrate();

    }

}
