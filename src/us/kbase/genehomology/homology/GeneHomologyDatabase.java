package us.kbase.genehomology.homology;

import static com.google.common.base.Preconditions.checkNotNull;

/** A gene homology database as created by software like LAST, DIAMOND, MMseqs2, etc.
 * @author gaprice@lbl.gov
 *
 */
public class GeneHomologyDatabase {
	
	private final GeneHomologyDBName name;
	private final GeneHomologyImplementationName minHashImplementationName;
	private final GeneHomologyDBLocation location;
	private final int sequenceCount;
	
	// a builder would be nice. Everything's required though.
	/** Create a gene homology database.
	 * @param dbname the name of the database.
	 * @param implName the name of the implementation that created the database.
	 * @param location the location of the database.
	 * @param sequenceCount the number of sequences / sketches in the database.
	 */
	public GeneHomologyDatabase(
			final GeneHomologyDBName dbname,
			final GeneHomologyImplementationName implName,
			final GeneHomologyDBLocation location,
			final int sequenceCount) {
		checkNotNull(dbname, "dbname");
		checkNotNull(implName, "implName");
		checkNotNull(location, "location");
		if (sequenceCount < 1) {
			throw new IllegalArgumentException("sequenceCount must be at least 1");
		}
		this.name = dbname;
		this.minHashImplementationName = implName;
		this.location = location;
		this.sequenceCount = sequenceCount;
	}

	/** Get the database name.
	 * @return the database name.
	 */
	public GeneHomologyDBName getName() {
		return name;
	}

	/** Get the name of the implementation that created the database.
	 * @return the implementation name.
	 */
	public GeneHomologyImplementationName getImplementationName() {
		return minHashImplementationName;
	}
	
	/** Get the location of the database.
	 * @return the database location.
	 */
	public GeneHomologyDBLocation getLocation() {
		return location;
	}

	/** Get the number of sequences / sketches in the database.
	 * @return the sequence count.
	 */
	public int getSequenceCount() {
		return sequenceCount;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((minHashImplementationName == null) ? 0 : minHashImplementationName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + sequenceCount;
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
		GeneHomologyDatabase other = (GeneHomologyDatabase) obj;
		if (location == null) {
			if (other.location != null) {
				return false;
			}
		} else if (!location.equals(other.location)) {
			return false;
		}
		if (minHashImplementationName == null) {
			if (other.minHashImplementationName != null) {
				return false;
			}
		} else if (!minHashImplementationName.equals(other.minHashImplementationName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (sequenceCount != other.sequenceCount) {
			return false;
		}
		return true;
	}
}
