package us.kbase.genehomology.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import us.kbase.genehomology.config.GeneHomologyConfig;
import us.kbase.genehomology.config.GeneHomologyConfigurationException;
import us.kbase.genehomology.core.Namespace;
import us.kbase.genehomology.homology.GeneHomologyDBLocation;
import us.kbase.genehomology.homology.GeneHomologyDBName;
import us.kbase.genehomology.homology.GeneHomologyDatabase;
import us.kbase.genehomology.homology.GeneHomologyImplementationException;
import us.kbase.genehomology.homology.last.LAST;
import us.kbase.genehomology.load.NamespaceLoadInfo;
import us.kbase.genehomology.loader.exceptions.LoadInputParseException;
import us.kbase.genehomology.service.exceptions.ExceptionHandler;

public class GeneHomologyService extends ResourceConfig {
	
	//TODO TEST
	//TODO JAVADOC
	
	@SuppressWarnings("unused")
	private final SLF4JAutoLogger logger; //keep a reference to prevent GC
	
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
		final Namespace ns = getNamespaceBySuperHackyMethod(c);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(c).to(GeneHomologyConfig.class);
				bind(c.getLogger()).to(SLF4JAutoLogger.class);
				bind(ns).to(Namespace.class);
			}
		});
	}

	// this should be replaced by a database and a loader that allows multiple namespaces.
	private Namespace getNamespaceBySuperHackyMethod(final GeneHomologyConfig c)
			throws GeneHomologyConfigurationException {
		try (final InputStream is = Files.newInputStream(c.getNamespaceYAMLFile())) {
			final NamespaceLoadInfo nsli = new NamespaceLoadInfo(
					is, c.getNamespaceYAMLFile().toString());
			final GeneHomologyDatabase db = new LAST(
					c.getPathToTemporaryFileDirectory(), c.getHomologyTimeoutSec())
					.getDatabase(
							new GeneHomologyDBName(nsli.getId().getName()),
							new GeneHomologyDBLocation(c.getLASTProjectFile()));
			return nsli.toNamespace(db);
		} catch (NoSuchFileException e) {
			throw new GeneHomologyConfigurationException("File not found: " + e.getMessage(), e);
		} catch (IOException | LoadInputParseException | GeneHomologyImplementationException e) {
			throw new GeneHomologyConfigurationException(e.getMessage(), e);
		}
	}
	
}
