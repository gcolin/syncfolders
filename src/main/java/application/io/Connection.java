package application.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection extends Closeable {

	Item[] list() throws IOException;
	InputStream get(Item item) throws IOException;
	OutputStream set(Item item) throws IOException;
	boolean mkdir(String name) throws IOException;
	boolean enter() throws IOException;
	void push(Item name) throws IOException;
	void pop() throws IOException;
	boolean remove(Item item) throws IOException;
	String path();
	Item item() throws IOException;
}
