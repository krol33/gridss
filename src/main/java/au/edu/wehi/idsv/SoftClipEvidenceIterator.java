package au.edu.wehi.idsv;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;

import java.util.Iterator;
import java.util.Queue;

import au.edu.wehi.idsv.sam.SAMRecordUtil;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;

/**
 * Iterators over soft clip evidence in the same order as the input iterator 
 * @author Daniel Cameron
 *
 */
public class SoftClipEvidenceIterator extends AbstractIterator<SoftClipEvidence> implements CloseableIterator<SoftClipEvidence> {
	private final ProcessingContext processContext;
	private final SAMEvidenceSource source;
	private final Iterator<SAMRecord> it;
	private final Queue<SoftClipEvidence> buffer = Queues.newArrayDeque();
	public SoftClipEvidenceIterator(ProcessingContext processContext, SAMEvidenceSource source, Iterator<SAMRecord> it) {
		this.processContext = processContext;
		this.source = source;
		this.it = it;
	}
	private void fillBuffer() {
		while (it.hasNext() && buffer.isEmpty()) {
			SAMRecord record = it.next();
			if (!record.getReadUnmappedFlag()) {
				if (SAMRecordUtil.getStartSoftClipLength(record) > 0) {
					SoftClipEvidence sce = SoftClipEvidence.create(processContext, source, BreakendDirection.Backward, record);
					if (sce.meetsEvidenceCritera(processContext.getSoftClipParameters())) {
						buffer.add(sce);
					}
				}
				if (SAMRecordUtil.getEndSoftClipLength(record) > 0) {
					SoftClipEvidence sce = SoftClipEvidence.create(processContext, source, BreakendDirection.Forward, record);
					if (sce.meetsEvidenceCritera(processContext.getSoftClipParameters())) {
						buffer.add(sce);
					}
				}
			}
		}
	}
	@Override
	protected SoftClipEvidence computeNext() {
		if (buffer.isEmpty()) fillBuffer();
		if (buffer.isEmpty()) return endOfData();
		return buffer.poll();
	}
	@Override
	public void close() {
		CloserUtil.close(it);
	}
}
