package com.aaej;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import se.lth.control.*;
import se.lth.control.plot.*;


// TODO: add   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// 		according to http://www.control.lth.se/previouscourse/FRTN01/Exercise4_14/Exercise4.html


/** Class that creates and maintains a GUI for the Ball and Beam ---NOPE, FURUTA process. */
public class FurutaGUI {    

	//private Regul regul;
	//private PIParameters innerPar;
	//private PIDParameters outerPar;
	private int priority;
	//private int mode;

	// Declarartion of main frame.
	private JFrame frame;

	// Declarartion of panels.
	private BoxPanel guiPanel, 					// Main panel holds JPanels plotterPanel and rightPanel 
					plotterPanel,				// Plot panel holds the PlotterPanels measPanel and ctrlPanel
	 				rightPanel,					// Holds estimatorParameterPanel and other buttons.
					ctrlParameterPanel,			// Holds parameters inputs for both controllers
					estimatorParameterPanel;	// Holds estimator parameter inputs

	private PlotterPanel measPanel,
					ctrlPanel;

	// Declaration of components.
	/*private DoubleField innerParHField = new DoubleField(5,3);
	private JButton innerApplyButton;*/

/*	private JRadioButton offModeButton;
	private JRadioButton beamModeButton;
	private JRadioButton ballModeButton;*/
	private JButton stopButton;

	//private boolean hChanged = false;
	//private boolean isInitialized = false;

	/** Constructor. */
	public FurutaGUI(int plotterPriority) {
		priority = plotterPriority;
	}

	/** Starts the threads. */
	/*public void start() {
		measPanel.start();
		ctrlPanel.start();
	}*/

	/** Sets up a reference to Regul. Called by Main. */
	/*public void setRegul(Regul r) {
		regul = r;
	}*/

	/** Creates the GUI. Called from Main. */
	public void initializeGUI() {
		// Create main frame.
		frame = new JFrame("Furuta GUI");

		// Create a panel for the plotters.
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);

		// Create PlotterPanels.
		measPanel = new PlotterPanel(2, priority);
		measPanel.setYAxis(20, -10, 2, 2);
		measPanel.setXAxis(10, 5, 5);
		measPanel.setUpdateFreq(10);
		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(20, -10, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		ctrlPanel.setUpdateFreq(10);

		plotterPanel.add(measPanel);
		plotterPanel.addFixed(10);
		plotterPanel.add(ctrlPanel);

		// -- Panel for the controllers parameters --

			// -- Top controller --
		int qSize = 2; // TODO: Should be read form controller!!
		int rSize = 2; // TODO: Should be read form controller!!

		DoubleField[][] qArrayField = new DoubleField[qSize][qSize];
		DoubleField[][] rArrayField = new DoubleField[qSize][qSize];

		BoxPanel topCtrlPanel, swingCtrlPanel;
		JPanel qFieldPanel, rFieldPanel;
		
		qFieldPanel = new JPanel(new GridLayout(qSize,qSize));
		qFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (int i = 0; i < qSize; i++) {
			for (int j = 0; j < qSize; j++) {
				qArrayField[i][j] = new DoubleField(5,3);
				qFieldPanel.add(qArrayField[i][j]);
			};
		}

		rFieldPanel = new JPanel(new GridLayout(rSize,rSize));
		rFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (int i = 0; i < rSize; i++) {
			for (int j = 0; j < rSize; j++) {
				rArrayField[i][j] = new DoubleField(5,3);
				rFieldPanel.add(rArrayField[i][j]);
			};
		}

		topCtrlPanel = new BoxPanel(BoxPanel.VERTICAL);
		topCtrlPanel.add(new JLabel("Q matrix"));
		topCtrlPanel.add(qFieldPanel);
		topCtrlPanel.add(new JLabel("R matrix"));
		topCtrlPanel.add(rFieldPanel);
		
			// ---------------

			// -- Swing up controller--

		JPanel swingLabelPanel, swingFieldPanel;

		swingLabelPanel = new JPanel();
		swingLabelPanel.setLayout(new GridLayout(0,1));
		swingLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		swingLabelPanel.add(new JLabel("Ellipse radius 1"));
		swingLabelPanel.add(new JLabel("Ellipse radius 2"));
		swingLabelPanel.add(new JLabel("Limit"));
		swingLabelPanel.add(new JLabel("Gain"));
		swingLabelPanel.add(new JLabel("Omega0"));

		DoubleField omega0Field, radius1Field, radius2Field, limitField, gainField;

		omega0Field = new DoubleField(5,3);
		radius1Field = new DoubleField(5,3);
		radius2Field = new DoubleField(5,3);
		limitField = new DoubleField(5,3);
		gainField = new DoubleField(5,3);

		swingFieldPanel = new JPanel();
		swingFieldPanel.setLayout(new GridLayout(0,1));
		swingFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);		
		swingFieldPanel.add(radius1Field);
		swingFieldPanel.add(radius2Field);
		swingFieldPanel.add(limitField);
		swingFieldPanel.add(gainField);
		swingFieldPanel.add(omega0Field);


		swingCtrlPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		swingCtrlPanel.add(swingLabelPanel);
		swingCtrlPanel.add(swingFieldPanel);

			// ---------------

		JButton saveCtrlButton;
		saveCtrlButton = new JButton("Save");

		ctrlParameterPanel = new BoxPanel(BoxPanel.VERTICAL);

		JLabel tst = new JLabel("Top controller");
		tst.setHorizontalAlignment(SwingConstants.CENTER);
		ctrlParameterPanel.add(tst);
		ctrlParameterPanel.add(topCtrlPanel);
		ctrlParameterPanel.add(new JLabel("Swing up controller"));
		ctrlParameterPanel.add(swingCtrlPanel);
		ctrlParameterPanel.add(saveCtrlButton);

		// ------------


		// -- Panel for the estimator parameters --

		DoubleField lambdaField, p0Field, theta0Field;

		lambdaField = new DoubleField(5,3);
		p0Field = new DoubleField(5,3);
		theta0Field = new DoubleField(5,3);

		JPanel estimatorLabelPanel, estimatorFieldPanel;
		BoxPanel estimatorButtonsPanel, estimatorGridPanel;

		estimatorLabelPanel = new JPanel();
		estimatorLabelPanel.setLayout(new GridLayout(0,1));
		estimatorLabelPanel.add(new JLabel("lambda: "));
		estimatorLabelPanel.add(new JLabel("P0: "));
		estimatorLabelPanel.add(new JLabel("Theta0: "));

		estimatorFieldPanel = new JPanel();
		estimatorFieldPanel.setLayout(new GridLayout(0,1));
		estimatorFieldPanel.add(lambdaField);
		estimatorFieldPanel.add(p0Field);
		estimatorFieldPanel.add(theta0Field);

		estimatorGridPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		estimatorGridPanel.add(estimatorLabelPanel);
		estimatorGridPanel.addGlue();
		estimatorGridPanel.add(estimatorFieldPanel);

		JButton resetEstimatorButton, saveEstimatorButton;
		saveEstimatorButton = new JButton("Save");
		resetEstimatorButton = new JButton("Reset");

		estimatorButtonsPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		estimatorButtonsPanel.add(resetEstimatorButton);
		estimatorButtonsPanel.add(saveEstimatorButton);

		estimatorParameterPanel = new BoxPanel(BoxPanel.VERTICAL);
		estimatorParameterPanel.add(estimatorGridPanel);
		estimatorParameterPanel.add(estimatorButtonsPanel);
		


		// ------------

		// -- Panel for start and stop buttons --

		JButton startButton;
		JPanel buttonPanel;

		startButton = new JButton("START");
		stopButton = new JButton("STOP");

		buttonPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
		                   BorderFactory.createLineBorder(Color.red),
		                   buttonPanel.getBorder()));

		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		startButton.setHorizontalAlignment(SwingConstants.LEFT);

		// ------------


		// Create panel holding everything but the plotters.
		rightPanel = new BoxPanel(BoxPanel.VERTICAL);
		//rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(new JLabel("Controller parameters"));
		rightPanel.add(ctrlParameterPanel);//, BorderLayout.NORTH);
		//rightPanel.addFixed(10);
		rightPanel.add(new JLabel("Estimator Parameters"));
		rightPanel.add(estimatorParameterPanel);//, BorderLayout.CENTER);
		//rightPanel.addFixed(10);
		rightPanel.add(buttonPanel);//, BorderLayout.SOUTH);


		// Create panel for the entire GUI.
		guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		guiPanel.add(plotterPanel);
		//guiPanel.addGlue();
		guiPanel.add(rightPanel);

		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();


		/*
		// Get initial parameters from Regul
		innerPar = regul.getInnerParameters();
		outerPar = regul.getOuterParameters();

		// Create panels for the parameter fields and labels, add labels and fields 
		innerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		innerParLabelPanel = new JPanel();
		innerParLabelPanel.setLayout(new GridLayout(0,1));
		innerParLabelPanel.add(new JLabel("K: "));
		innerParLabelPanel.add(new JLabel("Ti: "));
		innerParLabelPanel.add(new JLabel("Tr: "));
		innerParLabelPanel.add(new JLabel("Beta: "));
		innerParLabelPanel.add(new JLabel("h: "));
		innerParFieldPanel = new JPanel();
		innerParFieldPanel.setLayout(new GridLayout(0,1));
		innerParFieldPanel.add(innerParKField); 
		innerParFieldPanel.add(innerParTiField);
		innerParFieldPanel.add(innerParTrField);
		innerParFieldPanel.add(innerParBetaField);
		innerParFieldPanel.add(innerParHField);

		// Set initial parameter values of the fields
		innerParKField.setValue(innerPar.K);
		innerParTiField.setValue(innerPar.Ti);
		innerParTiField.setMinimum(-eps);
		innerParTrField.setValue(innerPar.Tr);
		innerParTrField.setMinimum(-eps);
		innerParBetaField.setValue(innerPar.Beta);
		innerParBetaField.setMinimum(-eps);
		innerParHField.setValue(innerPar.H);
		innerParHField.setMinimum(-eps);
		*/
		// Add action listeners to the fields
		/*innerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.K = innerParKField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});*/

		/*
		innerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Ti = innerParTiField.getValue();
				if (innerPar.Ti < eps) {
					innerPar.integratorOn = false;
				}
				else {
					innerPar.integratorOn = true;
				}
				innerApplyButton.setEnabled(true);
			}
		});
		innerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Tr = innerParTrField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Beta = innerParBetaField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.H = innerParHField.getValue();
				outerPar.H = innerPar.H;
				outerParHField.setValue(innerPar.H);
				innerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		// Add label and field panels to parameter panel
		innerParPanel.add(innerParLabelPanel);
		innerParPanel.addGlue();
		innerParPanel.add(innerParFieldPanel);
		innerParPanel.addFixed(10);

		// Create apply button and action listener.
		innerApplyButton = new JButton("Apply");
		innerApplyButton.setEnabled(false);
		innerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setInnerParameters(innerPar);
				if (hChanged) {
					regul.setOuterParameters(outerPar);
				}	
				hChanged = false;
				innerApplyButton.setEnabled(false);
			}
		});

		// Create panel with border to hold apply button and parameter panel
		BoxPanel innerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		innerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Inner Parameters"));
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerParPanel);
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerApplyButton);

		// The same as above for the outer parameters
		outerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		outerParLabelPanel = new JPanel();
		outerParLabelPanel.setLayout(new GridLayout(0,1));
		outerParLabelPanel.add(new JLabel("K: "));
		outerParLabelPanel.add(new JLabel("Ti: "));
		outerParLabelPanel.add(new JLabel("Td: "));
		outerParLabelPanel.add(new JLabel("N: "));
		outerParLabelPanel.add(new JLabel("Tr: "));
		outerParLabelPanel.add(new JLabel("Beta: "));
		outerParLabelPanel.add(new JLabel("h: "));

		outerParFieldPanel = new JPanel();
		outerParFieldPanel.setLayout(new GridLayout(0,1));
		outerParFieldPanel.add(outerParKField); 
		outerParFieldPanel.add(outerParTiField);
		outerParFieldPanel.add(outerParTdField);
		outerParFieldPanel.add(outerParNField);
		outerParFieldPanel.add(outerParTrField);
		outerParFieldPanel.add(outerParBetaField);
		outerParFieldPanel.add(outerParHField);
		outerParKField.setValue(outerPar.K);
		outerParTiField.setValue(outerPar.Ti);
		outerParTiField.setMinimum(-eps);
		outerParTdField.setValue(outerPar.Td);
		outerParTdField.setMinimum(-eps);
		outerParNField.setValue(outerPar.N);
		outerParTrField.setValue(outerPar.Tr);
		outerParBetaField.setValue(outerPar.Beta);
		outerParBetaField.setMinimum(-eps);
		outerParHField.setValue(outerPar.H);
		outerParHField.setMinimum(-eps);
		outerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.K = outerParKField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Ti = outerParTiField.getValue();
				if (outerPar.Ti < eps) {
					outerPar.integratorOn = false;
				}
				else {
					outerPar.integratorOn = true;
				}
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Td = outerParTdField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParNField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.N = outerParNField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Tr = outerParTrField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Beta = outerParBetaField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.H = outerParHField.getValue();
				innerPar.H = outerPar.H;
				innerParHField.setValue(outerPar.H);
				outerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		outerParPanel.add(outerParLabelPanel);
		outerParPanel.addGlue();
		outerParPanel.add(outerParFieldPanel);
		outerParPanel.addFixed(10);

		outerApplyButton = new JButton("Apply");
		outerApplyButton.setEnabled(false);
		outerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOuterParameters(outerPar);
				if (hChanged) {
					regul.setInnerParameters(innerPar);
				}	
				hChanged = false;
				outerApplyButton.setEnabled(false);
			}
		});

		BoxPanel outerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		outerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Outer Parameters"));
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerParPanel);
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerApplyButton);

		// Create panel for parameter fields, labels and apply buttons
		parPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		parPanel.add(innerParButtonPanel);
		parPanel.addGlue();
		parPanel.add(outerParButtonPanel);

		// Create panel for the radio buttons.
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		// Create the buttons.
		offModeButton = new JRadioButton("OFF");
		beamModeButton = new JRadioButton("BEAM");
		ballModeButton = new JRadioButton("BALL");*/
		
		/*// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(offModeButton);
		group.add(beamModeButton);
		group.add(ballModeButton);
		// Button action listeners.
		offModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOFFMode();
			}
		});
		beamModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBEAMMode();
			}
		});
		ballModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBALLMode();
			}
		});
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				System.exit(0);
			}
		});*/

		/*// Add buttons to button panel.
		buttonPanel.add(offModeButton, BorderLayout.NORTH);
		buttonPanel.add(beamModeButton, BorderLayout.CENTER);
		buttonPanel.add(ballModeButton, BorderLayout.SOUTH);*/



		// Select initial mode.
		/*mode = regul.getMode();
		switch (mode) {
		case OFF:
			offModeButton.setSelected(true);
			break;
		case BEAM:
			beamModeButton.setSelected(true);
			break;
		case BALL:
			ballModeButton.setSelected(true);
		}*/


		
		
		/*
		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				regul.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				System.exit(0);
			}
		});*/
		
		/*

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		frame.setLocation((sd.width-fd.width)/2, (sd.height-fd.height)/2);
		*/
		// Make the window visible.
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		
		//isInitialized = true;
	}

	/** Called by Regul to plot a control signal data point. */
	/*public synchronized void putControlDataPoint(DoublePoint dp) {
		if (isInitialized) {
			ctrlPanel.putData(dp.x, dp.y);
		} else {
			DebugPrint("Note: GUI not yet initialized. Ignoring call to putControlDataPoint().");
		}
	}*/

	/** Called by Regul to plot a measurement data point. */
	/*public synchronized void putMeasurementDataPoint(PlotData pd) {
		if (isInitialized) {
			measPanel.putData(pd.x, pd.yref, pd.y);
		} else {
			DebugPrint("Note: GUI not yet initialized. Ignoring call to putMeasurementDataPoint().");
		}
	}*/
	
	public static void debug(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
        debug("Starting...");
        FurutaGUI furutaGUI = new FurutaGUI(5);
        furutaGUI.initializeGUI();
	}
}