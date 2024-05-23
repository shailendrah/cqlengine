package oracle.cep.test.cqlxframework.verifier;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.interfaces.output.FileDestination;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.test.cqlxframework.IPostProcessor;
import oracle.cep.test.cqlxframework.IVerifier;
import oracle.cep.test.cqlxframework.IVerifier.Status;
import oracle.cep.util.PathUtil;

public abstract class BaseVerifier extends QueryOutputBase implements IVerifier {
	private static final int MAX_DIFFS = 10;

	protected String m_id;
	protected String m_outputFileName;
	protected String m_goldenOutputFileName;
	protected TupleValue m_lastTuple;
	protected boolean m_ignorets;
	protected boolean m_ignoreOrder;
	protected boolean m_convertts;
	protected boolean m_show = true;
	protected boolean m_genoutput = false;
	protected List<String> m_diffs;
	protected FileWriter m_outputFile;
	protected long m_count;
	protected IPostProcessor[] m_postProcessors;

	public BaseVerifier() {
		super(null);
		m_diffs = new LinkedList<String>();
	}

	@Override
	public void setShow(boolean b) {
		m_show = b;
	}

	@Override
	public void setGenOutputs(boolean b) {
		m_genoutput = b;
	}

	public String getGoldenOutputFileName() {
		return m_goldenOutputFileName;
	}

	@Override
	public void setPostProcessors(IPostProcessor[] processors) {
		m_postProcessors = processors;
	}

	@Override
	public void setIgnorets(boolean b) {
		m_ignorets = b;
	}

	@Override
	public void setConvertts(boolean b) {
		m_convertts = b;
	}

	@Override
	public void setIgnoreorder(boolean ignoreorder) {
		m_ignoreOrder = ignoreorder;
	}

	@Override
	public boolean needToCapture() {
		if (m_show || m_genoutput)
			return true;
		return false;
	}

	@Override
	public boolean isOnlineVerifier() {
		return false;
	}

	@Override
	public void setId(String id) {
		m_id = id;
	}

	@Override
	public void setGoldenOutputFile(String goldenOutputFile) {
		m_goldenOutputFileName = goldenOutputFile;
	}

	@Override
	public void setOutputFile(String ouputFileName) {
		m_outputFileName = ouputFileName;

	}

	@Override
	public String getMsg() {
		StringBuilder b = new StringBuilder();
		b.append(m_id);
		b.append(" ");
		if (m_diffs.size() != 0) {
			b.append(m_diffs.size());
			b.append(" difs - ");
			if (m_goldenOutputFileName != null)
				b.append(PathUtil.getFileName(m_goldenOutputFileName));
			b.append("\n");
			for (String s : m_diffs) {
				b.append(s);
				b.append("\n");
			}
		} else {
			appendSucMsg(b);
		}
		return b.toString();
	}

	protected void appendSucMsg(StringBuilder b) {
		b.append("suc");
	}

	public Status getStatus() {
		if (m_diffs.size() != 0) {
			return Status.FAIL;
		}
		return getSucStatus();
	}

	protected Status getSucStatus() {
		return Status.SUC;
	}

	protected boolean isErrorFull() {
		return m_diffs.size() > MAX_DIFFS;
	}

	protected void addError(String msg) {
		if (isErrorFull())
			return;
		StringBuilder dmsg = new StringBuilder();
		dmsg.append(m_id);
		dmsg.append(":");
		dmsg.append(m_count);
		dmsg.append(" - ");

		if (m_diffs.size() == MAX_DIFFS) {
			dmsg.append("... more than " + MAX_DIFFS + " diffs...");
		} else
			dmsg.append(msg);
		m_diffs.add(dmsg.toString());
	}

	public synchronized void log(TupleValue tuple) throws CEPException {
		m_lastTuple = tuple;
		String tuplestr = tupleToStr(tuple);
		int last = 0;
		int end = tuplestr.length();
		int count = 0;
		while (last < end) {
			int idx = tuplestr.indexOf("\n", last);
			if (idx < 0) {
				count++;
				last = end;
			} else {
				count++;
				last = idx + 1;
			}
		}
		// XML hack - there is empty lines
		if (count > 1)
			count++;
		m_count += count;
		synchronized (this) {
			this.notify();
		}

		if (m_genoutput) {
			try {
				if (m_outputFile == null) {
					m_outputFile = new FileWriter(m_outputFileName);
					writeHeader(tuple);
				} else {
					writeEvent(tuplestr);
				}
			} catch (Exception e) {
				throw new CEPException(ExecutionError.GENERIC_ERROR, e);
			}
		} else {
			if (m_show) {
				System.out.print(m_id + " " + tuplestr);
			}
		}
		verifyTuple(tuple, tuplestr);
	}

	protected void verifyTuple(TupleValue tuple, String tuplestr) {
	}

	protected String tupleToStr(TupleValue tuple) throws CEPException {
		QueueElement.Kind k;
		TupleKind kind = tuple.getKind();
		if (kind == TupleKind.PLUS)
			k = Kind.E_PLUS;
		else if (kind == TupleKind.MINUS)
			k = Kind.E_MINUS;
		else if (kind == TupleKind.UPDATE)
			k = Kind.E_UPDATE;
		else
			k = Kind.E_HEARTBEAT;
		String tuplestr = FileDestination.tupleToString(tuple, k,
				this.m_convertts);
		// strip batch
		int i = tuplestr.indexOf(';');
		if (i >= 0)
			tuplestr = tuplestr.substring(i + 2);
		if (this.m_ignorets) {
			i = tuplestr.indexOf(':');
			if (i >= 0)
				tuplestr = tuplestr.substring(i + 1);

		}
		return tuplestr;

	}


