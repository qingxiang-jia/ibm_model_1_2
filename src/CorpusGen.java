import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CorpusGen
{
    public ArrayList<ArrayList<String>> genCorpus(String fn, boolean foreign)
    {
        BufferedReader br;
        String line;
        ArrayList<ArrayList<String>> corpus = new ArrayList<ArrayList<String>>();
        try
        {
            br = new BufferedReader(new FileReader(fn));
            while ((line = br.readLine()) != null)
            {
                String[] tokens = line.split("\\s+");
                ArrayList<String> tokenList = new ArrayList<String>();
                if (!foreign)
                    tokenList.add("NULL");
                for (String token: tokens)
                    tokenList.add(token);
                corpus.add(tokenList);
            }
            br.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        return corpus;
    }

    // Test
    public static void main(String args[])
    {
        CorpusGen gen = new CorpusGen();
        ArrayList<ArrayList<String>> corpusEn = gen.genCorpus("corpus.en", false);
        for (ArrayList<String> tokens: corpusEn)
        {
            for (String token: tokens)
                System.out.print(token + " ");
            System.out.println("");
        }
    }
}
