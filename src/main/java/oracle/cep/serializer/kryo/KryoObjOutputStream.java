package oracle.cep.serializer.kryo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.Arrays;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class KryoObjOutputStream extends ObjectOutputStream {
    KryoSerializerFactory fac;
    Kryo kryo;
    Output kryoOutput;
    
    public KryoObjOutputStream(KryoSerializerFactory fac, OutputStream out) throws IOException {
        super();
        this.fac = fac;
        kryo = fac.get();
        kryoOutput = new Output(out);
        //kryoOutput = new UnsafeMemoryOutput(out);
    }

    @Override 
    public void reset() throws IOException
    {
        kryoOutput.clear();
    }

    @Override 
    public void flush()
    {
        kryoOutput.flush();
    }

    @Override 
    public void close()
    {
        kryoOutput.close();
        fac.release(kryo);
    }
    
    @Override protected void annotateClass(Class<?> cl) throws IOException { throw new UnsupportedOperationException(); }
    @Override protected void annotateProxyClass(Class<?> cl) throws IOException { throw new UnsupportedOperationException(); }
    @Override public void defaultWriteObject() throws IOException { throw new UnsupportedOperationException(); }
    @Override protected void drain() { throw new UnsupportedOperationException(); }
    @Override protected boolean enableReplaceObject(boolean enable) throws SecurityException {throw new UnsupportedOperationException(); }
    @Override public PutField putFields() {throw new UnsupportedOperationException(); }
    @Override protected Object replaceObject(Object obj) throws IOException {throw new UnsupportedOperationException(); }
    @Override public void useProtocolVersion(int version) throws IOException {throw new UnsupportedOperationException(); }
    @Override protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {throw new UnsupportedOperationException(); }
    @Override public void writeFields() throws IOException {throw new UnsupportedOperationException(); }
    @Override protected void writeStreamHeader() throws IOException {throw new UnsupportedOperationException(); }

    @Override 
    public void write(byte[] buf) throws IOException 
    {
        kryo.writeObject(kryoOutput, buf);
    }

    @Override 
    public void write(byte[] buf, int off, int len) throws IOException 
    {
        byte[] nbuf = Arrays.copyOfRange(buf, off, off+len+1);
        kryo.writeObject(kryoOutput, nbuf);
    }

    @Override 
    public void write(int val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }

    @Override 
    public void writeBoolean(boolean val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }

    @Override 
    public void writeBytes(String str) throws IOException 
    {
        kryo.writeObject(kryoOutput, str.getBytes());
    }
    
    @Override 
    public void writeChar(int val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }

    @Override 
    public void writeChars(String val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val.toCharArray() );
    }
    
    @Override 
    public void writeFloat(float val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    

    @Override 
    public void writeDouble(double val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    

    @Override 
    public void writeShort(int val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    

    @Override 
    public void writeInt(int val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    

    @Override 
    public void writeLong(long val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    

    @Override 
    protected void writeObjectOverride(Object val) throws IOException
    {
        kryo.writeClassAndObject(kryoOutput, val);
    }    

    @Override 
    public void writeUnshared(Object val) throws IOException
    {
        kryo.writeClassAndObject(kryoOutput, val);
    }    

    @Override 
    public void writeUTF(String val) throws IOException 
    {
        kryo.writeObject(kryoOutput, val);
    }    
}
