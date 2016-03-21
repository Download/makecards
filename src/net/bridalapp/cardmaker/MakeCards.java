package net.bridalapp.cardmaker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import static net.bridalapp.cardmaker.Card.View.*;
import static net.bridalapp.cardmaker.CardMaker.createCard;

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

	public static List<BufferedImage> loadImages(Path front, Path back, Path detail1, Path detail2, Path detail3) {
		Path[] paths = new Path[]{front, back, detail1, detail2, detail3};
		List<BufferedImage> results = new ArrayList<>();
		for (Path path : paths) {
			BufferedImage cardImage = loadImage(path);
			if (cardImage == null) return null;
			results.add(cardImage);
		}
		return results;
	}

	public static void saveCard(Card card, Path folder) throws IOException {
		ImageIO.write(card.get(FRONT), "jpg", folder.resolve("front-large.jpg").toFile()); 
		ImageIO.write(card.get(BACK), "jpg", folder.resolve("back-large.jpg").toFile()); 
		ImageIO.write(card.get(DETAIL1), "jpg", folder.resolve("detail-1-large.jpg").toFile()); 
		ImageIO.write(card.get(DETAIL2), "jpg", folder.resolve("detail-2-large.jpg").toFile()); 
		ImageIO.write(card.get(DETAIL3), "jpg", folder.resolve("detail-3-large.jpg").toFile());
		ImageIO.write(card.get(THUMBS), "jpg", folder.resolve("thumbs.jpg").toFile());
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
            List<BufferedImage> cardImages = loadImages(front, back, detail1, detail2, detail3);

            Card card = createCard(cardImages);
            System.out.println("Card created. Saving card images...");
            
            saveCard(card, folder);
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
	
	public static void main(String[] args) {
		String folder = args.length > 0 ? args[0] : System.getProperty("user.dir");
		System.out.println("BridalApp CardsMaker");
		processFolder(Paths.get(folder));
	}
}
