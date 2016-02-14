package test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;

import application.io.Connection;
import application.io.FTPConnection;
import application.io.Item;
import application.io.SimpleItem;

public class FtpTest {

	public static void main(String[] args) throws SocketException, IOException {
		FTPClient client = new FTPClient();
		client.connect("localhost");
		client.login("userftp", "admin");
		Connection c = new FTPConnection(client, null);
		try{
			OutputStream out=c.set(new SimpleItem(null, c, "hello.txt"));
			out.write("h2".getBytes());
			out.close();
			//print(c);
		}finally{
			c.close();
		}
		
	}

	private static void print(Connection c) throws IOException {
		for(Item item : c.list()) {
			System.out.println(item.getName()+" "+item.isDirectory());
			if(!item.isDirectory()) {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				InputStream in = c.get(item);
				int count = 0;
				while((count=in.read(b))>0) {
					bout.write(b, 0, count);
				}
				in.close();
				
				System.out.println("content : "+new String(bout.toByteArray()));
			}
		}
		if(c.enter()) {
			print(c);
		}
		
	}

}
