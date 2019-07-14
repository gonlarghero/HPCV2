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
 
public class window extends JPanel implements ActionListener,PropertyChangeListener {
 
	private static final long serialVersionUID = -8777720618293516863L;
	private JProgressBar progressBar;
    private JButton startButton;
    private Task task;
    private JButton btnRefresh;
    private Controller c;
    private JLabel imageLabel;
    private HpcAttack atacante;
    private JTextField textField;
    private JComboBox<String> comboBox;
    
    class Task extends SwingWorker<Void, Void> {
    	ExecutorService servicio;
    	String ret;
    	Future<String> result;
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
            JOptionPane.showMessageDialog(null, ret, "Resultado", JOptionPane.INFORMATION_MESSAGE);
        }
    }
 
    public window() {    	
    	
        super(new BorderLayout());
        
        try {
			c = Controller.getInstance();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        //Create the demo's UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
 
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GRAY);
 
        JPanel panel = new JPanel();
        JPanel imagePanel = new JPanel();
        
        imageLabel = new JLabel("");
        imageLabel.setIcon(new ImageIcon(c.getRandomCaptcha()));
        
        comboBox = new JComboBox<String>();
        comboBox.addItem("Diccionario");
        comboBox.addItem("Fuerza Bruta");
        panel.add(comboBox);
        panel.add(startButton);
        
        textField = new JTextField();
        panel.add(textField);
        textField.setColumns(10);
        panel.add(progressBar);
        imagePanel.add(imageLabel);
 
        add(panel, BorderLayout.PAGE_START);        
        add(imagePanel, BorderLayout.CENTER);
       
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        btnRefresh = new JButton("Refresh");
        add(btnRefresh, BorderLayout.SOUTH);
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
     }
 
    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        Integer threads;
        Boolean fb;
        progressBar.setValue(0);
        if (comboBox.getSelectedItem().equals("Diccionario")) {
        	progressBar.setForeground(Color.GRAY);
        	fb = false;
        }else {
        	progressBar.setForeground(Color.RED);
        	fb = true;
        }        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        
        if (textField.getText() != null && textField.getText().matches("-?\\d+")) {
        	threads = Integer.parseInt(textField.getText());
        }else{
        	threads = 3;
        }
        try {        	
			atacante = new HpcAttack(c.getShownCaptcha(),threads,fb, false);
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