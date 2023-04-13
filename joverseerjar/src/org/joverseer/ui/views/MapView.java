package org.joverseer.ui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.batik.swing.*;
import org.apache.batik.svggen.*;

import org.joverseer.JOApplication;
import org.joverseer.support.GameHolder;
import org.joverseer.ui.map.MapMetadata;
import org.joverseer.ui.map.MapPanel;
import org.joverseer.ui.map.renderers.ImageRenderer;
import org.joverseer.ui.support.JOverseerEvent;
import org.joverseer.ui.support.Messages;
import org.joverseer.ui.viewers.PopulationCenterViewer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.image.DefaultImageSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.layout.TableLayoutBuilder;

public class MapView extends AbstractView implements ApplicationListener {

	MapPanel mapPanel;
	JSVGScrollPane scp;
	JLabel introLabel;

	//injected dependencies
	GameHolder gameHolder;

	public GameHolder getGameHolder() {
		return this.gameHolder;
	}

	public void setGameHolder(GameHolder gameHolder) {
		this.gameHolder = gameHolder;
	}

	/**
	 * Create the actual UI control for this view. It will be placed into the
	 * window according to the layout of the page holding this view.
	 */
	@Override
	protected JComponent createControl() {
		// In this view, we're just going to use standard Swing to place a
		// few controls.
		this.scp = new JSVGScrollPane(this.mapPanel = new MapPanel(this.gameHolder));
		this.mapPanel.setFocusable(true);
		this.mapPanel.setPreferredSize(new Dimension(1000, 2500));
		this.mapPanel.setBackground(Color.white);
		this.scp.setPreferredSize(new Dimension(800, 500));
		MapMetadata mm = MapMetadata.instance();
		
		// Add introduction image to explain to new players what to do
//		ImageSource is = JOApplication.getImageSource();
//		Image mapIntro = is.getImage("map.intro");
//		if (mapIntro != null) {
//			this.introLabel = new JLabel(new ImageIcon(mapIntro));
//			this.mapPanel.add(this.introLabel);
//		}
//		
//		
//		TableLayoutBuilder tlb = new TableLayoutBuilder();
//		tlb.cell(new JLabel(Messages.getString("MapView.NewGame"))); //$NON-NLS-1$
		
		
		return this.scp;
	}

	public MapPanel getMapPanel() {
		return this.mapPanel;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof JOverseerEvent) {
			this.onJOEvent((JOverseerEvent) applicationEvent);
		}
	}
	public void onJOEvent(JOverseerEvent e) {
		switch(e.getType()) {
		case GameChangedEvent:
		case SelectedTurnChangedEvent:
		case RefreshTurnMapItems:
			this.mapPanel.remove(this.introLabel);
			this.mapPanel.invalidateAll();
			this.mapPanel.updateUI();
			break;
		case SelectedHexChangedEvent:
			if (e.getSender() != this.mapPanel) {
				Point p = (Point) e.getObject();
				this.mapPanel.setSelectedHex(p);
				Rectangle shr = this.mapPanel.getSelectedHexRectangle();
				this.mapPanel.updateUI();
				// expand shr
				Rectangle vr = this.mapPanel.getVisibleRect();

				vr.x = shr.x - (vr.width - shr.width) / 2;
				vr.y = shr.y - (vr.height - shr.height) / 2;
				this.mapPanel.scrollRectToVisible(vr);
			}
			break;
		case RefreshMapItems:
			// refreshAutoArmyRangeMapItems(null);
			
			this.mapPanel.invalidateMapItems();
			this.mapPanel.updateUI();
			break;
		case OrderChangedEvent:
			this.mapPanel.invalidateMapItems();
			this.mapPanel.updateUI();
			break;
		case MapMetadataChangedEvent:
			MapMetadata mm = MapMetadata.instance();
			this.mapPanel.setPreferredSize(new Dimension(mm.getGridCellWidth() * mm.getHexSize() * (mm.getMaxMapColumn() + 1), mm.getGridCellHeight() * mm.getHexSize() * mm.getMaxMapRow()));
			this.mapPanel.invalidateAndReset();
			this.mapPanel.updateUI();
			this.scp.updateUI();
			break;
		}
	}

}
