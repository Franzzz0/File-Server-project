package server;

public class Main {
    public static void main(String[] args) {

        try {
            Server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}