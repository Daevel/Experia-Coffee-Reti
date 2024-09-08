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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrdineServer {

    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public OrdineServer(int port) throws IOException {
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
        OrdineServer server = new OrdineServer(Constants.ORDINE_SERVER_PORT);
        server.start();
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private OrdineServer server;

        public ClientHandler(OrdineServer server, Socket socket) {
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
                            case Constants.ORDER_VIEW_LIST:
                                prepareViewOrderListRequest(output);
                                break;
                            case Constants.ORDER_VIEW_STATUS_LIST:
                                prepareViewOrderStatusListRequest(output);
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

        private void prepareViewOrderListRequest(ObjectOutputStream output) throws SQLException {
            try {
                // Ottieni la lista dei prodotti
                List<String> orders = viewOrders();

                // Invia la lista di prodotti indietro a DipendenteServer
                output.writeObject(orders);
            } catch (IOException e) {
                Log.error("Errore durante l'invio della lista di prodotti: " + e.getMessage());
            }
        }

        private void prepareViewOrderStatusListRequest(ObjectOutputStream output) throws SQLException {
            try {
                // Ottieni la lista dei prodotti
                List<String> status = viewOrderStatus();

                // Invia la lista di prodotti indietro a DipendenteServer
                output.writeObject(status);
            } catch (IOException e) {
                Log.error("Errore durante l'invio della lista di prodotti: " + e.getMessage());
            }
        }


        private List<String> viewOrders() throws SQLException {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            List<String> orders = new ArrayList<>();
            try {
                connection = Database.getInstance().getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(String.format(Queries.GENERIC_QUERY_SELECT, Constants.TBL_ORDINE));

                while (resultSet.next()) {
                    Integer id = resultSet.getInt("ID");
                    String fattura = resultSet.getString("FATTURA");
                    Integer numeroOrdine = resultSet.getInt("NUMERO_ORDINE");
                    Integer idCarrello = resultSet.getInt("ID_CARRELLO");
                    String indirizzoSpedizione = resultSet.getString("INDIRIZZO_SPEDIZIONE");
                    String statoOrdine = resultSet.getString("STATO_ORDINE");

                    String order = String.format("ID: %d, Fattura: %s, Numero ordine: %d, ID carrello: %d, Indirizzo spedizione: %s, Stato ordine: %s", id, fattura, numeroOrdine, idCarrello, indirizzoSpedizione, statoOrdine);
                    orders.add(order);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return orders;
        }

        private List<String> viewOrderStatus() throws SQLException {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            List<String> orderStatus = new ArrayList<>();
            try {
                connection = Database.getInstance().getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(String.format(Queries.GENERIC_QUERY_SELECT, Constants.TBL_ORDINE));

                while (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    String statoOrdine = resultSet.getString("STATO_ORDINE");

                    String status = String.format("ID: %s, Stato ordine: %s", id, statoOrdine);
                    orderStatus.add(status);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return orderStatus;
        }
    }
}