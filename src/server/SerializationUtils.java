package server;

import java.io.*;

public class SerializationUtils {

    public static void serialize(Object object, String filename) {
        File file = new File(filename);
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(object);
        } catch (Exception e) {
            System.out.println("serialization failed");
        }
    }

    public static Object deserialize (String filename) {
        File file = new File(filename);
        Object object;
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            object = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
        return object;
    }
}
