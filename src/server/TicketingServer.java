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

/**
 * @description classe inerente al TicketingServer, per la gestione dei ticket e delle segnalazioni da parte degli utenti di Experia Coffee
 */
public class TicketingServer {

    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public TicketingServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Log.info(String.format(Constants.SERVER_SOCKET_CONNECTION_ESTABLISHED, port));
    }

    public void start() throws IOException {
        Log.info(Constants.SERVER_LISTENING);
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                Log.info("Connessione statibilita con il client: " + clientSocket.getInetAddress());
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
        TicketingServer server = new TicketingServer(Constants.TICKETING_SERVER_PORT);
        server.start();
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private TicketingServer server;

        public ClientHandler(TicketingServer server, Socket socket) {
            this.server = server;
            this.clientSocket = socket;
        }

        private void prepareViewTicketsRequest(ObjectOutputStream output) throws SQLException {
            try {
                List<String> tickets = viewTickets();
                output.writeObject(tickets);

            } catch (IOException e) {
                Log.error(e.getMessage());
            }

        }

        private void prepareViewTicketsStatusRequest(ObjectOutputStream output) throws SQLException {
            try {
                List<String> ticketStatuses = viewTicketStatuses();
                output.writeObject(ticketStatuses);
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareDeleteTicketRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                int ticketId = (int) input.readObject();
                boolean deleted = deleteTicket(ticketId);
                if (deleted) {
                    output.writeObject(Constants.SUCCESS);
                    Log.info(String.format("Ticket con ID %d cancellato con successo.", ticketId));
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning(String.format("Cancellazione fallita per il ticket con ID %d.", ticketId));
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareInsertNewTicketRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                String ticketTitle = (String) input.readObject();
                String ticketDescription = (String) input.readObject();
                String ticketStatus = (String) input.readObject();
                Date ticketCreatedDate = (Date) input.readObject();
                boolean inserted = insertNewTicket(ticketTitle, ticketDescription, ticketStatus, ticketCreatedDate);
                if (inserted) {
                    output.writeObject(Constants.SUCCESS);
                    Log.info("Ticket inserito con successo.");
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning("Inserimento fallito del ticket.");
                }
            } catch (SQLException | RuntimeException | ClassNotFoundException | IOException e) {
                Log.error(e.getMessage());
            }

        }

        private void prepareUpdateTicketRequest(ObjectInputStream input, ObjectOutputStream output) throws SQLException {
            try {
                Integer ticketUpdateId = (Integer) input.readObject();
                String ticketUpdateStatus = (String) input.readObject();
                boolean updated = updateTicketStatus(ticketUpdateId, ticketUpdateStatus);
                if (updated) {
                    output.writeObject(Constants.SUCCESS);
                    Log.info("Ticket aggiornato con successo.");
                } else {
                    output.writeObject(Constants.FAILURE);
                    Log.warning("Aggiornamento fallito del ticket.");
                }
            } catch (SQLException | ClassNotFoundException | RuntimeException | IOException e) {
                Log.error(e.getMessage());
            }
        }

        private void prepareClosingClientAndServer(ObjectOutputStream output) throws SQLException {
            try {
                output.writeObject(Constants.SUCCESS);
                server.stop();
            } catch (IOException e) {
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
                            case Constants.VIEW_TICKETS:
                                prepareViewTicketsRequest(output);
                                break;
                            case Constants.VIEW_TICKETS_STATUSES:
                                prepareViewTicketsStatusRequest(output);
                                break;
                            case Constants.DELETE_TICKET:
                                prepareDeleteTicketRequest(input, output);
                                break;
                            case Constants.INSERT_NEW_TICKET:
                                prepareInsertNewTicketRequest(input, output);
                                break;
                            case Constants.UPDATE_TICKET:
                                prepareUpdateTicketRequest(input, output);
                                break;
                            case Constants.EXIT:
                                prepareClosingClientAndServer(output);
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
         * @return List<String> contenente la lista dei ticket trovati
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
         * @return List<String> contenente gli stati dei ticket suddivisi per ID
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
            } catch (SQLException ex) {
                Log.error(ex.getMessage());
            } finally {
                Database.closeConnection(connection, statement, resultSet);
            }
            return tickets;
        }

        /**
         * @return true se l'operazione e' andata a buon fine, altrimenti false
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

        /**
         * @param ticketTitle
         * @param ticketDescription
         * @param ticketStatus
         * @param ticketCreationDate
         * @return true se l'operazione e' andata a buon fine, altrimenti false
         * @throws SQLException
         * @description inserisce un nuovo ticket
         */
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

        /**
         * @param ticketId
         * @param ticketStatus
         * @return true se l'operazione e' andata a buon fine, altrimenti false
         * @throws SQLException
         * @description aggiorna lo stato di un ticket
         */
        private boolean updateTicketStatus(Integer ticketId, String ticketStatus) throws SQLException {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = Database.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(Queries.TBL_TICKETING_UPDATE_TICKET_STATUS_BY_QUERY);
                preparedStatement.setString(1, ticketStatus);
                preparedStatement.setInt(2, ticketId);

                int rowsAffected = preparedStatement.executeUpdate();

                // Se viene cancellata almeno una riga, la cancellazione è avvenuta con successo
                return rowsAffected > 0;

            } catch (SQLException e) {
                Log.error(e.getMessage());
            } finally {
                Database.closeConnection(connection, preparedStatement, null);
            }

            return false;
        }

    }

}
