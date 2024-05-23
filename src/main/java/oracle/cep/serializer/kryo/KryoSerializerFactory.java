package oracle.cep.serializer.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.snapshot.SnapshotContext.ClassInfo;
import oracle.cep.serializer.ObjStreamFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.*;
import com.esotericsoftware.kryo.serializers.ExternalizableSerializer;

public class KryoSerializerFactory implements ObjStreamFactory {

    KryoFactory factory;
    KryoPool pool;
    
    public KryoSerializerFactory()
    {
        factory = new KryoFactory() {
            public Kryo create () {
              Kryo kryo = new Kryo();
              //kryo.setRegistrationRequired(true);   //To findout unregistered class
              for (ClassInfo entry : SnapshotContext.getClasses()) {
                  if (entry.serializerType != ClassInfo.EXTERNALIZABLE) {
                      kryo.register(entry.cls, entry.id);
                  } else {
                      kryo.register(entry.cls, new ExternalizableSerializer(), entry.id);  
                  }
              }
              return kryo;
            }
          };  
        pool = new KryoPool.Builder(factory).softReferences().build();   
    }
    
    public Kryo get() {
        Kryo kryo = pool.borrow();
        assert(kryo != null);
        return kryo;
    }
    
    public void release(Kryo kryo)
    {
        pool.release(kryo);
    }
    
    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream out)  throws IOException  {
        return new KryoObjOutputStream(this, out);
    }

    @Override
    public ObjectInputStream createObjectInputStream(InputStream in)  throws IOException {
        return new KryoObjInputStream(this, in);
    }

}
