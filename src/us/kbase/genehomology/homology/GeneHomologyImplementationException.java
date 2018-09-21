package us.kbase.genehomology.homology;

@SuppressWarnings("serial")
public class GeneHomologyImplementationException extends Exception {

	public GeneHomologyImplementationException(final String message) {
		super(message);
	}
	
	public GeneHomologyImplementationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
