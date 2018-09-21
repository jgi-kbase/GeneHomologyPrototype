package us.kbase.genehomology.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.ini4j.Ini;
import org.productivity.java.syslog4j.SyslogIF;

import us.kbase.genehomology.service.SLF4JAutoLogger;
import us.kbase.genehomology.util.FileOpener;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.JsonServerSyslog.RpcInfo;
import us.kbase.common.service.JsonServerSyslog.SyslogOutput;

/** A configuration for the gene homology software package. Loads the configuration from
 * the ini file section "gene homology" with the keys
 * 
 * //TODO CFG add keys when legit keys exist
 * <pre>
 * homology-timeout
 * temp-dir
 * dont-trust-x-ip-headers
 * </pre>
 * 
 * The last key is optional and instructs the server to ignore the X-Real-IP and X-Forwarded-For
 * headers if set to {@link #TRUE}.
 * 
 * @author gaprice@lbl.gov
 *
 */
public class GeneHomologyConfig {
	
	// we may want different configuration implementations for different environments. YAGNI for now.
	
	public static final String ENV_VAR_ASSYHOM = "GENE_HOMOLOGY_CONFIG";
	public static final String ENV_VAR_KB_DEP = "KB_DEPLOYMENT_CONFIG";
	
	private static final String LOG_NAME = "GeneHomology";
	
	private static final String CFG_LOC = "genehomology";
	private static final String TEMP_KEY_CFG_FILE = "temp-key-config-file";
	
	private static final String KEY_TEMP_DIR = "temp-dir";
	private static final String KEY_IGNORE_IP_HEADERS = "dont-trust-x-ip-headers";
	
	private static final String KEY_HOMOLOGY_TIMEOUT = "homology-timeout";
	
	// in seconds
	private static final int DEFAULT_HOMOLOGY_TIMEOUT = 120;
	private static final int MINIMUM_HOMOLOGY_TIMEOUT = 1;
	
	public static final String TRUE = "true";
	
	private final Path tempDir;
	private final SLF4JAutoLogger logger;
	private final boolean ignoreIPHeaders;
	private final int homologyTimeoutSec;

	/** Create a new configuration.
	 * 
	 * Loads the configuration from an ini file specified by the environmental variables 
	 * {@link #ENV_VAR_ASSYHOM} or {@link #ENV_VAR_KB_DEP} in order of precedence.
	 * The JVM system properties take precedence over environmental variables if both are
	 * present for a given key.
	 * @throws GeneHomologyConfigurationException if the configuration is invalid.
	 */
	public GeneHomologyConfig() throws GeneHomologyConfigurationException {
		this(new FileOpener());
	}
	
	// for tests
	private GeneHomologyConfig(final FileOpener fileOpener)
			throws GeneHomologyConfigurationException {
		this(getConfigPathFromEnv(), false, fileOpener);
	}
	
	/** Create a new configuration.
	 *
	 * @param filepath the path to the ini file containing the configuration.
	 * @param nullLogger true to create a silent logger implementation.
	 * @throws GeneHomologyConfigurationException if the configuration is invalid.
	 */
	public GeneHomologyConfig(final Path filepath, final boolean nullLogger) 
			throws GeneHomologyConfigurationException {
		this(filepath, nullLogger, new FileOpener());
	}
	
	// for tests
	private GeneHomologyConfig(
			final Path filepath,
			final boolean nullLogger,
			final FileOpener fileOpener)
			throws GeneHomologyConfigurationException {
		// the logger is configured in in the configuration class so that alternate environments with different configuration mechanisms can configure their own logger
		if (nullLogger) {
			logger = new NullLogger();
		} else {
			// may want to allow configuring the logger name, but YAGNI
			logger = new JsonServerSysLogAutoLogger(new JsonServerSyslog(LOG_NAME,
					//TODO KBASECOMMON allow null for the fake config prop arg
					"thisisafakekeythatshouldntexistihope",
					JsonServerSyslog.LOG_LEVEL_INFO, true));
		}
		final Map<String, String> cfg = getConfig(filepath, fileOpener);
		ignoreIPHeaders = TRUE.equals(getString(KEY_IGNORE_IP_HEADERS, cfg));
		tempDir = Paths.get(getString(KEY_TEMP_DIR, cfg, true));
		homologyTimeoutSec = getInt(KEY_HOMOLOGY_TIMEOUT, cfg, DEFAULT_HOMOLOGY_TIMEOUT,
				MINIMUM_HOMOLOGY_TIMEOUT);
	}
	
	private int getInt(
			final String paramName,
			final Map<String, String> cfg,
			final int default_,
			int minimum)
			throws GeneHomologyConfigurationException {
		final String putative = getString(paramName, cfg);
		if (putative == null) {
			return default_;
		}
		try {
			int val = Integer.parseInt(putative);
			if (val < minimum) {
				throw new GeneHomologyConfigurationException(String.format(
						"Parameter %s in configuration file %s, section %s, " +
						"must have a minimum value of %s, was %s",
						paramName, cfg.get(TEMP_KEY_CFG_FILE), CFG_LOC, minimum, putative));
			}
			return val;
		} catch (NumberFormatException e) {
			throw new GeneHomologyConfigurationException(String.format(
					"Parameter %s in configuration file %s, section %s, " +
					"must be an integer, was %s",
					paramName, cfg.get(TEMP_KEY_CFG_FILE), CFG_LOC, putative));
		}
	}

