package us.kbase.genehomology.core.exceptions;


/** Base class of all exceptions caused by an authentication failure.
 * @author gaprice@lbl.gov
 *
 */
@SuppressWarnings("serial")
public class AuthenticationException extends GeneHomologyException {

	public AuthenticationException(final ErrorType err, final String message) {
		super(err, message);
	}
	
	public AuthenticationException(
			final ErrorType err,
			final String message,
			final Throwable cause) {
		super(err, message, cause);
	}
}
