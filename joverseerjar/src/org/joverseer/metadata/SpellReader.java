package org.joverseer.metadata;

import org.joverseer.metadata.domain.SpellInfo;
import org.joverseer.support.Container;

/**
 * Spell metadata reader. Reads spell metadata from data files.
 * 
 * @author Marios Skounakis
 * 
 */
public class SpellReader extends BaseMetadataReader  implements MetadataReader {
	Container<SpellInfo> spells;

	@Override
	protected void initFilename() {
		super.filename = "spells.csv";
	}

	@Override
	protected void start() {
		this.spells = new Container<SpellInfo>();
	}

	@Override
	protected void finish(GameMetadata gm) {
		gm.setSpells(this.spells);
		this.spells = null;
	}

	@Override
	protected void parseLine(String ln) {
		String[] parts = ln.split(";");
		SpellInfo si = new SpellInfo();
		si.setName(parts[0]);
		si.setDifficulty(parts[1]);
		si.setOrderNumber(new Integer(Integer.parseInt(parts[2])));
		si.setNumber(new Integer(Integer.parseInt(parts[3])));
		si.setRequiredInfo(parts[4]);
		si.setRequirements(parts[5]);
		si.setDescription(parts[6]);
		si.setList(parts[7]);
		this.spells.addItem(si);
	}
}
