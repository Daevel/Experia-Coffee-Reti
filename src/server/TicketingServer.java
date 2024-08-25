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

public class TicketingServer {

    private ServerSocket serverSocket;

    public TicketingServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Log.info(String.format(Constants.SERVER_SOCKET_CONNECTION_ESTABLISHED, port));
    }

    public void start() throws IOException {
        Log.info(Constants.SERVER_LISTENING);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                String request = (String) input.readObject();
                if (request.equals(Constants.VIEW_TICKETS)) {
                    List<String> tickets = viewTickets();
                    output.writeObject(tickets);
                } else {
                    output.writeObject(Constants.UNKNOWN_REQUEST);
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                try {
                    throw new IOException(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    try {
                        throw new IOException(e);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        private List<String> viewTickets() throws SQLException {

            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            List<String> tickets = new ArrayList<>();
            try {
                connection = Database.getInstance().getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(String.format(Queries.GENERIC_QUERY_SELECT, Constants.TBL_TICKETING));

                while (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    String titolo = resultSet.getString("TITOLO");
                    String descrizione = resultSet.getString("DESCRIZIONE");
                    String gestitoDa = resultSet.getString("GESTITO_DA");
                    String creatoDa = resultSet.getString("CREATO_DA");
                    Date dataCreazione = resultSet.getDate("DATA_CREAZIONE");
                    String stato = resultSet.getString("STATO");

                    // Costruisci una rappresentazione leggibile del ticket
                    String ticket = String.format("ID: %d, Titolo: %s, Descrizione: %s, Gestito da: %s, Creato da: %s, Data creazione: %s, Stato: %s",
                            id, titolo, descrizione, gestitoDa != null ? gestitoDa : "Non assegnato", creatoDa != null ? creatoDa : "Anonimo", dataCreazione, stato);

                    tickets.add(ticket);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return tickets;
        }

    }

    public static void main(String[] args) throws IOException {
        TicketingServer server = new TicketingServer(Constants.TICKETING_SERVER_PORT);
        server.start();
    }
}
