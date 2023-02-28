package server;

import java.io.*;
import java.net.Socket;

public class RequestHandler extends Thread {

    private final Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String msg = input.readUTF();
            if (msg.equalsIgnoreCase("exit")) {
                Server.shutdown();
                socket.close();
                return;
            }
            if (!msg.matches("exit|(PUT|GET|DELETE) .*")) {
                socket.close();
                return;
            }

            String[] parts = msg.split(" ");
            String request = parts[0];

            if (request.equals("PUT")) {
                String filename = parts.length == 2 ? parts[1] : newFileName();
                File file = new File(Server.getFilePath() + filename);
                int fileID;
                int length = input.readInt();
                byte[] content = new byte[length];
                input.readFully(content, 0, content.length);

                if (file.exists()) {
                    output.writeUTF("403");
                } else {
                    fileID = newFileID();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(content);
                        Server.getFilesMap().put(fileID, filename);
                        output.writeUTF(String.format("200 %d", fileID));
                    }
                }
            }
            if (request.equals("GET")) {
                File file = new File(getPathBy(parts));
                byte[] content;
                if (!file.exists()) {
                    output.writeUTF("404");
                } else {
                    try (FileInputStream fis = new FileInputStream(file);
                         BufferedInputStream bis = new BufferedInputStream(fis)
                    ) {
                        content = bis.readAllBytes();
                        output.writeUTF("200");
                        output.writeInt(content.length);
                        output.write(content);
                    }
                }
            }
            if (request.equals("DELETE")) {
                File file = new File(getPathBy(parts));
                if (!file.exists()) {
                    output.writeUTF("404");
                } else {
                    if (file.delete()) {
                        for (int id : Server.getFilesMap().keySet()) {
                            if (Server.getFilesMap().get(id).equals(file.getName())) {
                                Server.getFilesMap().remove(id);
                                break;
                            }
                        }
                        output.writeUTF("200");
                    } else {
                        output.writeUTF("403");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getPathBy(String[] parts) {
        if (parts[1].equals("BY_NAME")) {
            return Server.getFilePath() + parts[2];
        } else if (parts[1].equals("BY_ID")) {
            return Server.getFilePath() + Server.getFilesMap().get(Integer.parseInt(parts[2]));
        }
        return "";
    }

    private static int newFileID() {
        int id = 1;
        while (true) {
            if (!Server.getFilesMap().containsKey(id)) {
                return id;
            }
            id++;
        }
    }

    private static String newFileName() {
        int count = 1;
        String filename;
        while (true) {
            filename = String.format("New_File(%d)", count);
            if (!Server.getFilesMap().containsValue(filename)) {
                return filename;
            }
            count++;
        }
    }
}
