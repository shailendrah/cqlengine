package oracle.cep.test.ha.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.cep.serializer.ObjStreamFactory;
import oracle.cep.serializer.ObjStreamUtil;
import oracle.cep.snapshot.SnapshotContext;
import junit.framework.TestCase;

public class SnapshotEvolutionTest extends TestCase
{
    /**
     * Sets up the test fixture.
     * (Called before every test case method.)
     */
    public void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     * (Called after every test case method.)
     */
    public void tearDown()
    {
    }
  
    
    public byte[] createSnapshot(DummyHeader header) throws Exception
    {
      ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = fac.createObjectOutputStream(bos);
      SnapshotContext.writeVersion(out);
      out.writeObject(header);
      out.flush();
      out.close();
      return bos.toByteArray();
    }

    public DummyHeader loadSnapshot(byte[] snapshotBytes) throws Exception
    {
        ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
        ByteArrayInputStream bis = new ByteArrayInputStream(snapshotBytes);
        ObjectInputStream in = fac.createObjectInputStream(bis);
        SnapshotContext.readVersion(in);
        DummyHeader header = (DummyHeader) in.readObject();
        in.close();
        return header;
    }
    
    private void snapshotVersionTest(double version) {
        SnapshotContext.setVersion(version);
        Exception thrown = null;
        try {
            DummyHeader header = new DummyHeader();
            byte[] snapshot;
            snapshot = createSnapshot(header);
            DummyHeader header2 = loadSnapshot(snapshot);
            assert (header.equals(header2));
        } catch (Exception e) {
            thrown = e;
            System.out.println(e);
        }
        assert (thrown == null);
    }
    
    public void testAddField() {
        snapshotVersionTest(DummyHeader.V_ADD_FIELD);
    }

    public void testRemoveField() {
        snapshotVersionTest(DummyHeader.V_REMOVE_FIELD);
    }

    public void testChangeField() {
        snapshotVersionTest(DummyHeader.V_CHANGE_FIELD);
    }

    private static class DummyHeader implements Externalizable {
        private final static double V_ADD_FIELD = 0.01;
        private final static double V_REMOVE_FIELD = 0.02;
        private final static double V_CHANGE_FIELD = 0.03;

        private static int EXPECTED_A = 0x5a5a;
        private static int EXPECTED_B = 0xa5a5;
        private static int EXPECTED_C = 0x1234;
        private static float EXPECTED_D1 = 123.4567f;
        private static long EXPECTED_D2 = 1234567;
        int a = EXPECTED_A;
        int b = EXPECTED_B;
        int c = EXPECTED_C;
        float d1 = EXPECTED_D1;
        long d2 = EXPECTED_D2;

        public DummyHeader() {
            
        }
        
        public int hashCode() {
            List<Integer> vals = new ArrayList<Integer>();
            vals.add(a);
            if (SnapshotContext.getVersion() >= V_ADD_FIELD) {
                vals.add(b);
            }
            if (SnapshotContext.getVersion() < V_REMOVE_FIELD) {
                vals.add(c);
            }
            if (SnapshotContext.getVersion() >= V_CHANGE_FIELD) {
                vals.add(new Float(d1).hashCode());
            } else {
                vals.add(new Long(d2).hashCode());
            }
            int[] ar = new int[vals.size()];
            for (int i = 0; i < vals.size(); i++) ar[i] = vals.get(i);
            return Arrays.hashCode(ar);
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof DummyHeader)) return false;
            DummyHeader other = (DummyHeader) o;
            if (a != other.a) return false;
            if (SnapshotContext.getVersion() >= V_ADD_FIELD) {
                if (b != other.b) return false;
            }
            if (SnapshotContext.getVersion() < V_REMOVE_FIELD) {
                if (c != other.c) return false;
            }
            if (SnapshotContext.getVersion() >= V_CHANGE_FIELD) {
                if (d1 != other.d1) return false;
            } else {
                if (d2 != other.d2) return false;
            }
            return true;
        }
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(a);
            if (SnapshotContext.getVersion() >= V_ADD_FIELD) {
                out.writeInt(b);
            }
            if (SnapshotContext.getVersion() >= V_REMOVE_FIELD) {
                //c is gone
            } else {
                out.writeInt(c);
            }
            if (SnapshotContext.getVersion() >= V_CHANGE_FIELD) {
                out.writeFloat(d1);
            } else {
                out.writeLong(d2);
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            int v = in.readInt();
            if (v != EXPECTED_A) throw new RuntimeException("Failed to read. expected:"+EXPECTED_A+" but got:"+v);
            if (SnapshotContext.getVersion() >= V_ADD_FIELD) {
                v = in.readInt();
                if (v != EXPECTED_B) throw new RuntimeException("Failed to read. expected:"+EXPECTED_B+" but got:"+v);
            }
            if (SnapshotContext.getVersion() >= V_REMOVE_FIELD) {
                //c is gone
            } else {
                v = in.readInt();
                if (v != EXPECTED_C) throw new RuntimeException("Failed to read. expected:"+EXPECTED_C+" but got:"+v);
            }
            if (SnapshotContext.getVersion() >= V_CHANGE_FIELD) {
                float fv = in.readFloat();
                if (fv != EXPECTED_D1) throw new RuntimeException("Failed to read. expected:"+EXPECTED_D1+" but got:"+fv);
            } else {
                long lv = in.readLong();
                if (lv != EXPECTED_D2) throw new RuntimeException("Failed to read. expected:"+EXPECTED_D2+" but got:"+lv);
            }
        }
    }
    
}
