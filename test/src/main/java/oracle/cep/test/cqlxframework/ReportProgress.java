package oracle.cep.test.cqlxframework;

public interface ReportProgress
{
  void handleStartTest( String suite, String name );
  void handleAddError( String suite, String name, Throwable e);
  void handleAddFailure(String suite, String name, String msg);
  void handleEndTest( String suite, String name);
}
