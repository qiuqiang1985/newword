package love.cq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * java 一个简单的io操作
 * 
 * @author ansj
 * 
 */
public class IOUtil {
	public static final String UTF8 = "utf-8";
	public static final String GBK = "gbk";
	public static final String TABLE = "\t";
	public static final String LINE = "\n";
	public static final byte[] TABBYTE = TABLE.getBytes();
	public static final byte[] LINEBYTE = LINE.getBytes();

	public static InputStream getInputStream(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedReader getReader(String path, String charEncoding) throws UnsupportedEncodingException {
		InputStream is = getInputStream(path);
		return new BufferedReader(new InputStreamReader(is, charEncoding));
	}

	public static RandomAccessFile getRandomAccessFile(String path, String charEncoding) throws FileNotFoundException {
		InputStream is = getInputStream(path);
		if (is != null) {
			return new RandomAccessFile(new File(path), "r");
		}
		return null;
	}

	public static void Writer(String path, String charEncoding, String content) {
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(path));
			fos.write(content.getBytes(charEncoding));
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fos);
		}
	}

	public static BufferedReader getReader(InputStream inputStream, String charEncoding) throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(inputStream, charEncoding));
	}

	public static String getContent(String path, String charEncoding) {
		return getContent(new File(path), charEncoding);
	}

	public static String getContent(InputStream is, String charEncoding) {
		BufferedReader reader = null;
		try {
			reader = IOUtil.getReader(is, charEncoding);
			return getContent(reader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	public static String getContent(File file, String charEncoding) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			return getContent(is, charEncoding);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String getContent(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String temp = null;
		while ((temp = reader.readLine()) != null) {
			sb.append(temp);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 将一个对象序列化到硬盘中
	 * 
	 * @param string
	 * @param hm
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void WriterObj(String path, Serializable hm) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
			objectOutputStream.writeObject(hm);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	/**
	 * 关闭字符流
	 * 
	 * @param reader
	 */
	public static void close(Reader reader) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭字节流
	 * 
	 * @param is
	 */
	public static void close(InputStream is) {
		try {
			if (is != null)
				is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭字节流
	 * 
	 * @param is
	 */
	public static void close(OutputStream os) {
		try {
			if (os != null) {
				os.flush();
				os.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static FileIterator instanceFileIterator(String path, String charEncoding) {
		try {
			return new FileIterator(path, charEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加载一个文件到hashMap
	 * 
	 * @param path
	 * @param charEncoding
	 * @param key
	 * @param value
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings({ "unchecked" })
	public static <K, V> HashMap<K, V> loadMap(String path, String charEncoding, Class<K> key, Class<V> value) throws UnsupportedEncodingException {

		FileIterator iteartor = null;
		HashMap<K, V> hm = null;
		try {
			iteartor = instanceFileIterator(path, charEncoding);
			hm = new HashMap<K, V>();
			String[] split = null;
			while (iteartor.hasNext()) {
				String readLine = iteartor.readLine();
				split = readLine.split("\t");
				hm.put((K) ReflectUtil.conversion(split[0], key), (V) ReflectUtil.conversion(split[1], value));
			}
		} finally {
			iteartor.close();
		}
		return hm;
	}

	public static <K, V> void writeMap(HashMap<K, V> hm, String path, String charEncoding) throws IOException {
		Iterator<Entry<K, V>> iterator = hm.entrySet().iterator();
		FileOutputStream fos = null;
		Entry<K, V> next = null;
		try {
			fos = new FileOutputStream(path);
			while (iterator.hasNext()) {
				next = iterator.next();
				fos.write(next.getKey().toString().getBytes());
				fos.write(TABBYTE);
				fos.write(next.toString().getBytes());
				fos.write(LINEBYTE);
			}
			fos.flush();
		} finally {
			fos.close();
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		HashMap<String, Integer> loadMap = loadMap("/Users/ansj/git/ansj_seg/library/userLibrary/userLibrary.dic", "utf-8", null, null);
		System.out.println(loadMap.get("淘宝"));
	}
}