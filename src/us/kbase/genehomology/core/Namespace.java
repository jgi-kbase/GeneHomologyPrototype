package us.kbase.genehomology.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static us.kbase.genehomology.util.Util.isNullOrEmpty;

import java.time.Instant;

import com.google.common.base.Optional;

import us.kbase.genehomology.core.exceptions.IllegalParameterException;
import us.kbase.genehomology.core.exceptions.MissingParameterException;
import us.kbase.genehomology.homology.GeneHomologyDatabase;

/** A namespace containing a gene homology database. A namespace contains the homology database
 * and information about the source of the database.
 * 
 * @author gaprice@lbl.gov
 *
 */
public class Namespace {
	
	private final NamespaceID id;
	private final GeneHomologyDatabase homologyDatabase;
	private final DataSourceID dataSourceID;
	private final Instant modification;
	private final String sourceDatabaseID;
	private final Optional<String> description;

	private Namespace(
			final NamespaceID id,
			final GeneHomologyDatabase homologyDatabase,
			final DataSourceID dataSourceID,
			final Instant modification,
			final String sourceDatabaseID,
			final String description) {
		this.id = id;
		this.homologyDatabase = homologyDatabase;
		this.dataSourceID = dataSourceID;
		this.modification = modification;
		this.sourceDatabaseID = sourceDatabaseID;
		this.description = Optional.fromNullable(description);
	}

	/** Get the namespace ID.
	 * @return the ID.
	 */
	public NamespaceID getID() {
		return id;
	}

	/** Get the database associated with the namespace.
	 * @return the sketch database.
	 */
	public GeneHomologyDatabase getDatabase() {
		return homologyDatabase;
	}

	/** Get the ID of the data's source - often an institution like JGI, EMBL, etc.
	 * @return the data source ID.
	 */
	public DataSourceID getSourceID() {
		return dataSourceID;
	}

	/** Get the time this namespace was last modified.
	 * @return the modification time.
	 */
	public Instant getModification() {
		return modification;
	}

	/** Get the ID of the database within the data source where the data from which the
	 * database was created originates.
	 * @return the source database ID.
	 */
	public String getSourceDatabaseID() {
		return sourceDatabaseID;
	}

	/** Get a description of the namespace and the data contained within it, if any.
	 * @return the description or absent.
	 */
	public Optional<String> getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSourceID == null) ? 0 : dataSourceID.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modification == null) ? 0 : modification.hashCode());
		result = prime * result + ((homologyDatabase == null) ? 0 : homologyDatabase.hashCode());
		result = prime * result + ((sourceDatabaseID == null) ? 0 : sourceDatabaseID.hashCode());
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
		Namespace other = (Namespace) obj;
		if (dataSourceID == null) {
			if (other.dataSourceID != null) {
				return false;
			}
		} else if (!dataSourceID.equals(other.dataSourceID)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (modification == null) {
			if (other.modification != null) {
				return false;
			}
		} else if (!modification.equals(other.modification)) {
			return false;
		}
		if (homologyDatabase == null) {
			if (other.homologyDatabase != null) {
				return false;
			}
		} else if (!homologyDatabase.equals(other.homologyDatabase)) {
			return false;
		}
		if (sourceDatabaseID == null) {
			if (other.sourceDatabaseID != null) {
				return false;
			}
		} else if (!sourceDatabaseID.equals(other.sourceDatabaseID)) {
			return false;
		}
		return true;
	}

	/** Get a {@link Namespace} builder.
	 * @param id the ID of the namespace.
	 * @param homologyDatabase the gene homology database associated with the namespace.
	 * @param modification the last modification time of the namespace.
	 * @return a {@link Namespace} builder.
	 */
	public static Builder getBuilder(
			final NamespaceID id,
			final GeneHomologyDatabase homologyDatabase,
			final Instant modification) {
		return new Builder(id, homologyDatabase, modification);
	}
	
	/** A {@link Namespace} builder.
	 * @author gaprice@lbl.gov
	 *
	 */
	public static class Builder {
		
		private static final String DEFAULT = "default";
		private static final DataSourceID DEFAULT_DS_ID;
		static {
			try {
				DEFAULT_DS_ID = new DataSourceID("KBase");
			} catch (IllegalParameterException | MissingParameterException e) {
				throw new RuntimeException("Well this is unexpected.", e);
			}
		}
		
		private final NamespaceID id;
		private final GeneHomologyDatabase homologyDatabase;
		private final Instant modification;
		private DataSourceID dataSourceID = DEFAULT_DS_ID;
		private String sourceDatabaseID = DEFAULT;
		private String description = null;

		private Builder(
				final NamespaceID id,
				final GeneHomologyDatabase homologyDatabase,
				final Instant modification) {
			checkNotNull(id, "id");
			checkNotNull(homologyDatabase, "homologyDatabase");
			checkNotNull(modification, "modification");
			if (!id.getName().equals(homologyDatabase.getName().getName())) {
				// code smell here. Think about this later.
				throw new IllegalArgumentException("Namespace ID must equal homology DB ID");
			}
			this.id = id;
			this.homologyDatabase = homologyDatabase;
			this.modification = modification;
		}
		
		/** Add a data source ID. If the data source is null, the data source is reset to the
		 * default, "KBase".
		 * @param dataSourceID the ID of the source of the data.
		 * @return this builder.
		 */
		public Builder withNullableDataSourceID(final DataSourceID dataSourceID) {
			if (dataSourceID == null) {
				this.dataSourceID = DEFAULT_DS_ID;
			} else {
				this.dataSourceID = dataSourceID;
			}
			return this;
		}
		
		/** Add an ID for the database within the data source where the data originated. If null
		 * or whitespace, the ID is reset to the default, "default".
		 * @param sourceDatabaseID the ID of the source database.
		 * @return this builder.
		 */
		public Builder withNullableSourceDatabaseID(final String sourceDatabaseID) {
			if (isNullOrEmpty(sourceDatabaseID)) {
				this.sourceDatabaseID = DEFAULT;
			} else {
				this.sourceDatabaseID = sourceDatabaseID;
			}
			return this;
		}
		
		/** Add a description of the namespace. If null or whitespace, the description is set to
		 * null.
		 * @param description the namespace description.
		 * @return this builder.
		 */
		public Builder withNullableDescription(final String description) {
			if (isNullOrEmpty(description)) {
				this.description = null;
			} else {
				this.description = description;
			}
			return this;
		}
		
		/** Build the {@link Namespace}.
		 * @return the new {@link Namespace}.
		 */
		public Namespace build() {
			return new Namespace(id, homologyDatabase, dataSourceID,
					modification, sourceDatabaseID, description);
		}
	}
}
