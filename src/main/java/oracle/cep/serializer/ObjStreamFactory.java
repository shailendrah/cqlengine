package oracle.cep.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public interface ObjStreamFactory {
    ObjectOutputStream createObjectOutputStream(OutputStream out) throws IOException ;
    ObjectInputStream createObjectInputStream(InputStream in) throws IOException ;
}
