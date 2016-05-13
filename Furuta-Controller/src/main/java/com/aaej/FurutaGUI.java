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


// TODO: add frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// 		according to http://www.control.lth.se/previouscourse/FRTN01/Exercise4_14/Exercise4.html
//		although windowClosing used below might be enough.



//Class that creates and maintains a GUI for the FURUTA process.
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
							buttonPanel3,kalmanQPanel, kalmanRPanel, kalmanPanel, deadzonePanel;
	private PlotterPanel 	measPanel, ctrlPanel, rlsPanel;
	private JPanel 			qFieldPanel, rFieldPanel, swingLabelPanel, swingFieldPanel, generalLabelPanel,
							generalFieldPanel, estimatorLabelPanel, estimatorFieldPanel;

	// Declaration of buttons and fields
	private JButton 		startButton, stopButton, resetEstimatorButton, saveEstimatorButton, saveCtrlButton, brakeButton;
	private JButton			resetOffsetButton, resetOffsetOnTopButton;
	private JButton			frictionCompensatorOnButton, frictionCompensatorOffButton;
	private JButton         rlsConvergeTestButton, stepResponseTestButton, toggleKalmanButton, saveTestDataButton, stopSaveTestDataButton, toggleStepResponseButton;
	private DoubleField 	omega0Field, hField, radius1Field, radius2Field, limitField, gainField,
							lambdaField, p0Field, theta00Field, theta01Field, deadzonePendAngField;
	private DoubleField[][] qArrayField, rArrayField;
	private DoubleField[]	kalmanQArrayField, kalmanRArrayField;
	private JLabel currentController;

	DoubleField offsetBaseAngField, offsetBaseAngVelField, offsetPendAngField, offsetPendAngVelField;
	BoxPanel offsetPanel;

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
	public void setCommunicationManager(CommunicationManager communicationManager) {
		this.communicationManager = communicationManager;
	}
	/** Creates the GUI. Called from Main. */
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
		lowerLeftPlotPanel.add(new JLabel("u, f, u+f"));
		lowerLeftPlotPanel.add(ctrlPanel);
		
		rlsPanel = new PlotterPanel(3, priority);
		rlsPanel.setYAxis(0.6, -0.3, 2, 2);
		rlsPanel.setXAxis(50, 5, 5);
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


		// --------------

		// --- Offsets ---



		offsetBaseAngField = new DoubleField(7,5);
		offsetBaseAngField.setValue(communicationManager.getOffsetBaseAng());
		offsetBaseAngField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.setOffsetBaseAng(offsetBaseAngField.getValue());
			}
		});

		offsetBaseAngVelField = new DoubleField(7,5);
		offsetBaseAngVelField.setValue(communicationManager.getOffsetBaseAngVel());
		offsetBaseAngVelField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.setOffsetBaseAngVel(offsetBaseAngVelField.getValue());
			}
		});

		offsetPendAngField = new DoubleField(7,5);
		offsetPendAngField.setValue(communicationManager.getOffsetPendAng());
		offsetPendAngField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.setOffsetPendAng(offsetPendAngField.getValue());
			}
		});

		offsetPendAngVelField = new DoubleField(7,5);
		offsetPendAngVelField.setValue(communicationManager.getOffsetPendAngVel());
		offsetPendAngVelField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.setOffsetPendAngVel(offsetPendAngVelField.getValue());
			}
		});

		offsetPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		offsetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		offsetPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		offsetPanel.add(offsetPendAngField);
		offsetPanel.addFixed(5);
		offsetPanel.add(offsetPendAngVelField);
		offsetPanel.addFixed(5);
		offsetPanel.add(offsetBaseAngField);
		offsetPanel.addFixed(5);
		offsetPanel.add(offsetBaseAngVelField);
		offsetPanel.addFixed(5);





		// -------------


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


		lambdaField = new DoubleField(10,6);
		lambdaField.setValue(rlsPar.lambda);
		lambdaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.lambda = lambdaField.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		p0Field = new DoubleField(10,6);
		p0Field.setValue(rlsPar.p0);
		p0Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.p0 = p0Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		BoxPanel theta0Panel;

		theta00Field = new DoubleField(10,6);
		theta00Field.setValue(rlsPar.theta0[rlsPar.regressorModel][0]);
		theta00Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.theta0[rlsPar.regressorModel][0] = theta00Field.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		theta01Field = new DoubleField(10,6);
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

		int qKalmanSize = rlsPar.qKalman[0].length;
		kalmanQPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		kalmanQPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		kalmanQPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		kalmanQPanel.add(new JLabel("Kalman Q  "));
		kalmanQArrayField = new DoubleField[qKalmanSize];
		for (int i = 0; i < qKalmanSize; i++) {
			kalmanQArrayField[i] = new DoubleField(10,6);
			kalmanQArrayField[i].setValue(rlsPar.qKalman[i][i]);
			kalmanQArrayField[i].putClientProperty("i", (Integer) i);
			kalmanQArrayField[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = (Integer)((DoubleField)e.getSource()).getClientProperty("i");
					rlsPar.qKalman[i][i] = kalmanQArrayField[i].getValue();
					saveEstimatorButton.setEnabled(true);
				}
			});
			kalmanQPanel.add(kalmanQArrayField[i]);
		}

				int rKalmanSize = rlsPar.rKalman[0].length;
		kalmanRPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		kalmanRPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		kalmanRPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		kalmanRPanel.add(new JLabel("Kalman R  "));
		kalmanRArrayField = new DoubleField[rKalmanSize];
		for (int i = 0; i < rKalmanSize; i++) {
			kalmanRArrayField[i] = new DoubleField(10,6);
			kalmanRArrayField[i].setValue(rlsPar.rKalman[i][i]);
			kalmanRArrayField[i].putClientProperty("i", (Integer) i);
			kalmanRArrayField[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = (Integer)((DoubleField)e.getSource()).getClientProperty("i");
					rlsPar.rKalman[i][i] = kalmanRArrayField[i].getValue();
					saveEstimatorButton.setEnabled(true);
				}
			});
			kalmanRPanel.add(kalmanRArrayField[i]);
		}

		toggleKalmanButton = new JButton("Toggle Kalman filter");
		toggleKalmanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.toggleKalman();
			}
		});

		kalmanPanel = new BoxPanel(BoxPanel.VERTICAL);
		kalmanPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		kalmanPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		kalmanPanel.add(kalmanQPanel);
		kalmanPanel.add(kalmanRPanel);
		kalmanPanel.add(toggleKalmanButton);

		deadzonePendAngField = new DoubleField(10,6);
		deadzonePendAngField.setValue(rlsPar.deadzonePendAng);
		deadzonePendAngField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rlsPar.deadzonePendAng = deadzonePendAngField.getValue();
				saveEstimatorButton.setEnabled(true);
			}
		});

		deadzonePanel = new BoxPanel(BoxPanel.HORIZONTAL);
		deadzonePanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		deadzonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		deadzonePanel.add(new JLabel("PendAng Deadzone"));
		deadzonePanel.add(deadzonePendAngField);

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
		estimatorParameterPanel.add(kalmanPanel);
		estimatorParameterPanel.add(deadzonePanel);
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

		resetOffsetButton = new JButton("Reset Offset");
		resetOffsetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.resetOffsets(false);
				offsetBaseAngField.setValue(communicationManager.getOffsetBaseAng());
				offsetBaseAngVelField.setValue(communicationManager.getOffsetBaseAngVel());
				offsetPendAngField.setValue(communicationManager.getOffsetPendAng());
				offsetPendAngVelField.setValue(communicationManager.getOffsetPendAngVel());
			}
		});

		resetOffsetOnTopButton = new JButton("Reset Offset On Top");
		resetOffsetOnTopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.resetOffsets(true);
				offsetBaseAngField.setValue(communicationManager.getOffsetBaseAng());
				offsetBaseAngVelField.setValue(communicationManager.getOffsetBaseAngVel());
				offsetPendAngField.setValue(communicationManager.getOffsetPendAng());
				offsetPendAngVelField.setValue(communicationManager.getOffsetPendAngVel());
			}
		});

		frictionCompensatorOnButton = new JButton("Compensate On");
		frictionCompensatorOnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setEnableFrictionCompensation(true);
			}
		});
		frictionCompensatorOffButton = new JButton("Compensate Off");
		frictionCompensatorOffButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setEnableFrictionCompensation(false);
			}
		});

		rlsConvergeTestButton = new JButton("rls test");
		rlsConvergeTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.rlsConverge();
			}
		});

		stepResponseTestButton = new JButton("step test");
		stepResponseTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.stepResponse();
			}
		});

		saveTestDataButton = new JButton("start save");
		saveTestDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				communicationManager.startSaveArrays();
			}
		});
		stopSaveTestDataButton = new JButton("stop save");
		stopSaveTestDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				specificTests.saveDataGeneral();
			}
		});


		/*brakeButton = new JButton("Brake");
		brakeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.toggleBrakePendulum();
			}
		});*/

		toggleStepResponseButton = new JButton("STEPRESPONSE");
		toggleStepResponseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.toggleStepResponse();
			}
		});

		buttonPanel3 = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel3.add(rlsConvergeTestButton);
		buttonPanel3.add(stepResponseTestButton);
		buttonPanel3.add(saveTestDataButton);
		buttonPanel3.add(stopSaveTestDataButton);
		buttonPanel3.add(toggleStepResponseButton);
		buttonPanel3.setAlignmentX(Component.LEFT_ALIGNMENT);

		buttonPanel2 = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel2.add(frictionCompensatorOnButton);
		buttonPanel2.add(frictionCompensatorOffButton);
		buttonPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);

		buttonPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(resetOffsetButton);
		buttonPanel.add(resetOffsetOnTopButton);
		//buttonPanel.add(brakeButton);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


		// ------------

		// --- Controller in use label --

		currentController = new JLabel("NO CONTROLLER");

		// -------------

		// Create panel holding everything but the plotters.
		rightPanel = new BoxPanel(BoxPanel.VERTICAL);
		rightPanel.addFixed(10);

		rightPanel.add(ctrlParameterPanel);
		rightPanel.addFixed(10);
		rightPanel.add(estimatorParameterPanel);
		rightPanel.addFixed(10);
		rightPanel.add(new JLabel("Offsets (theta thetavel phi phivel)"));
		rightPanel.add(offsetPanel);
		rightPanel.addFixed(10);
		rightPanel.add(currentController);
		rightPanel.addFixed(10);
		rightPanel.add(buttonPanel);
		rightPanel.addFixed(10);
		rightPanel.add(buttonPanel2);
		rightPanel.addFixed(10);
		rightPanel.add(buttonPanel3);
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

		controller.registerObserver(this);


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
	public synchronized void putRLSDataPoint(double t, double y1, double y2, double y3) {
		if (isInitialized) {
			rlsPanel.putData(t, y1, y2, y3);
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
	
	public void update(Observable o, Object arg) { // For updates about which controller being used
	    if (arg instanceof String) {
	        currentController.setText((String) arg);
	        offsetBaseAngField.setValue(communicationManager.getOffsetBaseAng());
	        offsetBaseAngVelField.setValue(communicationManager.getOffsetBaseAngVel());
	        offsetPendAngField.setValue(communicationManager.getOffsetPendAng());
	        offsetPendAngVelField.setValue(communicationManager.getOffsetPendAngVel());
	    }
	}

}
