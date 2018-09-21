package us.kbase.genehomology.config;

/** Thrown when a configuration is invalid.
 * @author gaprice@lbl.gov
 *
 */
public class GeneHomologyConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public GeneHomologyConfigurationException(final String message) {
		super(message);
	}
	
	public GeneHomologyConfigurationException(
			final String message,
			final Throwable cause) {
		super(message, cause);
	}
}