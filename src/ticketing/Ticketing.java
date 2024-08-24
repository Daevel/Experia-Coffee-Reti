package ticketing;

import Logger.Log;
import Singleton.Database;
import utils.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Ticketing {

    public Connection getConnection() throws SQLException {
        String url = Constants.URL + "/" + Constants.DATABASE_NAME;
        return DriverManager.getConnection(Constants.URL, Constants.USER, Constants.PASSWORD);
    }

    public void inserisciTicket(String titolo, String descrizione, String gestito_da, String creato_da, String data_creazione, String stato) throws SQLException {
        Connection connection = getConnection();

        String sql = "INSERT INTO tbl_ticketing (titolo, descrizione, gestito_da, creato_da, data_creazione, stato) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, titolo);
            statement.setString(2, descrizione);
            statement.setString(3, gestito_da);
            statement.setString(4, creato_da);
            statement.setString(5, data_creazione);
            statement.setString(6, stato);

            // Esecuzione dell'update
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ticket inserito con successo!");
            } else {
                System.out.println("Errore: nessun ticket è stato inserito.");
            }
        } catch (SQLException e) {
            Log.error("Errore nell'inserimento del record nella tabella ticketing");
            throw e; // Rilancia l'eccezione per permettere al chiamante di gestirla
        } finally {
            // Chiusura della connessione
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    Log.error("Errore durante la chiusura della connessione al database");
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> ricercaTicketDisponibili() {
        List<String> ticketDisponibili = new ArrayList<String>();
        try (Connection connection = Database.getInstance().getConnection()) {
            String query = "SELECT * FROM tbl_ticketing";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String descrizioneTicket = resultSet.getString("descrizioneTicket");
                        String statoTicket = resultSet.getString("statoTicket");
                        ticketDisponibili.add(descrizioneTicket + " " + statoTicket);
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("Errore nella query di ricezione dei ticket");
            e.printStackTrace();
        }
        return ticketDisponibili;
    }


}
