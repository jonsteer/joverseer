package org.joverseer.ui.map;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDefsElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGStyleElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class JOSVGMap {

	
	private DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    private String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    
	public SVGDocument mapdoc;
	
	private SVGStyleElement styleSheet = null;
	private SVGDefsElement defs = null;
	
	public JOSVGMap () {
		this.mapdoc = (SVGDocument) this.impl.createDocument(this.svgNS, "svg", null);
	}

	public SVGElement getRoot() {
			return (SVGElement) this.mapdoc.getDocumentElement();
	}
	
	public void createStyleSheet() {
		this.styleSheet = (SVGStyleElement) this.mapdoc.createElementNS(this.svgNS, "style");
		this.styleSheet.setAttributeNS(null, "type", "text/css");

		this.getRoot().appendChild(this.styleSheet);
	}
	
	
	public void createDefs() {
		this.defs = (SVGDefsElement) this.mapdoc.createElementNS(this.svgNS, "defs");
		
		Element symbol = this.mapdoc.createElementNS(this.svgNS, "symbol");
		symbol.setAttributeNS(null, "id", "camp");
		//symbol.setAttributeNS(null, "viewBox", "-15 0 30 38");
		symbol.setAttributeNS(null, "width", "30");
		symbol.setAttributeNS(null, "height", "36");
		symbol.setAttributeNS(null, "x", "-8");
		symbol.setAttributeNS(null, "y", "-8");
		
		Element path = this.mapdoc.createElementNS(this.svgNS, "path");
		//path.setAttributeNS(null,"d","M2,15 L15,2 L28,15 L27,15 L27,35 L3,35 L3,15 L0,15 L28,15");
		path.setAttributeNS(null,"d","M1,8 L8,1 L15,8 L15,18 L1,18 L1,8 L15,8");
		//path.setAttributeNS(null,"style","stroke:black; fill:white; stroke-width:1");
		path.setAttributeNS(null,"style","stroke-width:1");
		
		symbol.appendChild(path);
		this.defs.appendChild(symbol);
		
		this.getRoot().appendChild(this.defs);

	}	
		
	public SVGElement createGroup(String groupId) {
		SVGElement group = this.createElementWithId("g", groupId);
		this.getRoot().appendChild(group);
		return group;
	}
	
	public SVGElement createGroup(String groupId, String parentId) {
		SVGElement group = this.createElementWithId("g", groupId);
		SVGElement parent = (SVGElement) this.mapdoc.getElementById(parentId);
		parent.appendChild(group);
		return group;
	}
	
	public SVGElement createElementWithId(String tag, String id) {
		SVGElement e = (SVGElement) this.mapdoc.createElementNS(this.svgNS, tag);
		e.setAttributeNS(null,"ID", id);
		e.setIdAttributeNS(null, "ID", true);
			
		return e;
	}

	public void addStyle(String style) {
		this.styleSheet.appendChild(this.mapdoc.createCDATASection(style));		
	}

	public void setupDefs() {
		this.createDefs();
		
	}

	public void setupStyleSheet() {
		this.createStyleSheet();
		
		this.addStyle("line.road { stroke: #9a9a9a; stroke-width: 4; }");	
		this.addStyle("line.majorRiver { stroke: #1067ac; stroke-width: 4; }");
		this.addStyle("line.minorRiver { stroke: #008adf; stroke-width: 3; }");
		this.addStyle("line.ford { stroke: #0f2f2f; stroke-width: 6; }");
		this.addStyle("line.bridge { stroke: #885500; stroke-width: 6; }");
		this.addStyle(".hexnumber { font: bold 12px sans-serif; fill: black; text-anchor: middle; }");
	
	}
}
