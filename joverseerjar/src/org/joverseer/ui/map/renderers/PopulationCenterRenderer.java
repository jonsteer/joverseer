package org.joverseer.ui.map.renderers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.joverseer.JOApplication;
import org.joverseer.domain.FortificationSizeEnum;
import org.joverseer.domain.HarborSizeEnum;
import org.joverseer.domain.PopulationCenter;
import org.joverseer.domain.PopulationCenterSizeEnum;
import org.joverseer.preferences.PreferenceRegistry;
import org.joverseer.ui.domain.mapOptions.MapOptionValuesEnum;
import org.joverseer.ui.domain.mapOptions.MapOptionsEnum;
import org.joverseer.ui.map.JOSVGMap;
import org.joverseer.ui.map.MapTooltipHolder;
import org.joverseer.ui.support.GraphicUtils;
import org.joverseer.ui.support.drawing.ColorPicker;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

/**
 * Renders PopulationCenter objects
 * 
 * @author Marios Skounakis
 */	
public class PopulationCenterRenderer extends SVGRenderer {
    
    @SuppressWarnings("hiding")
	static Logger logger = Logger.getLogger(PopulationCenterRenderer.class);

    @Override
	public boolean appliesTo(Object obj) {
        return PopulationCenter.class.isInstance(obj);
    }

