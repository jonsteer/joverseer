package org.joverseer.ui.map.renderers;

import org.joverseer.ui.domain.mapOptions.MapOptionValuesEnum;
import org.joverseer.ui.domain.mapOptions.MapOptionsEnum;
import org.joverseer.ui.map.MapMetadata;
import org.joverseer.ui.map.MapPanel;
import org.joverseer.ui.orders.OrderVisualizationData;
import org.joverseer.ui.support.Arrow;
import org.joverseer.ui.support.GraphicUtils;
import org.joverseer.domain.Army;
import org.joverseer.domain.Order;
import org.joverseer.domain.ProductEnum;
import org.joverseer.game.Game;
import org.joverseer.game.TurnElementsEnum;
import org.joverseer.support.GameHolder;
import org.joverseer.support.movement.MovementUtils;
import org.joverseer.support.movement.MovementDirection;
import org.springframework.richclient.application.Application;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;


public class OrderRenderer implements Renderer {
    MapMetadata mapMetadata = null;
    OrderVisualizationData orderVisualizationData = null;

    private OrderVisualizationData getOrderVisualizationData() {
        if (orderVisualizationData == null) {
            orderVisualizationData = (OrderVisualizationData)Application.instance().getApplicationContext().getBean("orderVisualizationData");
        }
        return orderVisualizationData;
    }
    
    private boolean drawOrders() {
        Game game = ((GameHolder)Application.instance().getApplicationContext().getBean("gameHolder")).getGame();
        HashMap mapOptions = (HashMap)Application.instance().getApplicationContext().getBean("mapOptions");
        Object obj = mapOptions.get(MapOptionsEnum.DrawOrders);
        if (obj == null) return false;
        if (obj == MapOptionValuesEnum.DrawOrdersOn) {
            return true;
        }
        return false;
    }

    public boolean appliesTo(Object obj) {
        return Order.class.isInstance(obj) && !((Order)obj).isBlank() && drawOrders() && getOrderVisualizationData().contains((Order)obj);
    }

    private void init() {
        mapMetadata = (MapMetadata) Application.instance().getApplicationContext().getBean("mapMetadata");
    }

    public void render(Object obj, Graphics2D g, int x, int y) {
        if (mapMetadata == null) init();

        // todo render order
        Order order = (Order)obj;
        if (order.getOrderNo() == 810 ||
                order.getOrderNo() == 870 ||
                order.getOrderNo() == 820) {
            renderCharacterMovementOrder(order, g);
        } else if (order.getOrderNo() == 850 || order.getOrderNo() == 860) {
            renderArmyMovementOrder(order, g);
        } else if (order.getOrderNo() == 947) {
            renderNatTranOrder(order, g);
        } else if (order.getOrderNo() == 948) {
            renderTranCarOrder(order, g);
        }
    }

    private void renderArmyMovementOrder(Order order, Graphics2D g) {
        if (order.getParameter(0) == null) {
            return;
        }
        try {
            int currentHexNo = Integer.parseInt(order.getCharacter().getHexNo());
            String[] params = order.getParameters().split(" ");
            Point p1;
            Point p2 = null;
            int cost = 0;
            
            Game game = ((GameHolder)Application.instance().getApplicationContext().getBean("gameHolder")).getGame();
            
            Army army = (Army)game.getTurn().getContainer(TurnElementsEnum.Army).findFirstByProperty("commanderName", order.getCharacter().getName());
            Boolean cav = army == null || army.computeCavalry();
            Boolean fed = army == null || army.computeFed();
            if (cav == null) cav = false;
            if (fed == null) fed = false;
            
            for (int i=1; i<params.length; i++) {
                String dir = params[i];
                MovementDirection md = MovementDirection.getDirectionFromString(dir);
                int nextHexNo = MovementUtils.getHexNoAtDir(currentHexNo, md);
                p1 = MapPanel.instance().getHexCenter(currentHexNo);
                p2 = MapPanel.instance().getHexCenter(nextHexNo);
                g.setStroke(GraphicUtils.getDashStroke(3, 6));
                g.setColor(Color.black);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);

                if (i == params.length - 2) {
                    // last segment
                    double theta = Math.atan2((p2.y - p1.y) , (p2.x - p1.x));
                    Shape a = Arrow.getArrowHead(p2.x, p2.y, 10, 15, theta);
                    g.fill(a);
                }
                if (i > 0) {
                    // draw distance so far
                    drawString(g, String.valueOf(cost), p1, p1);
                }

                int curCost = MovementUtils.calculateMovementCostForArmy(currentHexNo, dir, cav, fed);
                if (curCost == -1) {
                    cost = -1;
                } else {
                    cost += curCost;
                }

                if (i == (params.length - 1) / 2) {
                    // middle segment
                    drawString(g, order.getCharacter().getName(), p1, p2);
                }
                
                currentHexNo = nextHexNo;
            }
            // draw last distance
            drawString(g, String.valueOf(cost), p2, p2);
        }
        catch (Exception exc) {
            // parse or some other error, return
            return;
        }

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
        int xt = p.x - new Double(b.getWidth() / 2).intValue();
        int yt = p.y;
        b.translate(xt, yt);
        RoundRectangle2D rr = new RoundRectangle2D.Double(b.getX(), b.getY(), b.getWidth() + 2, b.getHeight() + 2, 3, 3);
        g.setFont(f);

