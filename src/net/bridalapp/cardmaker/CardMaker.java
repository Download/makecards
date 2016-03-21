package net.bridalapp.cardmaker;

import static net.bridalapp.cardmaker.Card.View.BACK;
import static net.bridalapp.cardmaker.Card.View.DETAIL1;
import static net.bridalapp.cardmaker.Card.View.DETAIL2;
import static net.bridalapp.cardmaker.Card.View.DETAIL3;
import static net.bridalapp.cardmaker.Card.View.FRONT;
import static net.bridalapp.cardmaker.Card.View.THUMBS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.imgscalr.Scalr;
import static org.imgscalr.Scalr.Mode.*;
import static org.imgscalr.Scalr.Method.*;

/**
 * Makes cards with 5 gallery images and a thumbnail in the correct sizes.
 * 
 * @see #createCard
 */
public class CardMaker {
	/**
	 * Creates a card.
	 * 
	 * @param cardImages A list containing exactly 5 images in order: front, back, detail1, detail2, detail3.
	 * @return a card with 5 gallery images and a thumbnail, or null if card creation failed.
	 */
	public static Card createCard(List<BufferedImage> cardImages) {
        if (cardImages == null) return null;
        List<BufferedImage> galleryImages = createGalleryImages(cardImages); 
        if (galleryImages == null) return null;
        BufferedImage thumbnail = createThumbnailImage(cardImages);
        if (thumbnail == null) return null;
        return new Card()
        	.set(FRONT, galleryImages.get(FRONT.ordinal()))
	        .set(BACK, galleryImages.get(BACK.ordinal()))
	        .set(DETAIL1, galleryImages.get(DETAIL1.ordinal()))
	        .set(DETAIL2, galleryImages.get(DETAIL2.ordinal()))
	        .set(DETAIL3, galleryImages.get(DETAIL3.ordinal()))
	        .set(THUMBS, thumbnail);
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
	
	public static List<BufferedImage> createGalleryImages(List<BufferedImage> cardImages) {
		List<BufferedImage> results = new ArrayList<>();
		for (BufferedImage cardImage : cardImages) {
			BufferedImage galleryImage = createGalleryImage(cardImage);
			if (galleryImage == null) return null;
			results.add(galleryImage);
		}
		return results;
	}	
	
	public static BufferedImage createThumbnailImage(List<BufferedImage> cardImages) {
		BufferedImage thumb = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D out = thumb.createGraphics();
		
		BufferedImage front = Scalr.resize(cardImages.get(FRONT.ordinal()), ULTRA_QUALITY, FIT_EXACT, 400, 600);
		out.drawImage(front, 0, 0, 400, 600, null);
		
		BufferedImage back = Scalr.resize(cardImages.get(BACK.ordinal()), ULTRA_QUALITY, FIT_EXACT, 200, 300);
		out.drawImage(back, 400, 0, 200, 300, null);
		
		BufferedImage detail1 = Scalr.resize(cardImages.get(DETAIL1.ordinal()), ULTRA_QUALITY, FIT_EXACT, 100, 150);
		out.drawImage(detail1, 400, 300, 100, 150, null);
		
		BufferedImage detail2 = Scalr.resize(cardImages.get(DETAIL2.ordinal()), ULTRA_QUALITY, FIT_EXACT, 100, 150);
		out.drawImage(detail2, 500, 300, 100, 150, null);

		BufferedImage detail3 = Scalr.resize(cardImages.get(DETAIL3.ordinal()), ULTRA_QUALITY, FIT_EXACT, 100, 150);
		out.drawImage(detail3, 400, 450, 100, 150, null);
		
		Color fillColor = calculateFillColor(detail2, detail3);
		out.setColor(fillColor);
		out.fillRect(500, 450, 100, 150);
		
		return thumb;
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

}
