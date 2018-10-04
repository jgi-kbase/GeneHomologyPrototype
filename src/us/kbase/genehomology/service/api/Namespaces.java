package us.kbase.genehomology.service.api;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.yeastrc.proteomics.fasta.FASTADataErrorException;
import org.yeastrc.proteomics.fasta.FASTAFileParser;
import org.yeastrc.proteomics.fasta.FASTAFileParserFactory;

import us.kbase.genehomology.config.GeneHomologyConfig;
import us.kbase.genehomology.core.Namespace;
import us.kbase.genehomology.core.NamespaceID;
import us.kbase.genehomology.core.exceptions.IllegalParameterException;
import us.kbase.genehomology.core.exceptions.MissingParameterException;
import us.kbase.genehomology.core.exceptions.NoSuchNamespaceException;
import us.kbase.genehomology.homology.AlignedSequence;
import us.kbase.genehomology.homology.GeneHomologyImplementationException;
import us.kbase.genehomology.homology.SequenceSearchResult;
import us.kbase.genehomology.homology.last.LAST;
import us.kbase.genehomology.service.Fields;

/** Handler for the endpoints under the {@link ServicePaths#NAMESPACE_ROOT} endpoints.
 * @author gaprice@lbl.gov
 *
 */
@javax.ws.rs.Path(ServicePaths.NAMESPACE_ROOT)
public class Namespaces {
	
	private final java.nio.file.Path tempDir;
	private final Namespace ns;
	
	/** Construct the handler. This is typically done by the Jersey framework.
	 * @param cfg the configuration for the gene homology service.
	 */
	@Inject
	public Namespaces(final Namespace ns, final GeneHomologyConfig cfg) {
		this.ns = ns;
		this.tempDir = cfg.getPathToTemporaryFileDirectory();
	}

