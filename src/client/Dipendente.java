package client;

import utils.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
                //System.out.println(Constants.INSERT_NEW_TICKET);
                System.out.println(Constants.SHOW_TICKETS);
                //System.out.println(Constants.DELETE_TICKETS);
                System.out.println(Constants.EXIT);
                System.out.print(Constants.CHOICE);

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consuma la nuova linea rimasta dopo nextInt()

                switch (choice) {
                    case 2:
                        output.writeObject(Constants.VIEW_TICKETS);
                        List<String> tickets = (List<String>) input.readObject();
                        System.out.println(Constants.AVAILABLE_TICKETS);
                        for (String ticket : tickets) {
                            System.out.println(ticket);
                        }
                        break;
                    case 4:
                        System.out.println(Constants.ON_CLOSING_CLIENT_MESSAGE);
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
