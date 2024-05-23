package oracle.cep.test.cqlxframework;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.test.cqlxframework.verifier.IntVerifier;
import oracle.cep.util.DebugUtil;
import junit.framework.TestCase;
import static org.junit.Assume.*;

public class CqlxTestCase extends TestCase 
{
  AbsCqlxTestSuite m_suite;
  String m_cqlxPath;
  boolean m_included = true;
  
  public CqlxTestCase(AbsCqlxTestSuite suite, String name, String cqlxPath)
  {
    super(name);
    m_suite = suite;
    m_cqlxPath = cqlxPath;
  }

  public String getCqlxPath() {
      return m_cqlxPath;
  }

  public void setInclude(boolean b) {
      m_included = b; 
  }

  @Override
  protected void runTest() throws Throwable
  {
      if (m_included) {
          CQLRunTest test = new CQLRunTest(m_suite.getRunner(), this);
          if (!test.start()) {
              fail("Timeout");
          }
      } else {
          //The intention was to make gradle report this to skipped.
          //But this does not work for the case of extending TestCase
          //https://issues.gradle.org/browse/GRADLE-1879
          //assumeTrue("Skipped", false);
          assertTrue("Skipped", true);
      }
  }
  
  // commands in rule
  enum Commands {ignoreexceptions, exceptions, verifier, postprocessors, verifierarg, ignorets, ignoreorder};

  IPostProcessor[] postProcessors;
  List<String> diffs;
  List<String> expectedExceptions;
  List<Throwable> unexpectedExceptions;
  String  verifier;
  String[]  verifierarg;
  boolean ignorets;
  boolean ignoreorder;
  boolean ignoreexceptions;
    
  public String toString()
  {
      StringBuilder b = new StringBuilder();
      b.append(getName());
      if (verifier != null) b.append(", "+verifier);
      if (ignorets) b.append(", ignore ts ");
      if (ignorets) b.append(", ignore order ");
      if (ignoreexceptions) b.append(", ignore exceptions ");
      if (postProcessors != null && postProcessors.length > 0)
      {
          b.append(", pp=");
          for (int i = 0; i < postProcessors.length; i++)
          {
              String p = postProcessors[i].getClass().getSimpleName();
              if (i > 0) b.append(";");
              b.append(p);
          }
      }
      return b.toString();
  }

  
  public IVerifier getVerifier() {
      if (verifier == null)
      {
          IntVerifier v = new IntVerifier();
          return v;
      }
      try
      {
          Class<?> cls = Class.forName(verifier);
          Constructor<?> ct = cls.getConstructor((Class<?>[])null);
          IVerifier ret = (IVerifier) ct.newInstance((Object[])null);
          ret.setArgs(verifierarg);
          return ret;
      }
      catch(Throwable e)
      {
          System.out.println(e);
          e.printStackTrace();
      }
      return null;
  }
  
  public IPostProcessor[] getPostProcessors() {return postProcessors;}
  
  public void setPostProcessors(String[] input)
  {
      postProcessors = new IPostProcessor[input.length];
      int i = 0;
      for (String processor : input)
      {
          try
          {
              Class<?> cls = Class.forName(processor);
              Constructor<?> ct = cls.getConstructor((Class<?>[])null);
              IPostProcessor ipp = (IPostProcessor) ct.newInstance((Object[])null);
              postProcessors[i++] = ipp;
          }
          catch(Throwable e)
          {
              System.out.println(e);
              e.printStackTrace();
          }           
      }
  }
  
  public boolean isIgnorets() {
      return ignorets;
  }

  public boolean isIgnoreorder() {
      return ignoreorder;
  }
  
  public boolean isIgnoreexceptions() {
      return ignoreexceptions;
  }
  
