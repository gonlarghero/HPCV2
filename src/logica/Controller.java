package logica;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Controller{
	
	private static int wordLength;
	private BufferedImage shownCaptcha;
	private static ArrayList<ArrayList<ArrayList<Double>>> angleCombinations = new ArrayList<ArrayList<ArrayList<Double>>>();
	private static ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private static List<String> dictionary = new ArrayList<String>();
	private static List<String> bruteForce = new ArrayList<String>();
	
	public Controller (int _wordLength) throws IOException {
		wordLength = _wordLength;
		generateAngleCombinations(wordLength);
		dictionary = readFile("/dictionary" + wordLength + ".txt");
		generateWordCombinations(wordLength);
		generateFiguresCombinations();
		setShownCaptcha(null);
	}
	
	public Image getRandomCaptcha() {
		BufferedImage buff = new captchaGenerator().getCaptchaImage();
		setShownCaptcha(buff);
		return buff.getScaledInstance(250, 150,  0);
	}
	
	public Image getCaptchaFromString(String word) {
		BufferedImage buff = new captchaGenerator().getCaptchaImageFromString(word);
		setShownCaptcha(buff);
		return buff.getScaledInstance(250, 150,  0);
	}
	
	private static ArrayList<ArrayList<Double>> generateAngleCombinations(int wordLength) {
		ArrayList<ArrayList<Double>> ret = new ArrayList<>();
		if(wordLength != 1) {			
			ArrayList<ArrayList<Double>> aux = generateAngleCombinations(wordLength -1);
			for(ArrayList<Double> r:aux) {
				for (Double rotation = -0.4; rotation < 0.4; rotation = rotation + 0.1) {
					Double rotation_clean = Math.round(rotation * 10) / 10.0;
					@SuppressWarnings("unchecked")
					ArrayList<Double> clone = (ArrayList<Double>) r.clone();
					clone.add(rotation_clean);
					ret.add(clone);
				}
			}
		}else {		
			for (double rotation = -0.4; rotation < 0.4; rotation = rotation + 0.1) {
				ArrayList<Double> clone = new ArrayList<>();
				Double rotation_clean = Math.round(rotation * 10) / 10.0;
				clone.add(rotation_clean);
				ret.add(clone);
			}
		}
		angleCombinations.add(ret);
		return ret;
	}
	
	private static ArrayList<String> generateWordCombinations(int wordLength) {
		ArrayList<String> ret = new ArrayList<>();
		if(wordLength != 1) {			
			ArrayList<String> aux = generateWordCombinations(wordLength -1);
			for(String r:aux) {
				for (int chara = 97; chara <= 122; chara++) {
					String clone = r.concat(Character.toString((char) chara));
					ret.add(clone);
				}
			}
		}else {		
			for (int chara = 97; chara <= 122; chara++) {
				String clone = Character.toString((char) chara);
				ret.add(clone);
			}
		}
		bruteForce.addAll(ret);
		return ret;
	}
	
	private static void generateFiguresCombinations() throws IOException {
		ArrayList<String> positions = readFile("/noise-positions.txt");
		for (int i = 0; i < positions.size(); i++)
		{
			ArrayList<ArrayList<Integer>> squares = new ArrayList<ArrayList<Integer>>();
			String chosePos = positions.get(i);
			String[] pos = chosePos.split(":");
			String[] aux;
			int index = 0;
			for(String s:pos) {
				ArrayList<Integer> coords = new ArrayList<Integer>();
				aux = s.split(",");	
				coords.add(0, Integer.parseInt(aux[0]));
				coords.add(1, Integer.parseInt(aux[1]));
				coords.add(2, Integer.parseInt(aux[2]));
				squares.add(index, coords);
				index++;				
			}
			figuresCombinations.add(i,squares);
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

	/**
	 * @return the shownCaptcha
	 */
	public BufferedImage getShownCaptcha() {
		return shownCaptcha;
	}

	/**
	 * @param shownCaptcha the shownCaptcha to set
	 */
	public void setShownCaptcha(BufferedImage shownCaptcha) {
		this.shownCaptcha = shownCaptcha;
	}

	public ArrayList<ArrayList<ArrayList<Double>>> getAngleCombinations() {
		return angleCombinations;
	}

	public ArrayList<ArrayList<ArrayList<Integer>>> getFiguresCombinations() {
		return figuresCombinations;
	}

	public List<String> getDictionary() {
		return dictionary;
	}

	public List<String> getBruteForce() {
		return bruteForce;
	}

	public void setAngleCombinations(ArrayList<ArrayList<ArrayList<Double>>> angleCombinations) {
		Controller.angleCombinations = angleCombinations;
	}

	public void setFiguresCombinations(ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations) {
		Controller.figuresCombinations = figuresCombinations;
	}

	public void setDictionary(List<String> dictionary) {
		Controller.dictionary = dictionary;
	}

	public void setBruteForce(List<String> bruteForce) {
		Controller.bruteForce = bruteForce;
	}

}