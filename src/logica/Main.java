package logica;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) throws Exception {
		int threadCount = Integer.parseInt(args[0]);
		boolean staticQueues = Boolean.parseBoolean(args[1]);
		boolean bruteForce = Boolean.parseBoolean(args[2]);
		String word = args[3];
		int wordLength = word.length();
		
		Controller c = Controller.getInstance(wordLength);		
		c.getCaptchaFromString(word);
		HpcAttack hpc = new HpcAttack(c.getShownCaptcha(), threadCount, bruteForce, staticQueues, wordLength);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		
		long time1 = System.currentTimeMillis();
		Future<String> result = executor.submit(hpc);
		while(!result.isDone()) {
			System.out.println("Progress:" + hpc.progress() + "%");
			TimeUnit.SECONDS.sleep(1);
		}
		word = result.get();
		executor.shutdown();
		long time2 = System.currentTimeMillis();
		long res = (time2-time1)/1000;
		System.out.print( word + " en " + res +" segundos");
	}

}
