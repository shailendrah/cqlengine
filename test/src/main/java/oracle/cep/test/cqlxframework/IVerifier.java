package oracle.cep.test.cqlxframework;

import oracle.cep.interfaces.output.QueryOutput;

public interface IVerifier extends QueryOutput {
	enum Status {PROCESSING, FAIL, SUC}
	
	boolean needToCapture();
	boolean isOnlineVerifier();
	void setId(String id);
	void setShow(boolean b);
	void setGenOutputs(boolean b);
    void setIgnorets(boolean b);
	void setIgnoreorder(boolean ignoreorder);
	void setConvertts(boolean b);
	void setOutputFile(String outputFile);
	void setGoldenOutputFile(String outputFile);
	void setArgs(String[] arg);
	void setPostProcessors(IPostProcessor[] processors);
	void init() throws Exception;
	boolean verify() throws Exception;
	Status getStatus();
	String getMsg();
}
