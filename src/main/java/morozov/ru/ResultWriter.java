package morozov.ru;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ResultWriter {

    public void writeText(String outputFile, String text) {
        try (FileWriter writer = new FileWriter(outputFile, false)) {
            File f = new File(outputFile);
            if (!f.exists()) {
                f.createNewFile();
            }
            writer.write(text);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