	// returns null if no string
	private String getString(
			final String paramName,
			final Map<String, String> config)
			throws GeneHomologyConfigurationException {
		return getString(paramName, config, false);
	}
	
	private String getString(
			final String paramName,
			final Map<String, String> config,
			final boolean except)
			throws GeneHomologyConfigurationException {
		final String s = config.get(paramName);
		if (s != null && !s.trim().isEmpty()) {
			return s.trim();
		} else if (except) {
			throw new GeneHomologyConfigurationException(String.format(
					"Required parameter %s not provided in configuration file %s, section %s",
					paramName, config.get(TEMP_KEY_CFG_FILE), CFG_LOC));
		} else {
			return null;
		}
	}
	
	private static Path getConfigPathFromEnv()
			throws GeneHomologyConfigurationException {
		String file = System.getProperty(ENV_VAR_ASSYHOM) == null ?
				System.getenv(ENV_VAR_ASSYHOM) : System.getProperty(ENV_VAR_ASSYHOM);
		if (file == null) {
			file = System.getProperty(ENV_VAR_KB_DEP) == null ?
					System.getenv(ENV_VAR_KB_DEP) : System.getProperty(ENV_VAR_KB_DEP);
		}
		if (file == null || file.trim().isEmpty()) {
			throw new GeneHomologyConfigurationException(String.format(
					"Could not find deployment configuration file from either " +
					"permitted environment variable / system property: %s, %s",
					ENV_VAR_ASSYHOM, ENV_VAR_KB_DEP));
		}
		return Paths.get(file);
	}
	
	private Map<String, String> getConfig(final Path file, final FileOpener fileOpener)
			throws GeneHomologyConfigurationException {
		final Ini ini;
		try (final InputStream is = fileOpener.open(file)){
			ini = new Ini(is);
		} catch (IOException ioe) {
			throw new GeneHomologyConfigurationException(String.format(
					"Could not read configuration file %s: %s",
					file, ioe.getMessage()), ioe);
		}
		final Map<String, String> config = ini.get(CFG_LOC);
		if (config == null) {
			throw new GeneHomologyConfigurationException(String.format(
					"No section %s in config file %s", CFG_LOC, file));
		}
		config.put(TEMP_KEY_CFG_FILE, file.toString());
		return config;
	}
	
	private static class NullLogger implements SLF4JAutoLogger {

		@Override
		public void setCallInfo(String method, String id, String ipAddress) {
			//  do nothing
		}

		@Override
		public String getCallID() {
			return null;
		}
	}
	
	// this is just too much of a pain to test, and testing manually is trivial.
	private static class JsonServerSysLogAutoLogger implements SLF4JAutoLogger {
		
		@SuppressWarnings("unused")
		private JsonServerSyslog logger; // keep a reference to avoid gc

		private JsonServerSysLogAutoLogger(final JsonServerSyslog logger) {
			super();
			this.logger = logger;
			logger.changeOutput(new SyslogOutput() {
				
				@Override
				public void logToSystem(
						final SyslogIF log,
						final int level,
						final String message) {
					System.out.println(message);
				}
				
			});
		}

		@Override
		public void setCallInfo(
				final String method,
				final String id,
				final String ipAddress) {
			final RpcInfo rpc = JsonServerSyslog.getCurrentRpcInfo();
			rpc.setId(id);
			rpc.setIp(ipAddress);
			rpc.setMethod(method);
		}

		@Override
		public String getCallID() {
			return JsonServerSyslog.getCurrentRpcInfo().getId();
		}
	}
	
	/** Get the timeout to use for any homology search processes.
	 * @return the timeout in seconds.
	 */
	public int getHomologyTimeoutSec() {
		return homologyTimeoutSec;
	}
	
	/** Get a path to directory in which to store temporary files. The directory may not exist.
	 * @return a temporary file directory.
	 */
	public Path getPathToTemporaryFileDirectory() {
		return tempDir;
	}
	
	/** Get a logger. The logger is expected to intercept SLF4J log events and log them
	 * appropriately. A reference to the logger must be maintained so that it is not garbage
	 * collected.
	 * @return the logger.
	 */
	public SLF4JAutoLogger getLogger() {
		return logger;
	}
	
	/** True if the X-Real-IP and X-Forwarded-For headers should be ignored.
	 * @return true to ignore IP headers.
	 */
	public boolean isIgnoreIPHeaders() {
		return ignoreIPHeaders;
	}
	
	public static void main(final String[] args) throws Exception {
		final GeneHomologyConfig cfg = new GeneHomologyConfig();
		System.out.println(cfg.getHomologyTimeoutSec());
		System.out.println(cfg.getPathToTemporaryFileDirectory());
		System.out.println(cfg.isIgnoreIPHeaders());
	}
}
