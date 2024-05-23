package oracle.cep.test.cqlxframework.verifier;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.test.cqlxframework.IPostProcessor;
import oracle.cep.test.cqlxframework.IVerifier;
import oracle.cep.test.cqlxframework.IVerifier.Status;

public class IntVerifier extends BaseVerifier {
	boolean m_verifyOnTheFly = true;
	boolean m_nocapture = false;
	GoldenOutput m_goldenOutputSrc;
	protected List<String> m_capturedOutputs;

	public IntVerifier() {
	}

	@Override
	public boolean needToCapture() {
		return true;
	}

	@Override
	public boolean isOnlineVerifier()
	{
		return m_verifyOnTheFly;
	}
	
	@Override
	public void setArgs(String[] args) {
	}

	@Override
	public void init() throws Exception {
		if (m_outputFileName != null && m_outputFile == null) {
			m_count = 0;
		}
		if (this.m_ignoreOrder) {
			m_verifyOnTheFly = false;
		}
		m_capturedOutputs = new LinkedList<String>();
	}

	private void startGoldenOutput() throws CEPException {
		if (new File(m_goldenOutputFileName).exists()) {
			m_goldenOutputSrc = new GoldenOutput(m_id, m_goldenOutputFileName);
			m_goldenOutputSrc.setIgnorets(m_ignorets);
			m_goldenOutputSrc.setConvertts(m_convertts);
			m_goldenOutputSrc.start();
		} else {
			addError("no golden output : " + m_goldenOutputFileName);
		}
	}

	@Override
	public void start() throws CEPException {
		if (m_verifyOnTheFly) {
			startGoldenOutput();
		}
	}

	public void close() throws Exception {
		if (m_goldenOutputSrc != null) {
			m_goldenOutputSrc.close();
			m_goldenOutputSrc = null;
		}
	}

	@Override
	protected void appendSucMsg(StringBuilder b) {
		b.append(m_count);
	}

	@Override
	protected Status getSucStatus() {
		if (m_verifyOnTheFly)
		{
			if (m_goldenOutputSrc != null)
			{
				if (!m_goldenOutputSrc.isEof())
				{
					try
					{
						List<String> remains = m_goldenOutputSrc.read();
						int count = 0;
						for (String s : remains)
						{
							if (!s.isEmpty()) count++;
						}	
						if (count > 0)
						{
							StringBuilder b = new StringBuilder();
							b.append(m_id);
							b.append("/");
							b.append(m_goldenOutputFileName);
							b.append(" remains ");
							for (String s : remains)
							{
								if (s.isEmpty()) continue;
								b.append(s);
								b.append("\n");
							}
							addError(b.toString());
							return Status.FAIL;
						}
						return Status.SUC;
					} catch(Exception e)
					{
						addError(e.toString());
						return Status.FAIL;
					}
				}
			}
		}
		return Status.SUC;
	}

	@Override
	protected void verifyTuple(TupleValue tuple, String tupleStr) {
		m_capturedOutputs.add(tupleStr);
		if (m_verifyOnTheFly) {
			verifyOneTuple(tupleStr);
		}
	}

	private void verifyTuples(List<String> tuplestrs) {
		List<String> refs = null;
		if (m_goldenOutputSrc == null)
			return;
		IPostProcessor[] postProcessors = m_postProcessors;
		if (m_verifyOnTheFly)
		{
			try
			{
				startGoldenOutput();
				postProcessors = new IPostProcessor[1];
				postProcessors[0] = new Canonicalizer();
				System.out.println(m_id + " failed. so trying agin with canonicalizer");				
			} catch(Exception e)
			{
				System.out.println(e);
			}
		}
		if (postProcessors != null)
		{
			try
			{
				refs = m_goldenOutputSrc.read();
			}catch(Exception e)
			{
				addError("exception " + e.toString());
			}
			for (IPostProcessor processor : postProcessors)
			{
				try
				{
					//System.out.println(m_id + " " + processor.getClass().getSimpleName());
					tuplestrs = processor.postProcess(tuplestrs);
					refs = processor.postProcess(refs);
				} catch(Throwable e)
				{
					addError("postprocessor exception " + e.toString());
					e.printStackTrace();
				}
			}
			int idx = 0;
			while (true)
			{
				if (isErrorFull())
					break;
				String output = null;
				String ref = null;
				if (idx < tuplestrs.size())
					output = tuplestrs.get(idx);
				if (idx < refs.size())
					ref = refs.get(idx);
				idx++;
				if (output == null && ref == null)
					break;
				if (output != null && ref == null)
				{
					addError(output + "(null)");
				} else if (output == null && ref != null)
				{
					addError("null("+ref+")");
				} else if (!output.equals(ref)) {
					addError(output + "(" + ref + ")");
				}
			}
		} 
		else 
		{
			try {
				for (String output : tuplestrs)
				{
					verifyOneTuple(output);
				}
			} catch (Exception e) {
				addError("exception " + e.toString());
			}
		}
	}

	private void verifyOneTuple(String tuplestr) {
		if (isErrorFull())
			return;
		try {
			List<String> diffs = m_goldenOutputSrc.compare(tuplestr);
			if (diffs != null && diffs.size() > 0) {
				for (String s : diffs) {
					addError(s);
				}
			}
		} catch (Exception e) {
			addError("exception " + e.toString());
		}
	}

	@Override
	public boolean verify() throws Exception {
		if (m_nocapture)
		{
			return compareFiles();
		}
		if (!m_verifyOnTheFly) {
			startGoldenOutput();
			verifyTuples(m_capturedOutputs);
		}
		if (m_diffs.size() > 0)
			System.out.println(m_diffs.size() + " difs");
		return (m_diffs.size() == 0);
	}

	private boolean compareFiles() throws Exception {
		try {
			boolean r = true;
			GoldenOutput outputSrc = new GoldenOutput(m_id, m_outputFileName);
			outputSrc.start();
			GoldenOutput goutputSrc = new GoldenOutput(m_id,
					m_goldenOutputFileName);
			goutputSrc.start();
			while (true) {
				boolean oeof = outputSrc.isEof();
				boolean geof = outputSrc.isEof();
				if (oeof && geof)
					break;
				if (oeof) {
					addError("no more tuples from output : "
							+ outputSrc.getFileName());
					r = false;
					break;
				}
				if (geof) {
					addError("no more tuples from golden output : "
							+ goutputSrc.getFileName());
					r = false;
					break;
				}
				String sline = outputSrc.getNext(null, 1);
				List<String> diffs = goutputSrc.compare(sline);
				if (diffs != null && diffs.size() > 0) {
					for (String s : diffs) {
						addError(s);
					}
				}
				r = diffs.size() == 0;
			}
			outputSrc.close();
			return r;
		} catch (Exception e) {
			addError("exception " + e.toString());
			return false;
		}
	}

}
