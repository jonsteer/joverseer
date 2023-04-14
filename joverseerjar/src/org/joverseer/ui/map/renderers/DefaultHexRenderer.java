package org.joverseer.ui.map.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.joverseer.metadata.domain.Hex;
import org.joverseer.metadata.domain.HexSideElementEnum;
import org.joverseer.metadata.domain.HexSideEnum;
import org.joverseer.metadata.domain.HexTerrainEnum;
import org.joverseer.preferences.PreferenceRegistry;
import org.joverseer.ui.LifecycleEventsEnum;
import org.joverseer.ui.domain.mapOptions.MapOptionsEnum;
import org.joverseer.ui.support.JOverseerEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.richclient.application.Application;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Renders a hex (terrain, sides, etc)
 * 
 * @author Marios Skounakis
 */
public class DefaultHexRenderer extends SVGRenderer implements ApplicationListener {
	HashMap<Integer, Color> terrainColors = new HashMap<Integer, Color>();

	protected int[] xPoints = new int[6];
	protected int[] yPoints = new int[6];

	Point hexCenter;

	protected Polygon polygon;

	Color majorRiverColor;
	Color minorRiverColor;
	Color roadColor;
	Color bridgeColor;
	Color fordColor;
	HashMap<MapOptionsEnum, Object> mapOptions;
	
	private boolean useTexture;
	
	public DefaultHexRenderer() {
	}

	@Override
	public void refreshConfig() {

		this.xPoints[0] = this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellWidth();
		this.xPoints[1] = this.mapMetadata.getHexSize() * this.mapMetadata.getGridCellWidth();
		this.xPoints[2] = this.mapMetadata.getHexSize() * this.mapMetadata.getGridCellWidth();
		this.xPoints[3] = this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellWidth();
		this.xPoints[4] = 0;
		this.xPoints[5] = 0;

		this.yPoints[0] = 0;
		this.yPoints[1] = this.mapMetadata.getHexSize() / 4 * this.mapMetadata.getGridCellHeight();
		this.yPoints[2] = this.mapMetadata.getHexSize() * 3 / 4 * this.mapMetadata.getGridCellHeight();
		this.yPoints[3] = this.mapMetadata.getHexSize() * this.mapMetadata.getGridCellHeight();
		this.yPoints[4] = this.mapMetadata.getHexSize() * 3 / 4 * this.mapMetadata.getGridCellHeight();
		this.yPoints[5] = this.mapMetadata.getHexSize() / 4 * this.mapMetadata.getGridCellHeight();

		this.hexCenter = new Point(this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellWidth(), this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellHeight());

		MessageSource colorSource = (MessageSource) Application.instance().getApplicationContext().getBean("colorSource");

		String colorStr = "#DDDDDD";
		for (HexTerrainEnum t : HexTerrainEnum.values()) {
			colorStr = colorSource.getMessage(t.toString() + ".color", null, Locale.getDefault());
			setTerrainColor(t, Color.decode(colorStr));
		}

		colorStr = colorSource.getMessage("majorRiver.color", null, Locale.getDefault());
		setMajorRiverColor(Color.decode(colorStr));
		colorStr = colorSource.getMessage("minorRiver.color", null, Locale.getDefault());
		setMinorRiverColor(Color.decode(colorStr));
		colorStr = colorSource.getMessage("road.color", null, Locale.getDefault());
		setRoadColor(Color.decode(colorStr));
		colorStr = colorSource.getMessage("bridge.color", null, Locale.getDefault());
		setBridgeColor(Color.decode(colorStr));
		colorStr = colorSource.getMessage("ford.color", null, Locale.getDefault());
		setFordColor(Color.decode(colorStr));

		this.mapOptions = (HashMap<MapOptionsEnum, Object>) Application.instance().getApplicationContext().getBean("mapOptions");

		String pval = PreferenceRegistry.instance().getPreferenceValue("map.terrainGraphics");
		this.useTexture = pval.equals("texture");
	}

