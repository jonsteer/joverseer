package org.joverseer.tools.infoCollectors.artifacts;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.joverseer.domain.Artifact;
import org.joverseer.domain.Character;
import org.joverseer.game.Game;
import org.joverseer.game.Turn;
import org.joverseer.game.TurnElementsEnum;
import org.joverseer.metadata.domain.ArtifactInfo;
import org.joverseer.metadata.domain.Nation;
import org.joverseer.support.Container;
import org.joverseer.support.GameHolder;
import org.joverseer.support.infoSources.MetadataSource;
import org.joverseer.ui.LifecycleEventsEnum;
import org.joverseer.ui.support.JOverseerEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.richclient.application.Application;

/**
 * Collects information from all turns for artifacts and stores them in a
 * container.
 * 
 * This class scans all turns and collates all information found with respect to
 * artifacts. More specifically it: - initializes the information from the
 * artifact metadata - updates powers, hex numbers and owners from each turn,
 * keeping the latest info each time - makes heavy use of info sources to
 * maintain the source of each piece of information
 * 
 * In the end of the day it aggregates all intelligence for artifacts as found
 * in: - xml turns - LA/LATs - starting info
 * 
 * @author Marios Skounakis
 * 
 */
public class ArtifactInfoCollector implements ApplicationListener {
	static Logger logger = Logger.getLogger(ArtifactInfoCollector.class);

	public static ArtifactInfoCollector instance() {
		return (ArtifactInfoCollector) Application.instance().getApplicationContext().getBean("artifactInfoCollector");
	}

	HashMap<Integer, Container<ArtifactWrapper>> turnInfo = new HashMap<Integer, Container<ArtifactWrapper>>();

	public Container<ArtifactWrapper> computeWrappersForTurn(int turnNo) {
		Game g = GameHolder.instance().getGame();

		MetadataSource ms = new MetadataSource();
		Container<ArtifactWrapper> aws = new Container<ArtifactWrapper>(new String[] { "number" });
		for (ArtifactInfo ai : (ArrayList<ArtifactInfo>) g.getMetadata().getArtifacts().getItems()) {
			ArtifactWrapper aw = new ArtifactWrapper(ai);
			aw.setInfoSource(ms);
			aw.setTurnNo(0);
			aws.addItem(aw);
		}

		// loop over turns
		for (int i = 0; i <= turnNo; i++) {
			Turn t = g.getTurn(i);
			if (t == null)
				continue;
			for (Artifact a : t.getArtifacts()) {
				ArtifactWrapper aw = aws.findFirstByProperty("number", a.getNumber());
				if (aw == null || a.getNumber() == 0) {
					// try to find by name
					aw = aws.findFirstByProperty("unAccentedName", a.getName());
					if (aw == null) {
						logger.error("Failed to find artifact wrapper for #" + a.getNumber() + " " + a.getName());
						// create new
						aw = new ArtifactWrapper();
						aw.setName(a.getName());
						aw.setTurnNo(i);
						aws.addItem(aw);
					}
					
					aw.setNumber(a.getNumber());
				}

				aw.setOwner(a.getOwner());
				aw.setHexNo(a.getHexNo());
				aw.setInfoSource(a.getInfoSource());
				aw.setTurnNo(t.getTurnNo());
				Character c = (Character) t.getContainer(TurnElementsEnum.Character).findFirstByProperty("name", aw.getOwner());
				if (c != null) {
					aw.setNationNo(c.getNationNo());
					if (c.getNationNo() == null || c.getNationNo() == 0) {
						c = (Character) g.getMetadata().getCharacters().findFirstByProperty("id", c.getId());
						if (c != null) {
							aw.setNationNo(c.getNationNo());
						}
					}
				}
				// owner is a nation (hidden artifact)
				Nation n = g.getMetadata().getNationByName(a.getOwner());
				if (n != null) {
					aw.setNationNo(n.getNumber());
				}
				
				
			}

			for (Character c : t.getCharacters()) {
				for (Integer aid : c.getArtifacts()) {
					ArtifactWrapper aw = aws.findFirstByProperty("number", aid);
					
					if (aw == null)
						continue;

					aw.setOwner(c.getName());
					aw.setHexNo(c.getHexNo());
					aw.setInfoSource(c.getInfoSource());
					aw.setNationNo(c.getNationNo());
					aw.setTurnNo(t.getTurnNo());
				}
			}
			for (ArtifactUserInfo aui : t.getArtifactsUser()) {
				this.checkAndSetUserInfo(aui, aws);
			}
		}
		return aws;

	}

	private void checkAndSetUserInfo(ArtifactUserInfo aui, Container<ArtifactWrapper> aws) {
		ArtifactWrapper aw;
		if(!aui.wasNoZero()) {
			aw = (aws.findFirstByProperty("number", aui.getNumber()));
			
		}
		else {
			aw = (aws.findFirstByProperty("name", aui.getName()));
		}
		
		if (aw != null) {
			
			if(aw.getNumber() == 0) {
				aw.setNumber(aui.getNumber());
				if(aui.wasNoZero()) aw.setNoZero(true);
			}
			
			
			if(aui.getName() != null) aw.setName(aui.getName());
			if(aui.getNationNo() != null) aw.setNationNo(aui.getNationNo());
			if(aui.getOwner() != null) aw.setOwner(aui.getOwner());
			if(aui.getHexNo() != 0) aw.setHexNo(aui.getHexNo());
			if(aui.getAlignment() != null) aw.setAlignment(aui.getAlignment());
			if(aui.getPower1() != null)aw.setPower1(aui.getPower1());
			if(aui.getPower2() != null)aw.setPower2(aui.getPower2());
			aw.setTurnNo(aui.getTurnNo());
			aw.setInfoSource(aui.getInfoSource());
		}	
	}
	
	public void refreshWrappers() {
		this.turnInfo.clear();
	}
	
	public ArtifactWrapper getArtifactForTurn(int number, int turnNo) {
		if (!GameHolder.hasInitializedGame()) {
			return null;
		}
		getWrappersForTurn(turnNo);
		Container<ArtifactWrapper> ret = this.turnInfo.get(turnNo);
		return ret.findFirstByProperty("number", number);
	}

	public ArrayList<ArtifactWrapper> getArtifactsForOwnerAndTurn(String owner, int turnNo) {
		if (!GameHolder.hasInitializedGame()) {
			return new ArrayList<ArtifactWrapper>();
		}
		getWrappersForTurn(turnNo);
		Container<ArtifactWrapper> ret = this.turnInfo.get(turnNo);
		return ret.findAllByProperty("owner", owner);
	}

	public ArrayList<ArtifactWrapper> getWrappers() {
		return getWrappersForTurn(-1);
	}

	public ArrayList<ArtifactWrapper> getWrappersForTurn(int turnNo) {
		if (!GameHolder.hasInitializedGame()) {
			Container<ArtifactWrapper> ret = new Container<ArtifactWrapper>(new String[] { "name", "turnNo", "number" });
			return ret.getItems();
		}
		
		Game game = GameHolder.instance().getGame();
		if (turnNo == -1) turnNo = game.getCurrentTurn();
		
		if (!this.turnInfo.containsKey(turnNo)) {
			this.turnInfo.put(turnNo, computeWrappersForTurn(turnNo));
		}
		
		return this.turnInfo.get(turnNo).getItems();
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof JOverseerEvent) {
			JOverseerEvent e = (JOverseerEvent) applicationEvent;
			if (e.isLifecycleEvent(LifecycleEventsEnum.GameChangedEvent)) {
				this.turnInfo.clear();
			}
		}
	}

}
