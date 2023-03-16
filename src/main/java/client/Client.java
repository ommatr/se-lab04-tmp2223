package client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    private static Socket socket;
    private static final Scanner scanner = new Scanner(System.in);
    private static final String PATHNAME = "src/main/java/client/data/";
    private static final String HOST = "localhost";
    private static final int PORT = 4444;

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST, PORT);
        } catch (Exception e) {
            System.out.println("В данный момент невозможно подключится к серверу, повторите попытку позже.");
            System.exit(0);
        }

        try (DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())){
            while (true) {
                System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): > ");
                boolean bye = false;
                switch (scanner.nextLine()) {
                    case "1" -> {
                        output.writeUTF("GET");
                        output.writeUTF(Enter());
                        System.out.println("The request was sent!");
                        getFile(input);
                    }
                    case "2" -> {
                        output.writeUTF("PUT");
                        saveFile(enterTheFileName(), input, output);
                    }
                    case "3" -> {
                        output.writeUTF("DELETE");
                        output.writeUTF(Enter());
                        deleteFile(input);
                    }
                    case "exit" -> {
                        output.writeUTF("EXIT");
                        bye = true;
                        System.out.println("The request was sent!");
                    }
                    default -> {
                        output.writeUTF("WRONG OUTPUT");
                        System.out.println(input.readUTF());
                    }
                }
                if (bye) break;
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("По техническим причинам сервер завершил работу.");
        }
    }

    public static String Enter(){
        while (true) {
            System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): > ");
            switch (scanner.nextLine()){
                case "1" -> {
                    return enterTheFileName();
                }
                case "2" -> {
                    return enterTheFileID();
                }
                default -> {}
            }
            System.out.println("Wrong answer.");
        }
    }

    public static String enterTheFileName(){
        System.out.print("Enter name of the file: > ");
        return scanner.nextLine();
    }

    public static String enterTheFileID(){
        System.out.print("Enter id: > ");
        return scanner.nextLine();
    }

    private static void deleteFile(DataInputStream input) {
        try {
            String servAns = input.readUTF();
            switch (servAns) {
                case "200" -> System.out.println("The response says that this file was deleted successfully!");
                case "404" -> System.out.println("The response says that this file is not found!");
                default -> System.out.println("???");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String fileName, DataInputStream input, DataOutputStream output) throws IOException {
        try {
            File myFile = new File(PATHNAME + fileName);
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            System.out.print("Enter name of the file to be saved on server: > ");
            fileName = scanner.nextLine();
            if (fileName.equals("")) fileName = generateString();

            DataInputStream dataInput = new DataInputStream(new BufferedInputStream
                    (new FileInputStream(myFile)));
            byte[] message = new byte[(int) myFile.length()];
            dataInput.readFully(message);

            System.out.println("The request was sent!");
            output.writeUTF(fileName + extension);
            output.writeInt(message.length);
            output.write(message);

            String[] data = input.readUTF().split(" ");
            System.out.println("Response says that file is saved! ID = " + data[1]);
            dataInput.close();
        } catch (Exception e) {
            System.out.println("File does not exist!");
            output.writeUTF("WRONG");
        }
    }

    public static void getFile(DataInputStream clientData) {
        try {
            String fileName = clientData.readUTF();
            if (fileName.equals("404")) {
                System.out.println("File does not exists.");
                return;
            }
            OutputStream output = new FileOutputStream(PATHNAME + fileName);
            int size = clientData.readInt();
            byte[] buffer = new byte[1024]; int bytes;
            while (size > 0 && (bytes = clientData.read(buffer)) != -1) {
                output.write(buffer, 0, bytes);
                size -= bytes;
            }
            output.close();
            System.out.println("The file was downloaded! Specify a name for it: > " + fileName + "\n" +
                    "File saved on the hard drive!" );
        } catch (IOException ex) {
            System.out.println("Error. File had not be downloaded.");
        }
    }

    public static String generateString() {
        return UUID.randomUUID().toString().substring(0,4);
    }

}
