package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import application.io.Item;
import application.io.Sync;

public class MemoryNode extends Item {

	private List<MemoryNode> children = new ArrayList<>();
	private boolean directory;
	private String content;
	private long timestamp;
	private long length;
	private String name = "/";
	private MemoryNode parent;
	
	public MemoryNode(Sync c, Item parent, MemoryConnection co) {
		super(c, parent, co);
	}
	
	public List<MemoryNode> getChildren() {
		return children;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent0() {
		return content;
	}
	
	@Override
	public InputStream getContent() throws IOException {
		return new ByteArrayInputStream(content.getBytes());
	}
	
	@Override
	public boolean isDirectory() {
		return directory;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public long getLength() {
		return length;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	public MemoryNode getParent2() {
		return parent;
	}

	public void setParent(MemoryNode parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (directory ? 1231 : 1237);
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemoryNode other = (MemoryNode) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (directory != other.directory)
			return false;
		if (length != other.length)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
	
	public void print(int tab) {
		System.out.println(name);
		for(MemoryNode n:children) {
			for(int i=0;i<tab;i++) {
				System.out.print("  ");
			}
			System.out.print("> ");
			n.print(tab+1);
		}
	}
	
	@Override
	public String toString() {
		return directory?"[]"+name:name;
	}
}
