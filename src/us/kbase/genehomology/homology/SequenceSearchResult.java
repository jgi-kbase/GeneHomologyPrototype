package us.kbase.genehomology.homology;

public class SequenceSearchResult {
	
	// %ID?
	
	private final AlignedSequence sequence1;
	private final AlignedSequence sequence2;
	private final double eValue;
	private final int bitScore;
	
	public SequenceSearchResult(
			final AlignedSequence sequence1,
			final AlignedSequence sequence2,
			final double eValue,
			final int bitScore) {
		this.sequence1 = sequence1;
		this.sequence2 = sequence2;
		this.eValue = eValue;
		this.bitScore = bitScore;
	}

	public double getEValue() {
		return eValue;
	}
	
	public int getBitScore() {
		return bitScore;
	}

	public AlignedSequence getSequence1() {
		return sequence1;
	}

	public AlignedSequence getSequence2() {
		return sequence2;
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
		result = prime * result + ((sequence1 == null) ? 0 : sequence1.hashCode());
		result = prime * result + ((sequence2 == null) ? 0 : sequence2.hashCode());
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
		if (sequence1 == null) {
			if (other.sequence1 != null) {
				return false;
			}
		} else if (!sequence1.equals(other.sequence1)) {
			return false;
		}
		if (sequence2 == null) {
			if (other.sequence2 != null) {
				return false;
			}
		} else if (!sequence2.equals(other.sequence2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SequenceSearchResult [sequence1=");
		builder.append(sequence1);
		builder.append(", sequence2=");
		builder.append(sequence2);
		builder.append(", eValue=");
		builder.append(eValue);
		builder.append(", bitScore=");
		builder.append(bitScore);
		builder.append("]");
		return builder.toString();
	}
}
