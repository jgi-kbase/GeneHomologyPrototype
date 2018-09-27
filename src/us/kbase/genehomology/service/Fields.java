package us.kbase.genehomology.service;

/** Field names used in incoming and outgoing data structures.
 * @author gaprice@lbl.gov
 *
 */
public class Fields {

	/* root */
	
	/** The server name. */
	public static final String SERVER_NAME = "servname";
	/** The version of the service. */
	public static final String VERSION = "version";
	/** The time, in milliseconds since the epoch, at the service. */
	public static final String SERVER_TIME = "servertime";
	/** The Git commit from which the service was built. */
	public static final String GIT_HASH = "gitcommithash";
	
	/* namespaces */
	
	/** An ID for a namespace. */
	public static final String NAMESPACE_ID = "id";
	/** The authentication source associated with a namespace. */
	public static final String NAMESPACE_AUTH_SOURCE = "authsource";
	/** A description for a namespace. */
	public static final String NAMESPACE_DESCRIPTION = "desc";
	/** The last modification date of the namespace. */
	public static final String NAMESPACE_LASTMOD = "lastmod";
	/** The implementation used to create the database associated with a namespace.
	 */
	public static final String NAMESPACE_IMPLEMENTATION = "impl";
	/** The number of sequences in a namespace sketch database. */
	public static final String NAMESPACE_SEQ_COUNT = "seqcount";
	/** The ID of the data source where the sequences in a namespace sketch database originated. */
	public static final String NAMESPACE_DATA_SOURCE_ID = "datasource";
	/** The ID of the database within the data source where the sequences in a namespace
	 * sketch database originated.
	 */
	public static final String NAMESPACE_DB_ID = "database";
	
	/* Aligment results */
	/** A set of alignments from a query sequence to one or more reference sequences. */
	public static final String ALIGNMENTS = "alignments";
	/** The implementation used to calculate the alignments. */
	public static final String ALIGN_IMPLEMENTATION = "impl";
	/** A set of namespaces. */
	public static final String ALIGN_NAMESPACES = "namespaces";
	/** The alignment e-value. */
	public static final String ALIGN_E_VAL = "evalue";
	/** The alignment bit score. */
	public static final String ALIGN_BIT_SCORE = "bitscore";
	/** The ID of the first sequence. */
	public static final String ALIGN_SEQ1_ID = "id1";
	/** The aligned first sequence. */
	public static final String ALIGN_SEQ1_SEQ = "alignseq1";
	/** The length of the first sequence. */
	public static final String ALIGN_SEQ1_LEN = "lenseq1";
	/** The start of the alignment for first sequence. */
	public static final String ALIGN_SEQ1_ALIGN_START = "alignstart1";
	/** The length of the alignment for first sequence. */
	public static final String ALIGN_SEQ1_ALIGN_LEN = "alignlen1";
	
	/** The ID of the second sequence. */
	public static final String ALIGN_SEQ2_ID = "id2";
	/** The aligned second sequence. */
	public static final String ALIGN_SEQ2_SEQ = "alignseq2";
	/** The length of the second sequence. */
	public static final String ALIGN_SEQ2_LEN = "lenseq2";
	/** The start of the alignment for second sequence. */
	public static final String ALIGN_SEQ2_ALIGN_START = "alignstart2";
	/** The length of the alignment for second sequence. */
	public static final String ALIGN_SEQ2_ALIGN_LEN = "alignlen2";
	
	/* errors */
	
	/** An error. */
	public static final String ERROR = "error";
	
	
}
