import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * This class is the IBM model 1, for Question 4.
 */
public class EM1
{
    /**
     * This method estimates the parameters using EM algorithm.
     * @param en The collection of English sentences.
     * @param de The collection of Deutsch sentences.
     * @param S Specifies how many iterations for EM algorithm.
     * @return The r parameter as the result of the Em algorithm.
     */
    public HashMap<List<String>, Double> estimateParameters(ArrayList<ArrayList<String>> en, ArrayList<ArrayList<String>> de, int S)
    {
        // Make sure we have the same number of English and German sentences.
        assert en.size() == de.size();
        final int n = en.size();

        /** Calculate n(e) **/
        ArrayList<Set<String>> enSetList = new ArrayList<Set<String>>(); // A list of sets of English tokens for each sentence
        ArrayList<Set<String>> deSetList = new ArrayList<Set<String>>(); // A list of sets of German tokens for each sentence
        for (ArrayList<String> tokens: en) // Generate a set of tokens for each English sentence
        {
            Set<String> tokenSet = new HashSet<String>();
            for (String token: tokens)
                tokenSet.add(token);
            enSetList.add(tokenSet);
        }
        for (ArrayList<String> tokens: de) // Generate a set of tokens for each German sentence
        {
            Set<String> tokenSet = new HashSet<String>();
            for (String token: tokens)
                tokenSet.add(token);
            deSetList.add(tokenSet);
        }
        HashMap<String, Set<String>> eNInter = new HashMap<String, Set<String>>();
        HashMap<String, Integer> eN = new HashMap<String, Integer>(); // The data structure assigns initial values for t parameters
        for (int i = 0; i < enSetList.size(); i++) // Compute 1/n(e) for each English word
        {
            Set<String> line = enSetList.get(i);
            Iterator<String> iter = line.iterator();
            while (iter.hasNext())
            {
                String token = iter.next();
                if (eNInter.containsKey(token))
                    eNInter.get(token).addAll(deSetList.get(i));
                else
                    eNInter.put(token, new HashSet<String>(deSetList.get(i)));
            }
        }
        for (String token: eNInter.keySet()) // Assign correct initial t parameter into eN
            eN.put(token, eNInter.get(token).size());

        /** Initialize counts and delta **/
        HashMap<List<String>, Double> c = new HashMap<List<String>, Double>();
        HashMap<List<String>, Double> t = new HashMap<List<String>, Double>();

        /** Initialize t **/
        boolean init = false;
        if (!init)
        {
            for (int k = 0; k < n; k++)
            {
//                System.out.printf("init k=%d\n", k);
                int m = de.get(k).size(), l = en.get(k).size();
                ArrayList<String> e = en.get(k), f = de.get(k);
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < l; j++)
                        t.put(Arrays.asList(f.get(i), e.get(j)), 1.0 / (double) eN.get(e.get(j)));
            }
        }

        /** EM Algorithm **/
        for (int s = 0; s < S; s++)
        {
            /** Set count to 0 **/
            for (int k = 0; k < n; k++)
            {
                int m = de.get(k).size(), l = en.get(k).size();
                ArrayList<String> e = en.get(k), f = de.get(k);
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < l; j++)
                    {
                        c.put(Arrays.asList(e.get(j), f.get(i)), 0.0);
                        c.put(Arrays.asList(e.get(j)), 0.0);
                    }
            }
            /** Compute counts **/
            for (int k = 0; k < n; k++)
            {
                int m = de.get(k).size(), l = en.get(k).size();
                ArrayList<String> e = en.get(k), f = de.get(k);
//                System.out.printf("s=%d k=%d\n", s, k);
                for (int i = 0; i < m; i++)
                {
                    double sum = 0.0;
                    for (int j = 0; j < l; j++)
                        sum += t.get(Arrays.asList(f.get(i), e.get(j))); // Denominator of delta
                    for (int j = 0; j < l; j++)
                    {
                        double d = t.get(Arrays.asList(f.get(i), e.get(j))) / sum; // Delta value

                        c.put(Arrays.asList(e.get(j), f.get(i)), c.get(Arrays.asList(e.get(j), f.get(i))) + d);
                        c.put(Arrays.asList(e.get(j)), c.get(Arrays.asList(e.get(j))) + d);
                    }
                }
            }
            /** Update t parameters for each iteration of EM algorithm **/
            for (int k = 0; k < n; k++)
            {
                int m = de.get(k).size(), l = en.get(k).size();
                ArrayList<String> e = en.get(k), f = de.get(k);
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < l; j++)
                        t.put(Arrays.asList(f.get(i), e.get(j)), c.get(Arrays.asList(e.get(j), f.get(i))) / c.get(Arrays.asList(e.get(j))));
            }
        }
        return t;
    }

    /**
     * This method returns a set of candidates for each word in devwords.txt .
     * @param t T parameters in EM algorithm.
     * @param e The word to find candidates for.
     * @param limit The maximum number of candidates wanted.
     * @return A list of TupleT object.
     */
    public List<TupleT> getCandidates(HashMap<List<String>, Double> t, String e, int limit)
    {
        ArrayList<TupleT> filteredT = new ArrayList<TupleT>();
        Iterator<List<String>> iter = t.keySet().iterator();
        while (iter.hasNext())
        {
            List<String> key = iter.next();
            if (key.get(1).equals(e))
                filteredT.add(new TupleT(key.get(0), key.get(1), t.get(key)));
        }
        Collections.sort(filteredT); // Sort in descending order
        return filteredT.subList(0, Math.min(limit, filteredT.size()));
    }

    /**
     * Compute the alignment for the collection of German sentence (to the collection of English sentences).
     * @param enSen A collection of English sentences.
     * @param deSen A collection of German sentences.
     * @param t T parameters.
     * @return An array of int that represents the alignment.
     */
    public int[] getAlignment(ArrayList<String> enSen, ArrayList<String> deSen, HashMap<List<String>, Double> t)
    {
        int[] allignment = new int[deSen.size()];
        for (int i = 0; i < deSen.size(); i++)
        {
            double max = -1;
            int maxIndex = -1;
            for (int j = 0; j < enSen.size(); j++)
            {
                double candidate = t.get(Arrays.asList(deSen.get(i), enSen.get(j)));
                if (candidate > max)
                {
                    max = candidate;
                    maxIndex = j;
                }
            }
            allignment[i] = maxIndex;
        }
        return allignment;
    }

    // Display results
    public static void main(String args[])
    {
        EM1 em1 = new EM1();
        CorpusGen gen = new CorpusGen();
        ArrayList<ArrayList<String>> enCorpus = gen.genCorpus("corpus.en", false);
        ArrayList<ArrayList<String>> deCorpus = gen.genCorpus("corpus.de", true);
        HashMap<List<String>, Double> t = em1.estimateParameters(enCorpus, deCorpus, 5); // 4 min
        LineReader reader = new LineReader();
        ArrayList<String> enWords = reader.getList("devwords.txt");
        /** Print top 10 candidates for each word in devwords.txt **/
        for (String enWord : enWords)
        {
            List<TupleT> candidates = em1.getCandidates(t, enWord, 10);
            System.out.printf("%s\n", enWord);
            System.out.println(candidates.toString());
            System.out.printf("\n");
        }
        StringBuilder sb = new StringBuilder();
        /** Print English, Deutsche, and alignment **/
        for (int i = 0; i < 20; i++)
        {
            int[] alignment = em1.getAlignment(enCorpus.get(i), deCorpus.get(i), t);
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
        /** Serialize t parameter **/
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream("t.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(t);
            out.close();
        } catch (Exception ex)  { ex.printStackTrace(); }
    }
}
