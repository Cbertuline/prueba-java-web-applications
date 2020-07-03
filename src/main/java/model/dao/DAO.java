package model.dao;

import model.database.OracleConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class DAO {
    // Database connection
    OracleConnection connection = new OracleConnection();
    PreparedStatement preparedStatement;
    String sql;

    // CRUD pattern
    public abstract void create();

    public abstract List<?> read() throws SQLException;

    public abstract Object read(int id);

    public abstract void update();

    public abstract void delete();

    protected void closeConnection() {
        try {
            if (preparedStatement != null) preparedStatement.close();
            connection.getConnection().close();
            System.out.println("Successful disconnection");
        } catch (SQLException e) {
            System.out.println("Disconnection error: " + e.getMessage());
        }
    }
}