package logica; 

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class HpcAttack implements Callable<String>{
	
	private BufferedImage image;
	private static Integer totalBlocks = 0;
	private static Integer currentBlock = 0;
	private int threadCount;
	private CountDownLatch latch;
	private int wordLength = 6;
	private String foundWord = "word not found";
	private static boolean found = false;
	private Semaphore mutex = new Semaphore(1);
	private ArrayList<Slave> threadList;
	private List<Integer> blocksPerThread;
	private ArrayList<ArrayList<ArrayList<Double>>> angleCombinations = new ArrayList<ArrayList<ArrayList<Double>>>();
	private ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private int blockSize = 1;
	private List<List<String>> blocks;
	
	//why??
	private int wordsPerIteration;
	
	public HpcAttack(BufferedImage img, Integer threadCount) throws IOException {	
	 	image = img;
		this.threadCount = threadCount;
		List<String> wordList;
		wordList = readFile("/dictionary.txt");
		java.util.Collections.sort(wordList, new StringLengthComparator()); 
		blocks = chopped(wordList,blockSize);
		totalBlocks = blocks.size();
		generateAngleCombinations(wordLength);
		generateFiguresCombinations();
		threadList = new ArrayList<Slave>();
		latch = new CountDownLatch(threadCount);
		blocksPerThread =  Collections.nCopies(threadCount, 0);
		
		//why?
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
			
			if (!found && currentBlock < totalBlocks)	{
				if ( slave.blockIndex > currentWord)
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
				slave.blocksCant = 0;
			}			
			blocksPerThread.add(slave.id, blocksPerThread.get(slave.id) + slave.blocksCant);
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
		Integer blockIndex;
		int blocksCant;
		String word;
		
		/*
		int jobFrom;
		int jobSize;
		int processedWords;
		int wordsPerIteration;
		*/
		   
		Slave(int threadId, CountDownLatch latch, int wordsPerIteration){
		    id = threadId; 
		    /*
		    processedWords = 0;
		    jobFrom = 0;
		    jobSize = 1;
		    this.wordsPerIteration = wordsPerIteration;*/
	        this.latch = latch;
		    t = new Thread(this, Integer.toString(id));
		    t.start();
		}
		
		public void run(){
			getJob(this); 
			while(!found && blocksCant != 0)
			{
				for (int i = 0; !found && i < blocks.get(blockIndex).size(); i++)
				{
					word = blocks.get(blockIndex).get(i);
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
					blockIndex++;
					blocksCant--;
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
		return currentBlock * 100 / totalBlocks;	
	}	
	
	class StringLengthComparator implements Comparator<String> {

		@Override
		public int compare(String s1, String s2) {
			return s1.length() - s2.length(); // compare length of Strings
		}
	}
	
	static <T> List<List<T>> chopped(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}
}
