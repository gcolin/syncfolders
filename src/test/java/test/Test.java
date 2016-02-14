package test;

import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String[] args) throws NoSuchAlgorithmException {
//		System.out.println(URI.create("ftp://user:password@hello.test/path/sub").getPort());
		
		MessageDigest.getInstance("SHA-512");
	}

}