	protected Polygon getSidePolygon(HexSideEnum side) {
		int i = side.getSide();
		Polygon p = new Polygon(new int[] { this.xPoints[i - 1 % 6], this.xPoints[i % 6] }, new int[] { this.yPoints[i - 1 % 6], this.yPoints[i % 6] }, 2);
		return p;
	}

	protected Polygon getSidePolygonInner(HexSideEnum side) {
		int i = side.getSide();
		int x1 = 0;
		int x2 = 0;
		int y1 = 0;
		int y2 = 0;
		if (side == HexSideEnum.BottomLeft) {
			x1 = 0;
			x2 = 2;
			y1 = -2;
			y2 = -2;
		} else if (side == HexSideEnum.Left) {
			x1 = 2;
			x2 = 2;
			y1 = -2;
			y2 = 2;
		} else if (side == HexSideEnum.TopLeft) {
			x1 = 2;
			x2 = 0;
			y1 = 2;
			y2 = 2;
		} else if (side == HexSideEnum.TopRight) {
			x1 = 0;
			x2 = -2;
			y1 = 2;
			y2 = 2;
		} else if (side == HexSideEnum.Right) {
			x1 = -2;
			x2 = -2;
			y1 = 0;
			y2 = 0;
		} else if (side == HexSideEnum.BottomRight) {
			x1 = -2;
			x2 = 0;
			y1 = -2;
			y2 = -2;
		}
		Polygon p = new Polygon(new int[] { this.xPoints[i - 1 % 6] + x1, this.xPoints[i % 6] + x2 }, new int[] { this.yPoints[i - 1 % 6] + y1, this.yPoints[i % 6] + y2 }, 2);
		return p;
	}

	protected Point getSideCenter(HexSideEnum side) {
		int i = side.getSide();
		return new Point((this.xPoints[i % 6] + this.xPoints[i - 1]) / 2, (this.yPoints[i % 6] + this.yPoints[i - 1]) / 2);
	}

	@Override
	public boolean appliesTo(Object obj) {
		return Hex.class.isInstance(obj);
	}

	protected Color getColor(Hex hex) {
		if (this.terrainColors.containsKey(hex.getTerrain().getTerrain())) {
			return this.terrainColors.get(hex.getTerrain().getTerrain());
		}
		return Color.white;
	}

	public void renderRoad(SVGDocument s, Element e, HexSideEnum side, int x, int y) {
		Point sideCenter = getSideCenter(side);
		Point center = new Point(this.hexCenter);
		
		Element r = s.createElementNS(this.svgNS, "line");
		
		r.setAttributeNS(null, "x1", "" + (center.x + x));
		r.setAttributeNS(null, "y1", "" + (center.y + y));
		r.setAttributeNS(null, "x2", "" + (sideCenter.x + x));
		r.setAttributeNS(null, "y2", "" + (sideCenter.y + y));
		
		r.setAttributeNS(null, "stroke", "#"+Integer.toHexString(getRoadColor().getRGB()).substring(2));
		r.setAttributeNS(null, "class", "road");
		
		//TODO move to style sheets but inline style for now
		r.setAttributeNS(null, "style", "stroke-width: 4;");
		
		e.appendChild(r);		

	}

	public void renderMajorRiver(SVGDocument s, Element e, HexSideEnum side, int x, int y) {

		int i = side.getSide();
		
		Element r = s.createElementNS(this.svgNS, "line");
		r.setAttributeNS(null, "x1", "" + (x + this.xPoints[i - 1 % 6]));
		r.setAttributeNS(null, "y1", "" + (y + this.yPoints[i - 1 % 6]));
		r.setAttributeNS(null, "x2", "" + (x + this.xPoints[i % 6]));
		r.setAttributeNS(null, "y2", "" + (y + this.yPoints[i % 6]));
		
		r.setAttributeNS(null, "stroke", "#"+Integer.toHexString(getMajorRiverColor().getRGB()).substring(2));
		r.setAttributeNS(null, "class", "majorRiver");
		
		//TODO move to style sheets but inline style for now
		r.setAttributeNS(null, "style", "stroke-width: 4;");
		
		e.appendChild(r);
		
	}

