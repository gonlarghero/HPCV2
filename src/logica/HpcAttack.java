package logica; 

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
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
	private int threadCount;
	private CountDownLatch latch;
	private int wordLength = 4;
	private String foundWord = "word not found";
	private static boolean found = false;
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
	
	public HpcAttack(BufferedImage img, Integer threadCount,boolean bf) throws IOException {	
	 	image = img;
	 	currentBlock = 0;
		this.threadCount = threadCount;
		if (bf) {
			wordList = generateWordCombinations(wordLength);
		}else {
			wordList = readFile("/dictionary.txt");
		}		
		java.util.Collections.sort(wordList, new StringLengthComparator()); 
		blocks = chopped(wordList,blockSize);
		totalBlocksCount = blocks.size();
		threadQueues = new ArrayList<LinkedList<Integer>>();
		generateAngleCombinations(wordLength);
		generateFiguresCombinations();
		threadList = new ArrayList<Slave>();
		mutexList = new ArrayList<Semaphore>();
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
	
	private ArrayList<String> generateWordCombinations(int wordLength) {
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
		wordList.addAll(ret);
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
			LinkedList<Integer> queue = threadQueues.get(slave.id);
			if (!found && queue.size() > 0)	{				
				slave.currentBlock = queue.getFirst();	
			}
			else if (found) {
				slave.currentBlock = -1;
			}
			else {
				fillInQueues(slave.id);
			}
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
			threadList.add(i, new Slave(i, latch));
		}
		
		fillInQueues(-1);
		
		 try {
			latch.await();
			return foundWord;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return foundWord;
		}
	}
	
	public int progress() {
		return currentBlock * 100 / totalBlocksCount;	
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
			// agarro los bloques correspondientes a mi iteración
			List<List<String>> iterationBlocks = blocks.subList((int)((currentIteration - 1) * ((double)totalBlocksCount / (double)iterationCount)),(int)( (currentIteration) * ((double)totalBlocksCount / (double)iterationCount)));
			
			//asigno un poco para cada thread según su porcentaje
			for (int i = 0; i < threadCount; i++)
			{
				int toIndex = currentBlock + (int)(iterationBlocks.size() * blocksRatioPerThread.get(i));
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
