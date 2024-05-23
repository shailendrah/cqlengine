package oracle.cep.extensibility.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IExternalHasResultSet {
    ResultSet getResultSet();
    boolean isClosed() throws SQLException;
}
