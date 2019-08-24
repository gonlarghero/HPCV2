package logica;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class captchaGenerator {

	private String captchaString = "";

	// Function to generate random captcha image and returns the BufferedImage
	public BufferedImage getCaptchaImage() {
		try {
			Color backgroundColor = Color.white;
			Color borderColor = Color.black;
			Color textColor = Color.black;
			Color circleColor = new Color(190, 160, 150);
			Font textFont = new Font("Verdana", Font.BOLD, 20);
			int width = 160;
			int height = 50;
			int index;

			float horizMargin = 10.0f;
			double rotationRange = 0.7;
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
			g.setColor(backgroundColor);
			g.fillRect(0, 0, width, height);
			g.setColor(circleColor);
			
			ArrayList<String> positions = readFile("/noise-positions.txt");
			index = (int) (Math.random() * positions.size());
			String chosePos = positions.get(index);
			String[] pos = chosePos.split(":");
			String[] aux;
			for(String s:pos) {
				aux = s.split(",");				
				int X = Integer.parseInt(aux[0]);
				int Y = Integer.parseInt(aux[1]);
				int L = Integer.parseInt(aux[2]);
				g.draw3DRect(X, Y, L * 2, L * 2, true);
			}
			
			g.setColor(textColor);
			g.setFont(textFont);
			FontMetrics fontMetrics = g.getFontMetrics();
			int maxAdvance = fontMetrics.getMaxAdvance();
			int fontHeight = fontMetrics.getHeight();

			// toma la palabra del diccionario
			ArrayList<String> wordList = readFile("/dictionary4.txt");
			index = (int) (Math.random() * wordList.size());
			String word = wordList.get(index);
			StringBuffer finalString = new StringBuffer();
			finalString.append(word);
			float spaceForLetters = -horizMargin * 2 + width;
			float spacePerChar = spaceForLetters / (word.length() - 1.0f);
			
					
			for (int i = 0; i < word.length(); i++) {
				char characterToShow = word.charAt(i);
				// this is a separate canvas used for the character so that
				// we can rotate it independently
				int charWidth = fontMetrics.charWidth(characterToShow);
				int charDim = Math.max(maxAdvance, fontHeight);
				int halfCharDim = (int) (charDim / 2);
				BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
				Graphics2D charGraphics = charImage.createGraphics();
				charGraphics.translate(halfCharDim, halfCharDim);
				double angle = (Math.random() - 0.5) * rotationRange;
				angle = Math.floor(angle * 10) / 10;				
				
				charGraphics.transform(AffineTransform.getRotateInstance(angle));
				charGraphics.translate(-halfCharDim, -halfCharDim);
				charGraphics.setColor(textColor);
				charGraphics.setFont(textFont);
				int charX = (int) (0.5 * charDim - 0.5 * charWidth);
				charGraphics.drawString("" + characterToShow, charX,
						(int) ((charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent()));
				float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
				int y = (int) ((height - charDim) / 2);
				g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
				charGraphics.dispose();
			}
			g.setColor(borderColor);
			g.drawRect(0, 0, width - 1, height - 1);
			g.dispose();
			captchaString = finalString.toString();
			// System.out.println(captchaString);
			return bufferedImage;
		} catch (Exception ioe) {
			throw new RuntimeException("Unable to build image", ioe);
		}
	}

	public BufferedImage getCaptchaImageFromString(String word) {
		try {
			Color backgroundColor = Color.white;
			Color borderColor = Color.black;
			Color textColor = Color.black;
			Color circleColor = new Color(190, 160, 150);
			Font textFont = new Font("Verdana", Font.BOLD, 20);
			int width = 160;
			int height = 50;
			int index;
			float horizMargin = 10.0f;
			double rotationRange = 0.7;
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
			g.setColor(backgroundColor);
			g.fillRect(0, 0, width, height);
			g.setColor(circleColor);
			
			ArrayList<String> positions = readFile("/noise-positions.txt");
			index = (int) (Math.random() * positions.size());
			String chosePos = positions.get(index);
			String[] pos = chosePos.split(":");
			String[] aux;
			for(String s:pos) {
				aux = s.split(",");				
				int X = Integer.parseInt(aux[0]);
				int Y = Integer.parseInt(aux[1]);
				int L = Integer.parseInt(aux[2]);
				g.draw3DRect(X, Y, L * 2, L * 2, true);
			}
			
			g.setColor(textColor);
			g.setFont(textFont);
			FontMetrics fontMetrics = g.getFontMetrics();
			int maxAdvance = fontMetrics.getMaxAdvance();
			int fontHeight = fontMetrics.getHeight();

			StringBuffer finalString = new StringBuffer();
			finalString.append(word);
			float spaceForLetters = -horizMargin * 2 + width;
			float spacePerChar = spaceForLetters / (word.length() - 1.0f);
			for (int i = 0; i < word.length(); i++) {

				char characterToShow = word.charAt(i);
				// this is a separate canvas used for the character so that
				// we can rotate it independently
				int charWidth = fontMetrics.charWidth(characterToShow);
				int charDim = Math.max(maxAdvance, fontHeight);
				int halfCharDim = (int) (charDim / 2);
				BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
				Graphics2D charGraphics = charImage.createGraphics();
				charGraphics.translate(halfCharDim, halfCharDim);
				double angle = (Math.random() - 0.5) * rotationRange;
				angle = Math.floor(angle * 10) / 10;
				charGraphics.transform(AffineTransform.getRotateInstance(angle));
				charGraphics.translate(-halfCharDim, -halfCharDim);
				charGraphics.setColor(textColor);
				charGraphics.setFont(textFont);
				int charX = (int) (0.5 * charDim - 0.5 * charWidth);
				charGraphics.drawString("" + characterToShow, charX,
						(int) ((charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent()));
				float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
				int y = (int) ((height - charDim) / 2);
				g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
				charGraphics.dispose();
			}
			g.setColor(borderColor);
			g.drawRect(0, 0, width - 1, height - 1);
			g.dispose();
			captchaString = finalString.toString();
			System.out.println(captchaString);
			return bufferedImage;
		} catch (Exception ioe) {
			throw new RuntimeException("Unable to build image", ioe);
		}
	}

	public static BufferedImage getCaptchaImageFromString(String word, ArrayList<ArrayList<Integer>> squares, ArrayList<Double> rotations) {
		try {
			Color backgroundColor = Color.white;
			Color borderColor = Color.black;
			Color textColor = Color.black;
			Color circleColor = new Color(190, 160, 150);
			Font textFont = new Font("Verdana", Font.BOLD, 20);
			int width = 160;
			int height = 50;
			float horizMargin = 10.0f;
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
			g.setColor(backgroundColor);
			g.fillRect(0, 0, width, height);
			g.setColor(circleColor);

			for(ArrayList<Integer> square:squares) {			
				int X = square.get(0);
				int Y = square.get(1);
				int L = square.get(2);
				g.draw3DRect(X, Y, L * 2, L * 2, true);
			}
			
			g.setColor(textColor);
			g.setFont(textFont);
			FontMetrics fontMetrics = g.getFontMetrics();
			int maxAdvance = fontMetrics.getMaxAdvance();
			int fontHeight = fontMetrics.getHeight();

			StringBuffer finalString = new StringBuffer();
			finalString.append(word);
			float spaceForLetters = -horizMargin * 2 + width;
			float spacePerChar = spaceForLetters / (word.length() - 1.0f);
			for (int i = 0; i < word.length(); i++) {

				char characterToShow = word.charAt(i);
				// this is a separate canvas used for the character so that
				// we can rotate it independently
				int charWidth = fontMetrics.charWidth(characterToShow);
				int charDim = Math.max(maxAdvance, fontHeight);
				int halfCharDim = (int) (charDim / 2);
				BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
				Graphics2D charGraphics = charImage.createGraphics();
				charGraphics.translate(halfCharDim, halfCharDim);
				double angle = rotations.get(i);
				charGraphics.transform(AffineTransform.getRotateInstance(angle));
				charGraphics.translate(-halfCharDim, -halfCharDim);
				charGraphics.setColor(textColor);
				charGraphics.setFont(textFont);
				int charX = (int) (0.5 * charDim - 0.5 * charWidth);
				charGraphics.drawString("" + characterToShow, charX,
						(int) ((charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent()));
				float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
				int y = (int) ((height - charDim) / 2);
				g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
				charGraphics.dispose();
			}
			g.setColor(borderColor);
			g.drawRect(0, 0, width - 1, height - 1);
			g.dispose();
			return bufferedImage;
		} catch (Exception ioe) {
			throw new RuntimeException("Unable to build image", ioe);
		}
	}

	private static ArrayList<String> readFile(String name) throws IOException {
		ArrayList<String> wordList = new ArrayList<String>();
		InputStream is = captchaGenerator.class.getResourceAsStream(name);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();

		while (line != null) {
			wordList.add(line);
			line = buf.readLine();
		}
		buf.close();
		return wordList;
	}

	// Function to return the Captcha string
	public String getCaptchaString() {
		return captchaString;
	}
}