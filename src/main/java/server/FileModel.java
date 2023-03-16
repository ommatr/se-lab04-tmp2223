package server;

public class FileModel {
    private int id;
    private String filename;

    public FileModel(){
    }

    public FileModel(int id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                '}';
    }
}
