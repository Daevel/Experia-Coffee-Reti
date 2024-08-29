package client;

import logger.Log;
import utils.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class Dipendente {

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(Constants.HOSTNAME, Constants.DIPENDENTE_CLIENT_PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println(Constants.CHOOSE);
                System.out.println(Constants.INSERT_NEW_TICKET);
                System.out.println(Constants.SHOW_TICKETS);
                System.out.println(Constants.SHOW_TICKETS_STATUSES);
                System.out.println(Constants.DELETE_TICKET);
                System.out.println(Constants.EXIT);
                System.out.print(Constants.CHOICE);

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consuma la nuova linea rimasta dopo nextInt()

                switch (choice) {
                    case 1:
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
                        break;
                    case 2:
                        output.writeObject(Constants.VIEW_TICKETS);
                        List<String> tickets = (List<String>) input.readObject();
                        System.out.println(Constants.AVAILABLE_TICKETS);
                        for (String ticket : tickets) {
                            System.out.println(ticket);
                        }
                        break;
                    case 3:
                        output.writeObject(Constants.VIEW_TICKETS_STATUSES);
                        List<String> ticketsByStatuses = (List<String>) input.readObject();
                        System.out.println(Constants.AVAILABLE_TICKETS);
                        for (String ticket : ticketsByStatuses) {
                            System.out.println(ticket);
                        }
                        break;
                    case 4:
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
                        break;
                    case 5:
                        System.out.println(Constants.ON_CLOSING_CLIENT_MESSAGE);
                        output.writeObject(Constants.EXIT);
                        Log.success("Client chiuso con successo.");
                        return;

                    default:
                        System.out.println(Constants.WRONG_CHOICE);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
