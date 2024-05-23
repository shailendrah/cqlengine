package oracle.cep.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

//import org.nustaq.serialization.FSTObjectInput;
//import org.nustaq.serialization.FSTObjectOutput;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializationBenchmark {
    /** Number of runs. */
    private static final int RUN_CNT = 3;
 
    /** Number of iterations. */
    private static final int ITER_CNT = 200000;
 
    public static void main(String[] args) throws Exception {
        // Create sample object.
        SampleObject obj = createObject();
 
        //benchmark("Fst serializtion ", obj, new FstSerializer());
        benchmark("Java externalizable serializtion ", obj, new JavaSerializer());
        benchmark("Kryo serializtion ", obj, new KryoSerializer());
 
    }
 
    private interface Serializer {
        void writeAndRead(Object obj) throws Exception;
    }
    
    private static class JavaSerializer implements Serializer {
        public void writeAndRead(Object obj) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            ObjectOutputStream objOut = null;

            try {
                objOut = new ObjectOutputStream(out);

                objOut.writeObject(obj);
            }
            finally {
                if (objOut != null) objOut.close();
            }

            ObjectInputStream objIn = null;

            try {
                objIn = new ObjectInputStream(
                    new ByteArrayInputStream(out.toByteArray()));

                SampleObject newObj = (SampleObject)objIn.readObject();
            }
            finally {
                if (objIn != null) objIn.close();
            }
        }
    }
        
    private static class KryoSerializer implements Serializer {
        Kryo marsh = new Kryo();
 
        public void writeAndRead(Object obj) throws Exception {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
 
                Output kryoOut = null;
 
                try {
                    kryoOut = new Output(out);
 
                    marsh.writeObject(kryoOut, obj);
                }
                finally {
                    if (kryoOut != null) kryoOut.close();
                    if (out != null) out.close();
                }
 
                Input kryoIn = null;
 
                try {
                    kryoIn = new Input(new ByteArrayInputStream(out.toByteArray()));
 
                    SampleObject newObj = marsh.readObject(kryoIn, SampleObject.class);
                }
                finally {
                    if (kryoIn != null) kryoIn.close();
                }
            }
        }

/*    
    private static class FstSerializer implements Serializer {
        
        public void writeAndRead(Object obj) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            FSTObjectOutput objOut = null;

            try {
                objOut = new FSTObjectOutput(out);

                objOut.writeObject(obj);
            }
            finally {
                if (objOut != null) objOut.close();
            }

            FSTObjectInput objIn = null;

            try {
                objIn = new FSTObjectInput(
                    new ByteArrayInputStream(out.toByteArray()));

                SampleObject newObj = (SampleObject)objIn.readObject();
            }
            finally {
                if (objIn != null) objIn.close();
            }
        }
    }
*/
    
    private static long benchmark(String name, SampleObject obj, Serializer ser) throws Exception {
        long avgDur = 0;
 
        for (int i = 0; i < RUN_CNT; i++) {
            long start = System.currentTimeMillis();
 
            for (int j = 0; j < ITER_CNT; j++) {
                ser.writeAndRead(obj);
            }
 
            long dur = System.currentTimeMillis() - start;
 
            avgDur += dur;
        }
 
        avgDur /= RUN_CNT;
 
        System.out.format("\n>>> "+ name + " (average): %,d ms\n\n", avgDur);
 
        return avgDur;
    }
 

    private static SampleObject createObject() {
        long[] longArr = new long[10];
 
        for (int i = 0; i < longArr.length; i++)
            longArr[i] = i;
 
        double[] dblArr = new double[10];
 
        for (int i = 0; i < dblArr.length; i++)
            dblArr[i] = 0.1 * i;
 
        return new SampleObject(123, 123.456f, (short)321, longArr, dblArr, "1234566abasdfaerasdfasdfasdfazdfadfadfadfadfadfqerqezasdfa");
    }
 
    private static class SampleObject 
        implements Externalizable, KryoSerializable {
        private int intVal;
        private float floatVal;
        private Short shortVal;
        private long[] longArr;
        private double[] dblArr;
        private String strVal;
        private SampleObject selfRef;
 
        public SampleObject() {}
 
        SampleObject(int intVal, float floatVal, Short shortVal, 
            long[] longArr, double[] dblArr, String strVal) {
            this.intVal = intVal;
            this.floatVal = floatVal;
            this.shortVal = shortVal;
            this.longArr = longArr;
            this.dblArr = dblArr;
            this.strVal = strVal;
            
            selfRef = this;
        }
 
        // Required by Java Externalizable.
        @Override public void writeExternal(ObjectOutput out) 
            throws IOException {
            out.writeInt(intVal);
            out.writeFloat(floatVal);
            out.writeShort(shortVal);
            out.writeObject(longArr);
            out.writeObject(dblArr);
            out.writeObject(strVal);
            out.writeObject(selfRef);
        }
 
        // Required by Java Externalizable.
        @Override public void readExternal(ObjectInput in) 
         throws IOException, ClassNotFoundException {
            intVal = in.readInt();
            floatVal = in.readFloat();
            shortVal = in.readShort();
            longArr = (long[])in.readObject();
            dblArr = (double[])in.readObject();
            strVal = (String) in.readObject();
            selfRef = (SampleObject)in.readObject();
        }
 
        // Required by Kryo serialization.
        @Override public void write(Kryo kryo, Output out) {
            kryo.writeObject(out, intVal);
            kryo.writeObject(out, floatVal);
            kryo.writeObject(out, shortVal);
            kryo.writeObject(out, longArr);
            kryo.writeObject(out, dblArr);
            kryo.writeObject(out, strVal);
            kryo.writeObject(out, selfRef);
        }
 
        // Required by Kryo serialization.
        @Override public void read(Kryo kryo, Input in) {
            intVal = kryo.readObject(in, Integer.class);
            floatVal = kryo.readObject(in, Float.class);
            shortVal = kryo.readObject(in, Short.class);
            longArr = kryo.readObject(in, long[].class);
            dblArr = kryo.readObject(in, double[].class);
            strVal = kryo.readObject(in, String.class);
            selfRef = kryo.readObject(in, SampleObject.class);
        }
    }
}