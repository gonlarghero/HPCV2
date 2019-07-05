package logica;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class HpcAttack implements Callable<String>{
	
	private BufferedImage image;
	private static List<String> wordList;
	private static Integer currentWord = 0;
	private int threadCount;
	private int jobSize = 1;
	private CountDownLatch latch;
	private int wordLength = 4;
	private String foundWord = "noup";
	private static boolean found = false;
	private ArrayList<Slave> threadList;
	private Semaphore mutex = new Semaphore(1);
	private Map<Integer, List<String>> slavesJob = new HashMap<Integer, List<String>>();
	private ArrayList<ArrayList<ArrayList<Double>>> angleCombinations = new ArrayList<ArrayList<ArrayList<Double>>>();
	private ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations = new ArrayList<ArrayList<ArrayList<Integer>>>();
    
	public static ArrayList<Double> test = new ArrayList<>();
	
	public HpcAttack(BufferedImage img, Integer threadCount) throws IOException {	
		image = img;
		this.threadCount = threadCount;
		wordList = readFile("/dictionary.txt");
		generateAngleCombinations(wordLength);
		generateFiguresCombinations();
		threadList = new ArrayList<Slave>();
		latch = new CountDownLatch(threadCount);
		
	}
		
	private ArrayList<ArrayList<Double>> generateAngleCombinations(int wordLength) {
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
	
	private void generateFiguresCombinations() throws IOException {
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
	
	private List<String> getJob(int currentThread){
		try {
			mutex.acquire();
			
			List<String> job = new ArrayList<String>();
			
			if (!found)
			{
				Integer limit;
				if (currentWord + jobSize <= wordList.size()) {
					limit = currentWord + jobSize;
				}
				else {
					limit = wordList.size();
				}
				job = wordList.subList(currentWord, limit);
				slavesJob.put(currentThread, job);
				currentWord = limit;
			}			
			
			mutex.release();
			
			return job;
        }
		catch (Exception x) {
			x.printStackTrace();
			return new ArrayList<String>();
		}
	}

	private static ArrayList<String> readFile(String name) throws IOException {
		ArrayList<String> wordList = new ArrayList<String>();
		InputStream is = new FileInputStream(captchaGenerator.class.getResource(name).getPath());
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();

		while (line != null) {
			wordList.add(line);
			line = buf.readLine();
		}
		buf.close();
		return wordList;
	}
	
	public class Slave implements Runnable {
		Thread t;
		int id;
		List<String> currentJob;
		CountDownLatch latch;
		   
		Slave(int threadId, CountDownLatch latch){
		    id = threadId; 
	        this.latch = latch;
		    t = new Thread(this, Integer.toString(id));
		    t.start();
		}
		
		public void run(){
			currentJob = getJob(id);
			while (currentJob.size() > 0 && !found)
			{
				for (int i = 0; !found && i < currentJob.size(); i++)
				{
					String word = currentJob.get(i);
					for (int j = 0; !found && j < figuresCombinations.size(); j++)
					{
						for (int k = 0; !found && k < angleCombinations.get(word.length() - 1).size(); k++) {
							ArrayList<Double> rotations = angleCombinations.get(word.length() - 1).get(k);
							ArrayList<ArrayList<Integer>> squares = figuresCombinations.get(j);
							BufferedImage generatedImage = captchaGenerator.getCaptchaImageFromString(word, squares, rotations);

							if (compareImages(image,generatedImage))
							{
								found = true;
								foundWord = word;
							}
						}
					}
				}
				currentJob = getJob(id);
			}
			latch.countDown();
		}
		
		public boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
			  // The images must be the same size.
			  if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
			    return false;
			  }

			  int width  = imgA.getWidth();
			  int height = imgA.getHeight();

			  // Loop over every pixel.
			  for (int y = 0; y < height; y++) {
			    for (int x = 0; x < width; x++) {
			      // Compare the pixels for equality.
			      if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
			        return false;
			      }
			    }
			  }

			  return true;
			}
	}

	public static Integer getCurrentWord() {
		return currentWord;
	}

	public static void setCurrentWord(Integer currentWord) {
		HpcAttack.currentWord = currentWord;
	}

	public static List<String> getWordList() {
		return wordList;
	}

	public static void setWordList(List<String> wordList) {
		HpcAttack.wordList = wordList;
	}

	public static boolean isFound() {
		return found;
	}

	public static void setFound(boolean found) {
		HpcAttack.found = found;
	}

	@Override
	public String call() throws Exception {
		for (int i = 0; i < threadCount; i++) {
			threadList.add(new Slave(i, latch));
		}
		
		 try {
			latch.await();
			return foundWord;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return foundWord;
		}
	}
	
	public int progress() {
		if(wordList != null && !wordList.isEmpty()) {
			return currentWord * 100 / wordList.size();
		}
		return 0;
	}
}
