package us.kbase.genehomology.homology;

public class AlignedSequence {

	// assumes seq is < 2B bases
	
	final private String id;
	final private int sequenceLength;
	final private String alignedSequence;
	final private int alignmentStart;
	final private int alignmentLength;
	final private boolean forwardStrand;

	// could use a builder, although everything is required
	public AlignedSequence(
			final String id,
			final int sequenceLength,
			final String alignedSequence,
			final int alignmentStart,
			final int alignmentLength,
			final boolean forwardStrand) {
		this.id = id;
		this.sequenceLength = sequenceLength;
		this.alignedSequence = alignedSequence;
		this.alignmentStart = alignmentStart;
		this.alignmentLength = alignmentLength;
		this.forwardStrand = forwardStrand;
	}

	public String getId() {
		return id;
	}

	public boolean isForwardStrand() {
		return forwardStrand;
	}

	public int getSequenceLength() {
		return sequenceLength;
	}

	public String getAlignedSequence() {
		return alignedSequence;
	}

	public int getAlignmentStart() {
		return alignmentStart;
	}

	public int getAlignmentLength() {
		return alignmentLength;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alignedSequence == null) ? 0 : alignedSequence.hashCode());
		result = prime * result + alignmentLength;
		result = prime * result + alignmentStart;
		result = prime * result + (forwardStrand ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + sequenceLength;
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
		AlignedSequence other = (AlignedSequence) obj;
		if (alignedSequence == null) {
			if (other.alignedSequence != null) {
				return false;
			}
		} else if (!alignedSequence.equals(other.alignedSequence)) {
			return false;
		}
		if (alignmentLength != other.alignmentLength) {
			return false;
		}
		if (alignmentStart != other.alignmentStart) {
			return false;
		}
		if (forwardStrand != other.forwardStrand) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (sequenceLength != other.sequenceLength) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlignedSequence [id=");
		builder.append(id);
		builder.append(", sequenceLength=");
		builder.append(sequenceLength);
		builder.append(", alignedSequence=");
		builder.append(alignedSequence);
		builder.append(", alignmentStart=");
		builder.append(alignmentStart);
		builder.append(", alignmentLength=");
		builder.append(alignmentLength);
		builder.append(", forwardStrand=");
		builder.append(forwardStrand);
		builder.append("]");
		return builder.toString();
	}
}
