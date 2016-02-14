package application.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FsConnection extends AbstractConnection {

	private File dir;

	public FsConnection(File file,Sync c) {
		super(c);
		dir = file;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	protected InputStream get0(Item item) throws IOException {
		return new FileInputStream(new File(dir, item.getName()));
	}

	@Override
	public OutputStream set(Item item) throws IOException {
		File f = new File(dir, item.getName());
		OutputStream out = new FileOutputStream(f);
		
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
				if(item.getTimestamp()>0){
					f.setLastModified(item.getTimestamp());
				}
			}
		};
	}

	@Override
	protected boolean mkdir0(String name) throws IOException {
		return new File(dir, name).mkdirs();
	}

	@Override
	protected FileItem[] list0(Item parent) {
		File[] all = dir.listFiles();
		FileItem[] items = new FileItem[all.length];
		for (int i = 0; i < all.length; i++) {
			items[i] = new FileItem(all[i], c, parent, this);
		}
		return items;
	}

	@Override
	protected void enter(Item e) {
		dir = new File(dir, e.getName());
	}

	@Override
	protected void up() {
		dir = dir.getParentFile();
	}
	
	@Override
	protected boolean remove0(Item item) throws IOException {
		return new File(dir, item.getName()).delete();
	}

}
