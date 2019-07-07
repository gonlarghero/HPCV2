package presentacion;


 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import logica.Controller;
import logica.HpcAttack;

import java.beans.*;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
public class window extends JPanel
                             implements ActionListener, 
                                        PropertyChangeListener {
 
	private static final long serialVersionUID = -8777720618293516863L;
	private JProgressBar progressBar;
    private JButton startButton;
    private Task task;
    private JButton btnRefresh;
    private Controller c = Controller.getInstance();
    private JLabel imageLabel;
    private HpcAttack atacante;
    private Future<String> result;
    private JTextField textField;
    
    class Task extends SwingWorker<Void, Void> {
    	ExecutorService servicio;
    	String ret;
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, ExecutionException {
            Random random = new Random();           
            int progress = 0;
            servicio = Executors.newFixedThreadPool(1);
            result = servicio.submit(atacante);
            //Initialize progress property.
            setProgress(0);
            while (progress < 100 && !result.isDone()) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress = atacante.progress();
                setProgress(Math.min(progress, 100));
            }
            ret = result.get();
            return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            servicio.shutdown();
            if (ret.equals("word not found")) {
            	ret += " \n ¿desea atacar por fuerza bruta?";
            	int reply = JOptionPane.showConfirmDialog(null, ret, "Resultado", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                  JOptionPane.showMessageDialog(null, "FUERZA BRUTA :v");
                }
            }else {
            	JOptionPane.showMessageDialog(null, ret, "Resultado", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
 
    public window() {
        super(new BorderLayout());
        //Create the demo's UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
 
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
 
        JPanel panel = new JPanel();
        JPanel imagePanel = new JPanel();
        
        imageLabel = new JLabel("");
        imageLabel.setIcon(new ImageIcon(this.getClass().getResource("/descarga.png")));
                 
        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String t = textField.getText();
        		if(t == null || t.trim().equals("") || t.matches("-?\\d+")) {
        			imageLabel.setIcon(new ImageIcon(c.getRandomCaptcha()));
        		}else {
        			imageLabel.setIcon(new ImageIcon(c.getCaptchaFromString(t)));
        		}
        	}
        });
        
        panel.add(btnRefresh);
        panel.add(startButton);
        
        textField = new JTextField();
        panel.add(textField);
        textField.setColumns(10);
        panel.add(progressBar);
        imagePanel.add(imageLabel);
 
        add(panel, BorderLayout.PAGE_START);        
        add(imagePanel, BorderLayout.CENTER);
       
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
     }
 
    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        Integer threads;
        if (textField.getText() != null && textField.getText().matches("-?\\d+")) {
        	threads = Integer.parseInt(textField.getText());
        }else{
        	threads = 3;
        }
        try {        	
			atacante = new HpcAttack(c.getShownCaptcha(),threads,false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();       
		
    }
 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            
        } 
    }
 
 
    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressBarDemo");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = new window();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}