package client;

import java.io.*;
import java.util.Scanner;

public class UI {
    private final Scanner scanner;
    private final Client client;
    private final String LOCAL_PATH = ".\\src\\client\\data\\"; // ".\src\client\data\"

    public UI() {
        this.client = new Client();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file):");
            String input = scanner.nextLine();
            switch (input) {
                case "exit" -> {
                    client.sendRequest(input);
                    return;
                }
                case "1" -> getFile();
                case "2" -> sendFile();
                case "3" -> deleteFile();
            }
        }
    }

    private void deleteFile() {
        System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
        while (true) {
            String input = scanner.nextLine();
            String response;
            if (input.equals("1")) {
                response = client.sendRequest(String.format("DELETE BY_NAME %s", getFilename(false)));
            } else if (input.equals("2")) {
                response = client.sendRequest(String.format("DELETE BY_ID %s", getFileID()));
            } else {
                System.out.println("Unknown command.");
                continue;
            }
            System.out.println(processResponse(response, "DELETE"));
            break;
        }
    }

    private void getFile() {
        System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        while (true) {
            String input = scanner.nextLine();
            String response;
            if (input.equals("1")) {
                response = client.sendRequest(String.format("GET BY_NAME %s", getFilename(false)));
            } else if (input.equals("2")) {
                response = client.sendRequest(String.format("GET BY_ID %s", getFileID()));
            } else {
                System.out.println("Unknown command.");
                continue;
            }
            saveFile(response, client.getFile());
            break;
        }
    }

    private void saveFile(String response, byte[] file) {
        System.out.println(processResponse(response, "GET"));
        if (!response.matches("200.*")) {
            return;
        }
        String filename = scanner.nextLine();
        try (FileOutputStream fileOutputStream = new FileOutputStream(LOCAL_PATH + filename)) {
            fileOutputStream.write(file);
            System.out.println("File saved on the hard drive!");
        } catch (IOException e) {
            System.out.println("Cannot save file.");
        }
    }

    private void sendFile() {
        File file = new File(LOCAL_PATH + getFilename(true));
        if (!file.isFile()) {
            System.out.println("File not found.");
            return;
        }
        byte[] fileData;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileData = fileInputStream.readAllBytes();
            String response = client.sendRequest(String.format("PUT %s", getFilename(false)), fileData);
            System.out.println(processResponse(response, "PUT"));
        } catch (IOException e) {
            System.out.println("Cannot read file.");
        }
    }

    private String getFilename(boolean local) {
        System.out.println(local ? "Enter name of the file: " : "Enter name of the file to be saved on server: ");
        return scanner.nextLine();
    }

    private String getFileID() {
        System.out.println("Enter id: ");
        return scanner.nextLine();
    }

    private String processResponse(String response, String input) {
        String fileNotFound = "The response says that this file is not found!";
        switch (input) {
            case "GET" -> {
                if (response.equals("404")) {
                    return fileNotFound;
                } else if (response.equals("200")) {
                    return "The file was downloaded! Specify a name for it: ";
                }
            }
            case "PUT" -> {
                if (response.matches("200.*")) {
                    return String.format("Response says that file is saved! ID = %s", response.substring(4));
                } else if (response.equals("403")) {
                    return "The response says that creating the file was forbidden!";
                }
            }
            case "DELETE" -> {
                if (response.equals("404")) {
                    return fileNotFound;
                } else if (response.equals("200")){
                    return "The response says that this file was deleted successfully!";
                } else {
                    return response;
                }
            }
        }
        return response;
    }
}
