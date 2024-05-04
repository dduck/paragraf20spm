import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BuildWgetScript {

    public static void main(String[] args) throws IOException {
        // Read the file
        String filename = "/Users/andersjohansen/Tmp/Lucas/json/raw/filelist.txt";
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);

        // For each line we want to do the following
        // 1. wget the file to a suitable directory
        // 2. extract the text from the PDF (perhaps later dahrling)

        PrintWriter pw = new PrintWriter("/Users/andersjohansen/Tmp/Lucas/json/raw/downloadpdf.sh");

        for (String l : lines)
        {
            StringBuilder sb = new StringBuilder("wget ");
            sb.append(l);
            sb.append(" -P ");
            String chopped = l.substring("https://www.ft.dk/samling/".length() + 1);
            chopped = chopped.substring(0, chopped.length() -1);
            String dir = chopped.substring(0, chopped.lastIndexOf('/') + 1);
            sb.append("pdf/");
            sb.append(dir);
            pw.println(sb.toString());

            StringBuilder sb2 = new StringBuilder("pdftotext -enc UTF-8 -layout ");
            String fileName = "pdf/" + chopped;

            sb2.append(fileName);
            sb2.append(" ");
            sb2.append(fileName);
            sb2.append(".txt");
            pw.println(sb2);
        }

        pw.flush();
        // Get rid of prefix
    }
}