  public void set(String rule)
  {
      String[] items = rule.split(" ");
      for (int i = 0; i < items.length; i++)
      {
          String s = items[i];
          String[] v = s.split("=");
          if (v == null || v.length ==0)
          {
              throw new RuntimeException("invalid command in rule " + s + " - " + rule);
          }
          String c = v[0];
          String val = null;
          if (v.length > 1) val = v[1];
          Commands cmd = Commands.valueOf(c);
          if (cmd != null)
          {
              switch(cmd)
              {
              case ignoreexceptions:
                  ignoreexceptions = true;
                  break;
              
              case exceptions:
                  if (val == null)
                      throw new RuntimeException("invalid command in rule " + s + " - " + rule);
                  if (expectedExceptions == null)
                  {
                      expectedExceptions = new LinkedList<String>();
                  }
                  String[] exs = val.split(",");
                  for (String ex : exs)
                  {
                      expectedExceptions.add(ex);
                  }
                  break;
              case verifier:
                  if (val == null)
                      throw new RuntimeException("invalid command in rule " + s + " - " + rule);
                  verifier = val;
                  break;
              case verifierarg:
                  if (val == null)
                      throw new RuntimeException("invalid command in rule " + s + " - " + rule);
                  verifierarg = val.split(",");
                  break;
              case postprocessors:
                  if (val == null)
                      throw new RuntimeException("invalid command in rule " + s + " - " + rule);
                  setPostProcessors( val.split(",") );
                  break;
              case ignorets:
                  ignorets = true;
                  break;
              case ignoreorder:
                  ignoreorder = true;
                  ignorets = true;
                  break;
              }
          } else {
              throw new RuntimeException("unknown command in rule " + s + " - " + rule);
          }
      }
  }
  
  private String getName(Throwable e)
  {
      String name = e.getClass().getName();
      if (e instanceof CEPException)
      {
          CEPException ce = (CEPException) e;
          name = ce.getErrorCode().name();
      }
      return name;
  }
  
  public void addDiff(String s)
  {
      if (diffs == null)
      {
          diffs = new LinkedList<String>();
      }
      diffs.add(s);
  }
  
  public void addUnexpected(Throwable e)
  {
      if (unexpectedExceptions == null)
      {
          unexpectedExceptions = new LinkedList<Throwable>();
      }
      unexpectedExceptions.add(e);
  }
  
  public void clear(Throwable e)
  {
      if (expectedExceptions == null)
      {
          addUnexpected(e);
          return;
      }
      String name = getName(e);
      int n = expectedExceptions.indexOf(name);
      if (n < 0)
      {
          addUnexpected(e);
      } else {
          expectedExceptions.remove(n);
      }
  }
  
  public boolean isAllClear()
  {
      int n = 0;
      if (!ignoreexceptions)
      {
          if (expectedExceptions != null)
              n += expectedExceptions.size();
          if (unexpectedExceptions != null)
              n += unexpectedExceptions.size();
      }
      if (diffs != null)
          n += diffs.size();
      return (n == 0);
  }
  

  public List<Throwable> getUnexpectedExceptions()
  {
      return unexpectedExceptions;
  }
  
  public String getErrMsg()
  {
      StringBuilder b = new StringBuilder();
      if (diffs != null)
      {
          b.append("Diffs: " + diffs.size() + "\n");
          for (String s : diffs)
          {
              b.append(s);
              b.append("\n");
          }
          
      }
      if (!ignoreexceptions)
      {
          if (expectedExceptions != null && expectedExceptions.size() > 0)
          {
              b.append("Not-cleared exceptions :");
              b.append(expectedExceptions.toString());
              b.append("\n");
          }
          if (unexpectedExceptions != null)
          {
              b.append(" Unexpected exceptions: " + unexpectedExceptions.size() + " " + getName() + " : ");    
              int i = 0;
              for (Throwable e : unexpectedExceptions)
              {
                  if (i > 0) b.append(",");
                  String name = getName(e);
                  b.append(name);
                  i++;
              }
              b.append("\n");
              for (Throwable e : unexpectedExceptions)
              {
                  b.append("------------------------------\n");
                  String name = getName(e);
                  b.append(name);
                  b.append("\n");
                  b.append(e.toString());
                  b.append("\n");
                  b.append(DebugUtil.getStackTrace(e));
                  b.append("\n");
              }
          }
      }   
      return b.toString();
  }

  public int getDiffs() {
      if (diffs != null)
          return diffs.size();
      return 0;
  }


  
}
