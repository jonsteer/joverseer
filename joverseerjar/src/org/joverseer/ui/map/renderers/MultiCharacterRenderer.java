package org.joverseer.ui.map.renderers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import org.joverseer.domain.Character;
import org.joverseer.domain.CharacterDeathReasonEnum;
import org.joverseer.game.Game;
import org.joverseer.game.Turn;
import org.joverseer.game.TurnElementsEnum;
import org.joverseer.preferences.PreferenceRegistry;
import org.joverseer.support.GameHolder;
import org.joverseer.support.info.InfoUtils;
import org.joverseer.ui.map.JOSVGMap;
import org.joverseer.ui.map.MapTooltipHolder;
import org.joverseer.ui.support.drawing.ColorPicker;
import org.w3c.dom.svg.SVGElement;

/**
 * Renders all characters in the hex as seperate dots
 * 
 * @author Marios Skounakis
 */
public class MultiCharacterRenderer extends AbstractBaseRenderer {

    @Override
	public boolean appliesTo(Object obj) {
        return Character.class.isInstance(obj);
    }

    @Override
	public void render(Object obj, JOSVGMap s, int x, int y) {
        Game game = GameHolder.instance().getGame();
        Turn turn = game.getTurn();

        org.joverseer.domain.Character c = (Character)obj;

        // show dead chars according to preference
        String pval = PreferenceRegistry.instance().getPreferenceValue("map.deadCharacters");
        if (pval.equals("no")) {
            if (c.getDeathReason() != CharacterDeathReasonEnum.NotDead) return;
        }

        // do not show hostages
        if (c.getHostage() != null && c.getHostage()) return;

        ArrayList<Character> charsInHex = null;
        charsInHex = turn.getContainer(TurnElementsEnum.Character).findAllByProperty("hexNo", c.getHexNo());
        ArrayList<Character> toRemove = new ArrayList<Character>();
        if (pval.equals("no")) {
            for (Character ch : charsInHex) {
                if (ch.getDeathReason() != CharacterDeathReasonEnum.NotDead) {
                    toRemove.add(ch);
                }
            }
        }
        for (Character ch : charsInHex) {
            if (ch.getHostage() != null && ch.getHostage() == true && !toRemove.contains(ch)) {
                toRemove.add(ch);
            }
        }
        charsInHex.removeAll(toRemove);

        int i = charsInHex.indexOf(c);
        //HashMap mapOptions = (HashMap)Application.instance().getApplicationContext().getBean("mapOptions");
        //boolean simpleColors =!mapOptions.get(MapOptionsEnum.HexGraphics).equals(MapOptionValuesEnum.HexGraphicsTexture);
        pval = PreferenceRegistry.instance().getPreferenceValue("map.charsAndArmies");
        boolean simpleColors = pval.equals("simplified");
        
        
        if (i>0 && simpleColors) return;
        
        int ii = i % 12;
        int jj = i / 12;

        int dx = this.mapMetadata.getFullHexSize() / 6;
        int dy = this.mapMetadata.getFullHexSize() / 4;
        int w = this.mapMetadata.getMarkerSize();
        int h = this.mapMetadata.getMarkerSize();

        //todo make decision based on allegiance, not nation no
//      if (c.getNationNo() > 10) {
//      dx = dx + mapMetadata.getGridCellWidth() * mapMetadata.getHexSize() / 2 - w;
//      } else {
//      dx = mapMetadata.getGridCellWidth() * mapMetadata.getHexSize() / 2 - dx;
//      }
        String color1 = ColorPicker.getInstance().getHexColor1(c.getNationNo());
        String color2 = ColorPicker.getInstance().getHexColor2(c.getNationNo());
        if (simpleColors) {
        	color1 = "white";
        	color2 = "black";
        }
        
        boolean dragon = InfoUtils.isDragon(c.getName()); 
        if (dragon && !simpleColors) {
        	color1 = ColorPicker.getInstance().getHexColor("dragon");
        }

        int cx = x + dx + (w * ii);
        int cy = y + dy + (h * jj);
        
        SVGElement marker = s.createRect(""+cx, ""+cy, ""+w, ""+h, "fill: "+color1+"; stroke:"+color2+";");
        marker.setAttributeNS(null, "rx", "2");
        marker.setAttributeNS(null, "ry", "2");
        s.mapdoc.getElementById("hexInfo").appendChild(marker);
        

        if (c.getDeathReason() != CharacterDeathReasonEnum.NotDead) {
            SVGElement cline = s.createLine(""+cx, ""+cy, ""+(cx+w), ""+(cy+h), "stroke:"+color2+";");
            s.mapdoc.getElementById("hexInfo").appendChild(cline);
        }        
        
        if (dragon && !simpleColors) {
            SVGElement marker2 = s.createRect(""+(cx+2), ""+(cy+2), ""+(w-2), ""+(h-2), "fill: "+color2+"; stroke:"+color2+"; stroke-width:0");
            s.mapdoc.getElementById("hexInfo").appendChild(marker2);
            
        }
        MapTooltipHolder.instance().addTooltipObject(new Rectangle(cx, cy, w, h), c);
    }

	@Override
	public void refreshConfig() {
		//nothing to do.
	}
}