	/** Get the extant namespaces.
	 * @return the namespaces in the system.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Map<String, Object>> getNamespaces() {
		return new HashSet<>(Arrays.asList(fromNamespace(ns)));
	}
	
	/** Get a particular namespace.
	 * @param namespace the ID of the namespace.
	 * @return the namespace.
	 * @throws NoSuchNamespaceException if there is no such namespace.
	 * @throws MissingParameterException if the ID is missing or white space only.
	 * @throws IllegalParameterException if the ID is not a valid namespace ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path(ServicePaths.NAMESPACE_SELECT)
	public Map<String, Object> getNamespace(
			@PathParam(ServicePaths.NAMESPACE_SELECT_PARAM) final String namespace)
			throws NoSuchNamespaceException, MissingParameterException,
				IllegalParameterException {
		final NamespaceID nsid = new NamespaceID(namespace);
		if (!nsid.equals(ns.getID())) {
			throw new NoSuchNamespaceException(namespace);
		}
		return fromNamespace(ns);
	}

	private Map<String, Object> fromNamespace(final Namespace ns) {
		final Map<String, Object> ret = new HashMap<>();
		ret.put(Fields.NAMESPACE_DESCRIPTION, ns.getDescription().orNull());
		ret.put(Fields.NAMESPACE_ID, ns.getID().getName());
		ret.put(Fields.NAMESPACE_LASTMOD, ns.getModification().toEpochMilli());
		ret.put(Fields.NAMESPACE_IMPLEMENTATION, ns.getDatabase()
				.getImplementationName().getName());
		ret.put(Fields.NAMESPACE_SEQ_COUNT, ns.getDatabase().getSequenceCount());
		ret.put(Fields.NAMESPACE_DB_ID, ns.getSourceDatabaseID());
		ret.put(Fields.NAMESPACE_DATA_SOURCE_ID, ns.getSourceID().getName());
		return ret;
	}
	
	/** Search a namespace. Expects a fasta file with one sequence in the request body.
	 * @param request the incoming servlet request.
	 * @param namespace a namespace ID.
	 * @return the matches.
	 * @throws IOException if an error occurs retrieving the fasta file from the
	 * request or saving the file to a temporary file.
	 * @throws NoSuchNamespaceException if the requested namespace does not exist.
	 * @throws MissingParameterException if the namespace ID parameter is missing.
	 * @throws IllegalParameterException if namespace ID is illegal.
	 * @throws GeneHomologyImplementationException if the homology implementation throws an
	 * error.
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path(ServicePaths.NAMESPACE_SEARCH)
	public Map<String, Object> searchNamespacesJson(
			@Context final HttpServletRequest request,
			@PathParam(ServicePaths.NAMESPACE_SELECT_PARAM) final String namespace)
			throws IOException, NoSuchNamespaceException, MissingParameterException,
				IllegalParameterException, GeneHomologyImplementationException {
		final List<SequenceSearchResult> seqs = getAlignments(request, namespace);
		//TODO NOW add impl version
		final Map<String, Object> ret = new HashMap<>();
		ret.put(Fields.ALIGN_NAMESPACES, new HashSet<>(Arrays.asList(fromNamespace(ns))));
		ret.put(Fields.ALIGN_IMPLEMENTATION, ns.getDatabase().getImplementationName().getName());
		ret.put(Fields.ALIGNMENTS, seqs.stream()
				.map(s -> fromSearchResult(s))
				.collect(Collectors.toList()));
		return ret;
	}
	
	@POST
	@Produces("application/blasttab")
	@javax.ws.rs.Path(ServicePaths.NAMESPACE_SEARCH)
	public void searchNamespacesBlastTab(
			@Context final HttpServletRequest request,
			@Context final HttpServletResponse response,
			@PathParam(ServicePaths.NAMESPACE_SELECT_PARAM) final String namespace)
			throws NoSuchNamespaceException, MissingParameterException, IllegalParameterException,
				FileNotFoundException, IOException, GeneHomologyImplementationException {
		final List<SequenceSearchResult> seqs = getAlignments(request, namespace);
		try (final ServletOutputStream sos = response.getOutputStream()) {
			final Writer out = new BufferedWriter(new OutputStreamWriter(sos));
			for (final SequenceSearchResult s: seqs) {
				final AlignedSequence q = s.getQuery();
				final AlignedSequence t= s.getTarget();
				out.write(String.format("%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
						q.getId(), t.getId(),
						s.getPercentID() * 100,
						s.getAlignmentLength(), s.getMismatches(),
						s.getGapOpenCount(),
						q.getAlignmentStart() + 1, q.getAlignmentStart() + q.getAlignmentLength(),
						t.getAlignmentStart() + 1, t.getAlignmentStart() + t.getAlignmentLength(),
						s.getEValue(),
						s.getBitScore()));
			}
			out.close();
		}
	}

	private List<SequenceSearchResult> getAlignments(
			final HttpServletRequest request,
			final String namespace)
			throws MissingParameterException, IllegalParameterException, NoSuchNamespaceException,
				IOException, FileNotFoundException, GeneHomologyImplementationException {
		final NamespaceID nsid = new NamespaceID(namespace);
		if (!nsid.equals(ns.getID())) {
			throw new NoSuchNamespaceException(namespace);
		}
		Path tempFile = null;
		final List<SequenceSearchResult> seqs;
		// should catch IOException and do something with it?
		try (final InputStream is = request.getInputStream()) {
			tempFile = Files.createTempFile(tempDir, "genehomol_input", ".tmp.fasta");
			Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
			validateFASTA(tempFile);
			seqs = new LAST(tempDir, 120).search(
					ns.getDatabase().getName(), ns.getDatabase().getLocation(), tempFile);
		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
			}
		}
		return seqs;
	}

	// this should live in the core code when it exists
	private void validateFASTA(final Path tempFile)
			throws IllegalParameterException, FileNotFoundException, IOException {
		try (final FASTAFileParser p = FASTAFileParserFactory.getInstance()
				.getFASTAFileParser(tempFile.toFile())) {
			if (p.getNextEntry() == null) {
				throw new IllegalParameterException("Empty input FASTA file");
			}
			if (p.getNextEntry() != null) {
				throw new IllegalParameterException(
						"FASTA input must contain exactly one sequence");
			}
		} catch (FASTADataErrorException e) {
			throw new IllegalParameterException("Invalid input FASTA: " + e.getMessage(), e);
		}
	}


	private Map<String, Object> fromSearchResult(final SequenceSearchResult ssr) {
		final Map<String, Object> ret = new HashMap<>();
		ret.put(Fields.ALIGN_E_VAL, ssr.getEValue());
		ret.put(Fields.ALIGN_BIT_SCORE, ssr.getBitScore());
		
		ret.put(Fields.ALIGN_QUERY_ID, ssr.getQuery().getId());
		ret.put(Fields.ALIGN_QUERY_SEQ, ssr.getQuery().getAlignedSequence());
		ret.put(Fields.ALIGN_QUERY_LEN, ssr.getQuery().getSequenceLength());
		ret.put(Fields.ALIGN_QUERY_ALIGN_START, ssr.getQuery().getAlignmentStart());
		ret.put(Fields.ALIGN_QUERY_ALIGN_LEN, ssr.getQuery().getAlignmentLength());
		
		ret.put(Fields.ALIGN_TARGET_ID, ssr.getTarget().getId());
		ret.put(Fields.ALIGN_TARGET_SEQ, ssr.getTarget().getAlignedSequence());
		ret.put(Fields.ALIGN_TARGET_LEN, ssr.getTarget().getSequenceLength());
		ret.put(Fields.ALIGN_TARGET_ALIGN_START, ssr.getTarget().getAlignmentStart());
		ret.put(Fields.ALIGN_TARGET_ALIGN_LEN, ssr.getTarget().getAlignmentLength());
		return ret;
	}
	
}
