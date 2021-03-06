package au.edu.wehi.idsv;

import com.google.common.collect.Lists;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;

/**
 * Creates a SAMSequenceDictionary that lazily adds contigs as they are referenced. 
 * @author Daniel Cameron
 *
 */
public class DynamicSAMSequenceDictionary extends SAMSequenceDictionary {
	private static final long serialVersionUID = 1L;
	public DynamicSAMSequenceDictionary(SAMSequenceDictionary dictionary) {
		super(Lists.newArrayList(dictionary.getSequences()));
	}
	@Override
	public SAMSequenceRecord getSequence(String name) {
		SAMSequenceRecord seq = super.getSequence(name); 
		if (seq == null) {
			addSequence(name);
		}
		return super.getSequence(name);
	}
	@Override
	public int getSequenceIndex(String sequenceName) {
		int index = super.getSequenceIndex(sequenceName); 
		if (index < 0) {
			addSequence(sequenceName);
		}
		return super.getSequenceIndex(sequenceName);
	}
	private synchronized void addSequence(String sequence) {
		int index = super.getSequenceIndex(sequence);
		if (index < 0) {
			addSequence(new SAMSequenceRecord(sequence, 0));
		}
	}
}
