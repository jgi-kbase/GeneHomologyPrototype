package us.kbase.genehomology.homology;

import static us.kbase.genehomology.util.Util.exceptOnEmpty;

/** The name of a gene homology database.
 * @author gaprice@lbl.gov
 *
 */
public class GeneHomologyDBName implements Comparable<GeneHomologyDBName>{

	private final String name;
	
	/** Create a database name.
	 * @param name the name.
	 */
	public GeneHomologyDBName(final String name) {
		exceptOnEmpty(name, "name");
		this.name = name;
	}

	/** Get the database name.
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(final GeneHomologyDBName o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GeneHomologyDBName other = (GeneHomologyDBName) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
