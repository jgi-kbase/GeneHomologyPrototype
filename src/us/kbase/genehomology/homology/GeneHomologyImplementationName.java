package us.kbase.genehomology.homology;

import us.kbase.genehomology.core.Name;
import us.kbase.genehomology.core.exceptions.IllegalParameterException;
import us.kbase.genehomology.core.exceptions.MissingParameterException;

/** The name of a gene homology implementation, like LAST or DIAMOND.
 * @author gaprice@lbl.gov
 *
 */
public class GeneHomologyImplementationName extends Name {

	/** Create the name.
	 * @param id the name.
	 * @throws MissingParameterException if the name is null or whitespace only.
	 * @throws IllegalParameterException if the name is too long or contains control characters.
	 */
	public GeneHomologyImplementationName(final String id)
			throws MissingParameterException, IllegalParameterException {
		super(id, "gene homology implementation name", 256);
	}
	
}
