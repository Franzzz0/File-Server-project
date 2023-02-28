package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private static final String FILES_PATH = ".\\src\\server\\data\\";
    private static final String FILES_MAP_PATH = FILES_PATH + "files.data";
    private static volatile Map<Integer, String> files;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static ServerSocket server;

    //path: .\src\server\data\ OR .\File Server\task\src\server\data\

    public static void start() throws Exception {
        files = loadFilesMap();
        server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS));
        System.out.println("Server started!");

        while(!server.isClosed()) {
            Socket socket;
            try {
                socket = server.accept();
            } catch (SocketException e) {
                break;
            }
            executor.submit(new RequestHandler(socket));
        }
    }

    public static void shutdown() {
        try {
            executor.shutdownNow();
            SerializationUtils.serialize(files, FILES_MAP_PATH);
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilePath() {
        return FILES_PATH;
    }

    public static Map<Integer, String> getFilesMap() {
        return files;
    }

    private static HashMap<Integer, String> loadFilesMap() {

        Object map = SerializationUtils.deserialize(FILES_MAP_PATH);
        if (map != null) {
            return (HashMap<Integer, String>) map;
        }
        return new HashMap<>();
    }
}
