import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class is for Question 6.
 */
public class Aligner
{
    HashMap<String, Integer> strToNum;  // A mapping between token and number
    String[] numToStr;                  // A mapping between number and token
    HashMap<List<String>, Double> t;    // T parameter generated by EM algorithm
    double[][][][] q;                   // Q parameter generated by EM algorithm
    CorpusGen gen;                      // Generate a collection of sentences by reading from files.
    final double nInf = -10e10;         // A large negative number

    public Aligner(HashMap<String, Integer> strToNum, String[] numToStr, HashMap<List<String>, Double> t, double[][][][] q)
    {
        this.strToNum = strToNum;
        this.numToStr = numToStr;
        this.t = t;
        this.q = q;
        gen = new CorpusGen();
    }

    /**
     * Generate alignment based on the results of IBM model 2.
     * @param scrEnFn The file name of the scrambled English sentences.
     * @param oriDeFn The file name of the original German sentences.
     */
    public void align(String scrEnFn, String oriDeFn)
    {
        /** Read in both files **/
        ArrayList<ArrayList<String>> scrEn = gen.genCorpus(scrEnFn, true);
        ArrayList<ArrayList<String>> oriDe = gen.genCorpus(oriDeFn, true);
        assert scrEn.size() == oriDe.size(); // Make sure we have the same number of sentences for both languages.
        /** Convert both files in to numbers **/
        int[][] scrEnN = new int[scrEn.size()][];
        int[][] oriDeN = new int[oriDe.size()][];
        for (int s = 0; s < scrEn.size(); s++)
        {
            scrEnN[s] = new int[scrEn.get(s).size()];
            for (int w = 0; w < scrEn.get(s).size(); w++)
                if (strToNum.containsKey(scrEn.get(s).get(w)))
                    scrEnN[s][w] = strToNum.get(scrEn.get(s).get(w));
                else // new word
                    scrEnN[s][w] = -1;
        }
        for (int s = 0; s < oriDe.size(); s++)
        {
            oriDeN[s] = new int[oriDe.get(s).size()];
            for (int w = 0; w < oriDe.get(s).size(); w++)
                if (strToNum.containsKey(oriDe.get(s).get(w)))
                    oriDeN[s][w] = strToNum.get(oriDe.get(s).get(w));
                else // new word
                    oriDeN[s][w] = -1;
        }
        /** For each German sentence, find the best aligned English sentence **/
        int[] order = new int[scrEnN.length];
        int k = 0;
        for (int[] dSen: oriDeN)
        {
            order[k] = findBestAlign(dSen, scrEnN);
//            System.out.printf("Find alignment for %d\n", k);
            k++;
        }
        StringBuilder sb = new StringBuilder();
        for (int g = 0; g < oriDe.size(); g++)
            printSen(scrEn.get(order[g]), sb);
    }

    /**
     * Print out sentence in correct form.
     * @param sen The sentence.
     * @param sb  StringBuilder.
     */
    private void printSen(ArrayList<String> sen, StringBuilder sb)
    {
        sb.setLength(0);
        for (String token: sen)
        {
            sb.append(token);
            sb.append(" ");
        }
        System.out.println(sb.toString().trim());
    }

    /**
     * Find the correct alignment based on the results of IBM model 2.
     * @param dSen The german sentence.
     * @param scrEnN The scrambled English sentences.
     * @return The integer that represents the alignment.
     */
    private int findBestAlign(int[] dSen, int[][] scrEnN)
    {
        int m = dSen.length;
        int bestAlign = 0;
        double bestSenResult = -Double.MAX_VALUE;
        for (int k = 0; k < scrEnN.length; k++) // Try to align with each English sentence
        {
            int[] eSen = scrEnN[k];
            int l = eSen.length;
            double senMax = 0.0;
            for (int dw = 0; dw < dSen.length; dw++) // For each german word in a given sentence...
            {
                double localMax = -Double.MAX_VALUE;
                for (int ew = 0; ew < eSen.length; ew++) // For each possible alignment in a given English sentence...
                {
                    double qVal;
                    if (q[ew][dw].length < l || q[ew][dw][l].length < m) // In case of unseen sentence length(s)
                        qVal = 0;
                    else
                        qVal = q[ew][dw][l][m];
                    double tVal;
                    if (dSen[dw] == -1 || eSen[ew] == -1) // In case of unseen words
                        tVal = 0;
                    else
                    {
                        List<String> tKey = Arrays.asList(numToStr[dSen[dw]], numToStr[eSen[ew]]);
                        if (t.containsKey(tKey))
                            tVal = t.get(tKey);
                        else
                            tVal = 0;
                    }
                    double termResult = qVal * tVal;
                    if (termResult == 0)
                        termResult = nInf; // If zero, set it to a big negative number
                    else
                        termResult = Math.log(qVal * tVal);
                    if (termResult > localMax)
                        localMax = termResult;
                }
                senMax += localMax;
            }
            if (senMax > bestSenResult)
            {
                bestSenResult = senMax;
                bestAlign = k;
            }
        }
        return bestAlign;
    }

    // Display results
    @SuppressWarnings("unchecked")
    public static void main(String args[])
    {
        /** Deserialize numToStr, strToNum, t, q **/
        FileInputStream fis;
        ObjectInputStream in;
        HashMap<String, Integer> strToNum = null;
        String[] numToStr = null;
        HashMap<List<String>, Double> t = null;
        double[][][][] q = null;
        try
        {
//            System.out.printf("Deserializing numToStr\n");
            fis = new FileInputStream("numToStr.ser");
            in = new ObjectInputStream(fis);
            numToStr = (String[]) in.readObject();
            in.close();
//            System.out.printf("Deserializing strToNum\n");
            fis = new FileInputStream("strToNum.ser");
            in = new ObjectInputStream(fis);
            strToNum = (HashMap<String, Integer>) in.readObject();
            in.close();
//            System.out.printf("Deserializing t\n");
            fis = new FileInputStream("em2_t.ser");
            in = new ObjectInputStream(fis);
            t = (HashMap<List<String>, Double>) in.readObject();
            in.close();
//            System.out.printf("Deserializing q\n");
            fis = new FileInputStream("em2_q.ser");
            in = new ObjectInputStream(fis);
            q = (double[][][][]) in.readObject();
            in.close();
        } catch (Exception ex) { ex.printStackTrace(); }
        Aligner aligner = new Aligner(strToNum, numToStr, t, q);
        aligner.align("scrambled.en", "original.de");
    }
}