package application.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class Sync {

	private static final String FTP = "ftp://";
	private static final String FTPS = "ftps://";
	private static final String SFTP = "sftp://";
	byte[] buffer = new byte[4096];
	byte[] buffer2 = new byte[4096];
	private Consumer<String> messageConsumer;
	private long lastTime = 0;

	public void setMessageConsumer(Consumer<String> messageConsumer) {
		this.messageConsumer = messageConsumer;
	}

	public Consumer<String> getMessageConsumer() {
		return messageConsumer;
	}

	public void sync(String src, String dest, boolean checkSize, boolean checkDate, boolean checkContent)
			throws IOException {
		List<Check> all = new ArrayList<>();
		if (checkSize) {
			all.add(Check.SIZE);
		}
		if (checkDate) {
			all.add(Check.TIMESTAMP);
		}
		if (checkContent) {
			all.add(Check.CONTENT);
		}
		Check c = all.isEmpty() ? Check.NOCHECK : Check.all(all.toArray(new Check[all.size()]));
		sync(src, dest, c);
	}

	private void notify(String message) {
		if (messageConsumer != null) {
			long now = System.currentTimeMillis();
			if (now - lastTime > 1000) {
				lastTime = now;
				messageConsumer.accept(message);
			}
		}
	}

	public void sync(String src, String dest, Check check) throws IOException {
		Connection csrc = null;
		Connection cdest = null;
		try {
			csrc = build(src);
			cdest = build(dest);
			sync0(check, csrc, cdest);
		} finally {
			close(csrc);
			close(cdest);
		}

		lastTime = 0;
		notify("end");
	}

	private void sync0(Check check, Connection csrc, Connection cdest) throws IOException {
		lastTime = 0;
		notify(csrc.path() + " <---> " + cdest.path());
		Item[] lsrc = csrc.list();
		Item[] ldest = cdest.list();
		int psrc = 0;
		int pdest = 0;
		while (lsrc.length>psrc || ldest.length>pdest) {
			if (ldest.length==pdest) {
				send(lsrc[psrc++], csrc, cdest);
			} else if (lsrc.length==psrc) {
				remove(ldest[pdest++], cdest);
			} else {
				Item iscr = lsrc[psrc++];
				Item idest = ldest[pdest++];
				if (iscr.getName().equals(idest.getName())) {
					if (iscr.isDirectory()) {
						csrc.push(iscr);
						cdest.push(iscr);
						sync0(check, csrc, cdest);
						csrc.pop();
						cdest.pop();
					} else if (check.test(iscr, idest, this)) {
						send(iscr, csrc, cdest);
					}
				} else if (iscr.getName().compareTo(idest.getName()) > 0) {
					remove(idest, cdest);
					psrc--;
				} else {
					send(iscr, csrc, cdest);
					pdest--;
				}
			}
		}
	}

	protected void remove(Item next, Connection cdest) throws IOException {
		if (next.isDirectory()) {
			cdest.push(next);
			for (Item item : cdest.list()) {
				remove(item, cdest);
			}
			cdest.pop();
			cdest.remove(next);
		} else {
			notify("remove " + cdest.path() + "/" + next.getName());
			cdest.remove(next);
		}
	}

	protected void send(Item next, Connection csrc, Connection cdest) throws IOException {
		if (next.isDirectory()) {
			cdest.mkdir(next.getName());
			csrc.push(next);
			cdest.push(next);
			for (Item item : csrc.list()) {
				send(item, csrc, cdest);
			}
			csrc.pop();
			cdest.pop();
		} else {
			notify("copy to " + cdest.path() + "/" + next.getName());
			OutputStream out = null;
			InputStream in = null;
			try {
				out = cdest.set(next);
				in = csrc.get(next);
				int count = 0;
				byte[] b = buffer;
				while ((count = in.read(b)) != -1) {
					out.write(b, 0, count);
				}
			} finally {
				close(out);
				close(in);
			}
		}
	}

	public void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
			}
		}
	}

	public Connection build(String addr) throws IOException {
		if (addr.startsWith(FTP)) {
			FTPClient client = new FTPClient();
			configure(client, URI.create(addr), false);
			return new FTPConnection(client, this);
		}
		if (addr.startsWith(FTPS)) {
			FTPClient client = new FTPSClient();
			configure(client, URI.create(addr), false);
			return new FTPConnection(client, this);
		}
		if (addr.startsWith(SFTP)) {
			JSch jsch = new JSch();
			URI uri = URI.create(addr);
			String userInfo = uri.getRawUserInfo();
			int split = userInfo.indexOf(':');
			String login = decode(userInfo.substring(0, split));
			String password = decode(userInfo.substring(split + 1));
			int port = 22;
			if (uri.getPort() != -1) {
				port = uri.getPort();
			}
			try {
				Session session = jsch.getSession(login, uri.getHost(), port);

				UserInfo info = new UserInfo() {
					
					private boolean firstTime = true;

					@Override
					public void showMessage(String message) {
					}

					@Override
					public boolean promptYesNo(String message) {
						return true;
					}

					@Override
					public boolean promptPassword(String message) {
						if (firstTime) {
				            firstTime = false;
				            return true;
				        }
				        return firstTime;
					}

					@Override
					public boolean promptPassphrase(String message) {
						return true;
					}

					@Override
					public String getPassword() {
						return password;
					}

					@Override
					public String getPassphrase() {
						return null;
					}
				};
				session.setUserInfo(info);
				session.connect();
				Channel channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp c = (ChannelSftp) channel;
				if (uri.getPath() != null && uri.getPath().length() > 1) {
					c.cd(uri.getPath().substring(1));
				}
				return new SFTPConnection(session, c, this);
			} catch (SftpException| JSchException e) {
				throw new IOException(e);
			}
		}
		if (addr.startsWith("file:")) {
			File f = new File(URI.create(addr).toURL().getFile());
			check(addr, f);
			return new FsConnection(f,this);
		}
		File f = new File(addr);
		check(addr, f);
		return new FsConnection(new File(addr),this);
	}

	public void check(String addr, File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException(addr);
		}
		if (!f.isDirectory()) {
			throw new IOException("the file must be a directory : " + addr);
		}
	}

	private void configure(FTPClient client, URI addr, boolean sftp) throws IOException {
		String userInfo = addr.getRawUserInfo();
		if (addr.getPort() == -1) {
			client.connect(addr.getHost());
		} else {
			client.connect(addr.getHost(), addr.getPort());
		}
		if (userInfo != null) {
			int split = userInfo.indexOf(':');
			client.login(decode(userInfo.substring(0, split)), decode(userInfo.substring(split + 1)));
		}
		if (addr.getPath() != null && addr.getPath().length() > 1) {
			client.changeWorkingDirectory(addr.getPath().substring(1));
		}
	}

	private String decode(String value) throws IOException {
		try {
			return URLDecoder.decode(value, "utf8");
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		}
	}

}
