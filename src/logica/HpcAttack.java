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
	private CountDownLatch latch;
	private int wordLength = 6;
	private String foundWord = "word not found";
	private static boolean found = false;
	private Semaphore mutex = new Semaphore(1);
	private ArrayList<Slave> threadList;
	private ArrayList<ArrayList<ArrayList<Double>>> angleCombinations = new ArrayList<ArrayList<ArrayList<Double>>>();
	private ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private int blockSize = 1;
	private int wordsPerIteration;
	int wordListSize;
	
	public HpcAttack(BufferedImage img, Integer threadCount) throws IOException {	
		image = img;
		this.threadCount = threadCount;
		wordList = readFile("/dictionary.txt");
		wordListSize = wordList.size();
		generateAngleCombinations(wordLength);
		generateFiguresCombinations();
		threadList = new ArrayList<Slave>();
		latch = new CountDownLatch(threadCount);
		wordsPerIteration = this.threadCount * blockSize;
		
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
	
	private void getJob(Slave slave){
		try {
			mutex.acquire();			
			
			if (!found && currentWord < wordListSize)	{
				if (slave.wordsPerIteration + slave.jobFrom > currentWord)
				{
					slave.jobSize = slave.jobSize + blockSize;
					wordsPerIteration = wordsPerIteration + blockSize;
				}
				
				slave.wordsPerIteration = wordsPerIteration;
				
				if (currentWord + slave.jobSize > wordListSize)
				{
					slave.jobSize = wordListSize - currentWord;
				}
				
				slave.jobFrom = currentWord;
				currentWord = currentWord + slave.jobSize;
			}
			else {
				slave.jobSize = 0;
			}
			
			mutex.release();
        }
		catch (Exception x) {
			x.printStackTrace();
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
		CountDownLatch latch;
		int jobFrom;
		int jobSize;
		int wordsPerIteration;
		   
		Slave(int threadId, CountDownLatch latch, int wordsPerIteration){
		    id = threadId; 
		    jobFrom = 0;
		    jobSize = 1;
		    this.wordsPerIteration = wordsPerIteration;
	        this.latch = latch;
		    t = new Thread(this, Integer.toString(id));
		    t.start();
		}
		
		public void run(){
			getJob(this); 
			while(!found && jobSize != 0)
			{
				for (int i = jobFrom; !found && i < jobFrom + jobSize; i++)
				{
					String word = wordList.get(i);
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
				getJob(this);
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
			threadList.add(i, new Slave(i, latch, wordsPerIteration));
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
