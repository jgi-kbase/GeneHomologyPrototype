package us.kbase.genehomology.homology.last;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import us.kbase.genehomology.core.exceptions.IllegalParameterException;
import us.kbase.genehomology.core.exceptions.MissingParameterException;
import us.kbase.genehomology.homology.AlignedSequence;
import us.kbase.genehomology.homology.GeneHomologyDBLocation;
import us.kbase.genehomology.homology.GeneHomologyDBName;
import us.kbase.genehomology.homology.GeneHomologyDatabase;
import us.kbase.genehomology.homology.GeneHomologyImplementationException;
import us.kbase.genehomology.homology.GeneHomologyImplementationName;
import us.kbase.genehomology.homology.SequenceSearchResult;

public class LAST {
	
	private static final String LAST_ALIGN = "lastal";
	private static final GeneHomologyImplementationName NAME;
	static {
		try {
			NAME = new GeneHomologyImplementationName("last");
		} catch (IllegalParameterException | MissingParameterException e) {
			throw new RuntimeException("You big dummy", e);
		}
	}
	
	private final Path tempFileDirectory;
	private final int lastTimeoutSec;
	
	//TODO AAA All the code in this repo is prototype. It all needs to be rewritten to prod quality.
	//TODO CODE there's quite a bit of similarity with the AssemblyHomologyService. Shared repo?
	
	public LAST(final Path tempFileDirectory, final int lastTimeoutSec)
			throws GeneHomologyImplementationException { //TODO CODE make init exception
		checkNotNull(tempFileDirectory, "tempFileDirectory");
		if (lastTimeoutSec < 1) {
			throw new IllegalArgumentException("mashTimeout must be > 0");
		}
		this.lastTimeoutSec = lastTimeoutSec;
		this.tempFileDirectory = tempFileDirectory;
		try {
			Files.createDirectories(tempFileDirectory);
		} catch (IOException e) {
			throw new GeneHomologyImplementationException( //TODO CODE init exception
					"Couldn't create temporary directory: " + e.getMessage(), e);
		}
	}
	
	public GeneHomologyDatabase getDatabase(
			final GeneHomologyDBName dbName,
			final GeneHomologyDBLocation loc)
			throws GeneHomologyImplementationException {
		checkNotNull(dbName, "dbName");
		checkNotNull(loc, "loc");
		final Properties props = new Properties();
		try (final InputStream is = Files.newInputStream(loc.getPathToFile().get())) {
			props.load(is);
		} catch (IOException e) {
			throw new GeneHomologyImplementationException(String.format(
					"Couldn't open LAST database file %s: %s",
					loc.getPathToFile().get(), e.getMessage()), e);
		}
		if (!props.containsKey("numofsequences")) { // could check a couple more keys
			throw new GeneHomologyImplementationException(String.format(
					"File %s is not a LAST database file", loc.getPathToFile().get()));
		}
		return new GeneHomologyDatabase(
				dbName,
				NAME,
				loc,
				Integer.parseInt(props.getProperty("numofsequences")));
	}
	
	public List<SequenceSearchResult> search(
			final GeneHomologyDBName dbName,
			final GeneHomologyDBLocation searchDB,
			final Path queryFasta)
			throws GeneHomologyImplementationException {
		checkNotNull(queryFasta, "queryFasta");
		@SuppressWarnings("unused")
		final GeneHomologyDatabase db = getDatabase(dbName, searchDB); //TODO NOW return
		Path tempFile = null;
		String dbpath = searchDB.getPathToFile().get().toString();
		dbpath = dbpath.substring(0, dbpath.length() - 4); // remove .prj
		try {
			tempFile = Files.createTempFile(tempFileDirectory, "last_output", ".tmp");
			runLASTToOutputFile(tempFile, dbpath, queryFasta.toString());
			return processLASTOutput(tempFile);
			// all of the below is really hard to test
		} catch (IOException e) {
			throw new GeneHomologyImplementationException(e.getMessage(), e);
		} finally {
			if (tempFile != null) {
				try {
					Files.delete(tempFile);
				} catch (IOException e) {
					throw new GeneHomologyImplementationException(e.getMessage(), e);
				}
			}
		}
	}
	
	private void runLASTToOutputFile(final Path outputPath, final String... arguments)
			throws GeneHomologyImplementationException {
		final List<String> command = new LinkedList<>(Arrays.asList(LAST_ALIGN));
		command.addAll(Arrays.asList(arguments));
		try {
			final ProcessBuilder pb = new ProcessBuilder(command);
			// it's far less complicated if we just redirect to a file rather than have
			// threads consuming output and error so they don't deadlock
			pb.redirectOutput(outputPath.toFile());
			final Process last = pb.start();
			if (!last.waitFor(lastTimeoutSec, TimeUnit.SECONDS)) {
				// not sure how to test this
				throw new GeneHomologyImplementationException(String.format(
						"Timed out waiting for %s to run", LAST_ALIGN));
			}
			if (last.exitValue() != 0) {
				try (final InputStream is = last.getErrorStream()) {
					throw new GeneHomologyImplementationException(String.format(
							"Error running %s: %s", LAST_ALIGN, IOUtils.toString(is).trim()));
				}
			}
		} catch (IOException | InterruptedException e) {
			// this is also very difficult to test
			throw new GeneHomologyImplementationException(String.format(
					"Error running %s: ", LAST_ALIGN) + e.getMessage(), e);
		}
	}
	
