package org.joverseer.ui.domain.mapItems;

import java.util.ArrayList;
import java.util.HashMap;

import org.joverseer.game.Game;
import org.joverseer.metadata.domain.Hex;
import org.joverseer.support.GameHolder;
import org.joverseer.tools.HexInfoHistory;

/**
 * Holds information about the Hex Info Turn Report
 * 
 * @author Marios Skounakis
 *
 */
public class HexInfoTurnReportMapItem extends AbstractMapItem {
    
    private static final long serialVersionUID = -3977172376914475299L;
	HashMap hexes;
    
    public HexInfoTurnReportMapItem() {
    	Game g = GameHolder.instance().getGame();
    	hexes = new HashMap();
    	for (Hex h : (ArrayList<Hex>)g.getMetadata().getHexes()) {
    		int i = HexInfoHistory.getLatestHexInfoTurnNoForHex(h.getHexNo());
    		hexes.put(h.getHexNo(), i);
    	}
    }
    
	public HashMap getHexes() {
		return hexes;
	}

	public String getDescription() {
		return String.format("Hex info turn report.", new Object[]{});
	}

}
