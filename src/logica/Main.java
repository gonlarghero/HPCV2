package logica;

import java.util.concurrent.ExecutorService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
	public static void main(String[] args) throws Exception {

		ArrayList<ArrayList<String>> arrayParametros = new ArrayList<ArrayList<String>>(
				Arrays.asList(
						//new ArrayList<String>(Arrays.asList("1", "true", "false", "1oo")),
						//new ArrayList<String>(Arrays.asList("1", "true", "true", "1oo")),
						//new ArrayList<String>(Arrays.asList("1", "true", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("1", "true", "false", "1ooo")),

						//new ArrayList<String>(Arrays.asList("4", "true", "true", "1oo")),
						new ArrayList<String>(Arrays.asList("4", "true", "false", "1oo")),
						//new ArrayList<String>(Arrays.asList("4", "true", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("4", "true", "false", "1ooo")),
						//new ArrayList<String>(Arrays.asList("4", "false", "true", "1oo")),
						new ArrayList<String>(Arrays.asList("4", "false", "false", "1oo"))
						//new ArrayList<String>(Arrays.asList("4", "false", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("4", "false", "false", "1ooo")),

						//new ArrayList<String>(Arrays.asList("8", "true", "true", "1oo")),
						//new ArrayList<String>(Arrays.asList("8", "true", "false", "1oo")),
						//new ArrayList<String>(Arrays.asList("8", "true", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("8", "true", "false", "1ooo")),
						//new ArrayList<String>(Arrays.asList("8", "true", "false", "1oooo")),

						//new ArrayList<String>(Arrays.asList("8", "false", "true", "1oo")),
						//new ArrayList<String>(Arrays.asList("8", "false", "false", "1oo"))
						//new ArrayList<String>(Arrays.asList("8", "false", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("8", "false", "false", "1ooo"))
						//new ArrayList<String>(Arrays.asList("8", "false", "false", "1oooo")),

						//new ArrayList<String>(Arrays.asList("16", "true", "true", "1oo")),
						//new ArrayList<String>(Arrays.asList("16", "true", "false", "1oo")),
						//new ArrayList<String>(Arrays.asList("16", "true", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("16", "true", "false", "1ooo")),
						//new ArrayList<String>(Arrays.asList("16", "true", "false", "1oooo")),
						//new ArrayList<String>(Arrays.asList("16", "true", "true", "1oooo")),

						//new ArrayList<String>(Arrays.asList("16", "false", "true", "1oo")),
						//new ArrayList<String>(Arrays.asList("16", "false", "false", "1oo")),
						//new ArrayList<String>(Arrays.asList("16", "false", "true", "1ooo")),
						//new ArrayList<String>(Arrays.asList("16", "false", "false", "1ooo")),
					    //new ArrayList<String>(Arrays.asList("16", "false", "false", "1oooo")),
						//new ArrayList<String>(Arrays.asList("16", "false", "true", "1oooo")),

						//new ArrayList<String>(Arrays.asList("32", "true", "false", "1oooo")),
						//new ArrayList<String>(Arrays.asList("32", "false", "false", "1oooo")),
						//new ArrayList<String>(Arrays.asList("32", "true", "true", "1oooo")),
						//new ArrayList<String>(Arrays.asList("32", "false", "true", "1oooo"))

				));
		for (ArrayList<String> arg : arrayParametros) {
			int threadCount = Integer.parseInt(arg.get(0));
			boolean staticQueues = Boolean.parseBoolean(arg.get(1));
			boolean bruteForce = Boolean.parseBoolean(arg.get(2));
			String word = arg.get(3);
			int wordLength = word.length();

			System.out.println(threadCount + " threads, " + staticQueues + " StaticQueues, " + bruteForce
					+ " bruteforce, " + wordLength + " de largo");

			Controller c = new Controller(wordLength);
			c.getCaptchaFromString(word);
			HpcAttack hpc = new HpcAttack(c.getShownCaptcha(), threadCount, bruteForce, staticQueues, wordLength, c);
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);

			long time1 = System.currentTimeMillis();
			Future<String> result = executor.submit(hpc);
			word = result.get();
			executor.shutdown();
			long time2 = System.currentTimeMillis();
			long res = (time2 - time1) / 1000;
			System.out.println(res + " segundos");
		}
	}

}
