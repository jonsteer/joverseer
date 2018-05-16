package org.joverseer.metadata;

import org.joverseer.metadata.domain.NationMapRange;
import org.joverseer.support.Container;

/**
 * Reads the nation map ranges from data files.
 * 
 * @author Marios Skounakis
 * 
 */
public class NationMapRangeReader extends BaseMetadataReader implements MetadataReader {
	Container<NationMapRange> mapRanges;

	@Override
	protected void initFilename() {
		super.filename = "maps.csv";
	}

	@Override
	protected void start() {
		this.mapRanges = new Container<NationMapRange>();
	}

	@Override
	protected void finish(GameMetadata gm) {
		gm.setNationMapRanges(this.mapRanges);
		this.mapRanges = null;
	}

	@Override
	protected void parseLine(String ln) {
		NationMapRange nmr = new NationMapRange();
		String[] parts = ln.split(";");
		int nationNo = Integer.parseInt(parts[0]);
		int x1 = Integer.parseInt(parts[1]);
		int y1 = Integer.parseInt(parts[2]);
		int x2 = Integer.parseInt(parts[3]);
		int y2 = Integer.parseInt(parts[4]);
		this.mapRanges.addItem(nmr);
		nmr.setNationNo(nationNo);
		nmr.setTlX(x1);
		nmr.setTlY(y1);
		nmr.setBrX(x2);
		nmr.setBrY(y2);
	}
}
