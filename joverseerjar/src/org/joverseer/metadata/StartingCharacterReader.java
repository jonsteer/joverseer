package org.joverseer.metadata;

import org.joverseer.domain.Character;
import org.joverseer.support.Container;
import org.joverseer.support.infoSources.MetadataSource;

public class StartingCharacterReader extends BaseMetadataReader implements MetadataReader {
	Container<Character> characters;
	MetadataSource ms;

	@Override
	protected void initFilename() {
		super.filename = "startchars";
		super.isOptional = true;
	}

	@Override
	protected void start() {
		this.characters = new Container<Character>();
		this.ms = new MetadataSource();
	}

	@Override
	protected void finish(GameMetadata gm) {
		gm.setStartDummyCharacters(this.characters);
		this.characters = null;
		this.ms=null;
	}

	@Override
	protected void parseLine(String ln) {
		String[] parts = ln.split(";");
		if (parts.length < 4)
			return;
		int nationNo = Integer.parseInt(parts[3]);
		String charName = parts[1];
		String id = charName.toLowerCase().substring(0, Math.min(5, charName.length()));
		int hexNo = Integer.parseInt(parts[0]);
		Character c = new Character();
		c.setNationNo(new Integer(nationNo));
		c.setName(charName);
		c.setId(id);
		c.setHexNo(hexNo);
		c.setInfoSource(this.ms);
		c.setStartInfoDummy(true);
		this.characters.addItem(c);
	}

}
