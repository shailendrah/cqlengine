package oracle.cep.test.cqlxframework.env;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import oracle.cep.common.Constants;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPDefaultEnvConfig;
import oracle.cep.service.ICartridgeLocator;
import oracle.cep.service.IDataSourceFinder;
import oracle.cep.service.IQueryDestLocator;
import oracle.cep.util.StringUtil;

public class EnvConfig extends CEPDefaultEnvConfig {
	IDataSourceFinder m_dsFinder = null;
	IQueryDestLocator m_queryDestLocator = null;
	ICartridgeLocator m_cartridgeLocator = null;

	String m_homeFolder;

	// Storage
	String m_storageFolder;
	String m_metadataStorageName;
	String m_spillStorageName;
	long m_storageCacheSize;
	boolean m_isMetadataCleanupOnStartup;

	// Scheduler
	String m_schedulerClassName;
	long m_schedRuntime;
	int m_schedNumThreads;
	int m_schedThreadPoolQSize;
	int m_schedTimeSlice;
	boolean m_schedOnNewThread;
	boolean m_isDirectInterop;

	// Date
	String m_dateFormat;
	String m_timeZone;

	// Spill
	boolean m_useSpilledQueueSrc;
	int m_queueSrcSpillNormalThreshold;
	int m_queueSrcSpillPartialThreshold;
	int m_queueSrcSpillFullThreshold;
	int m_queueSrcSpillSyncThreshold;

	// Trace
	boolean m_useLogXMLTag;
	String m_traceFolder;
	String m_tracePostfix;

	public EnvConfig() {
		m_homeFolder = ConfigManager.getWorkFolder();

		m_storageFolder = getFolder("storageFolder",
				Constants.DEFAULT_STORAGE_FOLDER);

		m_metadataStorageName = super.getMetadataStorageName();
		m_spillStorageName = super.getSpillStorageName();
		m_storageCacheSize = super.getStorageCacheSize();
		m_isMetadataCleanupOnStartup = super.getIsMetadataCleanupOnStartup();

		// Scheduler
		m_schedulerClassName = super.getSchedulerClassName();
		m_schedRuntime = super.getSchedRuntime();
		m_schedNumThreads = super.getSchedNumThreads();
		m_schedThreadPoolQSize = super.getSchedThreadPoolQSize();
		m_schedTimeSlice = super.getSchedTimeSlice();
		m_schedOnNewThread = super.getSchedOnNewThread();
		m_isDirectInterop = super.getDirectInterop();

		// Date
		m_dateFormat = super.getDateFormat();
		m_timeZone = super.getTimeZone();

		// Spill
		m_useSpilledQueueSrc = super.getUseSpilledQueueSrc();
		m_queueSrcSpillNormalThreshold = super
				.getQueueSrcSpillNormalThreshold();
		m_queueSrcSpillPartialThreshold = super
				.getQueueSrcSpillPartialThreshold();
		m_queueSrcSpillFullThreshold = super.getQueueSrcSpillFullThreshold();
		m_queueSrcSpillSyncThreshold = super.getQueueSrcSpillSyncThreshold();

		// Trace
		m_useLogXMLTag = super.getUseLogXMLTag();
		m_traceFolder = getFolder("traceFolder",
				Constants.DEFAULT_LOG_TRACE_FOLDER);

		m_tracePostfix = super.getTracePostfix();
	}

	private String getFolder(String name, String path) {
		HashMap<String, String> valMap = new HashMap<String, String>();
		valMap.put("HOME", m_homeFolder);
		path = StringUtil.expand(path, valMap);
		try {
			File f = new File(path);
			f.mkdirs();
		} catch (Exception e) {
			LogUtil.warning(LoggerType.CUSTOMER, "Failed to create " + name
					+ ":" + path + "\n" + e.toString());
		}
		return path;
	}

	public void setDataSourceFinder(IDataSourceFinder v) {
		m_dsFinder = v;
	}

	public IDataSourceFinder getDataSourceFinder() {
		return m_dsFinder;
	}

	public void setQueryDestLocator(IQueryDestLocator v) {
		m_queryDestLocator = v;
	}

	public IQueryDestLocator getQueryDestLocator() {
		return m_queryDestLocator;
	}

	public void setStorageFolder(String v) {
		m_storageFolder = getFolder("storageFolder", v);
	}

	public String getStorageFolder() {
		return m_storageFolder;
	}

	public void setMetadataStorageName(String v) {
		m_metadataStorageName = v;
	}

	public String getMetadataStorageName() {
		return m_metadataStorageName;
	}

	public void setSpillStorageName(String v) {
		m_spillStorageName = v;
	}

	public String getSpillStorageName() {
		return m_spillStorageName;
	}

	public void setStorageCacheSize(long v) {
		m_storageCacheSize = v;
	}

	public long getStorageCacheSize() {
		return m_storageCacheSize;
	}

	public void setIsMetadataCleanupOnStartup(boolean v) {
		m_isMetadataCleanupOnStartup = v;
	}

