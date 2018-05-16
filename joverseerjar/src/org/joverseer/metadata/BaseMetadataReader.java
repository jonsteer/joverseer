package org.joverseer.metadata;

import java.io.BufferedReader;
import java.io.IOException;

public class BaseMetadataReader implements MetadataReader {
	String filename = ""; // init to stop exception expection when child is improperly defined.
	boolean isOptional = false;

	protected void initFilename() {};
	protected void start() {};
	protected void parseLine(String ln) {};
	protected void finish(GameMetadata gm) {};
	@Override
	public void load(GameMetadata gm) throws IOException, MetadataReaderException {
		this.initFilename();
		this.start(); // call children
		BufferedReader reader;
		try {
			reader = gm.getUTF8ResourceByGame(this.filename); // try game specific version first.
		} catch (IOException exc) {
			try {
				reader = gm.getUTF8Resource(this.filename);
			} catch (IOException e) {
				if (this.isOptional) {
					return;
				} else {
					throw e;
				}
			}
		}
		try {
			String ln;
			while ((ln = reader.readLine()) != null) {
				if (ln.startsWith(("#")) || ln.equals(""))
					continue;
				try {
					this.parseLine(ln); // call children
				} catch (Exception exc) {
					System.out.println(ln);
					throw exc;
				}
			}
			this.finish(gm); // call children
		} catch (IOException exc) {
			exc.printStackTrace();
			throw exc;
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new MetadataReaderException("Error reading metadata in " + this.filename, exc);
		}

	}

}
