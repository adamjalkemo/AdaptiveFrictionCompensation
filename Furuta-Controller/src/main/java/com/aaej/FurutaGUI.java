package com.aaej;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import se.lth.control.*;
import se.lth.control.plot.*;


// TODO: add frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// 		according to http://www.control.lth.se/previouscourse/FRTN01/Exercise4_14/Exercise4.html
//		although windowClosing used below might be enough.



//Class that creates and maintains a GUI for the FURUTA process.
public class FurutaGUI {
	private final static Logger LOGGER = Logger.getLogger(FurutaGUI.class.getName());
	private MainController controller;
	private ControllerParameters ctrlPar;
	private RLSParameters rlsPar;
	private int priority;

	// Declaration of main frame.
	private JFrame frame;

	// Declaration of panels.
	private BoxPanel 		guiPanel, plotterPanel, rightPanel, ctrlParameterPanel,estimatorParameterPanel,
							lowerLeftPlotPanel, lowerRightPlotPanel, lowerPlotPanels, generalCtrlPanel, estimatorButtonsPanel,
							estimatorGridPanel, regressorPanel, topCtrlPanel, swingCtrlPanel, buttonPanel;
	private PlotterPanel 	measPanel, ctrlPanel, rlsPanel;
	private JPanel 			qFieldPanel, rFieldPanel, swingLabelPanel, swingFieldPanel, generalLabelPanel,
							generalFieldPanel, estimatorLabelPanel, estimatorFieldPanel;

	// Declaration of buttons and fields
	private JButton 		startButton, stopButton, resetEstimatorButton, saveEstimatorButton, saveCtrlButton;
	private DoubleField 	omega0Field, hField, radius1Field, radius2Field, limitField, gainField,
							lambdaField, p0Field, theta00Field, theta01Field;
	private DoubleField[][] qArrayField, rArrayField;

	// Width of right column. Real strange behaviour. However, this works for now.
	int width =  20000;
			
	//private boolean hChanged = false;
	private boolean isInitialized = false;

	/** Constructor. */
	public FurutaGUI(int plotterPriority) {
		priority = plotterPriority;
	}

	/** Starts the threads. */
	public void start() {
		measPanel.start();
		ctrlPanel.start();
		rlsPanel.start();
	}


