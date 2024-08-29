package utils;

public class Constants {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    
    /**
     * @description DATABASE TABLES NAMES
     */
    public static final String TBL_TICKETING = "Ticketing";
    public static final String TBL_EMPLOYEE = "Dipendente";
    public static final String TBL_FILIALE = "Filiale";

    /**
     * @description DATABASE CONSTANTS
     */
    public static final String URL = "jdbc:mysql://localhost:3306";
    public static final String USER = "root";
    public static final String PASSWORD = "";
    public static final String DATABASE_NAME = "experia_coffee_reti";
    public static final String URL_CONNECTION = URL + "/" + DATABASE_NAME;

    /**
     * @description QUERY INFO LOG MESASGES
     */
    public static final String SQL_DATABASE_ALREADY_EXISTS = "Il database %s esiste gia'.";
    public static final String SQL_TABLE_ALREADY_EXISTS = "La tabella %s esiste gia'.";

    /**
     * @description QUERY SUCCESS LOG MESASGES
     */
    public static final String SQL_DATABASE_IN_USE = "Database %s in uso!";
    public static final String SQL_DATABASE_CREATION_SUCCESS = "Creazione del database %s avvenuta con successo!";
    public static final String SQL_DATABASE_CONNECTION_SUCCEDED = "Connessione al database %s riuscita!";
    public static final String SQL_CREATION_TABLE_SUCCESS = "Creazione della tabella %s effettuata con successo!";
    public static final String SQL_INSERTION_TABLE_SUCCESS = "Inserimento dei record nella tabella %s avvenuta con successo!";

    /**
     * @description QUERY ERROR LOG MESASGES
     */
    public static final String SQL_CONNECTION_ERROR_CONNECTION_CLOSE = "Errore durante la chiusura della connessione di %s !";
    public static final String SQL_CONNECTION_ERROR_CONNECTION = "Errore durante la connessione al database %s !";
    public static final String SQL_VERIFY_EXISTENCE_DB_ERROR_CONNECTION = "Errore durante la creazione del database o controllo dell'esistenza!";
    public static final String SQL_DRIVER_JDBC_NOT_FOUND = "Driver JDBC non trovato!";
    public static final String SQL_CONNECTION_CLOSING_RESOURCES_ERROR = "Errore durante la chiusura delle risorse!";
    public static final String SQL_CREATION_TABLE_ERROR = "Errore durante la crezione della tabella %s !";

    /**
     * @description Costanti di server e client socket
     */
    public static final String SERVER_SOCKET_CONNECTION_ESTABLISHED = "Server avviato sulla porta %s";
    public static final String SERVER_LISTENING = "Server in ascolto...";
    public static final String CLIENT_SOCKET_CONNECTION_ESTABLISHED = "Client in ascolto sulla porta %s";

    /**
     * @description Server/Client socket configurations
     */
    public static final String HOSTNAME = "localhost";
    public static final Integer TICKETING_SERVER_PORT = 8080;
    public static final Integer DIPENDENTE_CLIENT_PORT = 8080;

    /**
     * @description Choices
     */
    public static final String VIEW_TICKETS = "VIEW_TICKETS";
    public static final String VIEW_TICKETS_STATUSES = "VIEW_TICKETS_STATUSES";
    public static final String AVAILABLE_TICKETS = "Tickets disponibili:";
    public static final String ON_CLOSING_CLIENT_MESSAGE = "Chiusura del client...";
    public static final String WRONG_CHOICE = "Scelta non valida. Riprovare.";

    /**
     * @description Dipendente Client decisions
     */
    public static final String CHOOSE = "Scegli un'operazione";
    public static final String INSERT_NEW_TICKET = "1. Inserisci un nuovo ticket";
    public static final String SHOW_TICKETS = "2. Visualizza tutti i ticket";
    public static final String SHOW_TICKETS_STATUSES = "3. Visualizza gli stati dei vari ticket";
    public static final String DELETE_TICKET = "4. Elimina ticket";
    public static final String EXIT = "5. Esci";
    public static final String CHOICE = "Scelta...  ";
    public static final String UNKNOWN_REQUEST = "Richiesta sconosciuta";

}
