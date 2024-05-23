package oracle.cep.test.cqlxframework.verifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.util.PathUtil;


public class GoldenOutput
{
  private String                                 m_filePath;
  private String                                 m_eventType;
  private BufferedReader 						 m_reader;
  private boolean						         m_ignorets;
  private boolean                                m_convertts;
  private boolean							     m_eof = false;
  
  public GoldenOutput(String evType, String filepath)
  {
    m_eventType = evType;

    m_filePath = filepath;
  }

  public String getFileName() {return PathUtil.getFileName(m_filePath);}
  public String getFilePath() {return m_filePath;}
  public String getEventType() {return m_eventType;}
  public void setIgnorets(boolean b) {m_ignorets = b;}
  public void setConvertts(boolean b) {m_convertts = b;}
  public boolean getConvertts() {return m_convertts;}
  
  public void start() throws CEPException
  {
    try
	{
    	m_reader = new BufferedReader(new FileReader(m_filePath));
	}
    catch(IOException e)
    {
    	throw new CEPException(ExecutionError.GENERIC_ERROR, e);
    }
  }

  public void close() throws CEPException
  {
	 try
		{
	  	  m_reader.close();
		}
	    catch(IOException e)
	    {
	    	throw new CEPException(ExecutionError.GENERIC_ERROR, e);
	    }
  }

  public boolean isEof()
  {
	  return m_eof;
  }
  
  public String getNext(String input, int lines) throws Exception
  {
	   if (m_eof) return null;
	   StringBuilder b = new StringBuilder();
	   int n = 0;
	   while (lines > 0)
	   {
		   String str = m_reader.readLine();
		   if (str == null)
		   {
			  m_eof = true;
			  break;
		   }
		   if (n == 0)
		   {
			//strip batch
			int i = str.indexOf(';');
			if (i >= 0) str = str.substring(i+2);
		   
			if (m_ignorets)
			{
				//strip ts
				i = str.indexOf(':');
				if (i >= 0) str = str.substring(i+1);
			}
		   } else b.append("\n");
		   b.append(str);
		   lines--;
		   n++;
	   }
	   if (b.length() == 0) return null;
	   if (input != null && input.endsWith("\n"))
	   {
		   b.append("\n");
		   m_reader.readLine();
	   }
	   return b.toString();
  }
  
  private int countLines(String tuplestr)
  {
	  	int lines = 0;
		int last=0;
		int end = tuplestr.length();
		while (last < end)
		{
			int idx = tuplestr.indexOf("\n", last);
			if (idx < 0) {
				lines++;
				last = end;
				break;
			} else {
				lines++;
				last = idx + 1;
			}
		}
		return lines;	  
  }
  
  public List<String> compare(String tuplestr) throws Exception
  {
	  int lines = countLines(tuplestr);
		List<String> diffs = new LinkedList<String>();
		String ref = getNext(tuplestr, lines);
		if (ref == null) {
			diffs.add(tuplestr+"(null)");
			return diffs;
		}
		//todo java diff utils
		if (!ref.equals(tuplestr)) {
			diffs.add(String.format("%s(%s)", tuplestr, ref));
		}
		return diffs;
  }

  public List<String> read() throws Exception {
	List<String> refs = new LinkedList<String>();
	while(true)
	{
		String ref = getNext(null, 1);
		if (ref == null)
			break;
		refs.add(ref);
	}
	return refs;
  }
}
