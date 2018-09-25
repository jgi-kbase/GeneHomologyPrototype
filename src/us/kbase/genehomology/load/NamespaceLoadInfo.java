package us.kbase.genehomology.load;

import static com.google.common.base.Preconditions.checkNotNull;
import static us.kbase.genehomology.load.ParseHelpers.fromYAML;
import static us.kbase.genehomology.load.ParseHelpers.getString;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;

import com.google.common.base.Optional;

import us.kbase.genehomology.core.DataSourceID;
import us.kbase.genehomology.core.Namespace;
import us.kbase.genehomology.core.NamespaceID;
import us.kbase.genehomology.core.exceptions.IllegalParameterException;
import us.kbase.genehomology.core.exceptions.MissingParameterException;
import us.kbase.genehomology.homology.GeneHomologyDatabase;
import us.kbase.genehomology.loader.exceptions.LoadInputParseException;


/** Represents load information for a namespace instantiated from an YAML or JSON input.
 * 
 * Example input:
 * 
 * <pre>
 * id: mynamespace
 * datasource: KBase
 * sourcedatabase: CI Refdata
 * description: some reference data
 * moddate: the last modification date of the data in epoch seconds
 * <pre>
 * 
 * @author gaprice@lbl.gov
 *
 */
public class NamespaceLoadInfo {
	
	private final NamespaceID id;
	private final DataSourceID dataSourceID;
	private final Optional<String> sourceDatabaseID;
	private final Optional<String> description;
	// this is a hack until namespace info is persisted in a database.
	private final Instant modificationDate;

	/** Generate load information for a namespace.
	 * @param input the input to parse.
	 * @param sourceInfo information about the source, often a file name.
	 * @throws LoadInputParseException if the input could not be parsed.
	 */
	public NamespaceLoadInfo(final InputStream input, final String sourceInfo)
			throws LoadInputParseException {
		final Object predata = fromYAML(input, sourceInfo);
		if (!(predata instanceof Map)) {
			throw new LoadInputParseException(
					"Expected mapping at / in " + sourceInfo);
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> data = (Map<String, Object>) predata;
		id = getID(data, "id", sourceInfo);
		dataSourceID = getDataSourceID(data, "datasource", sourceInfo);
		sourceDatabaseID = Optional.fromNullable(
				getString(data, "sourcedatabase", sourceInfo, true));
		description = Optional.fromNullable(getString(data, "description", sourceInfo, true));
		final Object mdate = data.get("moddate");
		if (!(mdate instanceof Number)) {
			throw new LoadInputParseException(String.format(
					"moddate %s is not in epoch seconds in %s", mdate, sourceInfo)); 
		}
		try {
			modificationDate = Instant.ofEpochSecond(((Number)mdate).longValue());
		} catch (NumberFormatException e) {
			throw new LoadInputParseException("moddate is not in epoch seconds in " + sourceInfo);
		}
	}

	/** Get the namespace ID.
	 * @return the namespace ID.
	 */
	public NamespaceID getId() {
		return id;
	}

	/** Get the ID of the data source from whence the data associated with this namespace came.
	 * @return the data source ID.
	 */
	public DataSourceID getDataSourceID() {
		return dataSourceID;
	}

	/** Get the ID of the database within the data source from whence the data came.
	 * @return the source database ID, or absent if absent.
	 */
	public Optional<String> getSourceDatabaseID() {
		return sourceDatabaseID;
	}

	/** Get the description of the namespace.
	 * @return the description, or absent if absent.
	 */
	public Optional<String> getDescription() {
		return description;
	}
	
	/** The date the data was last modified.
	 * @return the modification date.
	 */
	public Instant getModificationDate() {
		return modificationDate;
	}
	
	private NamespaceID getID(
			final Map<String, Object> data,
			final String key,
			final String sourceInfo)
			throws LoadInputParseException {
		final String nsid = getString(data, key, sourceInfo, false);
		try {
			return new NamespaceID(nsid);
		} catch (IllegalParameterException e) {
			throw new LoadInputParseException("Illegal namespace ID: " + nsid, e);
		} catch (MissingParameterException e) {
			throw new RuntimeException("this should be impossible", e);
		}
	}
	
	private DataSourceID getDataSourceID(
			final Map<String, Object> data,
			final String key,
			final String sourceInfo)
			throws LoadInputParseException {
		final String dsid = getString(data, key, sourceInfo, false);
		try {
			return new DataSourceID(dsid);
		} catch (IllegalParameterException e) {
			throw new LoadInputParseException("Illegal data source ID: " + dsid, e);
		} catch (MissingParameterException e) {
			throw new RuntimeException("this should be impossible", e);
		}
	}
	
	/** Create a namespace from the load info.
	 * @param db the database associated with the namespace.
	 * @return the new namespace.
	 */
	public Namespace toNamespace(final GeneHomologyDatabase db) {
		checkNotNull(db, "sketchDB");
		return Namespace.getBuilder(id, db, modificationDate)
				.withNullableDataSourceID(dataSourceID)
				.withNullableSourceDatabaseID(sourceDatabaseID.orNull())
				.withNullableDescription(description.orNull())
				.build();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSourceID == null) ? 0 : dataSourceID.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modificationDate == null) ? 0 : modificationDate.hashCode());
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
		NamespaceLoadInfo other = (NamespaceLoadInfo) obj;
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
		if (modificationDate == null) {
			if (other.modificationDate != null) {
				return false;
			}
		} else if (!modificationDate.equals(other.modificationDate)) {
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
}
