package application.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class Check {

	/**
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return true if different
	 * @throws IOException
	 */
	public abstract boolean test(Item a, Item b, Sync c) throws IOException;

	public static final Check NOCHECK = new Check() {

		@Override
		public boolean test(Item a, Item b, Sync c) {
			return false;
		}
	};

	public static final Check SIZE = new Check() {

		@Override
		public boolean test(Item a, Item b, Sync c) {
			return a.getLength() != b.getLength();
		}
	};

	public static final Check HASH = new Check() {

		@Override
		public boolean test(Item a, Item b, Sync c) throws IOException {
			return !Arrays.equals(a.getHash(), b.getHash());
		}
	};

	public static final Check TIMESTAMP = new Check() {

		@Override
		public boolean test(Item a, Item b, Sync c) {
			return a.getTimestamp() > b.getTimestamp();
		}
	};

	public static final Check CONTENT = new Check() {

		@Override
		public boolean test(Item a, Item b, Sync c) throws IOException {
			InputStream in = null;
			InputStream in2 = null;
			try {
				in = a.getContent();
				in2 = b.getContent();
				byte[] b1 = c.buffer;
				byte[] b2 = c.buffer2;
				int c1 = 0;
				int c2 = 0;
				while (true) {
					c1 = in.read(b1);
					c2 = in2.read(b2);
					if (c1 == -1 && c2 == -1) {
						break;
					} else if (c1 != c2 || !equals(b1, b2, c1)) {
						return true;
					}
				}
			} finally {
				c.close(in);
				c.close(in2);
			}
			return false;
		}

		public boolean equals(byte[] a, byte[] a2, int length) {
			for (int i = 0; i < length; i++) {
				if (a[i] != a2[i]) {
					return false;
				}
			}
			return true;
		}
	};

	public static Check all(Check... checks) {
		return new Check() {

			@Override
			public boolean test(Item a, Item b, Sync c) throws IOException {
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].test(a, b, c)) {
						return true;
					}
				}
				return false;
			}
		};
	}
}
