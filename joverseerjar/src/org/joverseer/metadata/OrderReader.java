package org.joverseer.metadata;

import org.joverseer.metadata.orders.OrderMetadata;
import org.joverseer.support.Container;

/**
 * Order metadata reader. Reads order metadata from data files.
 * 
 * @author Marios Skounakis
 * 
 */
public class OrderReader extends BaseMetadataReader implements MetadataReader {
	Container<OrderMetadata> orders;

	@Override
	protected void initFilename() {
		super.filename = "orders.csv";
	}

	@Override
	protected void start() {
		this.orders = new Container<OrderMetadata>();
	}

	@Override
	protected void finish(GameMetadata gm) {
		gm.setOrders(this.orders);
		this.orders = null;
	}

	@Override
	protected void parseLine(String ln) {
		String[] parts = ln.split(";");
		OrderMetadata om = new OrderMetadata();
		om.setName(parts[0]);
		om.setCode(parts[2]);
		om.setNumber(Integer.parseInt(parts[1]));
		om.setParameters(parts[3]);
		om.setDifficulty(parts[4]);
		om.setRequirement(parts[5]);
		om.setSkillRequirement(parts[6]);
		this.orders.addItem(om);
	}
}
