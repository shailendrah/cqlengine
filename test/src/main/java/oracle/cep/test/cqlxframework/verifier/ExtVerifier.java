package oracle.cep.test.cqlxframework.verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.util.PathUtil;

public class ExtVerifier extends BaseVerifier {
	private static final boolean SHOW_COMMAND = false;
	
	private static final String DIFF_PROGRAM_KEY = "DIFF";
	private static final String DEFAULT_DIFF_PROGRAM = "/usr/bin/diff -w"; //"/usr/local/nde/ade/bin/tkdiff";	

	
	public ExtVerifier() {
	}

	@Override
	public void setArgs(String[] args) {
	}

	@Override
	public boolean needToCapture() {
		if (super.needToCapture()) return true;
		return false;
	}

	@Override
	public boolean verify() throws Exception {
		String diffprog = System.getProperty(DIFF_PROGRAM_KEY);
		if (diffprog == null) diffprog = System.getenv(DIFF_PROGRAM_KEY);
		if (diffprog == null) diffprog = DEFAULT_DIFF_PROGRAM;
		String[] cmds = diffprog.split(" ");
		List<String> commands = new LinkedList<String>();
		for (String cmd : cmds) commands.add(cmd);
		File refile = new File(m_goldenOutputFileName);
		if (!refile.exists()) 
		{
			addError(PathUtil.getFileName(m_goldenOutputFileName) + " does not exist.");
			return false;
		}
		File outfile = new File(m_outputFileName);
		if (!outfile.exists()) 
		{
			addError(PathUtil.getFileName(m_outputFileName) + " does not exist.");
			return false;
		}
		
		commands.add(refile.getAbsolutePath()); 
		commands.add(outfile.getAbsolutePath());
		if (SHOW_COMMAND)
		{
			StringBuilder b = new StringBuilder();
			for (String c : commands)
			{
				b.append(c);
				b.append(" ");
			}
			System.out.println(b.toString());
		}
		ProcessBuilder builder = new ProcessBuilder(commands);

	    //Map<String, String> env= builder.environment();'
	    //env.put();
	    //builder.directory(directory);
	    builder.redirectErrorStream(true);
	    final Process process = builder.start();
	    InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    StringBuilder buf = new StringBuilder();
	    int lc = 0;
	    while ((line = br.readLine()) != null) {
	    	lc++;
	    	if (buf != null)
	    	{
	    	  buf.append(line);
	    	  buf.append("\n");
	    	}
	    	if (lc > 10)
	    	{
	    	  buf.append("....");
	          addError(buf.toString());
	    	  buf = null;
	    	}
	    }
		return (m_diffs.size() == 0);
	}
}
