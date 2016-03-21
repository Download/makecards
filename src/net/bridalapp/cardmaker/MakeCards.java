package net.bridalapp.cardmaker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import static org.imgscalr.Scalr.Method.ULTRA_QUALITY;
import static org.imgscalr.Scalr.Mode.FIT_EXACT;

public class MakeCards {
	
	public static BufferedImage loadImage(Path path) {
       	File imgFile = new File(path.toString());
       	BufferedImage result = null;
       	try {
       	    result = ImageIO.read(imgFile);
       	} catch (IOException e) {
           	System.err.println("Error reading image " + imgFile.toString() + ": " + e.getMessage());
       	}
       	return result;
	}

	public static BufferedImage createGalleryImage(BufferedImage img) {
       	BufferedImage result = img;
   		if (img.getWidth() != 856 || img.getHeight() != 1280) {
            System.out.println("Resizing from " + img.getWidth() + "x" + img.getHeight() + " to 856x1280...");
       		result = Scalr.resize(img, ULTRA_QUALITY, FIT_EXACT, 856, 1280);
       	} else {
            System.out.println("Image already has correct dimensions. Skipping resize.");
       	}
       	return result;
	}
	// for the last rectangle, we calculate a fill color based on the adjacent thumbnails, so we 
	// get as little contrast difference between the blank area and the detail images as possible
	public static Color calculateFillColor(BufferedImage detail2, BufferedImage detail3) {
		// calculate the average of the bottom row of pixels from detail2 and the
		// right column of pixels from detail3, then combine them for the optimal fill color
		double sumr = 0.0, sumg = 0.0, sumb = 0.0;
		int h = detail2.getHeight();
		int w = detail2.getWidth();
		for (int i = 0; i<w; i++) {
        	Color pixel = new Color(detail2.getRGB(i, h-1));
            sumr += pixel.getRed();
            sumg += pixel.getGreen();
            sumb += pixel.getBlue();
        }

		double avg2r = 0.0, avg2g = 0.0, avg2b = 0.0;
		avg2r = sumr / w;
		avg2g = sumg / w;
		avg2b = sumb / w;
		
		sumr = sumg = sumb = 0;
		
		h = detail3.getHeight();
		w = detail3.getWidth();
		for (int i = 0; i<h; i++) {
        	Color pixel = new Color(detail3.getRGB(w-1, i));
            sumr += pixel.getRed();
            sumg += pixel.getGreen();
            sumb += pixel.getBlue();
        }
		double avg3r = 0.0, avg3g = 0.0, avg3b = 0.0;
		avg3r = sumr / h;
		avg3g = sumg / h;
		avg3b = sumb / h;
		
	    return new Color((int) ((avg2r + avg3r) / 2), (int) ((avg2g + avg3g) / 2), (int) ((avg2b + avg3b) / 2));
	}	

	public static void processCard(Path folder, Path front, Path back, Path detail1, Path detail2, Path detail3) throws IOException {
		BufferedImage thumb = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D out = thumb.createGraphics();
		Path[] paths = new Path[]{front, back, detail1, detail2, detail3};
		String[] files = new String[]{"front-large.jpg", "back-large.jpg", "detail-1-large.jpg", "detail-2-large.jpg", "detail-3-large.jpg"};
		int[][] coords = new int[][]{
			{  0,   0, 400, 600}, 
			{400,   0, 200, 300},
			{400, 300, 100, 150},
			{500, 300, 100, 150},
			{400, 450, 100, 150}
		};
		
		BufferedImage detail2Img, detail3Img = detail2Img = null;
		for (int i=0; i<paths.length; i++) {
			Path path = paths[i];
			int[] coord = coords[i];
			
			BufferedImage galleryImage = createGalleryImage(loadImage(path));
			Path saveFile = folder.resolve(files[i]);
			System.out.println("Saving " + saveFile);
			ImageIO.write(galleryImage, "jpg", saveFile.toFile());
			if (i == 3) detail2Img = galleryImage;
			if (i == 4) detail3Img = galleryImage;
			System.out.println("Adding preview image to thumbnail");
			BufferedImage preview = Scalr.resize(galleryImage, ULTRA_QUALITY, FIT_EXACT, 400, 600);
			galleryImage = null;
			System.gc();
			out.drawImage(preview, coord[0], coord[1], coord[2], coord[3], null);
			preview = null;
			System.gc();
		}			
		Color fillColor = calculateFillColor(detail2Img, detail3Img);
		out.setColor(fillColor);
		out.fillRect(500, 450, 100, 150);
		Path saveFile = folder.resolve("thumbs.jpg");
		System.out.println("Saving " + saveFile);
		ImageIO.write(thumb, "jpg", saveFile.toFile());
		out.dispose();
		thumb.flush();
		System.gc();
	}
	
