package org.joverseer.support.readers.newXml;

import java.util.ArrayList;

import org.joverseer.domain.PopulationCenter;

public class PopCenterWrapper {
	int hexNo;
	boolean sieged;
	String terrain;
	String climate;

	ArrayList<ProductionWrapper> products = new ArrayList<ProductionWrapper>();

	ArrayList<String> foreignCharacters = new ArrayList<String>();

	public void addProduct(ProductionWrapper pw) {
		products.add(pw);
	}

	public String getClimate() {
		return climate;
	}

	public void setClimate(String climate) {
		this.climate = climate;
	}

	public int getHexNo() {
		return hexNo;
	}

	public void setHexNo(int hexNo) {
		this.hexNo = hexNo;
	}

	public boolean isSieged() {
		return sieged;
	}

	public void setSieged(boolean sieged) {
		this.sieged = sieged;
	}

	public String getTerrain() {
		return terrain;
	}

	public void setTerrain(String terrain) {
		this.terrain = terrain;
	}

	public ArrayList<ProductionWrapper> getProducts() {
		return products;
	}

	public void updatePopCenter(PopulationCenter pc) {
		for (ProductionWrapper pw : products) {
			pw.updatePopCenter(pc);
		}
	}

	public void addForeignCharacter(String name) {
		if (name.endsWith(" -")) {
			name = name.substring(0, name.length() - 2);
		}
		foreignCharacters.add(name);
	}

	public ArrayList<String> getForeignCharacters() {
		return foreignCharacters;
	}

}
