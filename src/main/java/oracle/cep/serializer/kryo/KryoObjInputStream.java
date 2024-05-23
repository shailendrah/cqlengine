package oracle.cep.serializer.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class KryoObjInputStream extends ObjectInputStream {
    KryoSerializerFactory fac;
    Kryo kryo;
    Input kryoInput;
    
    public KryoObjInputStream(KryoSerializerFactory fac, InputStream in) throws IOException {
        super();
        this.fac = fac;
        kryo = fac.get();
        kryoInput = new Input(in);
        //kryoInput = new UnsafeMemoryInput(in);
    }

    @Override 
    public int available() throws IOException
    {
        return kryoInput.available();
    }
    
    @Override 
    public void close() throws IOException
    {
        kryoInput.close();
        fac.release(kryo);
    }
    
    @Override 
    public int skipBytes(int len) throws IOException
    {
        kryoInput.skip(len);
        return len;
    }

    @Override public void defaultReadObject() throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }
    @Override protected boolean enableResolveObject(boolean enable) throws SecurityException { throw new UnsupportedOperationException(); }
    @Override protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }
    @Override protected void readStreamHeader() throws IOException, StreamCorruptedException { throw new UnsupportedOperationException(); }
    @Override public GetField readFields() throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }
    @Override public String readLine() { throw new UnsupportedOperationException(); }
    @Override public int readUnsignedByte() throws IOException { throw new UnsupportedOperationException(); }
    @Override public int readUnsignedShort() throws IOException { throw new UnsupportedOperationException(); }
    @Override public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException { throw new UnsupportedOperationException(); }
    @Override protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }
    @Override protected Object resolveObject(Object obj) throws IOException { throw new UnsupportedOperationException(); }
    @Override protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }


    @Override 
    public int read() throws IOException
    {
        return kryo.readObject(kryoInput, Integer.class);
    }

    @Override 
    public int read(byte[] buf, int off, int len) throws IOException
    {
        byte[] b = kryo.readObject(kryoInput, byte[].class);
        System.arraycopy(b, 0, buf, off, len);
        return len;
    }
    
    @Override 
    public boolean readBoolean() throws IOException
    {
        return kryo.readObject(kryoInput, Boolean.class);
    }    

    @Override 
    public byte readByte() throws IOException
    {
        return kryo.readObject(kryoInput, Byte.class);
    }    

    @Override 
    public char readChar() throws IOException
    {
        return kryo.readObject(kryoInput, Character.class);
    }    

    @Override 
    public double readDouble() throws IOException
    {
        return kryo.readObject(kryoInput, Double.class);
    }    

    @Override 
    public float readFloat() throws IOException
    {
        return kryo.readObject(kryoInput, Float.class);
    }    

    @Override 
    public short readShort() throws IOException
    {
        return kryo.readObject(kryoInput, Short.class);
    }    

    @Override 
    public int readInt() throws IOException
    {
        return kryo.readObject(kryoInput, Integer.class);
    }    

    @Override 
    public long readLong() throws IOException
    {
        return kryo.readObject(kryoInput, Long.class);
    }    

    @Override 
    public String readUTF() throws IOException
    {
        return kryo.readObject(kryoInput, String.class);
    }    


    @Override 
    public void readFully(byte[] buf) throws IOException
    {
        throw new UnsupportedOperationException(); 
    }    

    @Override 
    public void readFully(byte[] buf, int off, int len) throws IOException
    {
        throw new UnsupportedOperationException(); 
    }    

    @Override 
    protected Object readObjectOverride() throws IOException, ClassNotFoundException
    {
        return kryo.readClassAndObject(kryoInput);
    }     

    @Override 
    public Object readUnshared() throws IOException, ClassNotFoundException
    {
        return kryo.readClassAndObject(kryoInput);
    }     
}
