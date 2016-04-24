package cp;

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PU {

	static int[] AdSizes = { 728, 90, 300, 250, 300, 600, 320, 100, 336, 280,
	// 1940, 88,
	};

	static boolean isImage(File file) {

		return isImage(file.getName());
	}

	static boolean isImage(Path path) {

		return isImage(path.toAbsolutePath().toString());
	}

	static boolean isImage(String name) {

		return name.matches(".+\\.(png|gif|jpe?g)$");
	}

	static File[] imageFiles(String dir, int maxNum) {
		List<File> files = new ArrayList<File>();
		imageFiles(files, FileSystems.getDefault().getPath(dir), maxNum);
		return files.toArray(new File[0]);
	}

	static void imageFiles(List<File> files, Path dir, int maxNum) {
		
		int max = maxNum < 0 ? Integer.MAX_VALUE : maxNum;
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path path : stream) {
				if (path.toFile().isDirectory()) {
					imageFiles(files, path, max);
				} else {
					if (files.size() >= max) break;
					File file = path.toFile();
					
					if (isImage(file))
						files.add(file);

					// else System.err.println("SKIP: "+file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static float[] boundingEllipse(Rectangle[] r, int cx, int cy) {
		
		Rectangle b = boundingRect(r);
		int w = Math.max( Math.abs(cx - b.x), Math.abs(cx - (b.x + b.width )));
		int h = Math.max( Math.abs(cy - b.y), Math.abs(cy - (b.y + b.height)));
/*		float tl = dist(b.x, b.y, cx, cy);
		float tr = dist(b.x+b.width, b.y, cx, cy);
		float br = dist(b.x, b.y+b.height, cx, cy);
		float bl = dist(b.x+b.width, b.y+b.height, cx, cy);*/
		return new float[]{ w*2, h*2 };
	}

	static Rectangle boundingRect(Rectangle[] r) {

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, 
				maxX = -Integer.MAX_VALUE, maxY = -Integer.MAX_VALUE;
		
		for (int i = 0; i < r.length; i++) {
			minX = Math.min(minX, r[i].x);
			minY = Math.min(minY, r[i].y);
			maxX = Math.max(maxX, r[i].x + r[i].width);
			maxY = Math.max(maxY, r[i].y + r[i].height);
		}
		
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	static float boundingCircle(Rectangle[] r, int cx, int cy) {
		float[] be = boundingEllipse(r, cx, cy);
		return Math.max(be[0], be[1]);
	}
	
	static float boundingCircle2(Rectangle[] r, int cx, int cy) {

		float maxRadiusSoFar = 0;
		for (int i = 0; i < r.length; i++) {
			float d1 = dist(r[i].x, r[i].y, cx, cy); 														// upper-left
			float d2 = dist(r[i].x + r[i].width, r[i].y, cx, cy); 						  // upper-right
			float d3 = dist(r[i].x, r[i].y + r[i].height, cx, cy); 							// lower-left
			float d4 = dist(r[i].x + r[i].width, r[i].y + r[i].height, cx, cy); // lower-right
			float maxOfCorners = Math.max(Math.max(d1, d2), Math.max(d3, d4));
			maxRadiusSoFar = Math.max(maxRadiusSoFar, maxOfCorners);
		}

		return maxRadiusSoFar * 2;
	}

	static float dist(float x1, float y1, float x2, float y2) {

		return (float) Math.sqrt(sq(x2 - x1) + sq(y2 - y1));
	}

	static float sq(float f) {
		return (f * f);
	}

	static void sortByArea(Shape[] r) {
		java.util.Arrays.sort(r, new java.util.Comparator<Shape>() {
			public int compare(Shape s1, Shape s2) {
				Rectangle a = s1.getBounds(), b = s2.getBounds();
				return Float.compare(a.width * a.height, b.width * b.height);
			}
		});
	}
	
	static Rectangle[] testSetVariable(int num) {
		Rectangle[] r = new Rectangle[num];
		for (int i = 0; i < r.length; i++) {
			int w = (int) (20 + Math.random() * 200);
			int h = (int) (20 + Math.random() * 200);

			if (Math.random() < .01) {
				w = 300;
				h = 600;
			}
			if (Math.random() < .02) {
				w = 728;
				h = 90;
			}
			r[i] = new Rectangle(Integer.MAX_VALUE, 0, w, h);
		}
		return r;
	}

	static Rectangle[] testSetFixed(int num) {

		Rectangle[] r = new Rectangle[num];
		for (int i = 0; i < r.length; i++) {
			int idx = (int) (Math.random() * AdSizes.length / 2) * 2;
			int w = AdSizes[idx];
			int h = AdSizes[idx + 1];
			// System.out.println(w+"x"+h);
			r[i] = new Rectangle(Integer.MAX_VALUE, 0, w, h);
		}
		return r;
	}

	static float maxEdge(Rectangle r) {

		return Math.max(r.width, r.height);
	}

	static public byte[] loadBytes(InputStream input) {
		try {
			BufferedInputStream bis = new BufferedInputStream(input);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			int c = bis.read();
			while (c != -1) {
				out.write(c);
				c = bis.read();
			}
			return out.toByteArray();

		} catch (Throwable e) {
			System.err.println("WARN: "+e.getMessage());
			// throw new RuntimeException("Couldn't load bytes from stream");
		}
		return null;
	}

  static public byte[] loadBytes(File file) {
  	
    InputStream is = null;
		byte[] byteArr = null;
    try {
			is = new FileInputStream(file);
	    byteArr = loadBytes(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
    	if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
    }
    return byteArr;
  }
 
	public static File[] imageFiles(String path) {
		return imageFiles(path, -1);
	}

	public static IRect[] loadIRects(String path) {
		
		return loadIRects(path, -1);
	}

	public static IRect[] loadIRects(String path, int maxNum) {
		
		File[] ifs = PU.imageFiles(path, maxNum);
		List<IRect> pl = new ArrayList<IRect>();
		
		for (int i = 0; i < ifs.length; i++) {
		
			IRect pi = null;
			try {
				
				byte bytes[] = loadBytes(ifs[i]);
				if (bytes == null) throw new RuntimeException("NO BYTES FOR: "+ifs[i]);
				pi = new IRect(new javax.swing.ImageIcon(bytes).getImage());
			} 
			catch (Exception e) {
				System.err.println("[WARN] " + e.getMessage());
			}
			if (pi != null) pl.add(pi);
		}
		
		return pl.toArray(new IRect[pl.size()]);
	}

	
	// ///////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		
		// String[] files = imageNames("/Users/dhowe/Desktop/AdCrawl1");
		File[] files = imageFiles("/Users/dhowe/Desktop/AdCrawl1");
		System.out.println("Loaded "+files.length);

	}

}