	@Override
	public void start() throws CEPException {
	}

	@Override
	public void init() throws Exception {

	}

	@Override
	public void end() throws CEPException {
		if (m_outputFile != null) {
			try {
				m_outputFile.close();
				m_outputFile = null;
			} catch (Exception e) {
				throw new CEPException(ExecutionError.GENERIC_ERROR, e);
			}
		}
	}

	@Override
	public void putNext(TupleValue tuple, Kind kind) throws CEPException {
		if (kind == Kind.E_PLUS)
			tuple.setKind(TupleKind.PLUS);
		else if (kind == Kind.E_MINUS)
			tuple.setKind(TupleKind.MINUS);
		else if (kind == Kind.E_UPDATE)
			tuple.setKind(TupleKind.UPDATE);
		else
			tuple.setKind(TupleKind.HEARTBEAT);
		log(tuple);
	}

	private void writeHeader(TupleValue tuple) throws Exception {
		StringBuilder b = new StringBuilder();
		int onattrs = tuple.getNoAttributes();
		for (int loc = 0; loc < onattrs; loc++) {
			if (loc > 0)
				b.append(", ");
			AttributeValue attr = tuple.getAttribute(loc);
			Datatype typ = attr.getAttributeType();
			switch (typ.kind) {
			case INT:
				b.append("i");
				break;
			case BIGINT:
				b.append("l");
				break;
			case FLOAT:
				b.append("f");
				break;
			case DOUBLE:
				b.append("d");
				break;
			case BIGDECIMAL:
				b.append("n ");
				b.append(typ.getPrecision());
				b.append(" ");
				b.append(typ.getLength());
				break;
			case BYTE:
				b.append("b ");
				b.append(typ.getLength());
				break;
			case CHAR:
				b.append("c ");
				b.append(typ.getLength());
				break;
			case BOOLEAN:
				b.append("o");
				;
				break;
			case TIMESTAMP:
				b.append("t");
				;
				break;
			case OBJECT:
				b.append("z");
				break;
			case INTERVAL:
				b.append("v");
				break;
			case XMLTYPE:
				b.append("x");
				break;
			default:
				LogUtil.severe(LoggerType.CUSTOMER, "unknown datatype used "
						+ attr.getAttributeType().name());
			}
		}
		b.append("\n");
		m_outputFile.write(b.toString());
		m_outputFile.flush();
	}

	private void writeEvent(String tuplestr) throws Exception {
		m_outputFile.write(tuplestr);
		m_outputFile.write("\n");
		m_outputFile.flush();
	}

	private void addValue(StringBuilder b, Object v1, Object v2) {
		String v1s, v2s;
		if (v1 instanceof char[])
			v1s = new String((char[]) v1);
		else
			v1s = v1 == null ? "null" : v1.toString();
		if (v2 instanceof char[])
			v2s = new String((char[]) v2);
		else
			v2s = v2 == null ? "null" : v2.toString();
		b.append(v1s);
		if (!v1s.equals(v2s)) {
			b.append("(");
			b.append(v2s);
			b.append(")");
		}
	}

	protected String getTupleDiffString(TupleValue tuple, TupleValue gtuple) {
		StringBuilder b = new StringBuilder();
		b.append("Output(Expected) ");
		addValue(b, tuple.getKind(), gtuple.getKind());
		b.append(" ");
		int onattrs = tuple.getNoAttributes();
		for (int loc = 0; loc < onattrs; loc++) {
			if (loc > 0)
				b.append(", ");
			try {
				AttributeValue attr = tuple.getAttribute(loc);
				switch (attr.getAttributeType().kind) {
				case INT:
					addValue(b, tuple.iValueGet(loc), gtuple.iValueGet(loc));
					break;
				case BIGINT:
					addValue(b, tuple.lValueGet(loc), gtuple.lValueGet(loc));
					break;
				case FLOAT:
					addValue(b, tuple.fValueGet(loc), gtuple.fValueGet(loc));
					break;
				case DOUBLE:
					addValue(b, tuple.dValueGet(loc), gtuple.dValueGet(loc));
					break;
				case BIGDECIMAL:
					addValue(b, tuple.nValueGet(loc), gtuple.nValueGet(loc));
					break;
				case BYTE:
					addValue(b, tuple.bValueGet(loc), gtuple.bValueGet(loc));
					break;
				case CHAR:
					addValue(b, tuple.cValueGet(loc), gtuple.cValueGet(loc));
					break;
				case BOOLEAN:
					addValue(b, tuple.boolValueGet(loc), gtuple
							.boolValueGet(loc));
					break;
				case TIMESTAMP:
					addValue(b, tuple.tValueGet(loc), gtuple.tValueGet(loc));
					break;
				case OBJECT:
					addValue(b, tuple.oValueGet(loc), gtuple.oValueGet(loc));
					break;
				case INTERVAL:
					addValue(b, tuple.intervalValGet(loc), gtuple
							.intervalValGet(loc));
					break;
				case XMLTYPE:
					addValue(b, tuple.xValueGet(loc), gtuple.xValueGet(loc));
					break;
				default:
					LogUtil.severe(LoggerType.CUSTOMER,
							"unknown datatype used "
									+ attr.getAttributeType().name());
				}
			} catch (CEPException e) {
				b.append(loc + "_XXX");
			}
			loc++;
		}
		return b.toString();
	}
}