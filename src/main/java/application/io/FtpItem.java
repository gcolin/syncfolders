package application.io;

import org.apache.commons.net.ftp.FTPFile;

public class FtpItem extends Item {

	private FTPFile file;

	public FtpItem(FTPFile file, FTPConnection c,Sync s,Item parent) {
		super(s,parent, c);
		this.file = file;
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public long getLength() {
		return file.getSize();
	}

	@Override
	public long getTimestamp() {
		return file.getTimestamp().getTimeInMillis();
	}

}