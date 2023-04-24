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
	
	public SVGStyleElement createStyleSheet(String id) {
		SVGStyleElement style = (SVGStyleElement) this.mapdoc.createElementNS(this.svgNS, "style");
		style.setAttributeNS(null, "type", "text/css");
		style.setAttributeNS(null, "id", id);
		style.setIdAttributeNS(null, "id", true);
		
		return style;
	}
	
	
	public void createDefs() {
		this.defs = (SVGDefsElement) this.mapdoc.createElementNS(this.svgNS, "defs");
		this.getRoot().appendChild(this.defs);

	}	
	
	public SVGElement createSymbol(String id, String width, String height, String x, String y) {
		SVGElement symbol = (SVGElement) this.mapdoc.createElementNS(this.svgNS, "symbol");
		symbol.setAttributeNS(null, "id", id);
		symbol.setAttributeNS(null, "width", width);
		symbol.setAttributeNS(null, "height", height);
		symbol.setAttributeNS(null, "x", x);
		symbol.setAttributeNS(null, "y", y);
		
		return symbol;
	}
	
	public SVGElement createPattern(String id, String width, String height, String transform, String units) {
		SVGElement s = (SVGElement) this.mapdoc.createElementNS(this.svgNS, "pattern");
		s.setAttributeNS(null, "id", id);
		s.setAttributeNS(null, "width", width);
		s.setAttributeNS(null, "height", height);
		s.setAttributeNS(null, "patternTransform", transform);
		s.setAttributeNS(null, "patternUnits", units);
		
		return s;
	}	
	
	public SVGElement createLine(String x1, String y1, String x2, String y2, String style) {
		SVGElement s = (SVGElement) this.mapdoc.createElementNS(this.svgNS, "line");
		s.setAttributeNS(null, "style", style);
		s.setAttributeNS(null, "x1", x1);
		s.setAttributeNS(null, "y1", y1);
		s.setAttributeNS(null, "x2", x2);
		s.setAttributeNS(null, "y2", y2);
		
		return s;
	}	
	
	public SVGElement createPath(String d, String style) {
		SVGElement path = (SVGElement) this.mapdoc.createElementNS(this.svgNS, "path");
		path.setAttributeNS(null, "d", d);
		path.setAttributeNS(null, "style", style);
		
		return path;
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
		
		//Camp
		SVGElement symbol = this.createSymbol("camp", "16", "20", "-8", "-8");
		SVGElement path = this.createPath("M1,8 L8,1 L15,8 L15,18 L1,18 L1,8 L15,8",
											"stroke-width:1");
		symbol.appendChild(path);
		this.defs.appendChild(symbol);		
		
		//Village
		symbol = this.createSymbol("village", "22", "20", "-11", "-8");
		path = this.createPath("M0,8 L5,3 L10,8 L15,3 L20,8 z M20,18 L0,18 L0,8 L20,8 z",
											"stroke-width:1");
		symbol.appendChild(path);
		this.defs.appendChild(symbol);
		
		//Town
		symbol = this.createSymbol("town", "26", "20", "-13", "-8");
		path = this.createPath("M0,8 L6,1 L13,8 L19,1 L25,8 L24,8 L24,18 L1,18 L1,8 L24,8 z",
											"stroke-width:1");
		symbol.appendChild(path);
		path = this.createPath("M5,10 L13,4 L20,10 L7,10 L9,10 L9,18 L17,18 L17,10 z",
											"stroke-width:1");
		symbol.appendChild(path);
		this.defs.appendChild(symbol);	
		
		//Major Town
		symbol = this.createSymbol("majorTown", "40", "20", "-20", "-8");
		path = this.createPath("M0,8 L8,1 L15,8 L25,8 L33,1 L40,8 L39,8 L39,18 L1,18 L1,8 L39,8 z",
											"stroke-width:1");
		symbol.appendChild(path);
		path = this.createPath("M13,10 L20,4 L28,10 L14,10 L16,10 L16,18 L24,18 L24,10 z",
											"stroke-width:1");
		symbol.appendChild(path);
		this.defs.appendChild(symbol);	
		
		//City
		symbol = this.createSymbol("city", "46", "20", "-23", "-8");
		path = this.createPath("M0,8 L8,1 L15,8 L23,1 L30,8 L38,1 L45,8 L45,18 L0,18 L0,8 L45,8 z",
											"stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);	
		
		//Ruins
		symbol = this.createSymbol("ruins", "15", "20", "-7", "-8");
		path = this.createPath("M0,18 L4,8 L7,18 L11,13 L14,18 z",
											"stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);			
		
		
		//Tower
		symbol = this.createSymbol("tower", "18", "24", "-9", "-14");
		path = this.createPath("M1,24 L1,0 L7,0 L7,4 L10,4 L10,0 L16,0 L16,24 z",
											"stroke:black; fill:lightgray; stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);		
		
		//Fort
		symbol = this.createSymbol("fort", "26", "24", "-13", "-14");
		path = this.createPath("M0,24 L0,0 L6,0 L6,4 L9,4 L9,0 L16,0 L16,4 L19,4 L19,0 L25,0 L25,24 z",
											"stroke:black; fill:lightgray; stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);
		
		//Castle
		symbol = this.createSymbol("castle", "36", "24", "-18", "-14");
		path = this.createPath("M0,24 L0,0 L7,0 L7,4 L9,4 L9,0 L16,0 L16,4 L19,4 L19,0 L26,0 L26,4 L28,4 L28,0 L35,0 L35,24 z",
											"stroke:black; fill:lightgray; stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);		
		
		//Keep
		symbol = this.createSymbol("keep", "47", "24", "-23", "-14");
		path = this.createPath("M0,24 v -24 h 7 v 4 h 2.5 v -4 h 7 v 4 h 3 v-4 h 7 v 4 h 3 v -4 h 7 v 4 h 2.5 v -4 h 7 v 24 z",
											"stroke:black; fill:lightgray; stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);	
		
		//Citadel
		symbol = this.createSymbol("citadel", "56", "24", "-28", "-14");
		path = this.createPath("M0,24 v -24 h 6 v 4 h 3 v -4 h 7 v 4 h 3 v -4 h 7 v 4 h 3 v -4 h 7 v 4 h 3 v -4 h 7 v 4 h 3 v -4 h 6 v 24 z",
											"stroke:black; fill:lightgray; stroke-width:1");	
		symbol.appendChild(path);
		this.defs.appendChild(symbol);	
		
		//Hatched Pattern
		SVGElement pattern = this.createPattern("diagonalHatch", "5", "5", "rotate(45 0 0)", "userSpaceOnUse");
		SVGElement line = this.createLine("0", "0", "0", "5", "stroke:black; stroke-width:1");
		
		pattern.appendChild(line);
		this.defs.appendChild(pattern);	
		
		//Hatched Pattern for fog
		pattern = this.createPattern("diagonalHatchFog", "15", "15", "rotate(45 0 0)", "userSpaceOnUse");
		line = this.createLine("0", "0", "0", "15", "stroke:#444444; stroke-width:2");
		
		pattern.appendChild(line);
		this.defs.appendChild(pattern);			
	}

	public void setupStyleSheet() {
		//Set up the main stylesheet
		this.styleSheet = this.createStyleSheet("baseStyles");
		this.getRoot().appendChild(this.styleSheet);
		
		this.addStyle("line.road { stroke: #9a9a9a; stroke-width: 4; }");	
		this.addStyle("line.majorRiver { stroke: #1067ac; stroke-width: 4; }");
		this.addStyle("line.minorRiver { stroke: #008adf; stroke-width: 3; }");
		this.addStyle("line.ford { stroke: #0f2f2f; stroke-width: 6; }");
		this.addStyle("line.bridge { stroke: #885500; stroke-width: 6; }");
		this.addStyle(".hexnumber { font: bold 12px sans-serif; fill: black; text-anchor: middle; }");
		this.addStyle(".fogOfWar { fill: url(#diagonalHatchFog); }");
	
	}

}
