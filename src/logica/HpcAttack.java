package logica; 

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class HpcAttack implements Callable<String>{
	
	private BufferedImage image;
	private static Integer totalBlocksCount = 0;
	private static Integer currentBlock = 0;
	private static Integer totalProcessedBlocks = 0;
	private int threadCount;
	private CountDownLatch latch;
	private String foundWord = "word not found";
	private static boolean found = false;
	private Semaphore totalProcessedBlocksMutex;
	private ArrayList<Semaphore> mutexList;
	private ArrayList<Slave> threadList;
	private List<List<String>> blocks;
	private List<LinkedList<Integer>> threadQueues;
	private ArrayList<ArrayList<ArrayList<Double>>> angleCombinations = new ArrayList<ArrayList<ArrayList<Double>>>();
	private ArrayList<ArrayList<ArrayList<Integer>>> figuresCombinations = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private int blockSize = 1;
	private int currentIteration = 1;
	private int iterationCount = 10;
	private List<String> wordList;
	private int blocksPerIteration = 0;
	private boolean staticQueues;
	
	public HpcAttack(BufferedImage img, Integer threadCount,boolean bf, boolean staticQueues, int wordLength) throws IOException {
		Controller c = Controller.getInstance(wordLength);
		found = false;
	 	image = img;
	 	currentBlock = 0;
	 	totalProcessedBlocks = 0;
	 	totalProcessedBlocksMutex = new Semaphore(1);
		this.threadCount = threadCount;
		if (bf) {
			wordList = c.getBruteForce();
		}else {
			wordList = c.getDictionary();
		}		
		angleCombinations = c.getAngleCombinations();
		figuresCombinations = c.getFiguresCombinations();
		java.util.Collections.sort(wordList, new StringLengthComparator()); 
		blocks = chopped(wordList,blockSize);
		totalBlocksCount = blocks.size();
		threadQueues = new ArrayList<LinkedList<Integer>>();
		threadList = new ArrayList<Slave>();
		mutexList = new ArrayList<Semaphore>();
		latch = new CountDownLatch(threadCount);
		this.staticQueues = staticQueues;
	}
		
	
	
	private void getJob(Slave slave){
		try {			
			LinkedList<Integer> queue = threadQueues.get(slave.id);
			if (!found && queue.size() > 0)	{				
				slave.currentBlock = queue.getFirst();	
			}
			else if (found) {
				slave.currentBlock = -1;
			}
			else if (!staticQueues){
				fillInQueues(slave.id);
			}
			else
			{
				slave.currentBlock = -1;
			}
        }
		catch (Exception x) {
			x.printStackTrace();
		}
	}

	public class Slave implements Runnable {
		Thread t;
		int id;
		CountDownLatch latch;
		String word;
		int processedBlocks;
		int currentBlock;
		   
		Slave(int threadId, CountDownLatch latch){
		    id = threadId; 
	        this.latch = latch;
		    t = new Thread(this, Integer.toString(id));
		    t.start();
		}
		
		public void run(){
			try {
				getJob(this); 
				while(!found && currentBlock != -1) // currentBlock -1 es que no hay mas na'
				{
					for (int i = 0; !found && i < blocks.get(currentBlock).size(); i++)
					{
						word = blocks.get(currentBlock).get(i);
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
					processedBlocks++;
					totalProcessedBlocksMutex.acquire();
					totalProcessedBlocks++;
					System.out.print("Processed Blocks: " + totalProcessedBlocks);
					totalProcessedBlocksMutex.release();
					mutexList.get(id).acquire();				
					threadQueues.get(id).pollFirst(); // cada thread hace un poll cuando termina, no lo hace getJob
					mutexList.get(id).release();
					getJob(this);
				}
				latch.countDown();
			} catch (Exception e) {
				replaceThread(id);
			}
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
			mutexList.add(i, new Semaphore(1));
		}
		
		for (int i = 0; i < threadCount; i++) {
			threadQueues.add(i, new LinkedList<Integer>());
		}
		
		fillInQueues(-1);
		
		for (int i = 0; i < threadCount; i++) {
			threadList.add(i, new Slave(i, latch));
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
		return totalProcessedBlocks * 100 / totalBlocksCount;	
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
	
	private void fillInQueues(int currentThread) throws InterruptedException {
		ArrayList<Double> blocksRatioPerThread = new ArrayList<Double>();
		// hago los mutex
		for (int i = 0; i < threadCount; i++)
		{
			mutexList.get(i).acquire();
		}
		
		// si es la primera iteración parto en partes iguales
		if (currentIteration == 1)
		{
			blocksPerIteration = (int)((double)totalBlocksCount / (double)iterationCount);
			for (int i = 0; i < threadCount; i++)
			{
				blocksRatioPerThread.add(i,  (double)1 / (double)threadCount);
			}
		}
		else // si no es la primera iteración 
		{
			// calculo el total
			int totalProccessedBlocks = 0;
			for (int i = 0; i < threadCount; i++)
			{
				totalProccessedBlocks = totalProccessedBlocks + threadList.get(i).processedBlocks;
			}

			// calculo el porcentaje por cada thread
			for (int i = 0; i < threadCount; i++)
			{
				blocksRatioPerThread.add(i, ((double)threadList.get(i).processedBlocks / (double)totalProccessedBlocks));
			}
		}
		
		if (currentIteration <= iterationCount) // si todavía no llegó la etapa de robo
		{
			int blocksInCurrentIteration;
			if (!staticQueues && currentIteration < iterationCount)
			{
				blocksInCurrentIteration = blocksPerIteration;
			}
			else
			{
				blocksInCurrentIteration = totalBlocksCount - currentBlock;
			}
			//asigno un poco para cada thread según su porcentaje
			int aux = blocksInCurrentIteration;
			for (int i = 0; i < threadCount; i++)
			{
				int toIndex;
				if (i < threadCount - 1)
				{
					int threadBlocks =(int)(blocksInCurrentIteration * blocksRatioPerThread.get(i));
					toIndex = currentBlock + threadBlocks;
					aux = aux - threadBlocks;
				}
				else
				{
					toIndex = currentBlock + aux;
				}
				for (int j = currentBlock; j < toIndex; j++)
				{
					threadQueues.get(i).add(j);				
				}
				currentBlock = toIndex;
			}		
			
			currentIteration++;				
		}
		else // si llegamos a la etapa de robo
		{
			//encuentro al que tiene mas trabajo
			int busiestWorkQueueIndex = 0;
			for (int i = 1; i < threadCount; i++)
			{
				if (threadQueues.get(i).size() > threadQueues.get(busiestWorkQueueIndex).size())
				{
					busiestWorkQueueIndex = i;
				}
			}
			LinkedList<Integer> busiestWorkQueue = threadQueues.get(busiestWorkQueueIndex);
			
			if (busiestWorkQueue.size() > 1) // si queda trabajo
			{
				// le saco un trozo correspondiente a mi porcentaje
				int toIndex = 1 + (int)(busiestWorkQueue.size() * blocksRatioPerThread.get(currentThread));
				for (int j = 1; j < toIndex; j++)
				{
					threadQueues.get(currentThread).add(busiestWorkQueue.get(j));					;
					busiestWorkQueue.remove(j);				
				}
			}
			else // si no queda más trabajo
			{
				threadList.get(currentThread).currentBlock = -1;
			}
			
		}		
		
		// libero
		for (int i = 0; i < threadCount; i++)
		{
			mutexList.get(i).release();
		}
	}
	
	private void replaceThread(int threadId) {
		threadList.add(threadId, new Slave(threadId, latch));
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