        g.setColor(Color.BLACK);
        // fill rectangle behind char name
        g.fill(rr);
        // draw char name
        g.setColor(Color.WHITE);
        g.drawString(str, xt + 1, yt + 1);

    }

    private void renderCharacterMovementOrder(Order order, Graphics2D g) {
        if (order.getParameter(0) == null) {
            return;
        }
        try {
            String hexNoStr = order.getParameter(0);
            int hexNo = Integer.parseInt(hexNoStr);
            Point p1 = MapPanel.instance().getHexCenter(hexNo);
            Point p2 = MapPanel.instance().getHexCenter(Integer.parseInt(order.getCharacter().getHexNo()));

            // draw arrowhead
            double theta = Math.atan2((p1.y - p2.y) , (p1.x - p2.x));
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.BLACK);
            Shape arrowHead = Arrow.getArrowHead(p1.x, p1.y, 10, 15, theta);
            g.fill(arrowHead);

            Stroke s = GraphicUtils.getDashStroke(3, 6);
            g.setStroke(s);
            g.setColor(Color.BLACK);
            // draw line
            g.drawLine(p1.x, p1.y, p2.x, p2.y);

            drawString(g, order.getCharacter().getName(), p1, p2);
        }
        catch (Exception exc) {
            // parse or some other error, return
            return;
        }
    }
    
    private void renderNatTranOrder(Order order, Graphics2D g) {
        if (order.getParameter(0) == null) {
            return;
        }
        try {
            String hexNoStr = order.getParameter(0);
            int hexNo = Integer.parseInt(hexNoStr);
            Point p1 = MapPanel.instance().getHexCenter(hexNo);
            int hexNo2 = MovementUtils.getHexNoAtDir(hexNo, MovementDirection.Northwest);
            Point p2 = MapPanel.instance().getHexCenter(hexNo2);
            
            ProductEnum product = ProductEnum.getFromCode(order.getParameter(1));
            String pctStr = order.getParameter(2);
            int pct = Integer.parseInt(pctStr);
            
//          draw arrowhead
            double theta = Math.atan2((p1.y - p2.y) , (p1.x - p2.x));
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.BLACK);
            Shape arrowHead = Arrow.getArrowHead(p1.x, p1.y, 10, 15, theta);
            g.fill(arrowHead);

            Stroke s = GraphicUtils.getDashStroke(3, 6);
            g.setStroke(s);
            g.setColor(Color.BLACK);
            // draw line
            g.drawLine(p1.x, p1.y, p2.x, p2.y);

            String descr = product.getCode() + " " + pctStr + "%";
            drawString(g, descr, p2, p2);
            
        }
        catch (Exception exc) {
            // parse or some other error, return
            return;
        }
    }
    
    private void renderTranCarOrder(Order order, Graphics2D g) {
        if (order.getParameter(0) == null) {
            return;
        }
        try {
            String hexNoStr = order.getParameter(0);
            int hexNo = Integer.parseInt(hexNoStr);
            Point p2 = MapPanel.instance().getHexCenter(hexNo);
            String hexNoStr2 = order.getParameter(1);
            int hexNo2 = Integer.parseInt(hexNoStr2);
            Point p1 = MapPanel.instance().getHexCenter(hexNo2);
            
            ProductEnum product = ProductEnum.getFromCode(order.getParameter(2));
            String unitsStr = order.getParameter(3);
            
//          draw arrowhead
            double theta = Math.atan2((p1.y - p2.y) , (p1.x - p2.x));
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.BLACK);
            Shape arrowHead = Arrow.getArrowHead(p1.x, p1.y, 10, 15, theta);
            g.fill(arrowHead);

            Stroke s = GraphicUtils.getDashStroke(3, 6);
            g.setStroke(s);
            g.setColor(Color.BLACK);
            // draw line
            g.drawLine(p1.x, p1.y, p2.x, p2.y);

            String descr = product.getCode() + " " + unitsStr;
            drawString(g, descr, p1, p2);
            
        }
        catch (Exception exc) {
            // parse or some other error, return
            return;
        }
    }

}
