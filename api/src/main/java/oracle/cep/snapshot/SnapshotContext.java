package oracle.cep.snapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class SnapshotContext {
    /**
     * We are using two serial version
     * 1) serialVersionUID is used by java serialization framework
     *    For this, we use the generated serialVersionUID
     * 2) INTERNAL_SNAPSHOT_VERSION is for supporting the cases where java serialization cannot handle
     * For any serialization used for snapshots, we use externalizable to control.
     * As of 9/2016, we are converting every serializable to externalizable
     * 
     * When you change any serialization code for snapshots, do the followings:
     * 1) Before making changes, make sure the snapshot version unit test run fine with older versions 
     *    there should be stored snapshots for older version for the snapshot version
     *    Snapshots are stored in cqlengine/test/data/ha/snapshots/<version>
     *    If you do not see it, first create it without change and check it in first.
     * 2) Increase the snapshot version either major or minor
     *    major version change is for adding/removing new objects
     *    minor version change is from adding/removing/changing fields
     *    Leave history in this file.
     *    create a <feature>_VERSION = x.x
     * 3) Add version handling from the externalization handling methods (readExternal, writeExternal)
     *    with using if (SnapshotVersion.get() >= SnapshotContext.<feature>_VERSION)
     * 4) Generate the snapshots with new version and add it to the sanpshot version unit test by using
     *    test -Dskip.ha.unit.tests=false -Dskip.unit.tests=true -Dtest.createSnapshot=true
     * 5) create a zip file from test/data/ha/snapshots/<version> folder to test/data/ha/snapshots/<version>.zip without having folder.
     * 6) Make sure the unit test run fine with new version
     * 
     * Snapshot Version test with actual version increase was added to PatternStreamClassB operator.
     */
    
    public static double PATTERNB_VERSION = 1.0;
    public static double SOURCEOP_TUPID_VERSION=1.1;
    public static double GROUPAGGR_IN_SYN_VERSION=1.2;
    public static double PARTIAL_SNAPSHOT_IN_BINARY_OP_VERSION=1.3;
    public static double GROUPAGGR_EMPTY_GROUP_FLAG=1.4;
    /**
     * Version History:
     * VERSION = 1.0; Original Version
     * VERSION = 1.1; Added New Long Field "nextTupleId" in Snapshot for StreamSource & RelSource
     * VERSION = 1.2; Added Input synopsis handling for GroupAggr operator
     * VERSIOM = 1.3; Added Support of Partial Snapshots in Binary Operators to ensure that some of synopsis can be persisted incrementally.
     */
    public static double CURRENT_VERSION = 1.4;

    private static final ThreadLocal<Double> s_context = new ThreadLocal<Double>() {
        @Override protected Double initialValue() {
            return CURRENT_VERSION;
        }
    };
    
    public static void reset() {
        setVersion(CURRENT_VERSION);
    }
    
    public static double getVersion() {
        return s_context.get();
    }

    public static void setVersion(double version) {
        s_context.set(version);
    }

    public static void writeVersion(ObjectOutputStream out) throws IOException {
        out.writeDouble(getVersion());
    }
    
    public static void readVersion(ObjectInputStream in) throws IOException, ClassNotFoundException {
        //Read the version and set to TLS
        double version = in.readDouble(); 
        setVersion(version);
    }
    
    public static class ClassInfo {
        public static final int DEFAULT = 0;
        public static final int SERIALIZABLE = 1;
        public static final int EXTERNALIZABLE = 2;

        public int id;
        public Class<?> cls;
        public int serializerType;
        
        public ClassInfo(int id, Class<?> cls, int serializableType)
        {
            this.id = id;
            this.cls = cls;
            this.serializerType = serializableType;
        }
    }
    
    private static ConcurrentHashMap<Integer,ClassInfo> s_classesForSnapshots = new ConcurrentHashMap<Integer,ClassInfo>();

    public static void registerClass(int id, Class<?> cls) {
        registerClass(id, cls, ClassInfo.DEFAULT);
    }
    public static void registerClassSerializable(int id, Class<?> cls) {
        registerClass(id, cls, ClassInfo.SERIALIZABLE);
    }
    public static void registerClassExternalizable(int id, Class<?> cls) {
        registerClass(id, cls, ClassInfo.EXTERNALIZABLE);
    }
    public static void registerClass(int id, Class<?> cls, int serializableType) {
        ClassInfo old = s_classesForSnapshots.get(id);
        if (old != null) {
            throw new RuntimeException("Duplicate class registration with id="+id + " old="+old.cls.getName() + " new="+cls.getName());
        }
        s_classesForSnapshots.put(id, new ClassInfo(id, cls, serializableType));
    }
    
    public static Collection<ClassInfo> getClasses() {
        return s_classesForSnapshots.values();
    }
}
