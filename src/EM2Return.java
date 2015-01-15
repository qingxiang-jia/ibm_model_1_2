import java.util.HashMap;
import java.util.List;

public class EM2Return
{
    HashMap<List<String>, Double> t;
    double[][][][] q;

    public EM2Return(HashMap<List<String>, Double> t, double[][][][] q)
    {
        this.t = t;
        this.q = q;
    }
}
