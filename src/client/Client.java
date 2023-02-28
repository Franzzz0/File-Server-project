package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private byte[] receivedFile;

    public String sendRequest(String msg, byte[]... file) {
        try (Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            output.writeUTF(msg);
            System.out.println("The request was sent.");

            if (msg.matches("exit")) {
                return "";
            } else if (msg.matches("PUT.*")) {
                output.writeInt(file[0].length);
                output.write(file[0]);
                return input.readUTF();
            } else if (msg.matches("GET.*")) {
                String response = input.readUTF();
                if (response.equals("200")) {
                    int length = input.readInt();
                    receivedFile = new byte[length];
                    input.readFully(receivedFile, 0, length);
                }
                return response;
            } else if (msg.matches("DELETE.*")) {
                return input.readUTF();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    public byte[] getFile() {
        return receivedFile;
    }
}