	public void setController(MainController controller) {
		this.controller = controller;
	}
	/** Creates the GUI. Called from Main. */
	public void initializeGUI() {
		// Get initial parameters from Regul
		ctrlPar = controller.getControllerParameters();
		rlsPar = controller.getRLSParameters();

		// Create main frame.
		frame = new JFrame("Furuta GUI");

		// -- Panel for the plotters --
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		plotterPanel.setPreferredSize(new Dimension(1000,1000));

		// Create PlotterPanels.
		measPanel = new PlotterPanel(4, priority);
		measPanel.setYAxis(20, -10, 2, 2);
		measPanel.setXAxis(10, 5, 5);
		measPanel.setUpdateFreq(1);

		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(20, -10, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		ctrlPanel.setUpdateFreq(10);

		lowerLeftPlotPanel = new BoxPanel(BoxPanel.VERTICAL);
		lowerLeftPlotPanel.add(new JLabel("u, f, u+f"));
		lowerLeftPlotPanel.add(ctrlPanel);
		
		rlsPanel = new PlotterPanel(1, priority);
		rlsPanel.setYAxis(20, -10, 2, 2);
		rlsPanel.setXAxis(10, 5, 5);
		rlsPanel.setUpdateFreq(10);

		lowerRightPlotPanel = new BoxPanel(BoxPanel.VERTICAL);
		lowerRightPlotPanel.add(new JLabel("RLS"));
		lowerRightPlotPanel.add(rlsPanel);

		lowerPlotPanels = new BoxPanel(BoxPanel.HORIZONTAL);
		lowerPlotPanels.add(lowerLeftPlotPanel);
		lowerPlotPanels.add(lowerRightPlotPanel);

		plotterPanel.addFixed(10);
		plotterPanel.add(new JLabel("y"));
		plotterPanel.add(measPanel);
		plotterPanel.addFixed(10);
		plotterPanel.add(lowerPlotPanels);
		plotterPanel.addFixed(10);

		// ----------------

		// -- Panel for the controllers parameters --

			// -- Top controller --
		int qSize = ctrlPar.qMatrix.length;
		int rSize = ctrlPar.rMatrix.length;

		qArrayField = new DoubleField[qSize][qSize];
		rArrayField = new DoubleField[qSize][qSize];		
		
		qFieldPanel = new JPanel(new GridLayout(qSize,qSize));
		qFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (int i = 0; i < qSize; i++) {
			for (int j = 0; j < qSize; j++) {
				qArrayField[i][j] = new DoubleField(5,3);
				qArrayField[i][j].setValue(ctrlPar.qMatrix[i][j]);
				qArrayField[i][j].addActionListener(new ActionListener() {
					int i,j;
					public void actionPerformed(ActionEvent e) {
						this.i = i;
						this.j = j;
						ctrlPar.qMatrix[i][j] = qArrayField[i][j].getValue();
						saveCtrlButton.setEnabled(true);
					}
				});
				qFieldPanel.add(qArrayField[i][j]);
			};
		}

		rFieldPanel = new JPanel(new GridLayout(rSize,rSize));
		rFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (int i = 0; i < rSize; i++) {
			for (int j = 0; j < rSize; j++) {
				rArrayField[i][j] = new DoubleField(5,3);
				rArrayField[i][j].setValue(ctrlPar.rMatrix[i][j]);
				rArrayField[i][j].addActionListener(new ActionListener() {
					int i,j;
					public void actionPerformed(ActionEvent e) {
						this.i = i;
						this.j = j;
						ctrlPar.rMatrix[i][j] = rArrayField[i][j].getValue();
						saveCtrlButton.setEnabled(true);
					}
				});
				rFieldPanel.add(rArrayField[i][j]);
			};
		}

		topCtrlPanel = new BoxPanel(BoxPanel.VERTICAL);
		topCtrlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topCtrlPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		topCtrlPanel.add(new JLabel("Q matrix"));
		topCtrlPanel.add(qFieldPanel);
		topCtrlPanel.add(new JLabel("R matrix"));
		topCtrlPanel.add(rFieldPanel);
		
			// ---------------

			// -- Swing up controller--

		swingLabelPanel = new JPanel();
		swingLabelPanel.setLayout(new GridLayout(0,1));
		swingLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		swingLabelPanel.add(new JLabel("Ellipse radius 1"));
		swingLabelPanel.add(new JLabel("Ellipse radius 2"));
		swingLabelPanel.add(new JLabel("Limit"));
		swingLabelPanel.add(new JLabel("Gain"));


		radius1Field = new DoubleField(5,3);
		radius1Field.setValue(ctrlPar.ellipseRadius1);
		radius1Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ellipseRadius1 = radius1Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		radius2Field = new DoubleField(5,3);
		radius2Field.setValue(ctrlPar.ellipseRadius2);
		radius2Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ellipseRadius2 = radius2Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		limitField = new DoubleField(5,3);
		limitField.setValue(ctrlPar.limit);
		limitField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.limit = limitField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		gainField = new DoubleField(5,3);
		gainField.setValue(ctrlPar.gain);
		gainField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.gain = gainField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		swingFieldPanel = new JPanel();
		swingFieldPanel.setLayout(new GridLayout(0,1));
		swingFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);		
		swingFieldPanel.add(radius1Field);
		swingFieldPanel.add(radius2Field);
		swingFieldPanel.add(limitField);
		swingFieldPanel.add(gainField);


		swingCtrlPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		swingCtrlPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		swingCtrlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		swingCtrlPanel.add(swingLabelPanel);
		swingCtrlPanel.add(swingFieldPanel);

			// ---------------

			// -- General controller --

		generalLabelPanel = new JPanel(new GridLayout(0,1));
		generalLabelPanel.add(new JLabel("Sampling time h"));
		generalLabelPanel.add(new JLabel("ω0"));

		omega0Field = new DoubleField(5,3);
		omega0Field.setValue(ctrlPar.omega0);
		omega0Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.omega0 = omega0Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		hField = new DoubleField(5,3);
		hField.setValue(ctrlPar.h);
		hField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.h = (long)hField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		generalFieldPanel = new JPanel(new GridLayout(0,1));
		generalFieldPanel.add(hField);
		generalFieldPanel.add(omega0Field);

		generalCtrlPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		generalCtrlPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		generalCtrlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalCtrlPanel.add(generalLabelPanel);
		generalCtrlPanel.add(generalFieldPanel);


		// --------------


		saveCtrlButton = new JButton("Save");
		saveCtrlButton.setEnabled(false);
		saveCtrlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setControllerParameters(ctrlPar);
				saveCtrlButton.setEnabled(false);
			}
		});


		ctrlParameterPanel = new BoxPanel(BoxPanel.VERTICAL);
		ctrlParameterPanel.setBorder(BorderFactory.createTitledBorder(formatLabel("Controller parameters", true, 4, "#000033")));
		ctrlParameterPanel.add(new JLabel(formatLabel("Top controller", true, 2, "#444444")));
		ctrlParameterPanel.add(topCtrlPanel);
		ctrlParameterPanel.addFixed(5);
		ctrlParameterPanel.add(new JLabel(formatLabel("Swing up controller", true, 2, "#444444")));
		ctrlParameterPanel.add(swingCtrlPanel);
		ctrlParameterPanel.addFixed(5);
		ctrlParameterPanel.add(new JLabel(formatLabel("General controller settings", true, 2, "#444444")));
		ctrlParameterPanel.add(generalCtrlPanel);
		ctrlParameterPanel.add(saveCtrlButton);

		// ------------


		// -- Panel for the estimator parameters --

		String[] regressorModels = {"Coloumb friction [ sign(v) ]", "Viscous friction [ sign(v), v ]"};
		JComboBox regressorCombo = new JComboBox(regressorModels);
		regressorCombo.setSelectedIndex(rlsPar.regressorModel);

		regressorCombo.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        JComboBox cb = (JComboBox)e.getSource();
		        int regressorModel = cb.getSelectedIndex();
		        rlsPar.regressorModel = regressorModel;
		        theta00Field.setValue(rlsPar.theta0[regressorModel][0]);
		        if (regressorModel == 0)
		        	theta01Field.setVisible(false);
		        else {
		        	theta01Field.setVisible(true);
		        	theta01Field.setValue(rlsPar.theta0[regressorModel][1]);
		        }
		        saveEstimatorButton.setEnabled(true);
		    }
		});


		regressorPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		regressorPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		regressorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		regressorPanel.add(new JLabel("Model"));
		regressorPanel.add(regressorCombo);


		estimatorLabelPanel = new JPanel();
		estimatorLabelPanel.setLayout(new GridLayout(0,1));
		estimatorLabelPanel.add(new JLabel("lambda: "));
		estimatorLabelPanel.add(new JLabel("P0: "));
		estimatorLabelPanel.add(new JLabel("θ0: "));


		lambdaField = new DoubleField(5,3);
		lambdaField.setValue(rlsPar.lambda);
		lambdaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.lambda = lambdaField.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		p0Field = new DoubleField(5,3);
		p0Field.setValue(rlsPar.p0);
		p0Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.p0 = p0Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		BoxPanel theta0Panel;

		theta00Field = new DoubleField(5,3);
		theta00Field.setValue(rlsPar.theta0[rlsPar.regressorModel][0]);
		theta00Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][0] = theta00Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		theta01Field = new DoubleField(5,3);
		theta01Field.setValue(rlsPar.theta0[rlsPar.regressorModel][1]);
		theta01Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][1] = theta01Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		theta0Panel = new BoxPanel(BoxPanel.HORIZONTAL);
		theta0Panel.add(theta00Field);
		theta0Panel.add(theta01Field);

		estimatorFieldPanel = new JPanel();
		estimatorFieldPanel.setLayout(new GridLayout(0,1));
		estimatorFieldPanel.add(lambdaField);
		estimatorFieldPanel.add(p0Field);
		estimatorFieldPanel.add(theta0Panel);

		estimatorGridPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		estimatorGridPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		estimatorGridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		estimatorGridPanel.add(estimatorLabelPanel);
		estimatorGridPanel.add(estimatorFieldPanel);

		saveEstimatorButton = new JButton("Save");
		saveEstimatorButton.setEnabled(false);
		saveEstimatorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setRLSParameters(rlsPar);
				saveEstimatorButton.setEnabled(false);
			}
		});

		resetEstimatorButton = new JButton("Reset estimation");
		resetEstimatorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.resetEstimator();
			}
		});

		estimatorButtonsPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		estimatorButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		estimatorButtonsPanel.add(saveEstimatorButton);
		estimatorButtonsPanel.addFixed(5);
		estimatorButtonsPanel.add(resetEstimatorButton);


		estimatorParameterPanel = new BoxPanel(BoxPanel.VERTICAL);
		estimatorParameterPanel.setBorder(BorderFactory.createTitledBorder(formatLabel("Estimator Parameters", true, 4, "#000033")));
		estimatorParameterPanel.add(regressorPanel);
		estimatorParameterPanel.add(estimatorGridPanel);
		estimatorParameterPanel.add(estimatorButtonsPanel);
		
		// ------------


		// -- Panel for start and stop buttons --

		startButton = new JButton("START");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.regulatorActive(true);
			}
		});

		stopButton = new JButton("STOP");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.regulatorActive(false);
			}
		});

		buttonPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// ------------

		// Create panel holding everything but the plotters.
		rightPanel = new BoxPanel(BoxPanel.VERTICAL);
		rightPanel.addFixed(10);

		rightPanel.add(ctrlParameterPanel);
		rightPanel.addFixed(10);
		rightPanel.add(estimatorParameterPanel);
		rightPanel.addFixed(10);
		rightPanel.add(buttonPanel);
		rightPanel.addFixed(10);


		/* If the right panel does not fit, a scoll pane can be used. This messes with the layout though.
		JScrollPane jScrollPane;
		jScrollPane = new JScrollPane(rightPanel);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		*/

		// Create panel for the entire GUI.
		guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		guiPanel.addFixed(10);
		guiPanel.add(plotterPanel);
		guiPanel.addFixed(10);
		guiPanel.add(rightPanel);
		guiPanel.addFixed(10);


		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		
		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.shutDown();
				/*measPanel.stopThread(); // THIS CRASHES ??
				ctrlPanel.stopThread();
				rlsPanel.stopThread();*/
				System.exit(0);
			}
		});
		
		/*

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		frame.setLocation((sd.width-fd.width)/2, (sd.height-fd.height)/2);
		*/
		
		// Make the window visible.
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		
		isInitialized = true;
	}

	private String formatLabel(String str, boolean bold, Integer size, String color) {
		if (bold)
			str = "<b>" + str + "</b>";
		if ((size != null) || color !=null) {
			str = "<font" + (size != null ? " size='" + size + "'" : "") + (color != null ? " color='" + color + "'" : "") + ">" + str + "</font>";
		}

		return "<html>" + str + "</html>";
	}

	/** Called by Regul to plot a control signal data point. */
	public synchronized void putControlDataPoint(double t, double u) { // Consider using a modified PlotData here.
		if (isInitialized) {
			ctrlPanel.putData(t, u);
		} else {
			LOGGER.log(Level.FINE, "Note: GUI not yet initialized. Ignoring call to putControlDataPoint().");
		}
	}

	/** Called by Regul to plot a rls data point. */
	public synchronized void putRLSDataPoint(DoublePoint dp) { // Consider using a modified PlotData here.
		if (isInitialized) {
			rlsPanel.putData(dp.x, dp.y);
		} else {
			LOGGER.log(Level.FINE,"Note: GUI not yet initialized. Ignoring call to putRLSDataPoint().");
		}
	}

	/** Called by Regul to plot a measurement data point. */
	public synchronized void putMeasurementDataPoint(double t, double y1, double y2, double y3, double y4) {
		if (isInitialized) {
			measPanel.putData(t, y1, y2, y3, y4);
		} else {
			LOGGER.log(Level.FINE, "Note: GUI not yet initialized. Ignoring call to putMeasurementDataPoint().");
		}
	}
	

	public static void main(String[] args) {
        LOGGER.log(Level.FINE, "Starting...");
        FurutaGUI furutaGUI = new FurutaGUI(5);
        furutaGUI.initializeGUI();
	}
}