	final List<SequenceSearchResult> processLASTOutput(final Path output)
			throws IOException, GeneHomologyImplementationException {
		final List<SequenceSearchResult> ret = new ArrayList<>();
		try (final InputStream is = Files.newInputStream(output)) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					is, StandardCharsets.UTF_8));
			final List<String> recordLines = new ArrayList<String>(3);
			final List<Double> lambdaAndK = getKAndLambda(br);
			final double lambda = lambdaAndK.get(0);
			final double K = lambdaAndK.get(1);
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (line.startsWith("#") || line.trim().isEmpty()) {
					continue;
				}
				set3Lines(recordLines, line, br);
				ret.add(processLASTRecord(recordLines, lambda, K));
			}
		}
		return ret;
	}
	
	// list is 2 elements, lambda then K
	private List<Double> getKAndLambda(final BufferedReader lastOutput)
			throws GeneHomologyImplementationException, IOException {
		for (String line = lastOutput.readLine(); line != null; line = lastOutput.readLine()) {
			// super fragile and hacky, yuck.
			if (line.startsWith("#") && line.contains("K=") && line.contains("lambda=")) {
				final String[] chunks = line.split("\\s+");
				try {
					final double lambda = Double.parseDouble(chunks[1].substring(7));
					final double K = Double.parseDouble(chunks[2].substring(2));
					return Arrays.asList(lambda, K);
				} catch (NumberFormatException e) {
					// basically impossible to test
					throw new GeneHomologyImplementationException(
							"Improperly formatted K and lambda in LAST output line: " + line);
				}
			}
		}
		// also basically impossible to test
		throw new GeneHomologyImplementationException("Couldn't find lambda and K in LAST output");
	}

	private SequenceSearchResult processLASTRecord(
			final List<String> recordLines,
			final double lambda,
			final double K) {
		//TODO CODE lots of error checking here
		//TODO CODE nasty & brittle
		final String[] line1 = recordLines.get(0).split("\\s+");
		final double eVal = Double.parseDouble(line1[3].substring(2));
		final int score = Integer.parseInt(line1[1].substring(6));
		return new SequenceSearchResult(
				toAlignedSequence(recordLines.get(1)),
				toAlignedSequence(recordLines.get(2)),
				eVal,
				toBitScore(score, lambda, K));
	}
	
	final private static double ln2 = Math.log(2);
	
	private int toBitScore(final int score, final double lambda, final double K) {
		return (int) Math.round((lambda * score - Math.log(K)) / ln2);
	}

	private AlignedSequence toAlignedSequence(final String sequenceLine) {
		//TODO CODE lots of error checking here
		//TODO CODE nasty & brittle
		final String[] seqline = sequenceLine.split("\\s+");
		return new AlignedSequence(
				seqline[1],
				Integer.parseInt(seqline[5]),
				seqline[6],
				Integer.parseInt(seqline[2]),
				Integer.parseInt(seqline[3]),
				seqline[4].equals("+"));
	}

	private void set3Lines(
			final List<String> recordLines,
			final String firstLine,
			final BufferedReader br)
			throws GeneHomologyImplementationException, IOException {
		recordLines.clear();
		if (firstLine == null) {
			//TODO CODE better info about record
			throw new GeneHomologyImplementationException("Bad record in LAST output");
		}
		recordLines.add(firstLine);
		for (int i = 1; i <= 2; i++) {
			final String line = br.readLine();
			if (line == null) {
				//TODO CODE better info about record
				throw new GeneHomologyImplementationException("Bad record in LAST output");
			}
			recordLines.add(line);
		}
	}

	public static void main(final String[] args) throws Exception {
		final LAST l = new LAST(Paths.get("./temp_delete"), 300);
		
		final GeneHomologyDatabase db = l.getDatabase(
				new GeneHomologyDBName("foo"),
				new GeneHomologyDBLocation(Paths.get(
						"/media/mongohd/genehom/LAST/uniref50_40Mlines.last.prj")));
		
		System.out.println(db);
		
		final List<SequenceSearchResult> seqs = l.search(
				new GeneHomologyDBName("foo"),
				new GeneHomologyDBLocation(Paths.get(
						"/media/mongohd/genehom/LAST/uniref50_40Mlines.last.prj")),
				Paths.get("/media/mongohd/genehom/LAST/UniRef50_A0A257EYX4.fasta"));
		
		for (final SequenceSearchResult ssr: seqs) {
			System.out.println("--------------------------------");
			System.out.println(ssr.getEValue());
			System.out.println(ssr.getBitScore());
			final AlignedSequence as1 = ssr.getSequence1();
			System.out.println(String.format("%s %s %s %s %s",
					as1.getId(), as1.getSequenceLength(), as1.getAlignmentStart(),
					as1.getAlignmentLength(), as1.isForwardStrand()));
			final AlignedSequence as2 = ssr.getSequence2();
			System.out.println(String.format("%s %s %s %s %s",
					as2.getId(), as2.getSequenceLength(), as2.getAlignmentStart(),
					as2.getAlignmentLength(), as2.isForwardStrand()));
			System.out.println(as1.getAlignedSequence());
			System.out.println(as2.getAlignedSequence());
		}
	}
}

