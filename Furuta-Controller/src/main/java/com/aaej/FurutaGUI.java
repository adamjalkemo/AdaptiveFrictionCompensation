package com.aaej;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import se.lth.control.*;
import se.lth.control.plot.*;

import java.util.Observer;
import java.util.Observable;


/**
 * Swing based GUI for the furuta pendulum process.
 */
public class FurutaGUI implements Observer {
	private final static Logger LOGGER = Logger.getLogger(FurutaGUI.class.getName());
	private MainController controller;
	private CommunicationManager communicationManager;
	private ControllerParameters ctrlPar;
	private RLSParameters rlsPar;
	private SpecificTests specificTests;
	private int priority;

	// Declaration of main frame.
	private JFrame frame;

	// Declaration of panels.
	private BoxPanel 		guiPanel, plotterPanel, rightPanel, ctrlParameterPanel,estimatorParameterPanel,
							lowerLeftPlotPanel, lowerRightPlotPanel, lowerPlotPanels, generalCtrlPanel, estimatorButtonsPanel,
							estimatorGridPanel, regressorPanel, topCtrlPanel, swingCtrlPanel, buttonPanel, buttonPanel2,
							buttonPanel3, buttonPanel4, deadzonePanel, tiPanel;
	private PlotterPanel 	measPanel, ctrlPanel, rlsPanel;
	private JPanel 			qFieldPanel, rFieldPanel, swingLabelPanel, swingFieldPanel, generalLabelPanel,
							generalFieldPanel, estimatorLabelPanel, estimatorFieldPanel, deadzoneLabelPanel, deadzoneFieldPanel,
							tiLabelPanel, tiFieldPanel;

	// Declaration of buttons and fields
	private JButton 		startButton, stopButton, resetEstimatorButton, saveEstimatorButton, saveCtrlButton;
	private JButton			resetOffsetButton;
	private JButton			frictionCompensatorOnButton, frictionCompensatorOffButton;
	private JButton         rlsConvergeTestButton, stepResponseTestButton, saveTestDataButton, stopSaveTestDataButton;
	private DoubleField 	omega0Field, hField, radius1Field, radius2Field, limitField, gainField, ellipseRotationField,
							lambdaField, theta00Field, theta01Field, theta02Field, deadzoneBaseAngVelField,
							deadzonePendAngVelField, deadzoneBaseAngField, deadzonePendAngField, tiField;
	private DoubleField[][] qArrayField, rArrayField;
	private JLabel 			currentController;
	private JScrollPane 	rightPanelWithScroll;

