package decoster.cfdt.helper;

import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Decoster on 03/06/2016.
 */
public class LineParser {
    public static final String UTF8_BOM = "\uFEFF";

    public static List<String[]> parse(File file) {
        LineIterator it = null;
        ArrayList<String[]> output = new ArrayList<>();
        try {
            it = FileUtils.lineIterator(file, "UTF-8");
            boolean firstLine = true;
            while (it.hasNext()) {
                String line = it.nextLine();
                if (firstLine) {
                    line = removeUTF8BOM(line);
                    firstLine = false;
                }
                line = line.replaceAll("\\s", "");
                line = line.substring(0, line.indexOf("//") == -1 ? line.length() : line.indexOf("//"));
                String[] spl = line.indexOf(":") == -1 ? (new String[]{line, null}) : line.split(":", 2);


                output.add(spl);
                // do something with line
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            it.close();
        }

        return output;
    }

    public static void put(ArrayList<String[]> entries, File file) {
        try {
            PrintWriter writer = new PrintWriter(file);
            for (String[] entry : entries) {
                Log.d("TAG2", entry[0] + ":" + entry[1]);
                writer.println(entry[0] + ":" + entry[1]);
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
