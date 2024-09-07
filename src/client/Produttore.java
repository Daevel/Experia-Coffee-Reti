package client;

import logger.Log;
import utils.Constants;
import utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Produttore {

    public static void main(String[] args) throws IOException {
        try (
                Socket socket = new Socket(Constants.HOSTNAME, Constants.PRODUTTORE_CLIENT_PORT);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)) {

            while (true) {

                showChoices();

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        prepareInsertNewProductRequest(output, input, scanner);
                        break;
                    case 2:
                        prepareViewProductsRequest(output, input);
                        break;
                    case 3:
                        prepareUpdateProductRequest(output, input, scanner);
                        break;
                    case 4:
                        prepareDeleteProductRequest(output, input, scanner);
                        break;
                    case 5:
                        prepareClosingClientAndServerRequest(output, input);
                        return;
                }
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * @description mostra le scelte che un produttore può intraprendere
     */
    public static void showChoices() {
        System.out.println("Scegli un'operazione:");
        System.out.println("1 - Inserisci nuovo prodotto");
        System.out.println("2 - Visualizza prodotti");
        System.out.println("3 - Aggiorna quantità prodotto");
        System.out.println("4 - Elimina prodotto");
        System.out.println("5 - Esci");
        System.out.print("Scelta: ");
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabora la richiesta di inserimento di un nuovo prodotto
     */
    public static void prepareInsertNewProductRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {

            //CODICE_MAGAZZINO, ID_PRODOTTO, QUANTITA_PRODOTTO, NOME_PRODOTTO, NOME_MAGAZZINO

            System.out.print("Inserisci l'ID del prodotto: ");
            String productId = scanner.nextLine();
            System.out.print("Inserisci la quantità del prodotto: ");
            int productQuantity = scanner.nextInt();
            System.out.print("Inserisci il nome del prodotto: ");
            String productName = scanner.next();
            System.out.print("Inserisci il nome del magazzino: ");
            String nomeMagazzino = scanner.nextLine();

            scanner.nextLine();

            // Invia al server la richiesta di inserimento prodotto
            output.writeObject(Constants.MAGAZZINO_INSERT_NEW_PRODUCT);
            output.writeObject(productId);
            output.writeObject(productQuantity);
            output.writeObject(productName);
            output.writeObject(nomeMagazzino);

            // Leggi la risposta dal server
            String insertResponse = (String) input.readObject();

            if (insertResponse.equals(Constants.SUCCESS)) {
                Log.success(String.format("Prodotto %s inserito con successo.", productName));
            } else {
                Log.error(String.format("Errore nell'inserimento del prodotto %s.", productName));
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @description elabora la richiesta di visualizzazione dei prodotti
     */
    public static void prepareViewProductsRequest(ObjectOutputStream output, ObjectInputStream input) {
        try {
            output.writeObject(Constants.MAGAZZINO_VIEW_PRODUCTS);
            List<String> products = (List<String>) input.readObject();
            System.out.println("Prodotti disponibili:");
            System.out.println(Utils.formatProductList(products));
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabora la richiesta di aggiornamento della quantità di un prodotto
     */
    public static void prepareUpdateProductRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del prodotto da aggiornare: ");
            String productId = scanner.next();
            scanner.nextLine();

            System.out.print("Inserisci la nuova quantità: ");
            int newQuantity = scanner.nextInt();
            scanner.nextLine();

            // Invia al server la richiesta di aggiornamento
            output.writeObject(Constants.MAGAZZINO_UPDATE_PRODUCTS);
            output.writeObject(productId);
            output.writeObject(newQuantity);

            // Leggi la risposta dal server
            String updateResponse = (String) input.readObject();

            if (updateResponse.equals(Constants.SUCCESS)) {
                Log.success(String.format("Prodotto %s è stato aggiornato con successo. Quantità disponibile: %d", productId, newQuantity));
            } else {
                Log.error("Errore nell'aggiornamento del prodotto.");
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @param output
     * @param input
     * @param scanner
     * @description elabora la richiesta di eliminazione di un prodotto dal magazzino
     */
    public static void prepareDeleteProductRequest(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del prodotto da eliminare: ");
            String productId = scanner.next();
            scanner.nextLine();

            // Invia al server la richiesta di aggiornamento
            output.writeObject(Constants.MAGAZZINO_DELETE_PRODUCTS);
            output.writeObject(productId);

            // Leggi la risposta dal server
            String updateResponse = (String) input.readObject();

            if (updateResponse.equals(Constants.SUCCESS)) {
                Log.success(String.format("Prodotto %s rimosso con successo.", productId));
            } else {
                Log.error(String.format("Errore nella rimozione del prodotto %s.", productId));
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.error(e.getMessage());
        }
    }



    /**
     * @param output
     * @param input
     * @description elabora la richiesta di chiusura del client e server
     */
    public static void prepareClosingClientAndServerRequest(ObjectOutputStream output, ObjectInputStream input) {
        try {
            System.out.println("Chiusura del client...");
            output.writeObject(Constants.EXIT);

            // Leggi la conferma dal server
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