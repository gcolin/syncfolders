package application.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public abstract class Item {

	private byte[] hash;
	private Sync c;
	private Item parent;
	private Connection co;
	
	public Item(Sync c, Item parent,Connection co) {
		this.c = c;
		this.co = co;
		this.parent = parent;
	}
	
	public Item getParent() {
		return parent;
	}

	public abstract boolean isDirectory();
	
	public abstract String getName();
	
	public abstract long getLength();
	
	public abstract long getTimestamp();
	
	public InputStream getContent() throws IOException {
		return co.get(this);
	}
	
	public boolean remove() throws IOException {
		return co.remove(this);
	}
	
	public String getPath() {
		StringBuilder str = new StringBuilder();
		Item e = parent;
		while(e!=null) {
			str.insert(0, "/"+e.getName());
			e = e.parent;
		}
		str.append("/").append(getName());
		return str.toString();
	}
	
	public int deepSize() {
		int s = 0;
		Item p = getParent();
		while(p != null) {
			s++;
			p = p.getParent();
		}
		return s;
	}
	
	public byte[] getHash() throws IOException {
		if(hash == null) {
			InputStream in = null;
			try{
				in = getContent();
				MessageDigest d = MessageDigest.getInstance("SHA-512");
				byte[] b = c.buffer;
				int count = 0;
				while((count=in.read(b))!=-1) {
					d.update(b,0,count);
				}
				hash = d.digest();
			} catch(Exception e) {
				throw new IOException(e);
			} finally {
				c.close(in);
			}
		}
		return hash;
	}
}
