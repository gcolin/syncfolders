package application.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPConnection extends AbstractConnection {

	private Session session;
	private ChannelSftp c;

	public SFTPConnection(Session session, ChannelSftp c, Sync s) {
		super(s);
		this.session = session;
		this.c = c;
	}

	@Override
	protected InputStream get0(Item item) throws IOException {
		try {
			return c.get(item.getName());
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	@Override
	public OutputStream set(Item item) throws IOException {
		try {
			return c.put(item.getName(), ChannelSftp.OVERWRITE);
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected boolean remove0(Item item) throws IOException {
		try {
			if (item.isDirectory()) {
				c.rmdir(item.getName());
			} else {
				c.rm(item.getName());
			}
			return true;
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		session.disconnect();
	}

	@Override
	protected Item[] list0(Item parent) throws IOException {
		try {
			List<Item> all = new ArrayList<>();
			for (Object o : c.ls(".")) {
				if (o instanceof LsEntry) {
					LsEntry entry = (LsEntry) o;
					if (!entry.getFilename().equals("..") && !entry.getFilename().equals(".")) {
						Item item = new SFTPItem(entry, this, super.c, parent);
						all.add(item);
					}
				}
			}
			return all.toArray(new Item[all.size()]);
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void enter(Item e) throws IOException {
		try {
			c.cd(e.getName());
		} catch (SftpException e1) {
			throw new IOException(e1);
		}
	}

	@Override
	protected void up() throws IOException {
		try {
			c.cd("..");
		} catch (SftpException e1) {
			throw new IOException(e1);
		}
	}

	@Override
	protected boolean mkdir0(String name) throws IOException {
		try {
			c.mkdir(name);
			return true;
		} catch (SftpException e1) {
			throw new IOException(e1);
		}
	}

}
