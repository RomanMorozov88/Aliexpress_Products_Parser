package morozov.ru;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Запуск приложения.
 * На вход могут подаваться адрес и количество товаров,
 * которое нужно распарсить- по отдельности или всё сразу.
 * Ключи:
 * dir - фаил для сохранения.
 * count - количество товаров.
 */
public class MainClass {

    public static void main(String[] args) {

        String filePath = Paths.get("").toAbsolutePath().toString() + "\\parsed.csv";
        int count = 100;

        switch (args.length) {
            case 2:
                if (args[0].equals("dir")) {
                    filePath = args[1];
                } else if (args[0].equals("count")) {
                    count = Integer.parseInt(args[1]);
                }
                break;
            case 4:
                if (args[0].equals("count") && args[2].equals("dir")) {
                    count = Integer.parseInt(args[1]);
                    filePath = args[3];
                }
                break;
        }

        Parser parser = new Parser();
        ResultWriter writer = new ResultWriter();

        try {
            String resultString = parser.getReadyStringForWriting(count);
            writer.writeText(filePath, resultString);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
}
