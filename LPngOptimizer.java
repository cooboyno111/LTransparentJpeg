import java.io.*;

public class LPngOptimizer {
	// best png Optimizer tools
	// luqg
	//luqiuguang
	//Lovepenglu798
	public static void main(String[] args) {
		String mode = args[0];
		String pngname = args[1];
		// 8a(if pngquant Q<40) USE 16a(555)
		// 8b (call pngquant --verbose --force --ext _out.png in.png)
		// 16a (call convert in.png -dither FloydSteinberg -ordered-dither o8x8,32,32,32 out.png)
		// 16b (call convert in.png -dither FloydSteinberg -ordered-dither o8x8,32,64,32 out.png)
		// none (cp)
		// zip(call zopflipng out.png out6.png)
		String out = pngname.substring(0, pngname.lastIndexOf(".png")) + "_out.png";
		// DelFile(out);
		if (mode.equals("8a")) {
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("pngquant --speed 1 --force --quality=40-100 --ext _tmp.png " + pngname);
			if(!HasFile(tmp)) {
				doCmd("convert " + pngname + " -dither FloydSteinberg -ordered-dither o8x8,32,32,32 " + tmp);
			}
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("8b")) {//适合手绘及卡通类降低体积
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("pngquant  --speed 1 --force --ext _tmp.png " + pngname);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("8c")) {//适合手绘卡通类图片极限降低体积
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("pngquant 64 --speed 1 --force --ext _tmp.png " + pngname);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("16a")) {
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("convert " + pngname + " -dither FloydSteinberg -ordered-dither o8x8,32,32,32 " + tmp);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("16b")) {//适合高阶透明度png32图片降低体积
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("convert " + pngname + " -dither FloydSteinberg -ordered-dither o8x8,32,64,32 " + tmp);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("16c")) {//4096色 适合照片类降低体积
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("convert " + pngname + " -dither FloydSteinberg -ordered-dither o8x8,16,16,16 " + tmp);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		if (mode.equals("none")) {//只压缩不对颜色透明度进行处理
			String tmp = pngname.substring(0, pngname.lastIndexOf(".png")) + "_tmp.png";
			doCmd("cp " + pngname + " " + tmp);
			doCmd("./zopflipng -y " + tmp + " " + out);
			DelFile(tmp);
		}
		System.out.println("final");
	}

	public static boolean DelFile(String loc) {
		File f = new File(loc);
		return delFile(f);
	}

	public static boolean HasFile(String loc) {
		File file = new File(loc);
		if (!file.exists()) {
			System.out.println("no============");
			return false;
		} else {
			System.out.println("yes============");
			return true;
		}
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
}
