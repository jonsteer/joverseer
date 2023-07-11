package org.joverseer.ui.map;

import java.util.ArrayList;
import java.util.Collection;

import org.joverseer.metadata.domain.Hex;
import org.joverseer.ui.map.renderers.Renderer;
import org.springframework.richclient.application.Application;

/**
 * Metadata for the map (dimensions, grid sizes, etc)
 *
 * @author Marios Skounakis
 */
public class MapMetadata {
	int gridCellWidth;
	int gridCellHeight;
	int hexSize = 8;
	int maxMapColumn;
	int maxMapRow;
	int minMapColumn = 1;
	int minMapRow = 1;

	ArrayList<Renderer> renderers = new ArrayList<Renderer>();

	public int getGridCellHeight() {
		//return this.gridCellHeight;
		return 12;
	}

	public void setGridCellHeight(int gridCellHeight) {
		this.gridCellHeight = gridCellHeight;
	}

	public int getGridCellWidth() {
		//return this.gridCellWidth;
		return 12;
	}

	public void setGridCellWidth(int gridCellWidth) {
		this.gridCellWidth = gridCellWidth;
	}

	public int getHexSize() {
		return this.hexSize;
	}

	public void setHexSize(int hexSize) {
		this.hexSize = hexSize;
	}

	public int getMaxMapColumn() {
		return this.maxMapColumn;
	}

	public void setMaxMapColumn(int mapColumns) {
		this.maxMapColumn = mapColumns;
	}

	public int getMaxMapRow() {
		return this.maxMapRow;
	}

	public void setMaxMapRow(int mapRows) {
		this.maxMapRow = mapRows;
	}

	public int getMinMapColumn() {
		return this.minMapColumn;
	}

	public void setMinMapColumn(int minMapColumn) {
		this.minMapColumn = minMapColumn;
	}

	public int getMinMapRow() {
		return this.minMapRow;
	}

	public void setMinMapRow(int minMapRow) {
		this.minMapRow = minMapRow;
	}

	public void setRenderers(Collection<Renderer> renderers) {
		this.renderers.addAll(renderers);
	}

	public Collection<Renderer> getRenderers() {
		return this.renderers;
	}
	public static MapMetadata instance()
	{
		return (MapMetadata) Application.instance().getApplicationContext().getBean("mapMetadata");
	}
	public boolean withinMapRange(Hex hex) {
		return hex.isColumnWithin(getMinMapColumn(),getMaxMapColumn())
			&& hex.isRowWithin(getMinMapRow(),getMaxMapRow());
	}

}
