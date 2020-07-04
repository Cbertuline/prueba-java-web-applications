package model.dao;

import control.entities.Employee;
import control.entities.Summary;
import control.entities.Visit;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmployeeDAO extends DAO {
    Employee employee;
    List<Employee> employees;


    public EmployeeDAO() {
        employees = new ArrayList<>();
    }

    public EmployeeDAO(Employee employee) {
        this.employee = employee;
        employees = new ArrayList<>();
    }

    @Override
    public Object create() throws SQLException {
        sql = "INSERT INTO EMPLOYEES (NAME, LAST_NAME, EMAIL, PASSWORD, PHONE_NUMBER) VALUES (?,?,?,?,?)";
        ResultSet resultSet = null;
        try {
            setStatement();
            preparedStatement.executeUpdate();
            preparedStatement.close();
            sql = "SELECT MAX(EMPLOYEE_ID) FROM EMPLOYEES";
            resultSet = connection.getConnection().prepareStatement(sql).executeQuery();
            resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error while creating: " + e.getMessage());
        } finally {
            return read(resultSet.getInt(1));
        }
    }

    private void setStatement() throws SQLException {
        preparedStatement = connection.getConnection().prepareStatement(sql);
        preparedStatement.setString(1, employee.getName());
        preparedStatement.setString(2, employee.getLastName());
        preparedStatement.setString(3, employee.getEmail());
        preparedStatement.setString(4, employee.getPassword());
        preparedStatement.setString(5, employee.getPhoneNumber());
    }

    @Override
    public List<Employee> readAll() throws SQLException {
        sql = "SELECT * FROM EMPLOYEES";
        try (Statement statement = connection.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                sqlData(id, resultSet);
            }
        } catch (SQLException e) {
            System.out.println("Error while reading: " + e.getMessage());
        } finally {
            connection.getConnection().close();
            return employees;
        }
    }

    @Override
    public Object read(int id) {
        sql = "SELECT * FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            sqlData(id, resultSet);
        } catch (SQLException e) {
            System.out.println("Error while reading: " + e.getMessage());
        } finally {
            closeConnection();
            return !employees.isEmpty() ? employees.get(0) : -1;
        }
    }

    private void sqlData(int id, ResultSet resultSet) throws SQLException {
        String name = resultSet.getString(2);
        String lastName = resultSet.getString(3);
        String email = resultSet.getString(4);
        String password = resultSet.getString(5); // It's necessary?
        String phoneNumber = resultSet.getString(6);
        employees.add(new Employee(id, name, lastName, email, password, phoneNumber));
    }

    @Override
    public Object exists(String email, String password) throws SQLException {
        sql = "SELECT * FROM EMPLOYEES WHERE EMAIL = ? AND PASSWORD = ?";
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error while testing existence: " + e.getMessage());
        } finally {
            return read(resultSet.getInt(1));
        }
    }

    @Override
    public Object update() {
        sql = "UPDATE EMPLOYEES SET NAME = ?, LAST_NAME = ?, EMAIL = ?, PASSWORD = ?, " +
                "PHONE_NUMBER = ? WHERE EMPLOYEE_ID = ?";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sql);
            setStatement();
            preparedStatement.setInt(6, employee.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating: " + e.getMessage());
        } finally {
            return read(employee.getId());
        }
    }

    @Override
    public void delete(int id) {
        sql = "DELETE FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";
        try {
            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // TODO Implement all relevant methods for employees

    public List<Visit> pendingVisits() {
        return getVisits(0);
    }

    public List<Visit> readyVisits() {
        return getVisits(1);
    }

    public List<Visit> allVisits() {
        return getVisits(-1);
    }

    private List<Visit> getVisits(int ready) {
        List<Visit> visitList = new ArrayList<>();
        if (ready != -1) {
            sql = "SELECT * FROM VISITS V INNER JOIN EMPLOYEES E on E.EMPLOYEE_ID = V.EMPLOYEES_EMPLOYEE_ID WHERE EMPLOYEE_ID = ? AND READY = " + ready;
        } else {
            sql = "SELECT * FROM VISITS V INNER JOIN EMPLOYEES E on E.EMPLOYEE_ID = V.EMPLOYEES_EMPLOYEE_ID WHERE EMPLOYEE_ID = ?";
        }
        try {
            preparedStatement = connection.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, employee.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Visit visit = new Visit();
                Timestamp timestamp = (resultSet.getTimestamp(4));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String formattedDate = formatter.format(timestamp);

                visit.setId(resultSet.getInt(1));
                visit.setReady(resultSet.getString(2).equals("1")); // String (Char) to a boolean
                visit.setCustomerId(resultSet.getInt(3));
                visit.setDate(formattedDate); // resultSet.getData(4)
                visit.setEmployeeId(resultSet.getInt(5));
                visit.setSummaryId(resultSet.getInt(6));
                visit.setPaymentId(resultSet.getInt(7));
                visit.setActivities(getActivities(resultSet.getString(8))); // Codified string (A;;;;B;;;;C) to a List<String> ([A,B,C])

                visitList.add(visit);
            }
        } catch (SQLException e) {
            System.out.println("Error while trying to request pending visits: " + e.getMessage());
        } finally {
            closeConnection();
            return visitList;
        }
    }

    public Summary requestSummary(int visitId) {
        Summary requestedSummary = null;
        return requestedSummary;
    }
}
