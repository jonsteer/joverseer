package org.joverseer.ui.map.renderers;

import org.joverseer.metadata.domain.Hex;
import org.joverseer.ui.map.JOSVGMap;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;

/**
 * Renders the hex number
 *  
 * @author Marios Skounakis
 */
public class HexNumberRenderer extends AbstractBaseRenderer {
	// these are normally injected.
    String fontName = "SansSerif";
    int fontSize = 8;
    int fontStyle = Font.PLAIN;
    
    @Override
    public boolean isSVGReady() {
    	return true;
    }	    

    public HexNumberRenderer() {
    }

    @Override
	public boolean appliesTo(Object obj) {
        return Hex.class.isInstance(obj);
    }
    
    @Override
	public void render(Object obj, JOSVGMap s, int x, int y) {
        if (!appliesTo(obj)) {
            throw new IllegalArgumentException(obj.toString());
        }

        Hex hex = (Hex)obj;
        if (!this.mapMetadata.withinMapRange(hex)) return;
        //set font size based on cell width:
        //this.fontSize = this.mapMetadata.getGridCellWidth();
        this.fontSize = 12;

        String hexNo = hex.getHexNoStr();

        x = this.mapMetadata.getGridCellWidth() * this.mapMetadata.getHexSize() / 2 + x;
        y = this.mapMetadata.getGridCellHeight() * this.mapMetadata.getHexSize() / 4 + y;
 
        Element t = s.mapdoc.createElementNS(this.svgNS, "text");
        t.setAttributeNS(null, "x", ""+x);
        t.setAttributeNS(null, "y", ""+y);
        //t.setAttributeNS(null, "text-anchor", "middle");
        
        t.setAttributeNS(null, "class", "hexnumber");
        //TODO replace style with stylesheet
        //t.setAttributeNS(null, "style", "font: bold "+this.fontSize+"px sans-serif; fill: black;");
        
        t.setTextContent(hexNo);
        
        Element mapLabels = s.mapdoc.getElementById("mapLabels");
        mapLabels.appendChild(t);
        
        
    }

    public String getFontName() {
        return this.fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontStyle() {
        return this.fontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

	@Override
	public void refreshConfig() {
		//nothing to do.
		
	}
}
