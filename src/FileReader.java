import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileReader {
    public static String readFile(String file)throws IOException{
        return new String(Files.readAllBytes(Paths.get(file)));
    }

}
