package server;

import ticketing.Ticketing;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {

    private static final int PORT = 12346;
    private static final int NUM_THREADS = 10;
    protected static boolean statoServer = false;

    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    Inserire:\s
                    [1] Per avviare il server\s
                    [2] Per fermare il server
                    [3] Per inserire azienda""");
            int scelta = scanner.nextInt();

            switch (scelta) {
                case 1:
                    System.out.println("case 1");
                    break;
                case 2:
                    statoServer = false;
                    System.out.println("Server fermato");
                    break;
                case 3:
                    // Logica per l'inserimento dell'azienda
                    System.out.println("case 3");
                    Ticketing ticketing = new Ticketing();
                    ticketing.inserisciTicket("prova", "prova", "prova", "prova", "prova", "prova");
                    break;

                default:
                    break;
            }
        }

    }

    public static void main(String[] args) throws IOException, SQLException {
        Server server = new Server(1234);
        server.start();
    }
}

