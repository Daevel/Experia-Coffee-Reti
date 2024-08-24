package Singleton;

import Logger.Log;
import utils.Constants;
import utils.Queries;
import utils.Utils;

import java.sql.*;

public class Database {

    private static Database instance;

    private Database() {
    }

    public static Database getInstance() {
        if (Utils.isNull(instance)) {
            instance = new Database();
        }
        return instance;
    }

    public Connection establishMySQLConnection() throws SQLException {
        try {
            return DriverManager.getConnection(Constants.URL, Constants.USER, Constants.PASSWORD);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(Constants.URL_CONNECTION, Constants.USER, Constants.PASSWORD);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public static void closeConnection(Connection connection, Statement statement, ResultSet resultSet) throws SQLException {
        try {
            if (!(Utils.isNull(resultSet))) {
                resultSet.close();
            }
        } catch (SQLException e) {
            Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            throw new SQLException(e);
        }
        closeConnection(connection, statement);
    }

    public static void closeConnection(Connection connection, Statement statement) throws SQLException {
        try {
            if (!Utils.isNull(statement)) {
                statement.close();
            }
        } catch (SQLException e) {
            Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            throw new SQLException(e);
        }

        try {
            if (!Utils.isNull(connection)) {
                connection.close();
            }
        } catch (SQLException e) {
            Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            throw new SQLException(e);
        }
    }

    public static void closeConnection(Connection connection) throws SQLException {
        try {
            if (!Utils.isNull(connection)) {
                connection.close();
            }
        } catch (SQLException e) {
            Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            throw new SQLException(e);
        }
    }

    public static void createDatabaseIfNotExists() throws SQLException, ClassNotFoundException {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            //* Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            //* Connection to MySQL without specifying a certain database
            connection = getInstance().establishMySQLConnection();
            statement = connection.createStatement();

            //* Verify that database exists
            resultSet = statement.executeQuery(String.format(Queries.QUERY_SHOW_DATABASES_LIKE, Constants.DATABASE_NAME));
            if (resultSet.next()) {
                Log.info(String.format(Constants.SQL_DATABASE_ALREADY_EXISTS, Constants.DATABASE_NAME));
            } else {
                //* If database doesn't exist, create it
                String sql = String.format(Queries.QUERY_CREATE_DB, Constants.DATABASE_NAME);
                statement.executeUpdate(sql);
                Log.info(String.format(Constants.SQL_DATABASE_CREATION_SUCCESS, Constants.DATABASE_NAME));
            }

        } catch (ClassNotFoundException e) {
            Log.error(Constants.SQL_DRIVER_JDBC_NOT_FOUND);
            throw new ClassNotFoundException(Constants.SQL_DRIVER_JDBC_NOT_FOUND);
        } catch (SQLException e) {
            Log.error(Constants.SQL_VERIFY_EXISTENCE_DB_ERROR_CONNECTION);
            throw new SQLException(e);
        } finally {
            closeConnection(connection, statement, resultSet);
        }
    }


    /**
     * @throws SQLException handle SQL errors
     * @description Creation of tbl_ticketing table
     */
    private static void createTicketingTable() throws SQLException {
        Connection connection = null;
        Statement statement = null;

        try {
            //* Connection to a specific database
            connection = getInstance().getConnection();
            statement = connection.createStatement();

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, Constants.TBL_TICKETING, null);
            if (tables.next()) {
                Log.info(String.format(Constants.SQL_TABLE_ALREADY_EXISTS, Constants.TBL_TICKETING));
            } else {
                statement.executeUpdate(Queries.TBL_TICKETING_CREATE_QUERY_TABLE);
                Log.success(String.format(Constants.SQL_CREATION_TABLE_SUCCESS, Constants.TBL_TICKETING));
            }
        } catch (SQLException e) {
            Log.error(String.format(Constants.SQL_CREATION_TABLE_ERROR, Constants.TBL_TICKETING));
            throw new SQLException(e);
        } finally {
            try {
                closeConnection(connection, statement);
            } catch (SQLException e) {
                Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            }
        }
    }

    /**
     * @throws SQLException handle SQL errors
     * @description Creation of tbl_dipendente table
     */
    private static void createEmployeeTable() throws SQLException {
        Connection connection = null;
        Statement statement = null;

        try {
            //* Connection to a specific database
            connection = getInstance().getConnection();
            statement = connection.createStatement();

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, Constants.TBL_EMPLOYEE, null);
            if (tables.next()) {
                Log.info(String.format(Constants.SQL_TABLE_ALREADY_EXISTS, Constants.TBL_EMPLOYEE));
            } else {
                statement.executeUpdate(Queries.TBL_EMPLOYEE_CREATE_QUERY_TABLE);
                Log.success(String.format(Constants.SQL_CREATION_TABLE_SUCCESS, Constants.TBL_EMPLOYEE));

                statement.executeUpdate(Queries.TBL_EMPLOYEE_INSERT_QUERY_TABLE);
                Log.success(String.format(Constants.SQL_INSERTION_TABLE_SUCCESS, Constants.TBL_EMPLOYEE));
            }
        } catch (SQLException e) {
            Log.error(String.format(Constants.SQL_CREATION_TABLE_ERROR, Constants.TBL_EMPLOYEE));
            throw new SQLException(e);
        } finally {
            try {
                closeConnection(connection, statement);
            } catch (SQLException e) {
                Log.error(Constants.SQL_CONNECTION_CLOSING_RESOURCES_ERROR);
            }
        }
    }

    /**
     * @throws SQLException handle SQL errors
     * @description Creation of tbl_filiale table
     */
    private static void createFilialeTable() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet tables = null;

        try {
            // Connessione al database specifico
            connection = getInstance().getConnection();
            statement = connection.createStatement();

            // Ottieni i metadati del database
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            tables = databaseMetaData.getTables(null, null, Constants.TBL_FILIALE, null);

            if (tables.next()) {
                // La tabella esiste già
                Log.info(String.format(Constants.SQL_TABLE_ALREADY_EXISTS, Constants.TBL_FILIALE));
            } else {
                statement.executeUpdate(Queries.TBL_FILIALE_CREATE_QUERY_TABLE);
                Log.success(String.format(Constants.SQL_CREATION_TABLE_SUCCESS, Constants.TBL_FILIALE));

                statement.executeUpdate(Queries.TBL_FILIALE_INSERT_QUERY_TABLE);
                Log.success(String.format(Constants.SQL_INSERTION_TABLE_SUCCESS, Constants.TBL_FILIALE));
            }
        } catch (SQLException e) {
            Log.error(String.format(Constants.SQL_CREATION_TABLE_ERROR, Constants.TBL_FILIALE));
            throw new SQLException(e);
        } finally {
            // Chiudi la connessione e lo statement
            closeConnection(connection, statement);
        }
    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        createDatabaseIfNotExists();

        try (Connection connection = getInstance().getConnection()) {
            if (Utils.isNull(connection)) {
                Log.error(Constants.SQL_CONNECTION_ERROR_CONNECTION);
                return;
            } else {
                Log.success(String.format(Constants.SQL_DATABASE_CONNECTION_SUCCEDED, Constants.DATABASE_NAME));
            }

            //* It creates tables inside the database
            createFilialeTable();
            createEmployeeTable();
            createTicketingTable();

        } catch (SQLException e) {
            Log.error(String.format(Constants.SQL_CONNECTION_ERROR_CONNECTION, Constants.DATABASE_NAME));
            throw new SQLException(e);
        }
    }
}
