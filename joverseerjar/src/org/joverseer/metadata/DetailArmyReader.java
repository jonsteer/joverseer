package org.joverseer.metadata;

import org.joverseer.domain.Army;
import org.joverseer.domain.ArmyElement;
import org.joverseer.domain.ArmyElementType;
import org.joverseer.domain.ArmySizeEnum;
import org.joverseer.domain.InformationSourceEnum;
import org.joverseer.metadata.domain.NationAllegianceEnum;
import org.joverseer.support.Container;
import org.joverseer.support.infoSources.MetadataSource;

/**
 * Reads starting army information.
 * 
 * This is the "detailed" reader, ie it reads army details such as training,
 * weapon/armor/training ranks, etc
 * 
 * @author Marios Skounakis
 * 
 */
public class DetailArmyReader extends BaseMetadataReader implements MetadataReader {
	Container<Army> armies;

	@Override
	protected void initFilename() {
		super.filename = "startarmies";
	};

	@Override
	protected void start() {
		this.armies = new Container<Army>();
	};

	@Override
	protected void finish(GameMetadata gm) {
		gm.setArmies(this.armies);
		this.armies = null;
	};

	@Override
	protected void parseLine(String ln) {
		String parts[] = ln.split(";");
		String hexNo = parts[0];
		String commander = parts[1];
		NationAllegianceEnum allegiance = null;
		if (parts[2].equals("1")) {
			allegiance = NationAllegianceEnum.FreePeople;
		} else if (parts[2].equals("2")) {
			allegiance = NationAllegianceEnum.DarkServants;
		} else {
			allegiance = NationAllegianceEnum.Neutral;
		}
		int nationNo = Integer.parseInt(parts[3]);
		boolean navy = parts[4].equals("1");
		ArmySizeEnum size = ArmySizeEnum.unknown;
		int morale = Integer.parseInt(parts[7]);

		Army army = new Army();
		army.setHexNo(hexNo);
		army.setCommanderName(commander);
		army.setCommanderTitle("");
		army.setNationAllegiance(allegiance);
		army.setNationNo(new Integer(nationNo));
		army.setMorale(morale);
		army.setSize(size);
		army.setNavy(navy);
		int i = 0;
		for (ArmyElementType aet : ArmyElementType.values()) {
			int si = 8 + i * 5;
			i++;
			if (si + 1 >= parts.length)
				continue;
			String no = parts[si + 1];
			if (!no.equals("")) {
				ArmyElement ae = new ArmyElement(aet, Integer.parseInt(no));
				String training = parts[si + 2];
				if (!training.equals("")) {
					ae.setTraining(Integer.parseInt(training));
				}
				String we = parts[si + 3];
				if (!we.equals("")) {
					ae.setWeapons(Integer.parseInt(we));
				}
				String ar = parts[si + 4];
				if (!ar.equals("")) {
					ae.setArmor(Integer.parseInt(ar));
				}
				army.getElements().add(ae);
			}
		}
		army.setCavalry(army.computeCavalry());
		army.setInformationSource(InformationSourceEnum.detailed);
		army.setInfoSource(new MetadataSource());
		this.armies.addItem(army);
	}

}
