package oracle.cep.serializer;

import oracle.cep.serializer.java.JavaSerializerFactory;
import oracle.cep.serializer.kryo.KryoSerializerFactory;

public class ObjStreamUtil {
    public static final int JAVA_SERIALIZER = 0;  
    public static final int KRYO_SERIALIZER = 1;  
    public static final int DEFAULT_SERIALIZER = JAVA_SERIALIZER;  
    
    public static ObjStreamFactory getObjStreamFactory() {
        return getObjStreamFactory(DEFAULT_SERIALIZER);
    }

    public static ObjStreamFactory getObjStreamFactory(int serializerType) {
        switch(serializerType) {
            case JAVA_SERIALIZER : return new JavaSerializerFactory();
            case KRYO_SERIALIZER : return new KryoSerializerFactory();
        }
        throw new RuntimeException("Unknow serializer type : " + serializerType);
    }

}