    @Override
	public void render(Object obj, JOSVGMap s, int x, int y) {

        PopulationCenter popCenter = (PopulationCenter)obj;


        Point hexCenter = new Point(x + this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellWidth(),
                                    y + this.mapMetadata.getHexSize() / 2 * this.mapMetadata.getGridCellHeight());

        String fortlevel = null;
        if (popCenter.getFortification() != FortificationSizeEnum.none) {
            fortlevel = popCenter.getFortification().toString();

            Element fortElement = s.mapdoc.createElementNS(this.svgNS, "use");
            fortElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#"+fortlevel);
            fortElement.setAttributeNS(null, "x", ""+hexCenter.x);
            fortElement.setAttributeNS(null, "y", ""+hexCenter.y);
            fortElement.setAttributeNS(null, "id", "fort_"+popCenter.getHexNo());
            
            Element popCenters = s.mapdoc.getElementById("popCenters");
            popCenters.appendChild(fortElement);   
            
        }

     
        
        
        // docks
//        if (popCenter.getHarbor() != HarborSizeEnum.none) {
//            BufferedImage dockImage = getImage(popCenter.getHarbor().toString() + ".icon");
//            g.drawImage(dockImage, x + 5, hexCenter.y, null); 
//        }

//        BufferedImage pcImage = null;
        
        String capital = popCenter.getCapital() ? ".capital" : "";
        if (popCenter.getSize().getCode() < PopulationCenterSizeEnum.majorTown.getCode()) {
        	capital = "";
        }
//        pcImage = getImage(popCenter.getSize().toString() + capital + ".image");

//        BufferedImage img = copyImage(pcImage);
        
        String pcCode = popCenter.getSize().toString();
        //pcCode = "camp";
        
        Element pcElement = s.mapdoc.createElementNS(this.svgNS, "use");
        pcElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#"+pcCode);
        pcElement.setAttributeNS(null, "x", ""+hexCenter.x);
        pcElement.setAttributeNS(null, "y", ""+hexCenter.y);
        pcElement.setAttributeNS(null, "id", "pop_"+popCenter.getHexNo());
        
        Color color1 = ColorPicker.getInstance().getColor1(popCenter.getNationNo());
        Color color2 = ColorPicker.getInstance().getColor2(popCenter.getNationNo());
        
        pcElement.setAttributeNS(null, "style", "stroke: #"+Integer.toHexString(color2.getRGB()).substring(2)+"; fill: #"+Integer.toHexString(color1.getRGB()).substring(2)+";");
        Element popCenters = s.mapdoc.getElementById("popCenters");
        popCenters.appendChild(pcElement);
        
        boolean standardHiddenPopcenter = true;
    	String pval = PreferenceRegistry.instance().getPreferenceValue("map.hiddenPopsDrawStyle");
    	if (pval != null && pval.equals("h")) {
    		standardHiddenPopcenter = false;
    	}
        if (popCenter.getHidden() && standardHiddenPopcenter) {
 //       	makeHidden(img, color1, color2);
        }
 //       if (fortImage != null) {
 //           g.drawImage(fortImage, hexCenter.x - fortImage.getWidth() / 2, hexCenter.y - fortImage.getHeight(null) + pcImage.getHeight(null) / 2 , null);
 //       }
            
 //      int px = hexCenter.x - pcImage.getWidth(null) / 2;
 //       int py = hexCenter.y - pcImage.getHeight(null) / 2;
 //       g.drawImage(img, px, py, null);
//        if (popCenter.getHidden() && !standardHiddenPopcenter) {
//        	Point p = new Point(hexCenter.x, hexCenter.y + 3);
//            Font f = GraphicUtils.getFont("Microsoft Sans Serif", Font.BOLD, 9);
//            FontMetrics fm = g.getFontMetrics(f);
//            Rectangle2D bb = fm.getStringBounds("H", g);
//            Rectangle b = new Rectangle(((Double)bb.getX()).intValue(),
//                                            ((Double)bb.getY()).intValue(),
//                                            ((Double)bb.getWidth()).intValue(),
//                                            ((Double)bb.getHeight()).intValue());
//            int xt = p.x - Double.valueOf(b.getWidth() / 2).intValue();
//            int yt = p.y;
//            b.translate(xt, yt);
//            RoundRectangle2D rr = new RoundRectangle2D.Double(b.getX(), b.getY(), b.getWidth() + 2, b.getHeight() + 1, 0, 0);
//            g.setFont(f);

//            g.setColor(Color.BLACK);

//          Composite oc = g.getComposite();
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
//            g.fill(rr);
            // draw char name
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
//            g.setColor(Color.WHITE);
//            g.drawString("H", xt + 1, yt + 1);
//            g.setComposite(oc);
//        }

        //        MapTooltipHolder.instance().addTooltipObject(new Rectangle(px, py, img.getWidth(), img.getHeight()), popCenter);
//
//        HashMap mapOptions = JOApplication.getMapOptions();
//        Object map = mapOptions.get(MapOptionsEnum.PopCenterNames);
//        if (map!= null && map.equals(MapOptionValuesEnum.PopCenterNamesOn)) {
//	        String pcName = popCenter.getName();
//	        if (!pcName.toUpperCase().startsWith("UNKNOWN")) {
//	        	Point p = new Point(hexCenter.x, hexCenter.y +4);
//	    		drawString(g, pcName, p, p);
//	        }
//        }
    }
    
    private void drawString(Graphics2D g, String str, Point p1, Point p2) {
        // calculate and prepare character name rendering
        Point p = new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2);
        Font f = GraphicUtils.getFont("Microsoft Sans Serif", Font.PLAIN, 9);
        FontMetrics fm = g.getFontMetrics(f);
        Rectangle2D bb = fm.getStringBounds(str, g);
        Rectangle b = new Rectangle(((Double)bb.getX()).intValue(),
                                        ((Double)bb.getY()).intValue(),
                                        ((Double)bb.getWidth()).intValue(),
                                        ((Double)bb.getHeight()).intValue());
        int xt = p.x - Double.valueOf(b.getWidth() / 2).intValue();
        int yt = p.y;
        b.translate(xt, yt);
        RoundRectangle2D rr = new RoundRectangle2D.Double(b.getX(), b.getY(), b.getWidth() + 2, b.getHeight() + 2, 3, 3);
        g.setFont(f);

        g.setColor(Color.BLACK);
        // fill rectangle behind char name
        Composite oc = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.fill(rr);
        g.setComposite(oc);
        // draw char name
        g.setColor(Color.WHITE);
        g.drawString(str, xt + 1, yt + 1);
        
    }
}
