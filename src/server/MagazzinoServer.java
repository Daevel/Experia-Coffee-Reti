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
                            case Constants.MAGAZZINO_UPDATE_PRODUCTS:
                                prepareUpdateProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_VIEW_PRODUCTS:
                                prepareViewProductsRequest(output);
                                break;
                            case Constants.MAGAZZINO_DELETE_PRODUCTS:
                                prepareDeleteProductRequest(input, output);
                                break;
                            case Constants.EXIT:
                                output.writeObject(Constants.SUCCESS);
                                server.stop();
                                return;
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

        private void prepareInsertProductRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException, IOException {
            try {
                // Ricevi i parametri dal DipendenteServer
                String codiceMagazzino = (String) input.readObject();
                String productId = (String) input.readObject();
                int productQuantity = (int) input.readObject();
                String productName = (String) input.readObject();
                String nomeMagazzino = (String) input.readObject();

                // Inserisci il prodotto nel magazzino
                String response = insertProduct(codiceMagazzino, productId, productQuantity, productName, nomeMagazzino);

                // Invia la risposta al DipendenteServer
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                output.writeObject(Constants.FAILURE);
            }
        }

        private void prepareViewProductsRequest(ObjectOutputStream output) throws SQLException {
            try {
                // Ottieni la lista dei prodotti
                List<String> products = viewProducts();

                // Invia la lista di prodotti indietro a DipendenteServer
                output.writeObject(products);
            } catch (IOException e) {
                Log.error("Errore durante l'invio della lista di prodotti: " + e.getMessage());
            }
        }

        private void prepareUpdateProductRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException, IOException {
            try {
                // Ricevi l'ID del prodotto e la nuova quantità
                String productId = (String) input.readObject();
                int newQuantity = (int) input.readObject();

                // Effettua l'aggiornamento del prodotto
                String response = updateProduct(productId, newQuantity);

                // Invia la risposta a DipendenteServer
                output.writeObject(response);

            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                output.writeObject(Constants.FAILURE);
            }
        }

        private void prepareDeleteProductRequest(ObjectInputStream input, ObjectOutputStream output) {
            try {
                // Ricevi l'ID del prodotto dal DipendenteServer
                String productId = (String) input.readObject();

                // Esegui l'eliminazione del prodotto dal database
                String response = deleteProduct(productId);

                // Invia la risposta al DipendenteServer
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException | SQLException e) {
                Log.error(e.getMessage());
                try {
                    output.writeObject(Constants.FAILURE);
                } catch (IOException ex) {
                    Log.error(ex.getMessage());
                }
            }
        }


        // Metodo che esegue la cancellazione del prodotto dal database
        private String deleteProduct(String productId) throws SQLException {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = Database.getInstance().getConnection();

                String query = "DELETE FROM magazzino WHERE ID_PRODOTTO = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, productId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    return Constants.SUCCESS;
                } else {
                    return Constants.FAILURE;
                }
            } finally {
                Database.closeConnection(connection, statement, null);
            }
        }

        private String insertProduct(String codiceMagazzino, String productId, int productQuantity, String productName, String nomeMagazzino) throws SQLException {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = Database.getInstance().getConnection();

                String query = "INSERT INTO magazzino (CODICE_MAGAZZINO, ID_PRODOTTO, QUANTITA_PRODOTTO, NOME_PRODOTTO, NOME_MAGAZZINO) VALUES (?, ?, ?, ?, ?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, codiceMagazzino);
                statement.setString(2, productId);
                statement.setInt(3, productQuantity);
                statement.setString(4, productName);
                statement.setString(5, nomeMagazzino);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    return Constants.SUCCESS;
                } else {
                    return Constants.FAILURE;
                }
            } finally {
                Database.closeConnection(connection, statement, null);
            }
        }

        /**
         * @param productId
         * @param newQuantity
         * @return true se l'aggiornamento è avvenuto con successo, altrimenti false
         * @throws SQLException
         */
        private String updateProduct(String productId, int newQuantity) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                String updateQuery = "UPDATE " + Constants.TBL_MAGAZZINO + " SET QUANTITA_PRODOTTO = ? WHERE ID_PRODOTTO = ?";
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setInt(1, newQuantity);
                preparedStatement.setString(2, productId);

                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    Log.info(String.format("Prodotto %s aggiornato con successo a quantità: %d", productId, newQuantity));
                    return Constants.SUCCESS;
                } else {
                    Log.error("Nessun prodotto trovato con ID: " + productId);
                    return Constants.FAILURE;
                }
            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }

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
    }
}