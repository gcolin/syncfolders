package application.io;

import java.io.File;

public class FileItem extends Item {

	private File file;
	private boolean directory;
	private String name;
	private long length;
	private long timestamp;

	public FileItem(File file,Sync c, Item parent,Connection co) {
		super(c, parent, co);
		this.file = file;
		this.directory = file.isDirectory();
		this.name = file.getName();
		this.length = file.length();
		this.timestamp = file.lastModified();
	}
	
	public File getFile() {
		return file;
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
		return length;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
}
