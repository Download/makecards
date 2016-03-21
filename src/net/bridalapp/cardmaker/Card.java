package net.bridalapp.cardmaker;

import java.awt.image.BufferedImage;

public class Card {
	public static enum View {FRONT, BACK, DETAIL1, DETAIL2, DETAIL3, THUMBS}
	private BufferedImage[] images = new BufferedImage[6];
	
	public Card set(View view, BufferedImage image) {
		images[view.ordinal()] = image;
		return this;
	}
	
	public BufferedImage get(View view) {
		return images[view.ordinal()];
	}
}
