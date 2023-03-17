package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 34522;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Client started!");
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file, exit)");
            String command1 = scanner.nextLine();
            while (!command1.equals("exit")){
                switch (command1){
                    case "1" -> getFile(command1, input, output);
                    case "2" -> saveFile(command1, input, output);
                    case "3" -> deleteFile(command1, input, output);}


                System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file, exit)");
                command1 = scanner.nextLine();
                }

            output.writeUTF(command1);
            System.out.println("The request was sent.");
        } catch (java.net.SocketException e){
            System.out.println("Unable to get a response from the server");

        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void saveFile(String command1, DataInputStream input, DataOutputStream output) throws IOException, InterruptedException {
        System.out.print("Enter name of the file: ");
        String idName = scanner.nextLine();

        File file = new File("src/main/java/client/data/" + idName);
        if(file.exists()) {
            System.out.print("Enter name of the file to be saved on server: ");
            scanner.nextLine();
            String idName2 = scanner.nextLine();
            output.writeUTF(command1);
            output.writeUTF((idName2.length()>0)?idName2:idName);

            byte[] message = Files.readAllBytes(file.toPath());
            output.writeInt(message.length);
            output.write(message);

            String [] reply = input.readUTF().split(" ");
            if (Integer.parseInt(reply[0]) == 200){
                System.out.println("Response says that file is saved! ID = " + reply[1]);}
            else if (Integer.parseInt(reply[0]) == 403){
                System.out.println("The response says the file is not saved!");}}
        else {
            System.out.println("This file doesn't exist.");}}

    public static void getFile(String command1, DataInputStream input, DataOutputStream output) throws IOException {
        String idName = getDeleteDop(command1, output);
        String reply = input.readUTF();
        if (reply.equals("200")){
            int length = input.readInt();
            byte[] message = new byte[length];
            input.readFully(message, 0, message.length);
            System.out.print("The file was downloaded! Specify a name for it: ");
            String name = scanner.nextLine();
            try (FileOutputStream file = new FileOutputStream("src/main/java/client/data/" + ((name.length() > 0)?name:idName))) {
                file.write(message);}
            System.out.println("File saved on the hard drive!");}
        else if (reply.equals("404")){
            System.out.println("The response says that this file is not found!");
        }
    }
    public static void deleteFile(String command1, DataInputStream input, DataOutputStream output) throws IOException {
        getDeleteDop(command1, output);

        String reply = input.readUTF();
        if (reply.equals("200")){
            System.out.println("The response says that this file was deleted successfully!");}
        else if (command1.equals("3") && reply.equals("404")){
            System.out.println("The response says that this file is not found!");}}
    public static String getDeleteDop(String command1, DataOutputStream output) throws IOException {
        System.out.println("Do you want" + ((command1.equals("1"))?" to get":" to delete") + " the file by name or by id (1 - name, 2 - id)");
        String idName = "";
        int command2;
        command2 = scanner.nextInt();

        if (command2 == 1 || command2 == 2) {
            switch (command2) {
                case 1 -> System.out.print("Enter name: ");
                case 2 -> System.out.print("Enter id: ");}
            scanner.nextLine();
            idName = scanner.nextLine();
            output.writeUTF(command1);
            output.writeInt(command2);
            output.writeUTF(idName);
            System.out.println("The request was sent.");}
        return (command2==1)?idName:"";}
}

