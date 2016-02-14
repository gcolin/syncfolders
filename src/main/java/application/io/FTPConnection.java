package application.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPConnection extends AbstractConnection {

	private FTPClient client;
	
	public FTPConnection(FTPClient client,Sync c) {
		super(c);
		this.client = client;
	}

	@Override
	public void close() throws IOException {
		client.quit();
	}

	@Override
	protected InputStream get0(Item item) throws IOException {
		InputStream in = client.retrieveFileStream(item.getName());

		return new InputStream() {

			@Override
			public int read() throws IOException {
				return in.read();
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return in.read(b, off, len);
			}

			@Override
			public void close() throws IOException {
				client.completePendingCommand();
				in.close();
			}
		};
	}

	@Override
	public OutputStream set(Item item) throws IOException {
		OutputStream out = client.storeFileStream(item.getName());
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				out.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				out.write(b, off, len);
			}

			@Override
			public void close() throws IOException {
				out.close();
				client.completePendingCommand();
			}
		};
	}

	@Override
	protected boolean mkdir0(String name) throws IOException {
		return client.makeDirectory(name);
	}

	@Override
	protected FtpItem[] list0(Item parent) throws IOException {
		FTPFile[] all = client.listFiles();
		FtpItem[] items = new FtpItem[all.length];
		for (int i = 0; i < all.length; i++) {
			items[i] = new FtpItem(all[i], this, c, parent);
		}
		return items;
	}
	
	@Override
	protected void enter(Item e) throws IOException {
		client.changeWorkingDirectory(e.getName());
	}

	@Override
	protected void up() throws IOException {
		client.changeWorkingDirectory("..");
	}

	@Override
	protected boolean remove0(Item item) throws IOException {
		return client.deleteFile(item.getName());
	}
	
}
