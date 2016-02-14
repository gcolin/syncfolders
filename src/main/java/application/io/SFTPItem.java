package application.io;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SFTPItem extends Item {

	private LsEntry entry;
	
	public SFTPItem(LsEntry entry,SFTPConnection c,Sync s, Item parent) {
		super(s, parent, c);
		this.entry = entry;
	}

	@Override
	public boolean isDirectory() {
		return entry.getAttrs().isDir();
	}

	@Override
	public String getName() {
		return entry.getFilename();
	}

	@Override
	public long getLength() {
		return entry.getAttrs().getSize();
	}

	@Override
	public long getTimestamp() {
		return entry.getAttrs().getMTime()*1000l;
	}

}
