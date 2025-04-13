import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {
    public static String readFile(String file)throws IOException{
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    public static void main(String[] args) {
        String file="0.c";
        try {
            String fileContent = readFile(file);
            System.out.println(fileContent);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
