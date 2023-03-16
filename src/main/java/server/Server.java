package server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static final int PORT = 4444;
    private static final String PATHNAME = "src/main/java/server/data/";
    private static final Map<Integer, String> files = new HashMap<>();
    private static final String SUCCESS = "200";
    private static final String ERROR = "404";

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server started!");
            while (true) {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<FileModel> jsonArray = mapper.readValue(new File("database.json"),
                        mapper.getTypeFactory().constructCollectionType(List.class, FileModel.class));
                for (FileModel model: jsonArray) {
                    files.put(model.getId(), model.getFilename());
                }
                CLIENTConnection connection = new CLIENTConnection(serverSocket.accept());
                connection.start();
            }
        } catch (Exception e) {
            System.out.println("Необходимый порт уже используется.");
            System.exit(0);
        }
    }


    public static class CLIENTConnection extends Thread {

        private final Socket socket;

        public CLIENTConnection(Socket client) {
            this.socket = client;
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())){
                while (true) {
                    String clientSelection = input.readUTF();
                    switch (clientSelection) {
                        case "GET" -> getFile(input.readUTF(), output);
                        case "PUT" -> saveFile(input, output);
                        case "DELETE" -> deleteFile(input.readUTF(), output);
                        case "EXIT" -> {
                            ObjectMapper mapper = new ObjectMapper();
                            ArrayList<FileModel> models = new ArrayList<>();
                            for (Integer key:
                                 files.keySet()) {
                                models.add(new FileModel(key, files.get(key)));
                            }
                            mapper.writeValue(new File("database.json"), models);
                            System.exit(0);
                        }
                        default -> output.writeUTF(ERROR);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void deleteFile(String fileName, DataOutputStream output) throws IOException {
            int deleted = -1;
            try {
                if (isInt(fileName)) {
                    if (files.containsKey(Integer.parseInt(fileName))){
                        deleted = Integer.parseInt(fileName);
                        fileName = files.get(deleted);
                        files.remove(deleted);
                    }
                } else {
                    for (Integer key:
                         files.keySet()) {
                        if (fileName.equals(files.get(key))) deleted = key;
                    }
                    if (deleted != -1) files.remove(deleted);
                }
                File file = new File(PATHNAME + fileName);
                if (file.delete() || deleted != -1){
                    output.writeUTF(SUCCESS);
                } else {
                    output.writeUTF(ERROR);
                }
            } catch (Exception e) {
                output.writeUTF(ERROR);
            }
        }

        public void saveFile(DataInputStream input, DataOutputStream output) throws IOException {
            try {
                String fileName = input.readUTF();
                if (fileName.equals("WRONG")) {
                    output.writeUTF(ERROR);
                    return;
                }
                OutputStream fileStream = new FileOutputStream(PATHNAME + fileName);
                long size = input.readInt();
                byte[] buffer = new byte[1024]; int bytes;
                while (size > 0 && (bytes = input.read(buffer)) != -1) {
                    fileStream.write(buffer, 0, bytes);
                    size -= bytes;
                }
                int id = 20;
                if (files.size() != 0) {
                    int max = -1;
                    for (Integer key:
                         files.keySet()) {
                        max = Math.max(max, key);
                    }
                    id = max+1;
                }
                files.put(id, fileName);
                output.writeUTF(SUCCESS + " " + id);
                fileStream.close();
            } catch (IOException ex) {
                output.writeUTF(ERROR);
            }
        }

        public void getFile(String fileName, DataOutputStream output) throws IOException {
            try {
                if (isInt(fileName)) {
                    fileName = files.get(Integer.parseInt(fileName));
                }

                File myFile = new File(PATHNAME + fileName);
                byte[] message = new byte[(int) myFile.length()];

                DataInputStream dataInput = new DataInputStream(new BufferedInputStream(new FileInputStream(myFile)));
                dataInput.readFully(message, 0, message.length);

                output.writeUTF(myFile.getName());
                output.writeInt(message.length);
                output.write(message);

                dataInput.close();
            } catch (Exception e) {
                output.writeUTF("404");
            }
        }
        
        private static boolean isInt(String s){
            try {
                Integer.parseInt(s);
                return true;
            } catch (Exception e){
                return false;
            }
        }
    }
}