	public static void processImageFiles(Path folder) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder, new DirectoryStream.Filter<Path>(){
			@Override public boolean accept(Path entry) throws IOException {
				return !Files.isDirectory(entry);
			}
        })) {
           	Path thumb = null, front = null, back = null, detail1 = null, detail2 = null, detail3 = null;
            for (Path path : directoryStream) {
                // fileNames.add(path.toString());
            	String name = path.getFileName().toString().toLowerCase(); 
            	if (name.equals("thumbs.jpg"))	thumb   = path;
            	else if (name.equals("f.jpg")) 	front   = path;
            	else if (name.equals("b.jpg")) 	back    = path;
            	else if (name.equals("1.jpg"))	detail1 = path;
            	else if (name.equals("2.jpg"))	detail2 = path;
            	else if (name.equals("3.jpg"))	detail3 = path;
            }
            if (thumb != null) {
            	System.out.println("thumbs.jpg already exists. Skipping.");
            	return;
            }
            if ((front == null) && (back == null) && (detail1 == null) && (detail2 == null) && (detail3 == null)) {
            	return;
            }
            if ((front == null) || (back == null) || (detail1 == null) || (detail2 == null) || (detail3 == null)) {
            	System.out.println("Not all card images are available. Skipping");
            	return;
            }
            
            System.out.println("Found card images. Processing...");
            processCard(folder, front, back, detail1, detail2, detail3);
           	System.out.println("Card saved.");
        } 
        catch (IOException ex) {
        	System.err.println("Error processing image files in " + folder + ": " + ex.getMessage());
        }
	}
	
	public static void processFolder(Path folder) {
		System.out.println("Processing " + folder);
		
		processImageFiles(folder);
		
		// recurse
        try (DirectoryStream<Path> subfolders = Files.newDirectoryStream(folder, new DirectoryStream.Filter<Path>(){
			@Override public boolean accept(Path entry) throws IOException {
				return Files.isDirectory(entry);
			}
        })) {
        	for (Path subfolder : subfolders) {
        		processFolder(subfolder);
        	}
        } 
        catch (IOException ex) {
        	System.err.println("Error processing " + folder + ": " + ex.getMessage());
        }
	}
	
	public static void waitKey() throws IOException {
		byte[] buffer = new byte[256];
		System.in.read();
		while (System.in.available() > 0) {
			System.in.read(buffer, 0, Math.min(System.in.available(), 256));
		}
	}
	
	public static void main(String[] args) throws IOException {
		try
		{
			String folder = args.length > 0 ? args[0] : System.getProperty("user.dir");
			System.out.println("makecards: Processing " + folder);
			System.out.println("Press Ctrl+C to abort, or Enter to proceed: ");
	
			waitKey();
			
			processFolder(Paths.get(folder));
			
			System.out.println("makecards: Done with " + folder);
			System.out.println("Press Enter to close: ");
		}
		catch(Throwable e) {
			System.err.println("Error: " + e.getMessage());
		}
	
		waitKey();
	}
}
