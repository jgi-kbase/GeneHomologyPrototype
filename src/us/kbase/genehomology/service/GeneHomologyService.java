package us.kbase.genehomology.service;

import java.io.IOException;
import java.nio.file.Files;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.net.SocketConnector.ExceptionHandler;
import us.kbase.genehomology.config.GeneHomologyConfig;
import us.kbase.genehomology.config.GeneHomologyConfigurationException;

public class GeneHomologyService extends ResourceConfig {
	
	//TODO TEST
	//TODO JAVADOC
	
	@SuppressWarnings("unused")
	private final SLF4JAutoLogger logger; //keep a reference to prevent GC
	// TODO NOWNOW use java-fasta-utils to check fasta format & check only 1 query
	
	public GeneHomologyService()
			throws GeneHomologyConfigurationException {
		//TODO ZLATER CONFIG Get the class name from environment & load if we need alternate config mechanism
		final GeneHomologyConfig cfg = new GeneHomologyConfig();
		
		quietLogger();
		logger = cfg.getLogger();
		try {
			Files.createDirectories(cfg.getPathToTemporaryFileDirectory());
		} catch (IOException e) {
			throw new GeneHomologyConfigurationException(e.getMessage(), e);
		}
		buildApp(cfg);
	}

	private void quietLogger() {
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
				.setLevel(Level.INFO);
	}

	private void buildApp(
			final GeneHomologyConfig c)
			throws GeneHomologyConfigurationException {
		packages("us.kbase.genehomology.service.api");
		register(JacksonJaxbJsonProvider.class);
		register(LoggingFilter.class);
		register(ExceptionHandler.class);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(c).to(GeneHomologyConfig.class);
				bind(c.getLogger()).to(SLF4JAutoLogger.class);
			}
		});
	}
}
