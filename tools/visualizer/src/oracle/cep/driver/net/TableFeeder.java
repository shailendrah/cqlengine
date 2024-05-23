package oracle.cep.driver.net;

public interface TableFeeder {
    public void start ();    
    public void terminate ();
    public int  getNumTuplesSent ();
    public void pause ();
    public void unpause ();
    public void setRate (int rate);
    public boolean isAppTimestamped ();
}
