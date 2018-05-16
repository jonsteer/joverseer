package org.joverseer.metadata;

import org.joverseer.domain.Character;
import org.joverseer.support.Container;
import org.joverseer.support.infoSources.MetadataSource;

/**
 * Reads starting character information
 * 
 * @author Marios Skounakis
 * 
 */
public class CharacterReader extends BaseMetadataReader implements MetadataReader {
	Container<Character> characters;
	MetadataSource ms;
	@Override
	protected void initFilename() {
		super.filename= "chars.csv";
	}

	@Override
	protected void start() {
		this.characters = new Container<Character>();
		this.ms  = new MetadataSource();
	}


	@Override
	protected void finish(GameMetadata gm) {
		gm.setCharacters(this.characters);
		this.characters = null;
	}

	@Override
	protected void parseLine(String ln) {
		String[] parts = ln.split(";");
		int nationNo = Integer.parseInt(parts[0]);
		String charName = parts[1];
		String id = charName.toLowerCase().substring(0, Math.min(5, charName.length()));
		int command = Integer.parseInt(parts[2]);
		int agent = Integer.parseInt(parts[3]);
		int emmisary = Integer.parseInt(parts[4]);
		int mage = Integer.parseInt(parts[5]);
		int stealth = Integer.parseInt(parts[6]);
		int challenge = Integer.parseInt(parts[7]);
		int numberOfOrders = 2;
		if (parts.length > 9) {
			numberOfOrders = Integer.parseInt(parts[9]);
		}
		Character c = new Character();
		c.setNationNo(new Integer(nationNo));
		c.setName(charName);
		c.setId(id);
		c.setCommand(command);
		c.setAgent(agent);
		c.setEmmisary(emmisary);
		c.setMage(mage);
		c.setStealth(stealth);
		c.setChallenge(challenge);
		c.setNumberOfOrders(numberOfOrders);
		c.setInfoSource(this.ms);
		this.characters.addItem(c);
	}
}