	// Width of right column
	int width =  320;

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
	public void setCommunicationManager(CommunicationManager communicationManager) {
		this.communicationManager = communicationManager;
	}
	/** Creates the GUI. Called from App. */
	public void initializeGUI() {
		// Get initial parameters from Regul
		ctrlPar = controller.getControllerParameters();
		rlsPar = controller.getRLSParameters();
		specificTests = new SpecificTests(communicationManager, controller);

		// Create main frame.
		frame = new JFrame("Furuta GUI");

		// -- Panel for the plotters --
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		plotterPanel.setPreferredSize(new Dimension(1000,1000));

		// Create PlotterPanels.
		measPanel = new PlotterPanel(4, priority);
		measPanel.setYAxis(1, -0.5, 2, 2);
		measPanel.setXAxis(20, 5, 5);
		measPanel.setUpdateFreq(1);

		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(4, -2, 2, 2);
		ctrlPanel.setXAxis(20, 5, 5);
		ctrlPanel.setUpdateFreq(10);

		lowerLeftPlotPanel = new BoxPanel(BoxPanel.VERTICAL);
		lowerLeftPlotPanel.add(new JLabel("u"));
		lowerLeftPlotPanel.add(ctrlPanel);
		
		rlsPanel = new PlotterPanel(3, priority);
		rlsPanel.setYAxis(0.6, -0.3, 2, 2);
		rlsPanel.setXAxis(50, 5, 5);
		rlsPanel.setUpdateFreq(10);

		lowerRightPlotPanel = new BoxPanel(BoxPanel.VERTICAL);
		lowerRightPlotPanel.add(new JLabel("<html><font color='red'>Coulomb</font>, <font color='black'>viscous</font>, <font color='lime'>offset</font></html>"));
		lowerRightPlotPanel.add(rlsPanel);

		lowerPlotPanels = new BoxPanel(BoxPanel.HORIZONTAL);
		lowerPlotPanels.add(lowerLeftPlotPanel);
		lowerPlotPanels.add(lowerRightPlotPanel);

		plotterPanel.addFixed(10);
		plotterPanel.add(new JLabel("<html><font color='blue'>Theta</font>, <font color='red'>Theta vel.</font>, <font color='lime'>Phi</font>, <font color='black'>Phi vel.</font></html>"));
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
				qArrayField[i][j] = new DoubleField(10,6);
				qArrayField[i][j].setValue(ctrlPar.qMatrix[i][j]);
				qArrayField[i][j].putClientProperty("i", (Integer) i);
				qArrayField[i][j].putClientProperty("j", (Integer) j);
				qArrayField[i][j].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int i = (Integer)((DoubleField)e.getSource()).getClientProperty("i");
						int j = (Integer)((DoubleField)e.getSource()).getClientProperty("j");
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
				rArrayField[i][j] = new DoubleField(10,6);
				rArrayField[i][j].setValue(ctrlPar.rMatrix[i][j]);
				rArrayField[i][j].putClientProperty("i", (Integer) i);
				rArrayField[i][j].putClientProperty("j", (Integer) j);
				rArrayField[i][j].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int i = (Integer)((DoubleField)e.getSource()).getClientProperty("i");
						int j = (Integer)((DoubleField)e.getSource()).getClientProperty("j");
						ctrlPar.rMatrix[i][j] = rArrayField[i][j].getValue();
						saveCtrlButton.setEnabled(true);
					}
				});
				rFieldPanel.add(rArrayField[i][j]);
			};
		}

		tiField = new DoubleField(10,6);
		tiField.setValue(ctrlPar.ti);
		tiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ti = tiField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		tiFieldPanel = new JPanel(new GridLayout(0,1));
		tiFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tiFieldPanel.add(tiField);

		tiLabelPanel = new JPanel(new GridLayout(0,1));
		tiLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tiLabelPanel.add(new JLabel("Integrator factor Ti"));

		tiPanel = new BoxPanel(BoxPanel.VERTICAL);
		tiPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tiPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		tiPanel.add(tiLabelPanel);
		tiPanel.add(tiFieldPanel);

		topCtrlPanel = new BoxPanel(BoxPanel.VERTICAL);
		topCtrlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topCtrlPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		topCtrlPanel.add(new JLabel("Q matrix"));
		topCtrlPanel.add(qFieldPanel);
		topCtrlPanel.add(new JLabel("R matrix"));
		topCtrlPanel.add(rFieldPanel);
		topCtrlPanel.add(tiPanel);
		
		// ---------------

		// -- Swing up controller--

		swingLabelPanel = new JPanel();
		swingLabelPanel.setLayout(new GridLayout(0,1));
		swingLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		swingLabelPanel.add(new JLabel("Ellipse radius 1"));
		swingLabelPanel.add(new JLabel("Ellipse radius 2"));
		swingLabelPanel.add(new JLabel("Ellipse rotation (deg)"));
		swingLabelPanel.add(new JLabel("Limit"));
		swingLabelPanel.add(new JLabel("Gain"));

		radius1Field = new DoubleField(10,6);
		radius1Field.setValue(ctrlPar.ellipseRadius1);
		radius1Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ellipseRadius1 = radius1Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		radius2Field = new DoubleField(10,6);
		radius2Field.setValue(ctrlPar.ellipseRadius2);
		radius2Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ellipseRadius2 = radius2Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		ellipseRotationField = new DoubleField(10,6);
		ellipseRotationField.setValue(ctrlPar.ellipseRotationField);
		ellipseRotationField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.ellipseRotationField = ellipseRotationField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		limitField = new DoubleField(10,6);
		limitField.setValue(ctrlPar.limit);
		limitField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.limit = limitField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		gainField = new DoubleField(10,6);
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
		swingFieldPanel.add(ellipseRotationField);
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

		omega0Field = new DoubleField(10,6);
		omega0Field.setValue(ctrlPar.omega0);
		omega0Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.omega0 = omega0Field.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});
		hField = new DoubleField(10,6);
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

		// ---------------

		// -- Deadzone for controller --

		deadzoneLabelPanel = new JPanel();
		deadzoneLabelPanel.setLayout(new GridLayout(0,1));
		deadzoneLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		deadzoneLabelPanel.add(new JLabel("Theta"));
		deadzoneLabelPanel.add(new JLabel("Theta velocity"));
		deadzoneLabelPanel.add(new JLabel("Phi"));
		deadzoneLabelPanel.add(new JLabel("Phi velocity"));

		deadzoneBaseAngVelField = new DoubleField(10,6);
		deadzoneBaseAngVelField.setValue(ctrlPar.deadzoneBaseAngVel);
		deadzoneBaseAngVelField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.deadzoneBaseAngVel = deadzoneBaseAngVelField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		deadzonePendAngVelField = new DoubleField(10,6);
		deadzonePendAngVelField.setValue(ctrlPar.deadzonePendAngVel);
		deadzonePendAngVelField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.deadzonePendAngVel = deadzonePendAngVelField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		deadzoneBaseAngField = new DoubleField(10,6);
		deadzoneBaseAngField.setValue(ctrlPar.deadzoneBaseAng);
		deadzoneBaseAngField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.deadzoneBaseAng = deadzoneBaseAngField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		deadzonePendAngField = new DoubleField(10,6);
		deadzonePendAngField.setValue(ctrlPar.deadzonePendAng);
		deadzonePendAngField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlPar.deadzonePendAng = deadzonePendAngField.getValue();
				saveCtrlButton.setEnabled(true);
			}
		});

		deadzoneFieldPanel = new JPanel();
		deadzoneFieldPanel.setLayout(new GridLayout(0,1));
		deadzoneFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		deadzoneFieldPanel.add(deadzonePendAngField);
		deadzoneFieldPanel.add(deadzonePendAngVelField);
		deadzoneFieldPanel.add(deadzoneBaseAngField);
		deadzoneFieldPanel.add(deadzoneBaseAngVelField);

		deadzonePanel = new BoxPanel(BoxPanel.HORIZONTAL);
		deadzonePanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		deadzonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		deadzonePanel.add(deadzoneLabelPanel);
		deadzonePanel.add(deadzoneFieldPanel);

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
		ctrlParameterPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		ctrlParameterPanel.add(new JLabel(formatLabel("Top controller", true, 2, "#444444")));
		ctrlParameterPanel.add(topCtrlPanel);
		ctrlParameterPanel.addFixed(5);
		ctrlParameterPanel.add(new JLabel(formatLabel("Swing up controller", true, 2, "#444444")));
		ctrlParameterPanel.add(swingCtrlPanel);
		ctrlParameterPanel.addFixed(5);
		ctrlParameterPanel.add(new JLabel(formatLabel("General controller settings", true, 2, "#444444")));
		ctrlParameterPanel.add(generalCtrlPanel);
		ctrlParameterPanel.add(new JLabel(formatLabel("Controller Deadzones", true, 2, "#444444")));
		ctrlParameterPanel.add(deadzonePanel);
		ctrlParameterPanel.add(saveCtrlButton);

		// ------------


		// -- Panel for the estimator parameters --

		String[] regressorModels = {"Coulomb friction [ sign(v) ]", "+ Viscous friction [ sign(v), v ]", "+ V.f & offset[ sign(v), v, 1 ]"};
		JComboBox regressorCombo = new JComboBox(regressorModels);
		regressorCombo.setSelectedIndex(rlsPar.regressorModel);

		regressorCombo.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        JComboBox cb = (JComboBox)e.getSource();
		        int regressorModel = cb.getSelectedIndex();
		        rlsPar.regressorModel = regressorModel;
		        theta00Field.setValue(rlsPar.theta0[regressorModel][0]);
		        if (regressorModel == 0) {
					theta01Field.setVisible(false);
					theta02Field.setVisible(false);
				} else if (regressorModel == 1){
		        	theta01Field.setVisible(true);
		        	theta01Field.setValue(rlsPar.theta0[regressorModel][1]);
					theta02Field.setVisible(false);
		        } else {
					theta01Field.setVisible(true);
					theta01Field.setValue(rlsPar.theta0[regressorModel][1]);
					theta02Field.setVisible(true);
					theta02Field.setValue(rlsPar.theta0[regressorModel][2]);
				}
		        saveEstimatorButton.setEnabled(true);
				frame.validate(); // Doesn't update correctly otherwise
		    }
		});


		regressorPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		regressorPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		regressorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		regressorPanel.add(new JLabel("Model"));
		regressorPanel.addFixed(10);
		regressorPanel.add(regressorCombo);


		estimatorLabelPanel = new JPanel();
		estimatorLabelPanel.setLayout(new GridLayout(0,1));
		estimatorLabelPanel.add(new JLabel("lambda: "));
		estimatorLabelPanel.add(new JLabel("θ0: "));


		lambdaField = new DoubleField(10,6);
		lambdaField.setValue(rlsPar.lambda);
		lambdaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.lambda = lambdaField.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		BoxPanel theta0Panel;

		theta00Field = new DoubleField(10,6);
		theta00Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][0] = theta00Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});
		theta00Field.setVisible(true);

		theta01Field = new DoubleField(10,6);
		theta01Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][1] = theta01Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});
		theta00Field.setVisible(true);

		theta02Field = new DoubleField(10,6);
		theta02Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][2] = theta02Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});
		theta00Field.setVisible(true);

		theta0Panel = new BoxPanel(BoxPanel.HORIZONTAL);
		theta0Panel.add(theta00Field);
		theta0Panel.add(theta01Field);
		theta0Panel.add(theta02Field);

		estimatorFieldPanel = new JPanel();
		estimatorFieldPanel.setLayout(new GridLayout(0,1));
		estimatorFieldPanel.add(lambdaField);
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
		estimatorParameterPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		estimatorParameterPanel.add(regressorPanel);
		estimatorParameterPanel.add(estimatorGridPanel);
		estimatorParameterPanel.add(estimatorButtonsPanel);
		
		// ------------


		// -- Panel for start and stop buttons --
		rlsConvergeTestButton = new JButton("RLS convergence");
		rlsConvergeTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.rlsConverge();
			}
		});
		stepResponseTestButton = new JButton("Step response");
		stepResponseTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.stepResponse();
			}
		});
		buttonPanel4 = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel4.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		buttonPanel4.add(rlsConvergeTestButton);
		buttonPanel4.add(stepResponseTestButton);
		buttonPanel4.setAlignmentX(Component.LEFT_ALIGNMENT);

		saveTestDataButton = new JButton("Start");
		saveTestDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.startSaveArrays();
			}
		});
		stopSaveTestDataButton = new JButton("Stop");
		stopSaveTestDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.saveDataGeneral();
			}
		});
		buttonPanel3 = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel3.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		buttonPanel3.add(new JLabel("Save to file: "));
		buttonPanel3.addFixed(10);
		buttonPanel3.add(saveTestDataButton);
		buttonPanel3.add(stopSaveTestDataButton);
		buttonPanel3.setAlignmentX(Component.LEFT_ALIGNMENT);

		frictionCompensatorOnButton = new JButton("On");
		frictionCompensatorOnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setEnableFrictionCompensation(true);
			}
		});
		frictionCompensatorOffButton = new JButton("Off");
		frictionCompensatorOffButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setEnableFrictionCompensation(false);
			}
		});
		buttonPanel2 = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel2.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		buttonPanel2.add(new JLabel("Friction compensation: "));
		buttonPanel2.addFixed(10);
		buttonPanel2.add(frictionCompensatorOnButton);
		buttonPanel2.add(frictionCompensatorOffButton);
		buttonPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);

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
		resetOffsetButton = new JButton("Reset Offsets");
		resetOffsetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.resetOffsets();
			}
		});
		buttonPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(resetOffsetButton);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		// ------------

		// --- Controller in use label --

		currentController = new JLabel("No controller");

		// -------------

		// Create panel holding everything but the plotters.
		rightPanel = new BoxPanel(BoxPanel.VERTICAL);
		rightPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		rightPanel.addFixed(10);
		rightPanel.add(new JLabel(formatLabel("Current Controller", true, 4, "#000033")));
		rightPanel.add(currentController);
		rightPanel.addFixed(20);
		rightPanel.add(buttonPanel);
		rightPanel.add(buttonPanel2);
		rightPanel.add(buttonPanel3);
		rightPanel.add(new JLabel("Tests: "));
		rightPanel.add(buttonPanel4);
		rightPanel.addFixed(10);
		rightPanel.add(ctrlParameterPanel);
		rightPanel.addFixed(10);
		rightPanel.add(estimatorParameterPanel);
		rightPanel.addFixed(10);

		// If the right panel does not fit, a scroll pane can be used. This messes with the layout though.
		rightPanelWithScroll = new JScrollPane(rightPanel);
		rightPanelWithScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		rightPanelWithScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightPanelWithScroll.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));

		// Create panel for the entire GUI.
		guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		guiPanel.addFixed(10);
		guiPanel.add(plotterPanel);
		guiPanel.addFixed(10);
		guiPanel.add(rightPanelWithScroll);

		// For updates on current controller
		controller.registerObserver(this);


		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				rlsPanel.stopThread();
				frame.dispose();
			}
		});

		// Load regressor model
		for(ActionListener a: regressorCombo.getActionListeners()) {
			a.actionPerformed(new ActionEvent(regressorCombo, ActionEvent.ACTION_PERFORMED, null) {
				//
			});
		}

		// Make the window visible.
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		isInitialized = true;
	}

	/**
	 * Formatting of labels
     */
	private String formatLabel(String str, boolean bold, Integer size, String color) {
		if (bold)
			str = "<b>" + str + "</b>";
		if ((size != null) || color !=null) {
			str = "<font" + (size != null ? " size='" + size + "'" : "") + (color != null ? " color='" + color + "'" : "") + ">" + str + "</font>";
		}

		return "<html>" + str + "</html>";
	}

	/** Called by CommunicationManager to plot a control signal data point. */
	public synchronized void putControlDataPoint(double t, double u) { // Consider using a modified PlotData here.
		if (isInitialized) {
			ctrlPanel.putData(t, u);
		} else {
			LOGGER.log(Level.FINE, "Note: GUI not yet initialized. Ignoring call to putControlDataPoint().");
		}
	}

	/** Called by CommunicationManager to plot a rls data point. */
	public synchronized void putRLSDataPoint(double t, double y1, double y2, double y3) {
		if (isInitialized) {
			rlsPanel.putData(t, y1, y2, y3);
		} else {
			LOGGER.log(Level.FINE,"Note: GUI not yet initialized. Ignoring call to putRLSDataPoint().");
		}
	}

	/** Called by CommunicationManager to plot a measurement data point. */
	public synchronized void putMeasurementDataPoint(double t, double y1, double y2, double y3, double y4) {
		if (isInitialized) {
			measPanel.putData(t, y1, y2, y3, y4);
		} else {
			LOGGER.log(Level.FINE, "Note: GUI not yet initialized. Ignoring call to putMeasurementDataPoint().");
		}
	}

	/**
	 * For updates about which controller being used
     */
	public void update(Observable o, Object arg) {
	    if (arg instanceof String) {
	        currentController.setText((String) arg);
	    }
	}
}