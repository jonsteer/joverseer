package org.joverseer.ui.map.renderers;

import java.awt.Graphics2D;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.joverseer.ui.map.MapMetadata;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public abstract class AbstractBaseRenderer implements Renderer {
	// injected dependency
	MapMetadata mapMetadata = null;
	
	public DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    public String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;		

    @Override
    public boolean isSVGReady() {
    	return false;
    }	
    
	public MapMetadata getMapMetadata() {
		return this.mapMetadata;
	}

	public void setMapMetadata(MapMetadata mapMetadata) {
		this.mapMetadata = mapMetadata;
	}

	public AbstractBaseRenderer() {
	}

    @Override
	public void render(Object obj, SVGDocument s, int x, int y) {
    	//Not used for Image
    }	
    
	
    @Override
	public void render(Object obj, Graphics2D g, int x, int y) {
    	//not used for SVG
    }	    

}
