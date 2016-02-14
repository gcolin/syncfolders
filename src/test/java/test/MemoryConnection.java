package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import application.io.AbstractConnection;
import application.io.Item;
import application.io.Sync;

public class MemoryConnection extends AbstractConnection {

	public MemoryConnection(Sync c) {
		super(c);
	}

	private MemoryNode root = new MemoryNode(c, null,this);
	private MemoryNode current = root;
	
	public MemoryNode getRoot() {
		return root;
	}

	@Override
	protected InputStream get0(Item item) throws IOException {
		return new ByteArrayInputStream(((MemoryNode)item).getContent0().getBytes());
	}

	@Override
	public OutputStream set(Item item) throws IOException {
		MemoryNode n = new MemoryNode(c, null,this);
		n.setName(item.getName());
		n.setTimestamp(item.getTimestamp());
		current.getChildren().add(n);
		return new OutputStream() {
			
			StringBuilder str = new StringBuilder();
			
			@Override
			public void write(int b) throws IOException {
				str.append((char)b);
			}
			
			@Override
			public void close() throws IOException {
				n.setContent(str.toString());
				n.setLength(n.getContent0().length());
			}
		};
	}

	@Override
	protected boolean remove0(Item item) throws IOException {
		return current.getChildren().remove(item);
	}

	@Override
	public void close() throws IOException {		
	}

	@Override
	protected Item[] list0(Item parent) throws IOException {
		return current.getChildren().toArray(new Item[current.getChildren().size()]);
	}

	@Override
	protected void enter(Item e) throws IOException {
		for(MemoryNode n:current.getChildren()) {
			if(n.getName().equals(e.getName())) {
				current = n;
				break;
			}
		}
	}

	@Override
	protected void up() throws IOException {
		current = current.getParent2();
	}

	@Override
	protected boolean mkdir0(String name) throws IOException {
		MemoryNode n = new MemoryNode(c, null,this);
		n.setName(name);
		n.setDirectory(true);
		n.setParent(current);
		current.getChildren().add(n);
		return true;
	}

	@Override
	public String path() {
		StringBuilder str = new StringBuilder();
		MemoryNode m = current;
		while(m!=null) {
			str.insert(0, "/"+m.getName());
			m = m.getParent2();
		}
		return str.toString();
	}
	

}
