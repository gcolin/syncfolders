package application.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractConnection implements Connection {

	public static final Comparator<Item> COMPARATOR = new Comparator<Item>() {

		@Override
		public int compare(Item o1, Item o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	private static class Context {
		int next = 0;
		Item[] files;
		Item parent;
	}

	private int current = 0;
	private List<Context> stack = new ArrayList<>();
	private List<String> pathParts = new ArrayList<>();
	private String path = "/";
	protected Sync c;
	
	public AbstractConnection(Sync c) {
		this.c = c;
	}

	protected abstract Item[] list0(Item parent) throws IOException;

	private Context current(Item parent) throws IOException {
		if (stack.size() == current) {
			Context c = new Context();
			c.parent = parent;
			c.files = list0(parent);
			Arrays.sort(c.files, COMPARATOR);
			stack.add(c);
		}
		return stack.get(current);
	}

	@Override
	public Item[] list() throws IOException {
		return current(null).files;
	}
	
	protected abstract void enter(Item e) throws IOException;
	protected abstract void up() throws IOException;
	
	protected abstract InputStream get0(Item item) throws IOException;
	protected abstract boolean remove0(Item item) throws IOException;
	
	@Override
	public InputStream get(Item item) throws IOException {
		updatePosition(item);
		return get0(item);
	}
	
	@Override
	public boolean remove(Item item) throws IOException {
		updatePosition(item);
		return remove0(item);
	}

	public void updatePosition(Item item) throws IOException {
		if(item.getParent() != current(null).parent) {
			// find common ancestor
			int ds = item.deepSize();
			for(int i=current;i>ds;i--) {
				pop();
			}
			ArrayDeque<Item> queue = new ArrayDeque<>();
			Item desc = item.getParent();
			for(int i=ds;i>current;i--) {
				queue.addLast(desc);
				desc = desc.getParent();
			}
			while(desc!=item()) {
				pop();
				queue.addLast(desc);
				desc = desc.getParent();
			}
			// up to the item parent
			while(!queue.isEmpty()) {
				push(queue.removeLast());
			}
		}
	}
	
	protected void enter0(Item e) throws IOException {
		enter(e);
		pathParts.add(e.getName());
		buildPath();
	}

	@Override
	public boolean enter() throws IOException {
		Context c = current(null);
		for (int i = c.next; i < c.files.length; i++) {
			if (c.files[i].isDirectory()) {
				enter0(c.files[i]);
				current++;
				c.next = i + 1;
				current(c.files[i]);
				return true;
			}
		}
		if(current>0) {
			pop();
			if(enter()) {
				return true;
			}
		}
		return false;
	}
	
	protected abstract boolean mkdir0(String name) throws IOException;
	
	@Override
	public boolean mkdir(String name) throws IOException {
		boolean b = mkdir0(name);
		if(b) {
			Context cu = current(null);
			Item[] tmp = new Item[cu.files.length+1];
			System.arraycopy(cu.files, 0, tmp, 0, cu.files.length);
			tmp[cu.files.length] = new SimpleItem(c, this, name, true);
			cu.files = tmp;
		}
		return b;
	}
	
	private void buildPath() {
		StringBuilder str = new StringBuilder();
		for(String s:pathParts) {
			str.append("/").append(s);
		}
		if(str.length() == 0) {
			path = "/";
		}else{
			path = str.toString();
		}
	}

	
	@Override
	public void push(Item name) throws IOException {
		Context c = current(null);
		for (int i = 0; i < c.files.length; i++) {
			if (c.files[i].getName().equals(name.getName())) {
				enter0(name);
				current++;
				current(c.files[i]);
				break;
			}
		}
	}
	
	@Override
	public String path() {
		return path;
	}
	
	@Override
	public void pop() throws IOException {
		current--;
		stack.remove(stack.size() - 1);
		up();
		pathParts.remove(pathParts.size()-1);
		buildPath();
	}
	
	@Override
	public Item item() throws IOException {
		return current(null).parent;
	}
}
