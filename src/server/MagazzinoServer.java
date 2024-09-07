package server;

import logger.Log;
import singleton.Database;
import utils.Constants;
import utils.Queries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MagazzinoServer {

    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public MagazzinoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Log.info(String.format(Constants.SERVER_SOCKET_CONNECTION_ESTABLISHED, port));
    }

    public void start() throws IOException {
        Log.info(Constants.SERVER_LISTENING);
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                Log.info("Connessione stabilita con il client: " + clientSocket.getInetAddress());
                new ClientHandler(this, clientSocket).start();
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            Log.info("Server chiuso.");
        } catch (IOException e) {
            Log.error("Errore durante la chiusura del server: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        MagazzinoServer server = new MagazzinoServer(Constants.MAGAZZINO_SERVER_PORT);
        server.start();
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private MagazzinoServer server;

        public ClientHandler(MagazzinoServer server, Socket socket) {
            this.server = server;
            this.clientSocket = socket;
        }

        private void prepareViewProductsRequest(ObjectOutputStream output) throws SQLException {
            try {
                List<String> products = viewProducts();
                output.writeObject(products);

            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareUpdateProductRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                String productId = (String) input.readObject();
                int newQuantity = (int) input.readObject();
                boolean updated = updateProductQuantity(productId, newQuantity);
                if (updated) {
                    output.writeObject(Constants.SUCCESS);
                    Log.info("Prodotto aggiornato con successo.");
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning("Aggiornamento fallito.");
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareDeleteProductRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                String productId = (String) input.readObject();

                boolean deleted = deleteProductQuantity(productId);
                if (deleted) {
                    output.writeObject(Constants.SUCCESS);
                    Log.success(String.format("Prodotto %s rimosso con successo.", productId));
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning(String.format("Rimozione del prodotto %s fallita.", productId));
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareInsertProductRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                String codiceMagazzino = "Z000";
                String productId = (String) input.readObject();
                int productQuantity = (int) input.readObject();
                String productName = (String) input.readObject();
                String nomeMagazzino = (String) input.readObject();

                boolean inserted = insertNewProduct(codiceMagazzino, productId, productQuantity, productName, nomeMagazzino);
                if (inserted) {
                    output.writeObject(Constants.SUCCESS);
                    Log.info("Prodotto inserito con successo.");
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning("Inserimento fallito.");
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
            }
        }

        public void run() {
            try (
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())
            ) {
                while (true) {
                    try {
                        String request = (String) input.readObject();
                        switch (request) {
                            case Constants.MAGAZZINO_INSERT_NEW_PRODUCT:
                                prepareInsertProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_VIEW_PRODUCTS:
                                prepareViewProductsRequest(output);
                                break;
                            case Constants.MAGAZZINO_UPDATE_PRODUCTS:
                                prepareUpdateProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_DELETE_PRODUCTS:
                                prepareDeleteProductRequest(input, output);
                                break;
                            case Constants.EXIT:
                                server.stop();
                                break;
                            default:
                                output.writeObject(Constants.UNKNOWN_REQUEST);
                                break;
                        }
                    } catch (ClassNotFoundException | SQLException e) {
                        Log.error("Errore durante la gestione del client: " + e.getMessage());
                        output.writeObject(Constants.FAILURE);
                    }
                }
            } catch (IOException e) {
                Log.error("Errore di I/O: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    Log.error("Errore durante la chiusura del socket: " + e.getMessage());
                }
                Log.info("Connessione chiusa con il client");
            }
        }

        /**
         * @return List<String> contenente la lista dei prodotti disponibili
         * @throws SQLException
         */
        private List<String> viewProducts() throws SQLException {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            List<String> products = new ArrayList<>();
            try {
                connection = Database.getInstance().getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(String.format(Queries.GENERIC_QUERY_SELECT, Constants.TBL_MAGAZZINO));

                while (resultSet.next()) {
                    String codiceMagazzino = resultSet.getString("CODICE_MAGAZZINO");
                    String id = resultSet.getString("ID_PRODOTTO");
                    int quantita = resultSet.getInt("QUANTITA_PRODOTTO");
                    String nome = resultSet.getString("NOME_PRODOTTO");
                    String nomeMagazzino = resultSet.getString("NOME_MAGAZZINO");

                    String product = String.format("ID: %s, Nome: %s, Quantità: %d, Codice magazzino: %s, Nome magazzino: %s", id, nome, quantita, codiceMagazzino, nomeMagazzino);
                    products.add(product);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return products;
        }

        /**
         * @param productName
         * @param productQuantity
         * @return true se l'inserimento è avvenuto con successo, altrimenti false
         * @throws SQLException
         */
        private boolean insertNewProduct(String codiceMagazzino, String productId, int productQuantity, String productName, String nomeMagazzino) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_MAGAZZINO_INSERT_NEW_PRODUCT_QUERY);

                preparedStatement.setString(1, codiceMagazzino);
                preparedStatement.setString(2, productId);
                preparedStatement.setInt(3, productQuantity);
                preparedStatement.setString(4, productName);
                preparedStatement.setString(5, nomeMagazzino);

                int rowsAffected = preparedStatement.executeUpdate();

                return rowsAffected > 0;

            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }

        /**
         * @param productId
         * @param newQuantity
         * @return true se l'aggiornamento è avvenuto con successo, altrimenti false
         * @throws SQLException
         */
        private boolean updateProductQuantity(String productId, int newQuantity) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_MAGAZZINO_UPDATE_PRODUCT_QUANTITY_QUERY);
                preparedStatement.setInt(1, newQuantity);
                preparedStatement.setString(2, productId);

                int rowsAffected = preparedStatement.executeUpdate();

                return rowsAffected > 0;

            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }

        /**
         * @param productId
         * @return true se l'eliminazione è avvenuto con successo, altrimenti false
         * @throws SQLException
         */
        private boolean deleteProductQuantity(String productId) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_MAGAZZINO_DELETE_PRODUCT_QUERY);
                preparedStatement.setString(1, productId);

                int rowsAffected = preparedStatement.executeUpdate();

                return rowsAffected > 0;

            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }
    }
}