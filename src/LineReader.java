import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LineReader
{
    public ArrayList<String> getList(String fn)
    {
        BufferedReader br;
        String line;
        ArrayList<String> lines = new ArrayList<String>();
        try
        {
            br = new BufferedReader(new FileReader(fn));
            while ((line = br.readLine()) != null)
                lines.add(line);
            br.close();
        } catch (IOException e) { e.printStackTrace(); }
        return lines;
    }
}
