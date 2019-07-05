package logica;

import java.awt.Image;
import java.awt.image.BufferedImage;


public class Controller{
	
	private BufferedImage shownCaptcha;
	private static Controller instance;
	
	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}
	
	private Controller() {
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
	
	/*public String attack(Integer threads) throws IOException {
		HpcAttack atacante = new HpcAttack();
		return atacante.attack(shownCaptcha,threads);
	}*/

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

}