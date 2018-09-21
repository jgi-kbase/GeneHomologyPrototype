package us.kbase.genehomology.homology;

@SuppressWarnings("serial")
public class GeneHomologyException extends Exception {

	public GeneHomologyException(final String message) {
		super(message);
	}
	
	public GeneHomologyException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
