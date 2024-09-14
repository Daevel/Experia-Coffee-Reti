package server;

import logger.Log;
import utils.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;


/**
 * @description Classe inerente al server intermediario fra Produttore e MagazzinoServer e OrdineServer
 */
public class DipendenteServer {

    private ServerSocket serverSocket;
    private boolean isRunning = true;

    public DipendenteServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Log.info(String.format(Constants.SERVER_SOCKET_CONNECTION_ESTABLISHED, port));
    }

    public void start() throws IOException {
        Log.info(Constants.SERVER_LISTENING);
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                Log.info("Connessione statibilita con il client: " + clientSocket.getInetAddress());
                new DipendenteServer.ClientHandler(this, clientSocket).start();
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
        DipendenteServer server = new DipendenteServer(Constants.DIPENDENTE_SERVER_PORT);
        server.start();
    }

    private static class ClientHandler extends Thread {

        private Socket clientSocket;
        private DipendenteServer server;

        public ClientHandler(DipendenteServer server, Socket socket) {
            this.server = server;
            this.clientSocket = socket;
        }

        public void run() {
            try (
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())
            ) {
                // Ciclo che mantiene la connessione attiva
                while (true) {
                    try {
                        String request = (String) input.readObject();
                        switch (request) {
                            case Constants.ORDER_VIEW_LIST:
                                forwardViewOrderListRequest(output);
                                break;
                            case Constants.ORDER_VIEW_STATUS_LIST:
                                forwardViewOrderStatusListRequest(output);
                                break;
                            case Constants.ORDER_UPDATE_STATUS:
                                forwardUpdateOrderStatus(input, output);
                                break;
                            case Constants.MAGAZZINO_INSERT_NEW_PRODUCT:
                                forwardInsertNewProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_UPDATE_PRODUCTS:
                                forwardUpdateProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_DELETE_PRODUCTS:
                                handleDeleteProductRequest(input, output);
                                break;
                            case Constants.MAGAZZINO_VIEW_PRODUCTS:
                                forwardViewProductsRequest(output);
                                break;
                            case Constants.EXIT:
                                forwardCloseRequestToMagazzinoServer();
                                forwardCloseRequestToOrderServer();
                                prepareClosingClientAndServer(output);
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
        
        private void forwardInsertNewProductRequest(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
            try {
                String codiceMagazzino = (String) input.readObject();
                String productId = (String) input.readObject();
                int productQuantity = (int) input.readObject();
                String productName = (String) input.readObject();
                String nomeMagazzino = (String) input.readObject();

                // Inoltra la richiesta a MagazzinoServer
                String response = forwardInsertNewProductToMagazzinoServer(codiceMagazzino, productId, productQuantity, productName, nomeMagazzino);

                // Invia la risposta a Produttore
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                output.writeObject(Constants.FAILURE);
            }
        }

        private void forwardViewProductsRequest(ObjectOutputStream output) throws SQLException {
            try (
                    Socket magazzinoSocket = new Socket(Constants.HOSTNAME, Constants.MAGAZZINO_SERVER_PORT);
                    ObjectOutputStream magazzinoOutput = new ObjectOutputStream(magazzinoSocket.getOutputStream());
                    ObjectInputStream magazzinoInput = new ObjectInputStream(magazzinoSocket.getInputStream())
            ) {
                // Invia la richiesta a MagazzinoServer
                magazzinoOutput.writeObject(Constants.MAGAZZINO_VIEW_PRODUCTS);

                // Ricevi la lista di prodotti da MagazzinoServer
                List<String> products = (List<String>) magazzinoInput.readObject();

                // Invia la lista di prodotti a Produttore
                output.writeObject(products);
            } catch (IOException | ClassNotFoundException e) {
                Log.error("Errore durante l'inoltro della richiesta a MagazzinoServer: " + e.getMessage());
            }
        }

        private void forwardViewOrderListRequest(ObjectOutputStream output) throws SQLException {
            try (
                    Socket ordineSocket = new Socket(Constants.HOSTNAME, Constants.ORDINE_SERVER_PORT);
                    ObjectOutputStream ordineoOutput = new ObjectOutputStream(ordineSocket.getOutputStream());
                    ObjectInputStream ordineInput = new ObjectInputStream(ordineSocket.getInputStream())
            ) {
                // Invia la richiesta a MagazzinoServer
                ordineoOutput.writeObject(Constants.ORDER_VIEW_LIST);

                // Ricevi la lista di prodotti da MagazzinoServer
                List<String> orders = (List<String>) ordineInput.readObject();

                // Invia la lista di prodotti a Produttore
                output.writeObject(orders);
            } catch (IOException | ClassNotFoundException e) {
                Log.error("Errore durante l'inoltro della richiesta a MagazzinoServer: " + e.getMessage());
            }
        }

        private void forwardViewOrderStatusListRequest(ObjectOutputStream output) throws SQLException {
            try (
                    Socket ordineSocket = new Socket(Constants.HOSTNAME, Constants.ORDINE_SERVER_PORT);
                    ObjectOutputStream ordineoOutput = new ObjectOutputStream(ordineSocket.getOutputStream());
                    ObjectInputStream ordineInput = new ObjectInputStream(ordineSocket.getInputStream())
            ) {
                // Invia la richiesta a MagazzinoServer
                ordineoOutput.writeObject(Constants.ORDER_VIEW_STATUS_LIST);

                // Ricevi la lista di prodotti da MagazzinoServer
                List<String> orderStatuses = (List<String>) ordineInput.readObject();

                // Invia la lista di prodotti a Produttore
                output.writeObject(orderStatuses);
            } catch (IOException | ClassNotFoundException e) {
                Log.error("Errore durante l'inoltro della richiesta a MagazzinoServer: " + e.getMessage());
            }
        }

        private void forwardUpdateOrderStatus(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException, SQLException {
            try {
                Integer orderId = (Integer) input.readObject();
                String orderStatus = (String) input.readObject();

                // Inoltra la richiesta a MagazzinoServer
                String response = forwardUpdateOrderStatusToOrdineServer(orderId, orderStatus);

                // Invia la risposta a Produttore
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                output.writeObject(Constants.FAILURE);
            }
        }

        private String forwardUpdateOrderStatusToOrdineServer(Integer orderId, String orderStatus) throws IOException, ClassNotFoundException {
            try (
                    Socket orderSocket = new Socket(Constants.HOSTNAME, Constants.ORDINE_SERVER_PORT);
                    ObjectOutputStream orderOutput = new ObjectOutputStream(orderSocket.getOutputStream());
                    ObjectInputStream orderInput = new ObjectInputStream(orderSocket.getInputStream())
            ) {
                // Invia richiesta di aggiornamento a MagazzinoServer
                orderOutput.writeObject(Constants.ORDER_UPDATE_STATUS);
                orderOutput.writeObject(orderId);
                orderOutput.writeObject(orderStatus);

                // Leggi la risposta da MagazzinoServer
                return (String) orderInput.readObject();
            }
        }

        private void handleDeleteProductRequest(ObjectInputStream input, ObjectOutputStream output) {
            try {
                // Riceve l'ID del prodotto dal Produttore
                String productId = (String) input.readObject();

                // Inoltra la richiesta al MagazzinoServer
                String response = forwardDeleteProduct(productId);

                // Invia la risposta al Produttore
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                try {
                    output.writeObject(Constants.FAILURE);
                } catch (IOException ex) {
                    Log.error(ex.getMessage());
                }
            }
        }

        private String forwardInsertNewProductToMagazzinoServer(String codiceMagazzino, String productId, int productQuantity, String productName, String nomeMagazzino) throws IOException, ClassNotFoundException {
            try (
                    Socket magazzinoSocket = new Socket(Constants.HOSTNAME, Constants.MAGAZZINO_SERVER_PORT);
                    ObjectOutputStream magazzinoOutput = new ObjectOutputStream(magazzinoSocket.getOutputStream());
                    ObjectInputStream magazzinoInput = new ObjectInputStream(magazzinoSocket.getInputStream())
            ) {
                // Invia richiesta di inserimento a MagazzinoServer
                magazzinoOutput.writeObject(Constants.MAGAZZINO_INSERT_NEW_PRODUCT);
                magazzinoOutput.writeObject(codiceMagazzino);
                magazzinoOutput.writeObject(productId);
                magazzinoOutput.writeObject(productQuantity);
                magazzinoOutput.writeObject(productName);
                magazzinoOutput.writeObject(nomeMagazzino);

                // Leggi la risposta da MagazzinoServer
                return (String) magazzinoInput.readObject();
            }
        }

        private String forwardUpdateProductToMagazzinoServer(String productId, int newQuantity) throws IOException, ClassNotFoundException {
            try (
                    Socket magazzinoSocket = new Socket(Constants.HOSTNAME, Constants.MAGAZZINO_SERVER_PORT);
                    ObjectOutputStream magazzinoOutput = new ObjectOutputStream(magazzinoSocket.getOutputStream());
                    ObjectInputStream magazzinoInput = new ObjectInputStream(magazzinoSocket.getInputStream())
            ) {
                // Invia richiesta di aggiornamento a MagazzinoServer
                magazzinoOutput.writeObject(Constants.MAGAZZINO_UPDATE_PRODUCTS);
                magazzinoOutput.writeObject(productId);
                magazzinoOutput.writeObject(newQuantity);

                // Leggi la risposta da MagazzinoServer
                return (String) magazzinoInput.readObject();
            }
        }

        // Metodo che inoltra la richiesta di eliminazione al MagazzinoServer
        private String forwardDeleteProduct(String productId) {
            try (
                    Socket socket = new Socket(Constants.HOSTNAME, Constants.MAGAZZINO_SERVER_PORT);
                    ObjectOutputStream magazzinoOutput = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream magazzinoInput = new ObjectInputStream(socket.getInputStream())
            ) {
                // Invia la richiesta al MagazzinoServer
                magazzinoOutput.writeObject(Constants.MAGAZZINO_DELETE_PRODUCTS);
                magazzinoOutput.writeObject(productId);

                // Ricevi la risposta dal MagazzinoServer
                return (String) magazzinoInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                return Constants.FAILURE;
            }
        }

        private void forwardCloseRequestToMagazzinoServer() {
            try (
                    Socket magazzinoSocket = new Socket(Constants.HOSTNAME, Constants.MAGAZZINO_SERVER_PORT);
                    ObjectOutputStream magazzinoOutput = new ObjectOutputStream(magazzinoSocket.getOutputStream());
                    ObjectInputStream magazzinoInput = new ObjectInputStream(magazzinoSocket.getInputStream());
            ) {
                // Invia richiesta di chiusura a MagazzinoServer
                magazzinoOutput.writeObject(Constants.EXIT);

                // Attende conferma della chiusura
                String response = (String) magazzinoInput.readObject();
                if (Constants.SUCCESS.equals(response)) {
                    Log.info("Chiusura confermata da MagazzinoServer.");
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error("Errore durante la chiusura di MagazzinoServer: " + e.getMessage());
            }
        }

        private void forwardCloseRequestToOrderServer() {
            try (
                    Socket orderSocket = new Socket(Constants.HOSTNAME, Constants.ORDINE_SERVER_PORT);
                    ObjectOutputStream orderOutput = new ObjectOutputStream(orderSocket.getOutputStream());
                    ObjectInputStream orderInput = new ObjectInputStream(orderSocket.getInputStream());
            ) {
                // Invia richiesta di chiusura a OrdineServer
                orderOutput.writeObject(Constants.EXIT);

                // Attende conferma della chiusura
                String response = (String) orderInput.readObject();
                if (Constants.SUCCESS.equals(response)) {
                    Log.info("Chiusura confermata da OrdineServer.");
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.error("Errore durante la chiusura di OrdineServer: " + e.getMessage());
            }
        }

        private void forwardUpdateProductRequest(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException, SQLException {
            try {
                String productId = (String) input.readObject();
                int newQuantity = (int) input.readObject();

                // Inoltra la richiesta a MagazzinoServer
                String response = forwardUpdateProductToMagazzinoServer(productId, newQuantity);

                // Invia la risposta a Produttore
                output.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                Log.error(e.getMessage());
                output.writeObject(Constants.FAILURE);
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

    }


}
