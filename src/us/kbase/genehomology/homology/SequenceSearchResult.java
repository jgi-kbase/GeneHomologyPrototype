package us.kbase.genehomology.homology;

public class SequenceSearchResult {
	
	// could do gaps, matches, and mismatches in same pass, but meh for now
	
	private final AlignedSequence query;
	private final AlignedSequence target;
	private final double eValue;
	private final int bitScore;
	private int matches = -1;
	private int mismatches = -1;
	
	public SequenceSearchResult(
			final AlignedSequence query,
			final AlignedSequence target,
			final double eValue,
			final int bitScore) {
		if (query.getAlignmentLengthWithGaps() != target.getAlignmentLengthWithGaps()) {
			throw new IllegalArgumentException("Alignment lengths are unequal");
		}
		this.query = query;
		this.target = target;
		this.eValue = eValue;
		this.bitScore = bitScore;
	}

	public double getEValue() {
		return eValue;
	}
	
	public int getBitScore() {
		return bitScore;
	}

	public AlignedSequence getQuery() {
		return query;
	}

	public AlignedSequence getTarget() {
		return target;
	}
	
	public int getAlignmentLength() {
		return query.getAlignmentLengthWithGaps();
	}
	
	public int getGapOpenCount() {
		return query.getGapOpenCount() + target.getGapOpenCount();
	}

	public double getPercentID() {
		calculateMatchesAndMismatches();
		return matches / (double) getAlignmentLength();
	}
	
	public int getMismatches() {
		calculateMatchesAndMismatches();
		return mismatches;
	}
	
	private void calculateMatchesAndMismatches() {
		if (matches != -1) { 
			return;
		}
		matches = 0;
		mismatches = 0;
		final String qs = query.getAlignedSequence();
		final String ts = target.getAlignedSequence();
		for (int i = 0; i < getAlignmentLength(); i++) {
			final char q = qs.charAt(i);
			final char t = ts.charAt(i);
			if (q != '-' && t != '-') {
				if (q == t) {
					matches++;
				} else {
					mismatches++;
				}
				
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bitScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(eValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		SequenceSearchResult other = (SequenceSearchResult) obj;
		if (Double.doubleToLongBits(bitScore) != Double.doubleToLongBits(other.bitScore)) {
			return false;
		}
		if (Double.doubleToLongBits(eValue) != Double.doubleToLongBits(other.eValue)) {
			return false;
		}
		if (query == null) {
			if (other.query != null) {
				return false;
			}
		} else if (!query.equals(other.query)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SequenceSearchResult [sequence1=");
		builder.append(query);
		builder.append(", sequence2=");
		builder.append(target);
		builder.append(", eValue=");
		builder.append(eValue);
		builder.append(", bitScore=");
		builder.append(bitScore);
		builder.append("]");
		return builder.toString();
	}
}
