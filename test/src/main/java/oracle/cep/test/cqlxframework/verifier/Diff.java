package oracle.cep.test.cqlxframework.verifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Diff
{
  private static final int MAX_DIFF = 3;
  
  public static class Delta
  {
    String msg;
    
    public Delta(String m)
    {
      msg = m;
    }
    public String toString() {return msg;}
  }
  
  public static List<Delta> diff(List<String> base, List<String> ref)
  {
    Diff engine = new Diff();
    return engine.compareStrings(base, ref);
  }

  //TODO need more complicated diff logic or used external diff tool
  public List<Delta> compareStrings(List<String> base, List<String> ref)
  {
    List<Delta> results = new ArrayList<Delta>();
    if (base.size() != ref.size())
    {
      results.add(new Delta("different lines " + base.size() + " : expected " + ref.size()));
    }
    
    Collections.sort(base);
    Collections.sort(ref);
    
    for (int i = 0; i < base.size(); i++)
    {
      String s = i >= base.size() ? "" : base.get(i);
      s = s.replaceAll("\\r\\n|\\r|\\n", " ");
      s = s.trim();
      String r = i >= ref.size() ? "" : ref.get(i);
      r = r.replaceAll("\\r\\n|\\r|\\n", " ");
      r = r.trim();
      if (!(s.equals(r)))
      {
        if (results.size() < MAX_DIFF)
          results.add(new Delta("line#"+ (i+1) + " : " + s + "(" + r + ")"));
        else if (results.size() == MAX_DIFF)
        {
          results.add(new Delta("There are more than " + MAX_DIFF + " diffs. truncated"));
        }
      }
    }
    return results;
  }
}