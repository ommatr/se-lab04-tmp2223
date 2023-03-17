package server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.*;

public class Main {


    private static final int PORT = 34522;

    public static void main(String[] args) {

        File fileIDName = new File("src/main/java/server/data/fileIDName");
        Map<Integer, String> idFileName = new HashMap<>();
        if (fileIDName.exists()) {
            try (FileInputStream f = new FileInputStream(fileIDName);
                 ObjectInputStream s = new ObjectInputStream(f)) {
                idFileName = (HashMap<Integer, String>) s.readObject();}
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();}}

        int idCounter = idFileName.keySet().stream().max(Comparator.comparingInt(value -> value)).orElse(0);


        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                Session session = new Session(server.accept(), idCounter, idFileName);
                session.start(); // does not block this server thread

            }

        } catch (IOException e) {
            e.printStackTrace();
        }}


}

class Session extends Thread {
    private final Socket socket;
    private int idCounter;
    private final Map<Integer, String> idFileName;

    public Session(Socket socketForClient, int idCounter, Map<Integer, String> idFileName) {
        this.socket = socketForClient;
        this.idCounter = idCounter;
        this.idFileName = idFileName;
    }

    private int searchID(String fileName){
        for(Map.Entry<Integer, String> entry: idFileName.entrySet()){
            if (Objects.equals(entry.getValue(), fileName)){
                return entry.getKey();}}
        return -1;}

    public static void saveMap(Map<Integer, String> idFileName) throws IOException {
        File fileIDName = new File("src/main/java/server/data/fileIDName");
        fileIDName.createNewFile();
        FileOutputStream f = new FileOutputStream(fileIDName);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(idFileName);
        s.flush();
    }


    public synchronized int createID()
    {
        return ++idCounter;
    }

    public void run() {
        try(DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream())){
            String command = input.readUTF();
            while (!command.equals("exit")){
                switch (command){
                    case "1", "3" -> {
                        int command2 = input.readInt();
                        String idName = input.readUTF();
                        String nameFile = (command2 == 1)
                                ?idName
                                :idFileName.get(Integer.parseInt(idName));
                        File file = new File("src/main/java/server/data/" + nameFile);
                        if(file.exists()) {
                            if (command.equals("1")){
                                byte[] message = Files.readAllBytes(file.toPath());
                                output.writeUTF("200");
                                output.writeInt(message.length);
                                output.write(message);}
                            else {
                                idFileName.remove((command2 == 1)
                                        ?searchID(idName)
                                        :Integer.parseInt(idName));
                                if(file.delete()){
                                    output.writeUTF("200");}}
                        }
                        else {output.writeUTF("404");}}

                    case "2" -> {
                        try {
                            String nameFile;
                            nameFile = input.readUTF();
                            int length = input.readInt();
                            byte[] message = new byte[length];
                            input.readFully(message, 0, message.length);
                            File theDir = new File("src/main/java/server/data");
                            if (!theDir.exists()){
                                theDir.mkdirs();}
                            try (FileOutputStream file = new FileOutputStream("src/main/java/server/data/" + nameFile)) {
                                file.write(message);}

                            int id = createID();
                            idFileName.put(id, nameFile);
                            output.writeUTF("200 " + id);
                        }
                        catch (IOException e) {
                            output.writeUTF("403");
                        }
                    }}
                command = input.readUTF();

            }
            saveMap(idFileName);
            System.exit(0);
        } catch (IOException  e2) {
            e2.printStackTrace();
        }


    }}