	public void renderMinorRiver(SVGDocument s, Element e, HexSideEnum side, int x, int y) {
		
		int i = side.getSide();
		
		Element r = s.createElementNS(this.svgNS, "line");
		r.setAttributeNS(null, "x1", "" + (x + this.xPoints[i - 1 % 6]));
		r.setAttributeNS(null, "y1", "" + (y + this.yPoints[i - 1 % 6]));
		r.setAttributeNS(null, "x2", "" + (x + this.xPoints[i % 6]));
		r.setAttributeNS(null, "y2", "" + (y + this.yPoints[i % 6]));
		
		r.setAttributeNS(null, "stroke", "#"+Integer.toHexString(getMinorRiverColor().getRGB()).substring(2));
		r.setAttributeNS(null, "class", "majorRiver");
		
		//TODO move to style sheets but inline style for now
		r.setAttributeNS(null, "style", "stroke-width: 3;");	
		
		e.appendChild(r);	

	}

	public void renderBridge(SVGDocument s, Element e, HexSideEnum side, int x, int y) {
//		Stroke s = g.getStroke();
//		Stroke r = new BasicStroke(6);
		Point sideCenter = getSideCenter(side);
		Point center = new Point(this.hexCenter);
		Point start = new Point((center.x + 2 * sideCenter.x) / 3, (center.y + 2 * sideCenter.y) / 3);
//		start.translate(x, y);
//		sideCenter.translate(x, y);
//		g.setColor(getBridgeColor());
//		g.setStroke(r);
//		g.drawLine(start.x, start.y, sideCenter.x, sideCenter.y);
//		g.setStroke(s);
		
		Element r = s.createElementNS(this.svgNS, "line");
		
		r.setAttributeNS(null, "x1", "" + (start.x + x));
		r.setAttributeNS(null, "y1", "" + (start.y + y));
		r.setAttributeNS(null, "x2", "" + (sideCenter.x + x));
		r.setAttributeNS(null, "y2", "" + (sideCenter.y + y));
		
		r.setAttributeNS(null, "stroke", "#"+Integer.toHexString(getBridgeColor().getRGB()).substring(2));
		r.setAttributeNS(null, "class", "bridge");
		
		//TODO move to style sheets but inline style for now
		r.setAttributeNS(null, "style", "stroke-width: 6;");
		
		e.appendChild(r);	
	}

	public void renderFord(SVGDocument s, Element e, HexSideEnum side, int x, int y) {
//		Stroke s = g.getStroke();
//		Stroke r = new BasicStroke(6);
		Point sideCenter = getSideCenter(side);
		Point center = new Point(this.hexCenter);
		Point start = new Point((center.x + 2 * sideCenter.x) / 3, (center.y + 2 * sideCenter.y) / 3);
//		start.translate(x, y);
//		sideCenter.translate(x, y);
//		g.setColor(getFordColor());
//		g.setStroke(r);
//		g.drawLine(start.x, start.y, sideCenter.x, sideCenter.y);
//		g.setStroke(s);
		
		Element r = s.createElementNS(this.svgNS, "line");
		
		r.setAttributeNS(null, "x1", "" + (start.x + x));
		r.setAttributeNS(null, "y1", "" + (start.y + y));
		r.setAttributeNS(null, "x2", "" + (sideCenter.x + x));
		r.setAttributeNS(null, "y2", "" + (sideCenter.y + y));
		
		r.setAttributeNS(null, "stroke", "#"+Integer.toHexString(getFordColor().getRGB()).substring(2));
		r.setAttributeNS(null, "class", "ford");
		
		//TODO move to style sheets but inline style for now
		r.setAttributeNS(null, "style", "stroke-width: 6;");
		
		e.appendChild(r);			
	}



