import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TupleT implements Comparable<TupleT>
{
    String f, e;
    Double val;
    NumberFormat formatter;

    public TupleT(String f, String e, Double val)
    {
        this.f = f;
        this.e = e;
        this.val = val;
        formatter = new DecimalFormat("#0.00000000000000000");
    }

    public int compareTo(TupleT otherTuple) // Sort in descending order
    {
        if (this.val < otherTuple.val)
            return 1;
        else if (this.val == otherTuple.val)
            return 0;
        else
            return -1;
    }

    public String toString()
    {
        return "(\'"+f+"\', "+formatter.format(val)+")";
    }
}
