package client;

import logger.Log;
import utils.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class Dipendente {

    private static final int DIPENDENTE_SERVER_PORT = Constants.DIPENDENTE_SERVER_PORT;// Inserisci l'host del server Ticketing
    private static final int TICKETING_SERVER_PORT = Constants.TICKETING_SERVER_PORT; // Inserisci la porta del server Ticketing

    public static void main(String[] args) {
        new Thread(Dipendente::startServer).start();
        new Thread(Dipendente::startTicketingClient).start();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(DIPENDENTE_SERVER_PORT)) {
            Log.info("Server Dipendente in ascolto sulla porta " + DIPENDENTE_SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Log.info("Connessione accettata da " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            Log.error("Errore del server: " + e.getMessage());
        }
    }

    private static void startTicketingClient() {
        try (Socket socket = new Socket("localhost", TICKETING_SERVER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                showChoices();

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        prepareInsertNewTicketRequest(output, input, scanner);
                        break;
                    case 2:
                        prepareShowTicketsRequest(output, input);
                        break;
                    case 3:
                        prepareShowTicketStatusesRequest(output, input);
                        break;
                    case 4:
                        prepareDeleteTicketRequest(output, input, scanner);
                        break;
                    case 5:
                        prepareUpdateTicketRequest(output, input, scanner);
                        break;
                    case 6:
                        prepareClosingClientAndServerRequest(output, input);
                        return;
                }
            }
        } catch (IOException e) {
            Log.error("Errore del client Ticketing: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    Socket socket = new Socket(Constants.HOSTNAME, Constants.DIPENDENTE_CLIENT_PORT);
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    Scanner scanner = new Scanner(System.in)) {

                while (true) {

                    showChoices();

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            prepareInsertNewTicketRequest(output, input, scanner);
                            break;
                        case 2:
                            prepareShowTicketsRequest(output, input);
                            break;
                        case 3:
                            prepareShowTicketStatusesRequest(output, input);
                            break;
                        case 4:
                            prepareDeleteTicketRequest(output, input, scanner);
                            break;
                        case 5:
                            prepareUpdateTicketRequest(output, input, scanner);
                            break;
                        case 6:
                            prepareClosingClientAndServerRequest(output, input);
                            return;
                    }
                }
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }
    }

    /**
     * @description mostra le scelte che un dipendente puo' intraprendere
     */
    public static void showChoices() {
        System.out.println(Constants.CHOOSE);
        System.out.println(Constants.INSERT_NEW_TICKET);
        System.out.println(Constants.SHOW_TICKETS);
        System.out.println(Constants.SHOW_TICKETS_STATUSES);
        System.out.println(Constants.DELETE_TICKET);
        System.out.println(Constants.UPDATE_TICKET);
        System.out.println(Constants.EXIT);
        System.out.print(Constants.CHOICE);
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabrora la richiesta di inserimento di un ticket
     */
    public static void prepareInsertNewTicketRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {
            System.out.print("Inserisci il titolo del ticket: ");
            String ticketInsertTitle = scanner.nextLine();
            System.out.print("Inserisci la descrizione del ticket: ");
            String ticketInsertDescription = scanner.nextLine();
            System.out.print("Inserisci lo stato del ticket: ");
            String ticketInsertStatus = scanner.nextLine();

            Date actualDate = new Date(System.currentTimeMillis());

            // Invia al server la richiesta di cancellazione con l'ID del ticket
            output.writeObject(Constants.INSERT_NEW_TICKET);
            output.writeObject(ticketInsertTitle);
            output.writeObject(ticketInsertDescription);
            output.writeObject(ticketInsertStatus);
            output.writeObject(actualDate);

            // Leggi la risposta dal server
            String insertResponse = (String) input.readObject();

            if (insertResponse.equals(Constants.SUCCESS)) {
                Log.success("Il ticket e' stato inserito con successo.");
            } else {
                Log.error("Errore nell'inserimento del ticket");
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }


    }

    /**
     * @param output
     * @param input
     * @description elabrora la richiesta di visualizzazione di un ticket
     */
    public static void prepareShowTicketsRequest(ObjectOutputStream output, ObjectInputStream input) {
        try {
            output.writeObject(Constants.VIEW_TICKETS);
            List<String> tickets = (List<String>) input.readObject();
            System.out.println(Constants.AVAILABLE_TICKETS);
            for (String ticket : tickets) {
                System.out.println(ticket);
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @description elabrora la richiesta di visualizzazione di un ticket in base allo stato
     */
    public static void prepareShowTicketStatusesRequest(ObjectOutputStream output, ObjectInputStream input) {
        try {
            output.writeObject(Constants.VIEW_TICKETS_STATUSES);
            List<String> ticketsByStatuses = (List<String>) input.readObject();
            System.out.println(Constants.AVAILABLE_TICKETS);
            for (String ticket : ticketsByStatuses) {
                System.out.println(ticket);
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabrora la richiesta di cancellazione di un ticket
     */
    public static void prepareDeleteTicketRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del ticket da cancellare: ");
            int ticketIdToDelete = scanner.nextInt();
            scanner.nextLine();  // Consuma la nuova linea rimasta dopo nextInt()

            // Invia al server la richiesta di cancellazione con l'ID del ticket
            output.writeObject(Constants.DELETE_TICKET);
            output.writeObject(ticketIdToDelete);

            // Leggi la risposta dal server
            String response = (String) input.readObject();

            if (response.equals(Constants.SUCCESS)) {
                Log.success(String.format("Il ticket con ID %d è stato cancellato con successo.", ticketIdToDelete));
            } else {
                Log.error(String.format("Errore nella cancellazione del ticket con ID %d.", ticketIdToDelete));
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabrora la richiesta di aggiornamento di un ticket
     */
    public static void prepareUpdateTicketRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del ticket da aggiornare: ");
            Integer ticketUpdateID = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Inserisci lo stato da aggiornare: ");
            String ticketUpdateStatus = scanner.nextLine();


            output.writeObject(Constants.UPDATE_TICKET);
            output.writeObject(ticketUpdateID);
            output.writeObject(ticketUpdateStatus);

            // Leggi la risposta dal server
            String updateResponse = (String) input.readObject();

            if (updateResponse.equals(Constants.SUCCESS)) {
                Log.success("Il ticket è stato aggiornato con successo.");
            } else {
                Log.error("Errore nell'inserimento del ticket");
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @description elabrora la richiesta di chiusura del client e server
     */
    public static void prepareClosingClientAndServerRequest(ObjectOutputStream output, ObjectInputStream input) {
        try {
            System.out.println(Constants.ON_CLOSING_CLIENT_MESSAGE);
            output.writeObject(Constants.EXIT);

            // Leggi la conferma dal server prima di chiudere
            String exitResponse = (String) input.readObject();
            if (exitResponse.equals(Constants.SUCCESS)) {
                Log.success("Client chiuso con successo.");
            } else {
                Log.error("Errore durante la chiusura del client.");
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

}