	@Override
	public void render(Object obj, SVGDocument s, int x, int y) {
		if (!appliesTo(obj)) {
			throw new IllegalArgumentException(obj.toString());
		}

		Hex hex = (Hex) obj;
		if (!this.mapMetadata.withinMapRange(hex))
			return;

		boolean imageDrawn = false;

        // required to stop aliased background bleed through hex sides
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// options listed here for ease of tweaking for developers.
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//      g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//      g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//      g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Element hexGroup = s.createElementNS(this.svgNS, "g");
		hexGroup.setAttributeNS(null,"ID", "hex_group_"+hex.getHexNoStr());
		hexGroup.setIdAttributeNS(null, "ID", true);
		
		
		Element svghex = s.createElementNS(this.svgNS, "polygon");
		svghex.setAttributeNS(null,"ID", "hex_"+hex.getHexNoStr());
		svghex.setIdAttributeNS(null, "ID", true);
		svghex.setAttributeNS(null, "points", (this.xPoints[0] + x) + "," + (this.yPoints[0] + y) + " " + (this.xPoints[1] + x) + "," + (this.yPoints[1] + y) + " " + (this.xPoints[2] + x) + "," + (this.yPoints[2] + y) + " " + (this.xPoints[3] + x) + "," + (this.yPoints[3] + y) + " " + (this.xPoints[4] + x) + "," + (this.yPoints[4] + y) + " " + (this.xPoints[5] + x) + "," + (this.yPoints[5] + y));
		
		if (this.useTexture) {

		}
		
		svghex.setAttributeNS(null, "fill", "#"+Integer.toHexString(getColor(hex).getRGB()).substring(2));
		svghex.setAttributeNS(null, "stroke", "#000000");

		//Element svgRoot = s.getDocumentElement();
		Element baseMap = s.getElementById("baseMap");
		hexGroup.appendChild(svghex);
		
		
		for (HexSideEnum side : HexSideEnum.values()) {
			Collection<HexSideElementEnum> elements = hex.getHexSideElements(side);
			if (elements.size() > 0) {
				if (elements.contains(HexSideElementEnum.MajorRiver)) {
					renderMajorRiver(s, hexGroup, side, x, y);
				} else if (elements.contains(HexSideElementEnum.MinorRiver)) {
					renderMinorRiver(s, hexGroup, side, x, y);
				}
				;
				if (elements.contains(HexSideElementEnum.Road)) {
					renderRoad(s, hexGroup, side, x, y);
				}
				;
				if (elements.contains(HexSideElementEnum.Bridge)) {
					renderBridge(s, hexGroup, side, x, y);
				}
				;
				if (elements.contains(HexSideElementEnum.Ford)) {
					renderFord(s, hexGroup, side, x, y);
				}
				;

			}
		}
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		baseMap.appendChild(hexGroup);
	}

	public void setTerrainColor(HexTerrainEnum terrain, Color c) {
		setTerrainColor(terrain.getTerrain(), c);
	}

	public void setTerrainColor(int i, Color c) {
		if (this.terrainColors.containsKey(i)) {
			this.terrainColors.remove(i);
		}
		this.terrainColors.put(i, c);
	}

	public Color getBridgeColor() {
		return this.bridgeColor;
	}

	public void setBridgeColor(Color bridgeColor) {
		this.bridgeColor = bridgeColor;
	}

	public Color getFordColor() {
		return this.fordColor;
	}

	public void setFordColor(Color fordColor) {
		this.fordColor = fordColor;
	}

	public Color getMajorRiverColor() {
		return this.majorRiverColor;
	}

	public void setMajorRiverColor(Color majorRiverColor) {
		this.majorRiverColor = majorRiverColor;
	}

	public Color getMinorRiverColor() {
		return this.minorRiverColor;
	}

	public void setMinorRiverColor(Color minorRiverColor) {
		this.minorRiverColor = minorRiverColor;
	}

	public Color getRoadColor() {
		return this.roadColor;
	}

	public void setRoadColor(Color roadColor) {
		this.roadColor = roadColor;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof JOverseerEvent) {
			JOverseerEvent e = (JOverseerEvent) applicationEvent;
			if (e.isLifecycleEvent(LifecycleEventsEnum.MapMetadataChangedEvent)) {
				refreshConfig();
			}
		}
	}
}
