package application.io;

import java.io.IOException;
import java.io.InputStream;

public class SimpleItem extends Item {

	private String name;
	private long timestamp;
	private boolean directory;
	private Connection c;
	
	public SimpleItem(Sync s,Connection c,String name, long timestamp) {
		super(s, null, null);
		this.name = name;
		this.timestamp = timestamp;
	}
	
	public SimpleItem(Sync s,Connection c,String name) {
		super(s, null, null);
		this.name = name;
		this.timestamp = System.currentTimeMillis();
	}
	
	public SimpleItem(Sync s,Connection c,String name, boolean directory) {
		super(s, null, null);
		this.name = name;
		this.timestamp = System.currentTimeMillis();
		this.directory = directory;
	}

	@Override
	public boolean isDirectory() {
		return directory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getLength() {
		return -1;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public InputStream getContent() throws IOException {
		return c.get(this);
	}

}
