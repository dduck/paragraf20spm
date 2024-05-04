import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class GenerateJson {

    private static class filT
    {
        public String filuri;
        public String titel;
        public String versionsdato;
        public String fulltext;

        public String cleantext;
    }

    private static class spoergsmaalT extends filT
    {
        public String stilletAf;
        public String besvaretAf;
    }

    private static class sag
    {
        public spoergsmaalT spoergsmaal;
        public List<filT> svar = new ArrayList<>();
    }

    static Map<String, sag> sager = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Just read the file
        String filename = "/Users/andersjohansen/Tmp/Lucas/json/raw/titlerdatofiler.txt";
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String titel = reader.readLine();

        while (titel != null) {
            titel = titel.substring(1, titel.length() - 1);
            String versionsdato = reader.readLine();
            // Get rid of the quotes
            versionsdato = versionsdato.substring(1, versionsdato.length() - 1);

            String filurl = reader.readLine();
            if (filurl.equals("\"\""))
            {
                System.err.println("Skipping, no uri");
                // read next line
                titel = reader.readLine();
                continue;
            }

            String key = getKey(filurl);
            sag s = sager.computeIfAbsent(key, (k) -> new sag());
            Path p = Path.of(getTextFilePath(filurl));
            if (!Files.exists(p))
            {
                System.err.println("Skipping " + filurl + ", no text file from pdf");
                // read next line
                titel = reader.readLine();
                continue;
            }

            String fullText = Files.readString(p);

            // Let's sanitize the filurl for now
            filurl = filurl.substring(1, filurl.length() - 1);

            // Now to decide if this is a question or an answer

            List<String> lines = Files.readAllLines(p);

            if (isSporgsmaal(filurl))
            {
                spoergsmaalT sporgsmaal = new spoergsmaalT();
                sporgsmaal.versionsdato = versionsdato;
                sporgsmaal.titel = titel;
                sporgsmaal.filuri = filurl;
                sporgsmaal.fulltext = fullText;
                sporgsmaal.besvaretAf = getFirstLineStartingWith("Til:", lines);
                sporgsmaal.stilletAf = getFirstLineStartingWith("Stillet af:", lines);
                sporgsmaal.cleantext = cleanSporgsmaal(lines);
                s.spoergsmaal = sporgsmaal;
            }
            else
            {
                filT svar = new filT();
                svar.versionsdato = versionsdato;
                svar.titel = titel;
                svar.filuri = filurl;
                svar.fulltext = fullText;
                svar.cleantext = cleanSvar(lines);
                s.svar.add(svar);
            }

            // read next line
            titel = reader.readLine();
        }

        reader.close();

        Gson gson = new Gson();
        FileWriter fw = new FileWriter("/Users/andersjohansen/Tmp/Lucas/json/bigjson.json");
        gson.toJson(sager.values(), fw);
        fw.flush();
        fw.close();
    }

    private static String cleanSporgsmaal(List<String> lines) {
        final List<String> dest = new ArrayList<>();

        boolean collect = false;
        for (String l : lines)
        {
            String ltrim = l.trim();

            // Should we start collecting on the next line?
            if (!collect) {
                if (ltrim.startsWith("Stillet af:") || ltrim.startsWith("Omtrykt:") || ltrim.startsWith("OMTRYKT:")) {
                    collect = true;
                }
            }
            else
            {
                if (ltrim.startsWith("Svaret bedes sendt elektronisk til sp") || ltrim.startsWith("(Spm. nr. "))
                {
                    break;
                }

                if (ltrim.matches("^Side \\d+/\\d+"))
                {
                    continue;
                }

                dest.add(ltrim);
            }

        }

        if (dest.isEmpty())
        {
            System.err.println("Failed to clean question");
            return "** Could not automatically clean text please do so manually from fulltext **";
        }

        return String.join(" ", dest).trim();
    }

    private static String cleanSvar(List<String> lines) {
        final List<String> dest = new ArrayList<>();

        boolean collect = false;
        for (String l : lines)
        {
            String ltrim = l.trim();

            // Should we start collecting on the next line?
            if (!collect) {
                if (ltrim.equals("Svar") || ltrim.equals("Svar:") || ltrim.equals("Sv ar") || ltrim.equals("Sv ar:")
                    || ltrim.startsWith("Svar: ") || ltrim.endsWith(" Svar:") || ltrim.startsWith("Svar         ")
                    || ltrim.startsWith("Svar :")) {
                    collect = true;
                }
            }
            else
            {
                if (ltrim.startsWith("Med venlig hilsen"))
                {
                    break;
                }

                if (ltrim.matches("^Side \\d+/\\d+"))
                {
                    continue;
                }

                dest.add(ltrim);
            }

        }

        if (dest.isEmpty())
        {
            System.err.println("Failed to clean answer");
            return "** Could not automatically clean text please do so manually from fulltext **";
        }

        return String.join(" ", dest).trim();
    }

    private static String getFirstLineStartingWith(String s, List<String> lines)
    {
        try {
            for (String l : lines) {
                if (l.startsWith(s)) {
                    try
                    {
                        return l.substring(s.length() + 1).trim();
                    }
                    catch (Exception e)
                    {
                        return "";
                    }
                }
            }

            return "";
        }
        catch (Exception e)
        {
            return "";
        }
    };

    private static String getTextFilePath(String filuri) {
        try
        {
            String chopped = filuri.substring("https://www.ft.dk/samling/".length() + 1);
            chopped = chopped.substring(0, chopped.length() - 1);

            StringBuilder sb = new StringBuilder("/Users/andersjohansen/Tmp/Lucas/json/raw/pdf/");
            sb.append(chopped);
            sb.append(".txt");

            String filename = sb.toString();
            return filename;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private static boolean isSporgsmaal(String url)
    {
        String[] parts = url.split("/");
        return "spm".equals(parts[7]);
    }

    private static String getKey(String url)
    {
        // This part: https://www.ft.dk/samling/20211/spoergsmaal/s922/ of the url

        String[] parts = url.split("/");

        // We want to join the 7 first parts again with /
        String res = String.join("/", Arrays.copyOfRange(parts, 0, 7));
        return res;
    }
}
