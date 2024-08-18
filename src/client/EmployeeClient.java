package client;

import java.io.*;
import java.net.*;

public class EmployeeClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        System.out.println("Response: " + resp);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        EmployeeClient client = new EmployeeClient();
        client.startConnection("127.0.0.1", 1234);
        client.sendMessage("Hello Server!");
        client.stopConnection();
    }
}

