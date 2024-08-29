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
                } else if (request.equals(Constants.VIEW_TICKETS_STATUSES)) {
                    List<String> ticketStatuses = viewTicketStatuses();
                    output.writeObject(ticketStatuses);
                } else if (request.equals(Constants.DELETE_TICKET)) {
                    int ticketId = (int) input.readObject();
                    boolean deleted = deleteTicket(ticketId);
                    if (deleted) {
                        output.writeObject(Constants.SUCCESS);
                        Log.info(String.format("Ticket con ID %d cancellato con successo.", ticketId));
                    } else {
                        output.writeObject(Constants.FAILURE);
                        Log.warning(String.format("Cancellazione fallita per il ticket con ID %d.", ticketId));
                    }
                } else if (request.equals(Constants.INSERT_NEW_TICKET)) {
                    String ticketTitle = (String) input.readObject();
                    String ticketDescription = (String) input.readObject();
                    String ticketStatus = (String) input.readObject();
                    Date ticketCreatedDate = (Date) input.readObject();
                    boolean inserted = insertNewTicket(ticketTitle, ticketDescription, ticketStatus, ticketCreatedDate);
                    if (inserted) {
                        output.writeObject(Constants.SUCCESS);
                        Log.info("Ticket con ID %d inserito con successo.");
                    } else {
                        output.writeObject(Constants.FAILURE);
                        Log.warning("Inserimento fallito del ticket.");
                    }
                } else if (request.equals(Constants.EXIT)) {
                    clientSocket.close();
                } else {
                    output.writeObject(Constants.UNKNOWN_REQUEST);
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                try {
                    throw new IOException(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        /**
         * @throws SQLException
         * @description funzione la quale mostra tutti i ticket disponibili
         */
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
                            id, titolo, descrizione, gestitoDa != null ? gestitoDa : "Non asegnato", creatoDa != null ? creatoDa : "Anonimo", dataCreazione, stato);

                    tickets.add(ticket);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return tickets;
        }

        /**
         * @throws SQLException
         * @description funzione la quale mostra gli id dei ticket con i rispettivi stati
         */
        private List<String> viewTicketStatuses() throws SQLException {

            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            List<String> tickets = new ArrayList<>();
            try {
                connection = Database.getInstance().getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(Queries.TBL_TICKETING_SELECT_STATUSES_QUERY);

                while (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    String stato = resultSet.getString("STATO");

                    // Costruisci una rappresentazione leggibile del ticket
                    String ticket = String.format("ID: %d, Stato: %s", id, stato);
                    tickets.add(ticket);
                }
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return tickets;
        }

        /**
         * @throws SQLException
         * @description elimina ticket in base all'id fornito
         */
        private boolean deleteTicket(int ticketId) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_TICKETING_DELETE_TICKET_BY_ID_QUERY);
                preparedStatement.setInt(1, ticketId);

                int rowsAffected = preparedStatement.executeUpdate();

                // Se viene cancellata almeno una riga, la cancellazione è avvenuta con successo
                return rowsAffected > 0;

            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }

        private boolean insertNewTicket(String ticketTitle, String ticketDescription, String ticketStatus, Date ticketCreationDate) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_TICKETING_INSERT_NEW_TICKET_BY_QUERY);
                preparedStatement.setString(1, ticketTitle);
                preparedStatement.setString(2, ticketDescription);
                preparedStatement.setString(3, ticketStatus);
                preparedStatement.setDate(4, ticketCreationDate);

                int rowsAffected = preparedStatement.executeUpdate();

                // Se viene cancellata almeno una riga, la cancellazione è avvenuta con successo
                return rowsAffected > 0;

            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        TicketingServer server = new TicketingServer(Constants.TICKETING_SERVER_PORT);
        server.start();
    }
}
