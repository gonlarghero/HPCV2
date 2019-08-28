package logica;
import java.util.concurrent.TimeUnit;

public class main {

	public main(String[] args) throws Exception {
		int threadCount = Integer.parseInt(args[0]);
		boolean staticQueues = Boolean.parseBoolean(args[1]);
		boolean bruteForce = Boolean.parseBoolean(args[2]);
		String word = args[3];
		int wordLength = word.length();
		
		Controller c = Controller.getInstance(wordLength);
		
		c.getCaptchaFromString(word);
		HpcAttack hpc = new HpcAttack(c.getShownCaptcha(), threadCount, bruteForce, staticQueues, wordLength);
		hpc.call();
		TimeUnit.MINUTES.sleep(1000);
	}

}
