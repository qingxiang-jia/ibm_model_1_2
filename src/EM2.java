import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * This class is the implementation of IBM model 2, for Question 5.
 */
public class EM2
{
    String[] numToStr;                   // A mapping between numbers and tokens
    HashMap<String, Integer> strToNum;   // A mapping between tokens and numbers
    ArrayList<ArrayList<Integer>> enN;   // A collection of English sentences in numerical representation
    ArrayList<ArrayList<Integer>> deN;   // A collection of German sentences in numerical representation
    ArrayList<ArrayList<String>> en;     // A collection of English sentences
    ArrayList<ArrayList<String>> de;     // A collection of German sentences
    int maxEnSenLen;  // Maximum English sentence length
    int maxDeSenLen;  // Maximum German sentence length
    HashMap<List<String>, Double> t;     // T parameter for EM algorithm

    /**
     * This method estimates both T and Q parameter by EM algorithm.
     * @param S The number of iterations.
     * @param t The T parameter initialized by EM1.
     * @return T and Q parameters.
     */
    public EM2Return estimateParameters(int S, HashMap<List<String>, Double> t)
    {
        assert enN.size() == deN.size(); // Make sure we have the same number of English and German sentences
        final int n = enN.size();
        boolean init = false;

        /** Initialize counts and delta **/
        HashMap<List<Integer>, Double> c = new HashMap<List<Integer>, Double>();
        double[][][][] q  = new double[maxEnSenLen][maxDeSenLen][maxEnSenLen][maxDeSenLen];
        double[][][][] c4;
        double[][][]   c3;

        /** Initialize q parameter **/
        if (!init)
        {
            /** Iterate sentence by sentence **/
            for (int k = 0; k < n; k++)
            {
//                System.out.printf("k=%d\n", k);
                ArrayList<Integer> e = enN.get(k);
                ArrayList<Integer> f = deN.get(k);
                int l = e.size(), m = f.size();
                for (int j = 0; j < l; j++)
                    for (int i = 0; i < m; i++)
                        q[j][i][l][m] = 1.0/l;
            }
            init = true;
        }

        /** EM Algorithm **/
        for (int s = 0; s < S; s++)
        {
            /** Set all counts to 0 **/
            for (int k = 0; k < n; k++)
            {
                ArrayList<Integer> e = enN.get(k);
                ArrayList<Integer> f = deN.get(k);
                int l = e.size(), m = f.size();
                for (int j = 0; j < l; j++)
                    for (int i = 0; i < m; i++)
                    {
                        c.put(Arrays.asList(e.get(j), f.get(i)), 0.0);
                        c.put(Arrays.asList(e.get(j)), 0.0);
                    }
            }
            c4 = new double[maxEnSenLen][maxDeSenLen][maxEnSenLen][maxDeSenLen];
            c3 = new double             [maxDeSenLen][maxEnSenLen][maxDeSenLen];

            /** Update counts **/
            for (int k = 0; k < n; k++)
            {
                int m = deN.get(k).size(), l = enN.get(k).size();
                List<Integer> lm = Arrays.asList(l, m);
                ArrayList<Integer> e = enN.get(k), f = deN.get(k);
//                System.out.printf("s=%d k=%d\n", s, k);
                for (int i = 0; i < m; i++)
                {
                    double sum = 0.0;
                    /** Compute denominator of delta (d) **/
                    for (int j = 0; j < l; j++)
                        sum += q[j][i][l][m] * t.get(Arrays.asList(numToStr[f.get(i)], numToStr[e.get(j)]));
                    /** Update counts **/
                    for (int j = 0; j < l; j++)
                    {
                        /** Compute delta (d) **/
                        double d = (q[j][i][l][m] * t.get(Arrays.asList(numToStr[f.get(i)], numToStr[e.get(j)])) ) / sum;

                        /** Update counts **/
                        c.put(Arrays.asList(e.get(j), f.get(i)), c.get(Arrays.asList(e.get(j), f.get(i))) + d);
                        c.put(Arrays.asList(e.get(j)), c.get(Arrays.asList(e.get(j))) + d);

                        c4[j][i][l][m] += d;
                        c3   [i][l][m] += d;
                    }
                }
            }
            /** Update t and q **/
            for (int k = 0; k < n; k++)
            {
                int m = deN.get(k).size(), l = enN.get(k).size();
                ArrayList<Integer> e = enN.get(k), f = deN.get(k);
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < l; j++)
                    {
                        t.put(Arrays.asList(numToStr[f.get(i)], numToStr[e.get(j)]), c.get(Arrays.asList(e.get(j), f.get(i))) / c.get(Arrays.asList(e.get(j))));
                        q[j][i][l][m] = c4[j][i][l][m]/c3[i][l][m];
                    }
            }
        }
        return new EM2Return(t, q);
    }


    /**
     * This method deserialize the T parameter that has been saved by EM1.
     */
    @SuppressWarnings("unchecked")
    public HashMap<List<String>, Double> getT(String fn)
    {
        FileInputStream fis;
        ObjectInputStream in;
        HashMap<List<String>, Double> t = null;
        try
        {
            fis = new FileInputStream(fn);
            in = new ObjectInputStream(fis);
            t = (HashMap<List<String>, Double>) in.readObject();
            in.close();
        } catch (Exception ex) { ex.printStackTrace(); }
        this.t = t;
        return t;
    }

    /**
     * Convert the collection of English and German sentence to those with numerical representation to avoid
     * huge heap memory usage.
     * @param en The collection of English sentences.
     * @param de The collection of German sentences.
     */
    public void convertToNumArrays(ArrayList<ArrayList<String>> en, ArrayList<ArrayList<String>> de)
    {
        this.en = en;
        this.de = de;
        strToNum = new HashMap<String, Integer>();
        int id = 0;
        for (ArrayList<String> enLine: en) // Assign an ID for each English token
            for (String enToken: enLine)
                if (!strToNum.containsKey(enToken))
                    strToNum.put(enToken, id++);
        for (ArrayList<String> deLine: de) // Assign an ID for each German token
            for (String deToken: deLine)
                if (!strToNum.containsKey(deToken))
                    strToNum.put(deToken, id++);
        numToStr = new String[strToNum.size()];
        for (String word: strToNum.keySet())
            numToStr[strToNum.get(word)] = word;
        enN = new ArrayList<ArrayList<Integer>>();
        deN = new ArrayList<ArrayList<Integer>>();
        int localEnMax = -1;
        int localDeMax = -1;
        for (ArrayList<String> enLine: en)
        {
            ArrayList<Integer> enLineN = new ArrayList<Integer>();
            if (enLine.size() > localEnMax)
                localEnMax = enLine.size();
            for (String word: enLine)
                enLineN.add(strToNum.get(word));
            enN.add(enLineN);
        }
        for (ArrayList<String> deLine: de)
        {
            ArrayList<Integer> deLineN = new ArrayList<Integer>();
            if (deLine.size() > localDeMax)
                localDeMax = deLine.size();
            for (String word: deLine)
                deLineN.add(strToNum.get(word));
            deN.add(deLineN);
        }
        maxEnSenLen = localEnMax+1;
        maxDeSenLen = localDeMax+1;
    }

    /**
     * Generate an alignment for the kth sentence.
     * @param k The index of the sentence in the collection.
     * @param q Q parameter.
     * @return An array that represents the alignment.
     */
    public int[] getAlignment(int k, double[][][][] q)
    {
        ArrayList<Integer> enSen = enN.get(k);
        ArrayList<Integer> deSen = deN.get(k);
        int l = enSen.size(), m = deSen.size();
        int[] alignment = new int[m];
        List<Integer> lm = Arrays.asList(l, m);
        for (int i = 0; i < m; i++)
        {
            double max = -1;
            int maxIndex = -1;
            for (int j = 0; j < l; j++)
            {
                double candidate = q[j][i][l][m] * t.get(Arrays.asList(numToStr[deSen.get(i)], numToStr[enSen.get(j)]));
                if (candidate > max)
                {
                    max = candidate;
                    maxIndex = j;
                }
            }
            alignment[i] = maxIndex;
        }
        return alignment;
    }

    // Display results
    public static void main(String args[])
    {
        EM2 em2 = new EM2();
        CorpusGen gen = new CorpusGen();
        ArrayList<ArrayList<String>> enCorpus = gen.genCorpus("corpus.en", false);
        ArrayList<ArrayList<String>> deCorpus = gen.genCorpus("corpus.de", true);
        em2.convertToNumArrays(enCorpus, deCorpus);
        EM2Return em2Res = em2.estimateParameters(5, em2.getT("t.ser"));
        /** Print alignment for the top 20 sentences **/
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++)
        {
            int[] alignment = em2.getAlignment(i, em2Res.q);
            sb.setLength(0);
            /** Print English Solution **/
            for (int n = 1; n < enCorpus.get(i).size(); n++)
            {
                sb.append(enCorpus.get(i).get(n));
                sb.append(" ");
            }
            System.out.println(sb.toString().trim());
            sb.setLength(0);
            /** Print Deutsche **/
            for (String token: deCorpus.get(i))
            {
                sb.append(token);
                sb.append(" ");
            }
            System.out.println(sb.toString().trim());
            sb.setLength(0);
            /** Print alignment **/
            System.out.println(Arrays.toString(alignment));
            System.out.println("");
        }
        /** Serialize q, t parameter **/
        FileOutputStream fos;
        ObjectOutputStream out;
        try
        {
            fos = new FileOutputStream("em2_t.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(em2Res.t);
            out.close();
            fos = new FileOutputStream("em2_q.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(em2Res.q);
            out.close();
            fos = new FileOutputStream("strToNum.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(em2.strToNum);
            out.close();
            fos = new FileOutputStream("numToStr.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(em2.numToStr);
            out.close();
        } catch (Exception ex)  { ex.printStackTrace(); }
    }
}
