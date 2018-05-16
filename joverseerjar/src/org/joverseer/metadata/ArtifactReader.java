package org.joverseer.metadata;

import org.joverseer.metadata.domain.ArtifactInfo;
import org.joverseer.support.Container;

/**
 * Reads starting artifact information
 * 
 * @author Marios Skounakis
 * 
 */
public class ArtifactReader extends BaseMetadataReader implements MetadataReader {
	Container<ArtifactInfo> artifacts;
	@Override
	protected void initFilename() {
		super.filename ="arties.csv";
	}

	@Override
	protected void start() {
		this.artifacts = new Container<ArtifactInfo>();
	}

	@Override
	protected void parseLine(String ln) {
		String[] parts = ln.split(";");
		int no = Integer.parseInt(parts[0]);
		String name = parts[1];
		String power1 = parts.length > 3 ? parts[3] : "";
		String bonus = parts.length > 4 ? parts[4] : "";
		power1 += " " + bonus;
		String owner = (parts.length > 6 ? parts[6] : "");
		String alignment = parts[2];
		String power2 = (parts.length >= 6 ? parts[5] : "");
		ArtifactInfo artifact = new ArtifactInfo();
		artifact.setNo(no);
		artifact.setName(name);
		artifact.setOwner(owner);
		artifact.setAlignment(alignment);
		artifact.setPower(0, power1);
		if (!power2.equals("")) {
			artifact.setPower(1, power2);
		}
		this.artifacts.addItem(artifact);
	}

	@Override
	protected void finish(GameMetadata gm) {
		gm.setArtifacts(this.artifacts);
		this.artifacts = null;
	}

}
