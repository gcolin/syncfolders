package test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import application.io.AbstractConnection;
import application.io.Check;
import application.io.Connection;
import application.io.Item;
import application.io.Sync;
import org.junit.Assert;

public class SyncTest {

	private Map<String, MemoryConnection> fakeConnections = new HashMap<>();
	
	private class Sync0 extends Sync {
		
		int remove = 0;
		int send = 0;
		
		@Override
		public Connection build(String addr) throws IOException {
			if(fakeConnections.containsKey(addr)) {
				return fakeConnections.get(addr);
			}
			return super.build(addr);
		}
		
		@Override
		protected void send(Item next, Connection csrc, Connection cdest) throws IOException {
			send++;
			super.send(next, csrc, cdest);
		}
		
		@Override
		protected void remove(Item next, Connection cdest) throws IOException {
			remove++;
			super.remove(next, cdest);
		}
	}
	
	Sync0 s;
	
	@Before
	public void before() {
		s = new Sync0();
		fakeConnections.put("empty", new MemoryConnection(s));
		fakeConnections.put("empty2", new MemoryConnection(s));
		
		MemoryConnection simple = new MemoryConnection(s);
		simple.getRoot().getChildren().add(buildFile(simple,null));
		
		fakeConnections.put("simple", simple);
		
		MemoryConnection complex = new MemoryConnection(s);
		complex.getRoot().getChildren().add(buildFile(simple,null));
		
		complex.getRoot().getChildren().add(buildFile2(complex, null));
		
		MemoryNode dir = new MemoryNode(s, null, simple);
		dir.setName("dir");
		dir.setDirectory(true);
		
		MemoryNode dir2 = new MemoryNode(s, null, simple);
		dir2.setName("dir2");
		dir2.setDirectory(true);
		
		complex.getRoot().getChildren().add(dir);
		dir.setParent(complex.getRoot());
		complex.getRoot().getChildren().add(dir2);
		dir2.setParent(complex.getRoot());
		MemoryNode dir3 = new MemoryNode(s, dir2, simple);
		dir3.setName("dir3");
		dir3.setDirectory(true);
		dir3.setParent(dir2);
		dir2.getChildren().add(dir3);
		dir2.getChildren().add(buildFile(simple,dir2));
		Collections.sort(dir2.getChildren(), AbstractConnection.COMPARATOR);
		
		MemoryNode dir4 = new MemoryNode(s, dir3, simple);
		dir4.setName("dir4");
		dir4.setDirectory(true);
		dir4.setParent(dir3);
		dir3.getChildren().add(dir4);
		dir4.getChildren().add(buildFile(simple,dir4));
		dir4.getChildren().add(buildFile2(complex, dir4));
		Collections.sort(dir4.getChildren(), AbstractConnection.COMPARATOR);
		
		Collections.sort(complex.getRoot().getChildren(), AbstractConnection.COMPARATOR);
		
		fakeConnections.put("complex", complex);
	}

	public MemoryNode buildFile2(MemoryConnection simple,Item parent) {
		MemoryNode file2 = new MemoryNode(s, parent, simple);
		file2.setContent("hello2");
		file2.setName("file2");
		file2.setLength(file2.getContent0().length());
		file2.setTimestamp(System.currentTimeMillis());
		return file2;
	}

	public MemoryNode buildFile(MemoryConnection simple,Item parent) {
		MemoryNode file = new MemoryNode(s, parent, simple);
		file.setContent("hello");
		file.setName("file");
		file.setLength(file.getContent0().length());
		file.setTimestamp(System.currentTimeMillis());
		return file;
	}
	
	@Test
	public void testEmpty() throws IOException {
		s.sync("empty", "empty2", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertTrue(fakeConnections.get("empty").getRoot().getChildren().isEmpty());
		Assert.assertTrue(fakeConnections.get("empty2").getRoot().getChildren().isEmpty());
	}
	
	@Test
	public void testSimple() throws IOException {
		s.sync("simple", "empty", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertEquals(fakeConnections.get("simple").getRoot(),fakeConnections.get("empty").getRoot());
		Assert.assertEquals(0, s.remove);
		Assert.assertEquals(1, s.send);
	}
	
	@Test
	public void testSimple2() throws IOException {
		s.sync("empty","simple", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertEquals(fakeConnections.get("simple").getRoot(),fakeConnections.get("empty").getRoot());
		Assert.assertEquals(1, s.remove);
		Assert.assertEquals(0, s.send);
	}
	
	@Test
	public void testSimple3() throws IOException {
		s.sync("simple","simple", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertEquals(0, s.remove);
		Assert.assertEquals(0, s.send);
	}
	
	@Test
	public void testComplex() throws IOException {
		s.sync("complex","empty", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertEquals(fakeConnections.get("complex").getRoot(),fakeConnections.get("empty").getRoot());
		Assert.assertEquals(0, s.remove);
		Assert.assertEquals(9, s.send);
	}
	
	@Test
	public void testComplex2() throws IOException {
		s.sync("empty","complex", Check.all(Check.SIZE,Check.TIMESTAMP));
		Assert.assertEquals(fakeConnections.get("complex").getRoot(),fakeConnections.get("empty").getRoot());
		Assert.assertEquals(9, s.remove);
		Assert.assertEquals(0, s.send);
	}
	
}
