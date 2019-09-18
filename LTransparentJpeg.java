import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

public class LTransparentJpeg {
	//此工具的理论基础 https://www.cnblogs.com/U-tansuo/p/JPG-Mask.html
	//实现了PNG24/32更高压缩率的图片压缩方法
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start LTransparentJpeg");
		String quality = "90";
		String dither="0";
		String pngname = "test0.png";
		String finalout = pngname.substring(0, pngname.lastIndexOf(".png")) + ".jpg";
		String out = pngname.substring(0, pngname.lastIndexOf(".png")) + "_out.jpg";
		String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.jpg";
		if(dither.equals("0")) {
		    doCmd("convert " + pngname + " -quality " + quality
				+ " " + tmp);
		}else {
			doCmd("convert " + pngname + " -dither FloydSteinberg -ordered-dither o8x8,32,64,32 " + " -quality " + quality
					+ " " + tmp);
		}
		doCmd("./guetzlijpg --verbose --quality " + quality + " " + tmp + " " + out);
		exportalpha(pngname, pngname + ".alpha", true);
		mergeFiles(finalout, new String[] { out, pngname + ".alpha" });
		DelFile(tmp);
		DelFile(out);
		// 转换为png格式图片
		ltjpegToPng(finalout, finalout + ".png");
//		ltjpgToPng("2d-circle-2.jpg","2d-circle-2.png");
	}

	public static void ltjpegToPng(String jpg, String png) {
		try {
			System.out.println("start ltjpegToPng");
			ImageIcon imageIcon = new ImageIcon(jpg);
			BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
			g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
			int rgb[] = new int[imageIcon.getIconWidth() * imageIcon.getIconHeight()];
			byte[] a = readAlpha(jpg);
			int ww = imageIcon.getIconWidth();
			int hh = imageIcon.getIconHeight();
			for (int xx = bufferedImage.getMinX(); xx < bufferedImage.getWidth(); xx++) {
				for (int yy = bufferedImage.getMinY(); yy < bufferedImage.getHeight(); yy++) {
					if (a != null) {
						int alpha = a[yy * ww + xx];
						int trgb = bufferedImage.getRGB(xx, yy) & 0x00FFFFFF;
						int argb8888 = (alpha << 24) | trgb;
						rgb[yy * ww + xx] = argb8888;
					} else {
						rgb[yy * ww + xx] = bufferedImage.getRGB(xx, yy);
					}
				}
			}
			BufferedImage bf = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
			bf.setRGB(0, 0, ww, hh, rgb, 0, ww);
			try {
				ImageIO.write(bf, "png", new File(png));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] readAlpha(String file) {
		//查找EOI字符出现的位置，如果是JPEG必然存在EOI。
		byte[] data = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			data = new byte[fis.available()];
			fis.read(data);
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int XZPOS = -1;
		if (data != null) {
			for (int i = 0; i < data.length-1; i++) {
				// 找jpeg的EOI文件尾
				if (data[i] == -1) {
					if (data[i + 1] == -39) {
						System.out.println("EOI=" + i);
						System.out.println("next=" + data[i + 1]);
						XZPOS = i + 2;
						if (XZPOS >= data.length) {
							XZPOS = -1;
						}
					}
				}
			}
		}
		if (XZPOS == -1) {
			System.out.println("no has transpart");
			return null;
		} else {
			System.out.println("has transpart");
			byte[] trans = new byte[data.length - XZPOS];
			System.arraycopy(data, XZPOS, trans, 0, trans.length);
			System.out.println("first=" + trans[0]);
			System.out.println("sec=" + trans[1]);
			return xzuncompress(trans);
		}
	}

	public static void mergeFiles(String outFile, String[] files) {
		int BUFSIZE = 1024 * 8;
		FileChannel outChannel = null;
		System.out.println("Merge " + Arrays.toString(files) + " into " + outFile);
		try {
			outChannel = new FileOutputStream(outFile).getChannel();
			for (String f : files) {
				FileChannel fc = new FileInputStream(f).getChannel();
				ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
				while (fc.read(bb) != -1) {
					bb.flip();
					outChannel.write(bb);
					bb.clear();
				}
				fc.close();
			}
			System.out.println("Merged!! ");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (outChannel != null) {
					outChannel.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	public static void exportalpha(String loc1, String loc2, boolean xz) {
		try {
			System.out.println("start exportalpha");
			ImageIcon imageIcon = new ImageIcon(loc1);
			BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
					BufferedImage.TYPE_INT_ARGB);
			// 处理数据为565形式的rgb数据
			Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
			g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
			byte a[] = new byte[imageIcon.getIconWidth() * imageIcon.getIconHeight()];
			int ww = imageIcon.getIconWidth();
			int hh = imageIcon.getIconHeight();
			for (int xx = bufferedImage.getMinX(); xx < bufferedImage.getWidth(); xx++) {
				for (int yy = bufferedImage.getMinY(); yy < bufferedImage.getHeight(); yy++) {
					int pixel = bufferedImage.getRGB(xx, yy);
					int alpha = (pixel & 0xFF000000) >>> 24;
					a[yy * ww + xx] = (byte) alpha;
				}
			}
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(ba);
			dos.write(a);
			dos.close();
			byte[] data = ba.toByteArray();
			FileOutputStream fos = new FileOutputStream(loc2);
			if (xz) {
				fos.write(xzcompress(data));
			} else {
				fos.write(data);
			}
			fos.close();
			System.out.println("exportalpha ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] xzuncompress(byte[] bytes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try {
			XZInputStream ungzip = new XZInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = ungzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			ungzip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	public static byte[] xzcompress(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		LZMA2Options options = new LZMA2Options();
		options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives
		options.setDictSize(8192 * 1024);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XZOutputStream xzOutputStream = new XZOutputStream(out, options);

		byte[] buf = new byte[8192];
		int size;

		while ((size = in.read(buf)) != -1) {
			xzOutputStream.write(buf, 0, size);
		}
		xzOutputStream.finish();
		xzOutputStream.close();
		return out.toByteArray();
	}

	public static void doCmd(String command) {
		// System.out.println(command);
		try {
			Process process = Runtime.getRuntime().exec(command);
			BufferedInputStream bis = new BufferedInputStream(process.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			process.waitFor();
			if (process.exitValue() != 0) {
				System.out.println("error!");
			}

			bis.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean DelFile(String loc) {
		File f = new File(loc);
		return delFile(f);
	}

	public static boolean delFile(File file) {
		if (!file.exists()) {
			return false;
		}

		if (!file.isDirectory()) {
			return file.delete();
		}
		return false;
	}

}
