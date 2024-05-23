package oracle.cep.serializer.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import oracle.cep.serializer.ObjStreamFactory;

public class JavaSerializerFactory implements ObjStreamFactory {

    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream out)  throws IOException  {
        return new ObjectOutputStream(out);
    }

    @Override
    public ObjectInputStream createObjectInputStream(InputStream in)  throws IOException {
        return new ObjectInputStream(in);
    }

}