	public boolean getIsMetadataCleanupOnStartup() {
		return m_isMetadataCleanupOnStartup;
	}

	public void setSchedulerClassName(String v) {
		m_schedulerClassName = v;
	}

	public String getSchedulerClassName() {
		return m_schedulerClassName;
	}

	public void setSchedRuntime(long v) {
		m_schedRuntime = v;
	}

	public long getSchedRuntime() {
		return m_schedRuntime;
	}

	public void setSchedNumThreads(int v) {
		m_schedNumThreads = v;
	}

	public int getSchedNumThreads() {
		return m_schedNumThreads;
	}

	public void setSchedThreadPoolQSize(int v) {
		m_schedThreadPoolQSize = v;
	}

	public int getSchedThreadPoolQSize() {
		return m_schedThreadPoolQSize;
	}

	public void setSchedTimeSlice(int v) {
		m_schedTimeSlice = v;
	}

	public int getSchedTimeSlice() {
		return m_schedTimeSlice;
	}

	public void setSchedOnNewThread(boolean v) {
		m_schedOnNewThread = v;
	}

	public boolean getSchedOnNewThread() {
		return m_schedOnNewThread;
	}

	public void setDirectInterop(boolean v) {
		m_isDirectInterop = v;
	}

	public boolean getDirectInterop() {
		return m_isDirectInterop;
	}

	// Date
	public void setDateFormat(String v) {
		m_dateFormat = v;
	}

	public String getDateFormat() {
		return m_dateFormat;
	}

	public void setTimeZone(String v) {
		m_timeZone = v;
	}

	public String getTimeZone() {
		return m_timeZone;
	}

	// Spill
	public void setUseSpilledQueueSrc(boolean v) {
		m_useSpilledQueueSrc = v;
	}

	public boolean getUseSpilledQueueSrc() {
		return m_useSpilledQueueSrc;
	}

	public void setQueueSrcSpillNormalThreshold(int v) {
		m_queueSrcSpillNormalThreshold = v;
	}

	public int getQueueSrcSpillNormalThreshold() {
		return m_queueSrcSpillNormalThreshold;
	}

	public void setQueueSrcSpillPartialThreshold(int v) {
		m_queueSrcSpillPartialThreshold = v;
	}

	public int getQueueSrcSpillPartialThreshold() {
		return m_queueSrcSpillPartialThreshold;
	}

	public void setQueueSrcSpillFullThreshold(int v) {
		m_queueSrcSpillFullThreshold = v;
	}

	public int getQueueSrcSpillFullThreshold() {
		return m_queueSrcSpillFullThreshold;
	}

	public void setQueueSrcSpillSyncThreshold(int v) {
		m_queueSrcSpillSyncThreshold = v;
	}

	public int getQueueSrcSpillSyncThreshold() {
		return m_queueSrcSpillSyncThreshold;
	}

	// Trace
	public void setUseLogXMLTag(boolean v) {
		m_useLogXMLTag = v;
	}

	public boolean getUseLogXMLTag() {
		return m_useLogXMLTag;
	}

	public void setTraceFolder(String v) {
		m_traceFolder = getFolder("traceFolder", v);
	}

	public String getTraceFolder() {
		return m_traceFolder;
	}

	public void setTracePostfix(String v) {
		m_tracePostfix = v;
	}

	public String getTracePostfix() {
		return m_tracePostfix;
	}

	@Override
	public ICartridgeLocator getCartridgeLocator() {
		return m_cartridgeLocator;
	}

	public void setCartridgeLocator(ICartridgeLocator locator) {
		m_cartridgeLocator = locator;
	}

	public void setOverrides(EnvConfigOverride overrides) {
		Map<String, Object> properties = overrides.getOverride();
		Method methodlist[] = this.getClass().getDeclaredMethods();
		for (String key : properties.keySet()) {
			Object value = properties.get(key);
			try {
				String mname = "set" + key.substring(0, 1).toUpperCase()
						+ key.substring(1);
				for (Method m : methodlist) {
					if (mname.equals(m.getName())) {
						Class<?> params[] = m.getParameterTypes();
						if (params.length == 1) {
							Object argList[] = new Object[1];
							if (params[0].equals(Boolean.class)
									|| params[0].equals(boolean.class)) {
								argList[0] = new Boolean(value.toString());
							} else if (params[0].equals(Integer.class)
									|| params[0].equals(int.class)) {
								argList[0] = new Integer(value.toString());
							} else if (params[0].equals(Long.class)
									|| params[0].equals(long.class)) {
								argList[0] = new Long(value.toString());
							} else if (params[0].equals(Float.class)
									|| params[0].equals(float.class)) {
								argList[0] = new Float(value.toString());
							} else if (params[0].equals(Double.class)
									|| params[0].equals(double.class)) {
								argList[0] = new Double(value.toString());
							} else if (params[0].equals(String.class)) {
								argList[0] = value.toString();
							} else {
								System.out.println("unknown type "
										+ params[0].getName());
							}
							m.invoke(this, argList);
							break;
						}
					}
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
		}
	}
}
