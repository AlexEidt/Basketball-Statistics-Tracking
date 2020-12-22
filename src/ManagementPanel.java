// Alex Eidt
// Basketball Statistics Tracking Program
// ManagementPanel Class
// The ManagementPanel is where the statistic tracking actually happens.
// If the number of players entered equals the number of starters for the game, then the ManagementPanel
// will show a button for each player entered.
// If the number of players entered is greater than the number of starters for the game, then the
// ManagementPanel will split into a list of player buttons showing players that are 'Currently on Court'
// and another list showing players that are on the 'bench'.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;

public class ManagementPanel extends GUISettings {
    
    private static final long serialVersionUID = 1L;
    
	// Indices of Buttons in the 'buttonArray'
	private static final int BOXSCORE_BUTTON = 0;
	private static final int UNDO_BUTTON = 1;
	private static final int DONE_BUTTON = 2;
	private static final int START_BUTTON = 3;
	private static final int TIMEOUT_BUTTON = 4;
	private static final int HOME_BUTTON = 5;
	
	// Key Bindings for each button in 'buttonArray'
	private static final int UNDO_BUTTON_KEY = KeyEvent.VK_U;
	private static final int BOXSCORE_BUTTON_KEY = KeyEvent.VK_B;
	private static final int START_BUTTON_KEY = KeyEvent.VK_ENTER;
	private static final int TIMEOUT_BUTTON_KEY = KeyEvent.VK_T;
	private static final int HOME_BUTTON_KEY = KeyEvent.VK_ESCAPE;
		
    private String fileName; // Name of the file the game data is written to
    private JButton[] buttonArray; // All the Buttons on the ManagementPanel
    /*
     * 'menuPanel' is the panel on the ManagementPanel frame that has all the player playerButtons.
     *  If the number of starters entered by the user equals the number of players entered by
     *  the user, then 'menuPanel' includes all player playerButtons in a FlowLayout
     */
    private JPanel playersOnBenchPanel; 
    /*
     * 'onFloorPanel' only is used when the number of starters entered by the user equals the number
     * of players entered by the user. It stores a label of each player currently on the court.
     */
    private JPanel onFloorPanel;
    /*
     * 'headerPanel' is the panel that includes the label 'Roster Management' and the 'Score', 'Box Score',
     *  'Undo', 'Done', 'Start/Stop', 'Settings' and the timer.
     */
    private JPanel headerPanel;
    private List<Player> players, bench, total; // Player lists
    private List<JButton> playerButtons; // List of player buttons.
    private List<Undo> undo; // List of 'undo' representing every statistic tracked in chronological order.
    private JFrame frame; // The frame used for the ManagementPanel.
    private JLabel score; // The label showing the total points scored by the team.
    private JLabel teamFouls; // The label showing the total team fouls.
    private boolean splitPane; // If more players are entered than starters, then the lower panel is split.
    private boolean startStop; // Alternates the function of the 'Start/Stop' button.
    private boolean gameOver; // True if game is over, false if not.
    private boolean isQuarters; // True if quarters is period type, false if Halves is period type.
    private GameSettings SETTINGS; // Settings used for the game.
    private JPanel managementPanel; // Used to frame the ManagementPanel in a JFrame.
    private JPanel pane; // Panel used to switch between different windows.
    private JPanel timePanel; // Panel used to store the timer.
    private GameClock clock; // The timer used for the game.
    private int period; // The current period of the game.
    private int timeouts; // The number of timeouts for the team.
    private int teamFoulsInPeriod; // The number of team fouls for the current period.
    private Map<String, Integer> keyMap; // Maps keys pressed to their corresponding statistic.
    private Map<Integer, Integer> playerKeys; // Maps numbers to corresponding players for each player button.
    
	private final int personalFouls; // The number of personal fouls allowed selected by the user in the settings.
	private final int technicalFouls; // The number of technical fouls allowed selected by the user in the settings.
	private final int flagrantI; // The number of Flagrant I fouls allowed selected by the user in the settings.
	private final int flagrantII; // The number of Flagrant II fouls allowed selected by the user in the settings.
	private final int numberStarters; // The number of starters for the game selected by the user in the settings.
	private final Color background; // The background color selected by the user in the settings.
    
    // Parameters:
    // 'players': List of players currently on the court (Starting if new game is being started).
    // 'bench': List of players currently on the bench.
    // 'undo': Statistical history of the game (empty if new game is being started).
    // 'fileName': The name of the file the game data will be stored in.
    // 'settings': The settings for the current game.
    public ManagementPanel(List<Player> players, List<Player> bench, 
    		               List<Undo> undo, String fileName, GameSettings settings) {
    	// Initialize fields with parameters
        this.SETTINGS = settings;
        this.players = players;
        this.bench = bench;
        this.undo = undo;
        this.total = new ArrayList<Player>(players.size() + bench.size());
        this.total.addAll(players);
        this.total.addAll(bench);
        this.fileName = fileName;
        
    	this.personalFouls = (int) SETTINGS.getSetting(Setting.PERSONAL_FOULS);
    	this.technicalFouls = (int) SETTINGS.getSetting(Setting.TECHNICAL_FOULS);
    	this.flagrantI = (int) SETTINGS.getSetting(Setting.FLAGRANT_I);
    	this.flagrantII = (int) SETTINGS.getSetting(Setting.FLAGRANT_II);
    	this.background = (Color) SETTINGS.getSetting(Setting.BACKGROUND_COLOR);
        this.timeouts = (int) SETTINGS.getSetting(Setting.TIMEOUTS);
        this.period = (int) SETTINGS.getSetting(Setting.CURRENT_PERIOD);
        this.numberStarters = (int) SETTINGS.getSetting(Setting.NUMBER_OF_STARTERS);
        this.isQuarters = (boolean) SETTINGS.getSetting(Setting.PERIOD_TYPE);
        this.playerButtons = new ArrayList<JButton>(this.total.size());
        this.keyMap = new HashMap<String, Integer>();
    	for (int i = 0; i < STAT_KEYS.length; i++) {
        	this.keyMap.put(STAT_KEYS[i], KEY_EVENTS[i]);
    	}
    	int[] playerKey = {KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, 
    			           KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9};
    	this.playerKeys = new HashMap<Integer, Integer>();
    	for (int i = 0; i < playerKey.length; i++) {
    		this.playerKeys.put(i, playerKey[i]);
    	}
        this.clock = new GameClock((int)((double) this.SETTINGS.getSetting(Setting.TIME_REMAINING) * 60 * 10));
        this.score = formatLabel("Score: " + getTotal("PTS") + "    ", FONT_SIZE * 4 / 7, SETTINGS);
        format();
        getTeamFoulsInPeriod();
        this.gameOver = period == END_OF_GAME;
        this.playersOnBenchPanel.setBackground(background);
        initializeButtons();
    }
    
    // Post: Correctly formats the UI for the ManagementPanel.
    private void format() {
        this.teamFouls = formatLabel("  Team Fouls: " + this.teamFoulsInPeriod, FONT_SIZE * 4 / 7, SETTINGS);
        this.playersOnBenchPanel = new JPanel();
        this.headerPanel = new JPanel();  
        this.onFloorPanel = new JPanel(new GridLayout(this.players.size() + 1, 1));
        this.managementPanel = new JPanel();
        this.managementPanel.setBackground(this.background);
        this.pane = new JPanel();
        this.pane.setBackground(this.background);
        this.frame = new JFrame("Roster Management - " + this.fileName);
        if (this.total.size() != this.numberStarters) {
            this.playersOnBenchPanel.setLayout(new GridLayout(bench.size(), 1));
            this.splitPane = false;
            this.onFloorPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        } else {
            this.playersOnBenchPanel.setLayout(new FlowLayout(1, 60, 40));
            this.splitPane = true;
        }
    }
    
    // Post: Creates and formats all buttons used in the ManagementPanel.
    private void initializeButtons() {
        // Create all buttons in the ManagementPanel
        String[] buttonNames = {" [B]ox Score", " [U]ndo", " Done", "   Start", " [T]imeouts: " + this.timeouts, "  Return to Game"};
        int size = FONT_SIZE * 4 / 7;
        int[] sizes = {size, size, size, size, size, size}; 
        this.buttonArray = createButtonArray(buttonNames, sizes, SETTINGS);
        this.buttonArray[UNDO_BUTTON].setEnabled(!this.undo.isEmpty());
		this.buttonArray[TIMEOUT_BUTTON].setEnabled(this.timeouts != 0);
		this.buttonArray[HOME_BUTTON].setBorder(null);
        setIcon(buttonArray, HOME_BUTTON, ROSTER_MANAGEMENT_ICON);
        // Add Icons to all buttons
        int[] indices = {DONE_BUTTON, BOXSCORE_BUTTON, UNDO_BUTTON, START_BUTTON, TIMEOUT_BUTTON};
        String[] icons = {DONE_BUTTON_ICON, BOXSCORE_BUTTON_ICON, UNDO_BUTTON_ICON, 
        		          PLAY_BUTTON_ICON, TIMEOUT_BUTTON_ICON};
        formatIcons(this.buttonArray, indices, icons);
    }
    
    // Post: Sets 'teamFoulsInPeriod' to the current team fouls in the period.
    private void getTeamFoulsInPeriod() {
        for (Undo u : this.undo) {
        	char periodChar = u.getTimeOf().trim().charAt(0);
        	int adjust = 0;
        	if (periodChar == 'O') {
        		periodChar = u.getTimeOf().trim().charAt(2);
        		if (this.isQuarters) {
        			adjust = QUARTERS - 1;
        		} else {
        			adjust = HALVES - 1;
        		}
        	}
        	if (Integer.valueOf(periodChar + "") + adjust == this.period + 1) {
        		if (u.getStatFromPlayer().equals("Personal Foul")) {
            		this.teamFoulsInPeriod++;
        		}
        	}
        }
    }
    
    // Post: Adds function to the 'Box Score' button. Opens a new window with a table showing the
    //       statistics for each player.
    public void createTableButton() {
        this.buttonArray[BOXSCORE_BUTTON].addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	pane.removeAll();
            	pane.setLayout(new BorderLayout());
            	JLabel title = formatLabel("  Box Score", FONT_SIZE * 5 / 6, SETTINGS);
            	setIcon(title, BOXSCORE_BUTTON_ICON);
            	JPanel header = panelfy(title, background, new GridLayout(1, 2), new MatteBorder(0, 0, BORDER_SIZE * 3, 0, background));
            	pane.add(header, BorderLayout.NORTH);
                Table table = new Table(total, getData(), FONT_SIZE / 3, FONT_SIZE / 3, SETTINGS);
            	JPanel allPanels = panelfy(table.getTable(), background, new GridLayout(1, 1), null);
                JScrollPane scrollTable = addScrollPane(allPanels);
                scrollTable.setBorder(null);
                scrollTable.setBackground(background);
                JPanel scroll = panelfy(scrollTable, background, new GridLayout(1, 1), null);
                frame.setTitle("Box Score");
                pane.add(scroll, BorderLayout.CENTER);
                pane.add(buttonArray[HOME_BUTTON], BorderLayout.SOUTH);
                pane.repaint();
                pane.revalidate();
            }
        });
    	setButtonKey(this.buttonArray[BOXSCORE_BUTTON], BOXSCORE_BUTTON_KEY, 0);
    }
    
    // Post: Adds functionality to the Home Button. Brings the user back to the Roster Management window.
    public void createHomeButton() {
    	this.buttonArray[HOME_BUTTON].addActionListener(new AbstractAction() {
    		public void actionPerformed(ActionEvent e) {
    			pane.removeAll();
    			setPane();
    			frame.setTitle("Roster Management - " + fileName);   
    			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		}
    	});
    	setButtonKey(this.buttonArray[HOME_BUTTON], HOME_BUTTON_KEY, 0);
    }
    
    // Post: Adds function to the 'Undo' button. Allows the user to select which period of the game
    //       they want to eliminate a statistic from and shows a history of all statistics entered
    //       during that period with the most recently entered statistic shown at the top.
    public void createUndoButton() {
        this.buttonArray[UNDO_BUTTON].addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	pane.removeAll();
            	pane.setLayout(new BorderLayout());
            	frame.setTitle("Undo Statistic");
                int numPeriods;
                String periodType;
                int periodNumbers = period;
                if (gameOver) {
                	if (isQuarters) {
                		periodNumbers = QUARTERS;
                	} else {
                		periodNumbers = HALVES;
                	}
                }
                if (isQuarters) {
                	numPeriods = QUARTERS;
                	periodType = "Quarter";
                } else {
                	numPeriods = HALVES;
                	periodType = "Half";
                }
                boolean[] hasPeriod = new boolean[Math.max(periodNumbers + 1, numPeriods)];
                for (int i = 0; i < hasPeriod.length; i++) {
                	for (Undo u : undo) {
                		if (i < GAME_PERIODS.length) {
                    		if (u.getTimeOf().contains(GAME_PERIODS[i])) {
                    			hasPeriod[i] = true;
                    			break;
                    		}
                		} else {
                			if (u.getTimeOf().contains("OT" + (i - GAME_PERIODS.length + 1))) {
                				hasPeriod[i] = true;
                				break;
                			}
                		}
                	}
                }
                JPanel periods = new JPanel(new GridLayout(1, hasPeriod.length));
                JPanel undoPanel = new JPanel();
                for (int i = 0; i < hasPeriod.length; i++) {
                	String periodText;
                	String check;
                	if (i < GAME_PERIODS.length) {
                		periodText = GAME_PERIODS[i] + " " + periodType;
                		check = GAME_PERIODS[i];
            		} else {
            			check = periodText = "OT" + (i - GAME_PERIODS.length + 1);
            		}
                	JButton selectPeriod = new JButton(periodText);
                	selectPeriod.setEnabled(hasPeriod[i]);
                	formatButton(selectPeriod, BUTTON_HEIGHT, BUTTON_HEIGHT, FONT_SIZE / 3, SETTINGS);
                	periods.add(selectPeriod);
                	selectPeriod.addActionListener(new AbstractAction() {
                		public void actionPerformed(ActionEvent e) {
                			undoPanel.removeAll();
                			int grid = 0;
                			for (int i = undo.size() - 1; i >= 0; i--) {
                				Undo u = undo.get(i);
                            	Player play = u.getPlayer();
                            	String undoString = u.toString();
                            	if (u.getTimeOf().contains(check)) {
                            		grid++;
                            		JButton undoButton = new JButton(play.toString() + " " + undoString);
                                    formatButton(undoButton, BUTTON_HEIGHT, BUTTON_HEIGHT, FONT_SIZE / 3, SETTINGS);
                                    undoPanel.add(undoButton);
                                    undoButton.addActionListener(new AbstractAction() {
                                    	public void actionPerformed(ActionEvent e) {
                                    		if (!u.getStatFromPlayer().equals("Substitution")) {
                                                Player astPlayer = u.getAstPlayer();
                                                if (u.hasAssistPlayer()) {
                                                	int index = total.indexOf(astPlayer);
                                                	Player player = total.get(index);
                                                	player.add(false, "Assist");
                                                }
                                                String stat = u.getStatFromPlayer();
                                                play.add(false, stat);
                                                switch (stat) {
                                                case "Personal Foul":
                                                case "PF":
                                                	teamFoulsInPeriod--;
                                                	teamFouls.setText("  Team Fouls: " + teamFoulsInPeriod);
                                                case "Technical Foul":
                                                case "TF":
                                                case "Flagrant I Foul":
                                                case "FLGI":
                                                case "Flagrant II Foul":
                                                case "FLGII":
                                                	updateOnFloorPanel();
                                                	break;
                                                }
                                            }
                                            undo.remove(u);
                                            score.setText("Score: " + getTotal("PTS") + "    ");
                                            buttonArray[UNDO_BUTTON].setEnabled(!undo.isEmpty());
                                			pane.removeAll();
                                			setPane();
                                			frame.setTitle("Roster Management - " + fileName);   
                                			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                            buttonArray[HOME_BUTTON].setText("  Return to Game");
                                            setIcon(buttonArray, HOME_BUTTON, ROSTER_MANAGEMENT_ICON);
                                    	}
                                    });
                            	}
                            }
                			undoPanel.setLayout(new GridLayout(grid, 1));
                			undoPanel.repaint();
                			undoPanel.revalidate();
                		}
                	});
                };
                undoPanel.setBackground(background);
                periods.setBackground(background);
                JLabel label = formatLabel("  Undo", FONT_SIZE * 3 / 4, SETTINGS);
                setIcon(label, UNDO_BUTTON_ICON);
                JScrollPane scrollPane = addScrollPane(undoPanel);
                scrollPane.setBorder(null);
                scrollPane.setBackground(background);
                JPanel panel = panelfy(label, background, null, new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE * 2, BORDER_SIZE, background));
                JPanel scroll = panelfy(scrollPane, background, new GridLayout(1, 1), null);
                JPanel total = new JPanel();
                total.add(panel);
                total.add(periods);
                total.add(scroll);
                total.setBackground(background);
                total.setLayout(new BoxLayout(total, BoxLayout.Y_AXIS));
                pane.add(total, BorderLayout.CENTER);
            	pane.add(buttonArray[HOME_BUTTON], BorderLayout.SOUTH);
                pane.revalidate();
                pane.repaint();
            }
        });
    	setButtonKey(this.buttonArray[UNDO_BUTTON], UNDO_BUTTON_KEY, 0);
    }
    
    // Post: Creates all the player buttons in the case that the number of players entered equals the
    //       number of starters entered by the user.
    public void createTeamButtons() {
    	int fontSize = FONT_SIZE * 5 / 7;
    	int height = BUTTON_HEIGHT * 6 / 5;
    	this.playersOnBenchPanel.setLayout(new GridLayout(this.numberStarters, 1));
    	for (int i = 0; i < this.total.size(); i++) {
    		Player player = this.total.get(i);
            JButton playerButton = new JButton("[" + (i + 1) + "]  " + player.toString());
            boolean isOut = !player.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII);
            playerButton.setEnabled(isOut);
            int width = (int) getDimension(formatLabel(playerButton.getText(), fontSize, SETTINGS)).getWidth();
            formatButton(playerButton, width * 2, height, fontSize, SETTINGS);
            if (!isOut) {
            	playerButton.setBackground(FOULED_OUT_BUTTON_COLOR);
            }
            this.playersOnBenchPanel.add(playerButton);
            playerButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    addStatButton(player, playerButton);
                }
            });
            setButtonKey(playerButton, this.playerKeys.get(i + 1), 0);
            this.playerButtons.add(playerButton);
        }
    }
    
    // Post: Creates all the player buttons in the case that the number of players entered is greater
    //       than the number of starters entered by the user.
    public void createPlayerButtons() {
    	int fontSize = FONT_SIZE * 2 / 3;
        for (Player player : this.total) {
        	JButton playerButton = new JButton(player.toString());
            formatButton(playerButton, BUTTON_HEIGHT, BUTTON_HEIGHT, fontSize, SETTINGS);
            playerButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (player.isStarter()) {
                        addStatButton(player, playerButton);
                    } else {
                    	subNewPlayer(player, players, bench, false);
                    }
                }
            });
            this.playerButtons.add(playerButton);
        }
        updateOnFloorPanel();
    }
   
    // Post: Adds function to the 'Done' button. Closes the ManagementPanel.
    public void createDoneButton() {
        this.buttonArray[DONE_BUTTON].addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Player player = new Player("DONE", "DONE", "DONE");
                undo.add(new Undo(player, "DONE", "DONE"));
                updateFile(total);
                if (clock.isRunning()) {
                	clock.stopTimer();
                }
                frame.dispose();
            }
        });
	    this.frame.addWindowListener(new WindowAdapter() {
	        public void windowClosed(WindowEvent w) {
                Player player = new Player("DONE", "DONE", "DONE");
                undo.add(new Undo(player, "DONE", "DONE"));
                updateFile(total);
                if (clock.isRunning()) {
                	clock.stopTimer();
                }
                frame.dispose();
            }
	    });
    }
    
    // Post: Adds function to the 'Start/Stop' buttons used to manage the time.
    public void createStartStopButton() {
        this.buttonArray[START_BUTTON].addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                startStop = !startStop;
                if (startStop) {
                    clock.startTimer();
                    setIcon(buttonArray, START_BUTTON, PAUSE_BUTTON_ICON);
                    buttonArray[START_BUTTON].setText("   Stop");
                } else {
                	if (clock.isRunning()) {
                		clock.stopTimer();
                	}
                    setIcon(buttonArray, START_BUTTON, PLAY_BUTTON_ICON);
                    buttonArray[START_BUTTON].setText("   Start");
                }
            }
        });
        setButtonKey(buttonArray[START_BUTTON], START_BUTTON_KEY, 0);
    }
    
    // Post: Adds function to the 'Timeouts' button. Decreases the number of timeouts by 1 every time 
    //       it is pressed and stops the clock if it is running.
    public void createTimeoutButton() {
    	this.buttonArray[TIMEOUT_BUTTON].addActionListener(new AbstractAction() {
    		public void actionPerformed(ActionEvent e) {
    			if (timeouts > 0) {
        			timeouts--;
        			buttonArray[TIMEOUT_BUTTON].setEnabled(timeouts != 0);
        			if (clock.isRunning()) {
        				clock.stopTimer();
        				startStop = !startStop;
        				setIcon(buttonArray, START_BUTTON, PLAY_BUTTON_ICON);
                        buttonArray[START_BUTTON].setText("   Start");
        			}
        			SETTINGS.setSetting(timeouts, Setting.TIMEOUTS);
        			updateFile(total);
    			} 
    			buttonArray[TIMEOUT_BUTTON].setText(" [T]imeouts: " + timeouts);
    		}
    	});
    	setButtonKey(this.buttonArray[TIMEOUT_BUTTON], TIMEOUT_BUTTON_KEY, 0);
    }
    
    // Post: Adds all the Components created to the ManagementPanel and formats the ManagementPanel.
    public void addElements() {
    	// The panel that stores the header with 'Roster Management' that appears at the top of the
    	// ManagementPanel.
    	JLabel header = formatLabel("   Roster Management", FONT_SIZE * 5 / 6, SETTINGS);
    	setIcon(header, ROSTER_MANAGEMENT_ICON);
        
        startTimerWindow();
        
        FlowLayout layout = new FlowLayout(1, 20, 10);
        // The first row of the ManagementPanel that has the 'Score' label, the 'Box Score', 'Undo', and 'Done' buttons
        JPanel buttonHeader = new JPanel();
        setIcon(this.score, SCORE_BUTTON_ICON);
        formatPanel(buttonHeader, new Component[] {this.score, 
        		                                   this.buttonArray[BOXSCORE_BUTTON], 
        		                                   this.buttonArray[UNDO_BUTTON], 
        		                                   this.buttonArray[DONE_BUTTON]}, 
        		    null, layout, background);
        // The second row of the ManagementPanel that has the current time and period of the game (timePanel), the 'Start'
        // and 'Timeout' buttons and the 'Team Fouls' label.
        JPanel timerHeader = new JPanel();
        setIcon(this.teamFouls, TEAMFOULS_ICON);
        formatPanel(timerHeader, new Component[] {timePanel, 
        		                                  this.buttonArray[START_BUTTON], 
        		                                  this.buttonArray[TIMEOUT_BUTTON], 
        		                                  this.teamFouls}, 
        		    null, layout, background);
        
        // The panel that stores all of the components above in a 3 x 1 grid with the header coming first,
        // then the first row of buttons, followed by the second row of buttons.
        formatPanel(this.headerPanel, new Component[] {panelfy(header, background, new GridBagLayout(), null), 
        		                                       buttonHeader, timerHeader},
        		    new MatteBorder(0, 0, BORDER_SIZE * 4, 0, background), new GridLayout(3, 1), background);
        
        // The panel that stores the 'Bench' label that appears above all the players currently on the bench
        JPanel bench = panelfy(formatLabel("Bench", FONT_SIZE * 2 / 3, SETTINGS), DEFAULT_BACKGROUND_COLOR, null,
        		               new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, DEFAULT_TEXT_BORDER_COLOR));
        // The panel that stores all the players on the bench. A scroll bar is added if the number of players on the bench
        // is large
        JPanel menu = new JPanel();
        formatPanel(menu, new Component[] {bench, addScrollPane(this.playersOnBenchPanel)}, null, null, background);
        // The panel that stores all the players currently on the court
        JPanel onCourt = panelfy(formatLabel("Currently on Court", FONT_SIZE * 2 / 3, SETTINGS), DEFAULT_BACKGROUND_COLOR,
        		                 null, new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, DEFAULT_TEXT_BORDER_COLOR));
        JPanel onCourtPanel = new JPanel();
        formatPanel(onCourtPanel, new Component[] {onCourt, addScrollPane(this.onFloorPanel)}, null, null, background);
        
        // Switches which side the bench panel and the on court panel are based on if the user selected
        // left/right handed
        if (!this.splitPane) {
            JPanel pane = new JPanel(new GridLayout(1, 2));
            if (RIGHT_HANDED) {
            	pane.add(menu);
            	pane.add(onCourtPanel);
            } else {
            	pane.add(onCourtPanel);
            	pane.add(menu);
            }
        	pane.setBackground(background);
            this.managementPanel = pane;
        } else {
            this.managementPanel = this.playersOnBenchPanel;
        }
    }
    
    // Post: Starts the loop that updates the 'timePanel' which stores the timer for the game.
    private void startTimerWindow() {
    	// Timer
        this.timePanel = new JPanel();
        JLabel currentTime = formatLabel(getPeriodText(), FONT_SIZE * 6 / 7, SETTINGS);
        this.timePanel.add(currentTime);
        Timer timer = new Timer();
        TimerTask t = new TimerTask() {
            public void run() {
            	// If the clock is at 0.0, then it is time for the next period of the game.
                if (clock.isRunning()) {
                	if (clock.getTime().equals("0.0")) {
                        buttonArray[START_BUTTON].setText("   Start");
                        startStop = !startStop;
                        setIcon(buttonArray, START_BUTTON, PLAY_BUTTON_ICON);
                        clock.setTime((int) ((double) SETTINGS.getSetting(Setting.GAME_LENGTH) * 10 * 60));
                        if (clock.isRunning()) {
                            clock.stopTimer();
                        }
                        teamFoulsInPeriod = 0;
                        period++;
                        SETTINGS.setSetting(period, Setting.CURRENT_PERIOD);
                        // If the last period of the game is over, then the current period is set to END_OF_GAME to signal the
                        // end of the game.
                        if (((isQuarters && period > HALVES) || (!isQuarters && period > QUARTERS)) && !gameOver) {
                        	AbstractAction yesButton = new AbstractAction() {
                        		public void actionPerformed(ActionEvent e) {
                        			SETTINGS.setSetting(period, Setting.CURRENT_PERIOD);
                        			double gameLength = (double) SETTINGS.getSetting(Setting.OVERTIME_LENGTH);
                        			SETTINGS.setSetting(gameLength, Setting.TIME_REMAINING);
                        			SETTINGS.setSetting(gameLength, Setting.GAME_LENGTH);
                        			clock.setTime((int) ((double) SETTINGS.getSetting(Setting.GAME_LENGTH) * 10 * 60));
                        			pane.removeAll();
                        			setPane();
                        			gameOver = false;
                        			timeouts = (int) SETTINGS.getSetting(Setting.OT_TIMEOUTS);
                        			buttonArray[TIMEOUT_BUTTON].setText(" [T]imeouts: " + timeouts);
                        			buttonArray[TIMEOUT_BUTTON].setEnabled(true);
                                    setTimerLabel(timer, currentTime);
                        		}
                        	};
                        	AbstractAction noButton = new AbstractAction() {
                        		public void actionPerformed(ActionEvent e) {
                        			period = END_OF_GAME;
                                    SETTINGS.setSetting(END_OF_GAME, Setting.CURRENT_PERIOD);
                        			pane.removeAll();
                        			setPane();
                        			gameOver = true;
                        		}
                        	};
                        	confirmPane(pane, frame, noButton, yesButton, "Overtime?", OVERTIME_ICON, SETTINGS);
                        }
                    	updateFile(total);
                    }
                	// Increment the time the player is on the court
                    for (Player p : players) {
                        p.add(true, "MIN");
                    }
                    SETTINGS.setSetting(clock.getTenthsTime() / 600.0, Setting.TIME_REMAINING);
                    // Update the time the players are on the court every minute
                    if (clock.getTime().endsWith(":00")) {
                        updateFile(total);
                    }
                    if (timePanel.isDisplayable()) {
                    	setTimerLabel(timer, currentTime);
                    } 
                }           
            }
        };
        timer.schedule(t, 0, TIMER_SETTING);        
        this.timePanel.setBackground(background);
    }
    
    // Post: Sets the current period number and time left in the period for the timer.
    private void setTimerLabel(Timer timer, JLabel currentTime) {
    	String result = getPeriodText();
        if (period == END_OF_GAME) { // If the game has ended
            buttonArray[START_BUTTON].setEnabled(false);
            // Disable all buttons
            for (JButton btn : playerButtons) {
                btn.setEnabled(false);
            }
            if (clock.isRunning()) {
            	clock.stopTimer();
            }
            // Stop the timer
            timer.cancel();
            timer.purge();
        }
        // Label showing the current period and time of game
        currentTime.setText(result);
        currentTime.revalidate();
        currentTime.repaint();
    }
    
    // Post: Opens a window asking the user to substitute the given Player 'play' for a new player.
    // Parameters:
    // 'play': The player being substituted.
    // 'players': The list of Players from which the substitute is chosen.
    // 'bench': The list of Players from which the player is pulled out of.
    // 'freeze': Whether a player is being substituted because they have fouled out of the game (If so freeze: true)
    private void subNewPlayer(Player play, List<Player> players, List<Player> bench, boolean freeze) {
    	this.frame.setTitle("Substitution - " + play.toString() + " for:");
    	this.pane.removeAll();
    	this.pane.setLayout(new GridBagLayout());
    	JLabel label = formatLabel(" SUBSTITUTION", FONT_SIZE * 3 / 4, SETTINGS);
    	JPanel header = panelfy(label, background, new GridBagLayout(), 
    			                new MatteBorder(BORDER_SIZE * 2, 0, BORDER_SIZE * 3, 0, background));
    	setIcon(label, SUB_PLAYER_ICON);
    	JPanel subHeader = panelfy(formatLabel(play.toString() + " for...", FONT_SIZE * 2 / 3, SETTINGS),
    			                   background, new GridBagLayout(), null);
    	JPanel headers = new JPanel();
    	formatPanel(headers, new Component[] {header, subHeader},
    			    new MatteBorder(0, 0, 0, BORDER_SIZE * 10, background),
    			    new GridLayout(2, 1), background);
    	this.pane.add(headers);
    	JPanel buttons = new JPanel();
    	buttons.setBorder(new MatteBorder(0, BORDER_SIZE, 0, 0, DEFAULT_TEXT_BORDER_COLOR));
        int numberButtons = 0;
        JPanel subPanel = new JPanel();
        subPanel.setBackground(background);
        for (Player player : players) {
        	if (!player.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII)) {
        		JButton sub = new JButton("[" + (numberButtons + 1) + "]  " + player.toString());
                formatButton(sub, BUTTON_HEIGHT * 4, BUTTON_HEIGHT, FONT_SIZE / 2, SETTINGS);
                sub.setBorder(null);
                buttons.add(sub);
                sub.addActionListener(new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        bench.remove(play);
                        bench.add(player);
                        players.remove(player);
                        players.add(play);
                        if (freeze) {
                            play.setOffFloor();
                            player.setOnFloor();
                        } else {
                            player.setOffFloor();
                            play.setOnFloor();
                        }              
                        onFloorPanel.revalidate();
                        playersOnBenchPanel.revalidate();
                    	buttonArray[HOME_BUTTON].setText("  Return to Game");
                    	setIcon(buttonArray, HOME_BUTTON, ROSTER_MANAGEMENT_ICON);
                        pane.removeAll();
                        setPane();
                        updateOnFloorPanel();
                    }
                });	
                setButtonKey(sub, this.playerKeys.get(numberButtons + 1), 0);
        		numberButtons++;
        	}
        }
        if (numberButtons == 0) {
        	updateOnFloorPanel();
        } else {
        	int grid = numberButtons;
            if (!freeze && this.buttonArray[HOME_BUTTON].getActionListeners().length > 0) {
            	JButton cancel = new JButton("[ESC] ::: Cancel");
            	formatButton(cancel, BUTTON_HEIGHT * 4, BUTTON_HEIGHT, FONT_SIZE / 2, SETTINGS);
            	cancel.setBorder(null);
            	cancel.addActionListener(this.buttonArray[HOME_BUTTON].getActionListeners()[0]);
            	setButtonKey(cancel, KeyEvent.VK_ESCAPE, 0);
            	buttons.add(cancel);
            	grid = players.size() + 1;
            }
            buttons.setLayout(new GridLayout(grid, 1));
            this.pane.add(buttons);
	        this.pane.repaint();
	        this.pane.revalidate();
	        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	        this.playersOnBenchPanel.revalidate();
        }
    }
    
    // Post: Returns a two-dimensional String Array with all the player data. Used for the table from the 'Box Score'
    //       button.
    private String[][] getData() {
    	int rows = this.total.size() + 1;
    	if (this.total.size() == 1) {
    		rows -= 1;
    	}
        String[][] data = new String[rows][STATISTIC_ABBREVIATIONS.length];
        int i = 0;
        for (Player player : this.total){
            for (int column = 0; column < STATISTIC_ABBREVIATIONS.length; column++) {
                data[i][column] = player.getStat(STATISTIC_ABBREVIATIONS[column] + "*").replaceAll("_", " ");
            }
            i++;
        }
        return data;
    }
    
    // Post: Updates the 'onFloorPanel' to include all players currently on the court. Used to update after substitutions
    //       are made.
    private void updateOnFloorPanel() {
        Set<String> fouledOut = new HashSet<String>(this.total.size());
        for (Player player : this.total) {
        	if (player.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII)) {
        		fouledOut.add(player.toString());
        	}
        }
    	if (!this.splitPane) {
    		this.onFloorPanel.removeAll();
        	this.playersOnBenchPanel.removeAll();
        	this.onFloorPanel.setLayout(new GridLayout(this.players.size(), 2));
            Set<String> names = new HashSet<String>(this.players.size());
            for (Player player : this.players) {
            	names.add(player.toString());
            }
            int index = 1;
            int benchIndex = 0;
        	for (JButton playerButton : this.playerButtons) {
        		String text = playerButton.getText();
    			if ("123456789".contains(text.charAt(1) + "")) {
    				text = text.substring(3).trim();
    			} else if (text.contains("Shift")) {
    				text = text.substring(9).trim();
    			}
        		if (names.contains(text)) {
        			playerButton.setText("[" + index + "]" + "  " + text);
        			this.onFloorPanel.add(playerButton);
        			playerButton.setBackground(DEFAULT_BACKGROUND_COLOR);
                    setButtonKey(playerButton, this.playerKeys.get(index), 0);
        			index++;
        		} else {
        			playerButton.setText("[Shift-" + benchIndex + "]" + "  " + text);
        			this.playersOnBenchPanel.add(playerButton);
        			playerButton.setBackground(BENCH_BUTTON_COLOR);
                    setButtonKey(playerButton, this.playerKeys.get(benchIndex), 
                        		 InputEvent.SHIFT_DOWN_MASK);
        			benchIndex++;
        		}
    			playerButton.setEnabled(true);
        		if (fouledOut.contains(text)) {
        			playerButton.setText(text);
        			playerButton.setBackground(FOULED_OUT_BUTTON_COLOR);
        			playerButton.setEnabled(false);
        		}
        	}
        	if (this.onFloorPanel.isDisplayable()) {
            	this.onFloorPanel.repaint();
            	this.onFloorPanel.revalidate();
        	}
        	if (this.playersOnBenchPanel.isDisplayable()) {
            	this.playersOnBenchPanel.repaint();
            	this.playersOnBenchPanel.revalidate();	
        	}
    	} else {
    		for (JButton playerButton : this.playerButtons) {
    			if (fouledOut.contains(playerButton.getText())) {
    				playerButton.setBackground(FOULED_OUT_BUTTON_COLOR);
    				playerButton.setEnabled(false);
    			} else {
    				playerButton.setBackground(DEFAULT_BACKGROUND_COLOR);
    				playerButton.setEnabled(true);
    			}
    		}
    	}
    	this.pane.revalidate();
    	this.pane.repaint();
    }
    
    // Post: Returns a String used to display the time in the ManagementPanel.
    private String getPeriodText() {
        String labelText;
        if (this.period != END_OF_GAME) {
        	if (this.period < GAME_PERIODS.length) {
                labelText = GAME_PERIODS[this.period] + "   " + this.clock.getTime();
        	} else {
        		int OTConstant;
        		if (this.isQuarters) {
        			OTConstant = QUARTERS;
        		} else {
        			OTConstant = HALVES;
        		}
        		labelText = "OT" + (this.period - OTConstant + 1) + "   " + this.clock.getTime();
        	}
        } else {
        	labelText = "FINAL";
        }
        return labelText;
    }
    
    // Post: Opens a window with an array of buttons each labeled with a certain basketball statistic. Clicking on a button
    //       will increment that players statistic and close the window that appeared. In the case of made shots, the user will
    //       be asked if the shot was assisted. In the case of missed shots, the user will be asked if someone on their team
    //       secured the offensive rebound for the shot.
    // Parameters:
    // 'play': The player to which the statistic will be added.
    // 'frame': The frame the window will appear in.
    // 'button': The button that corresponds to the player.
    private void addStatButton(Player play, JButton button) {
        JFrame statsMenu = new JFrame(play.getStatLine());
    	JPanel statsMenuPanel = new JPanel(new GridLayout(STATISTICS.length / 2, 2));
        JButton statButton;
        int fontSize = FONT_SIZE * 5 / 12;
        for (int i = 0; i < STATISTICS.length; i++) {
        	String stat = STATISTICS[i];
            statButton = new JButton("[" + STAT_KEYS[i] + "] ::: " + stat + "  " + play.getStat(stat));
            formatButton(statButton, BUTTON_HEIGHT, BUTTON_HEIGHT, fontSize, SETTINGS);
            statsMenuPanel.add(statButton);   
            Undo recent = new Undo(play, stat, getPeriodText().substring(0, 5).trim() + " " + this.clock.getTime());
            statButton.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						statsMenu.dispose();
					}
				}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyTyped(KeyEvent e) {}          	
            });
            statButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    statButtonFunction(play, stat, recent, button, statsMenu);
                }
            });
            if (STAT_KEYS[i].contains("Shift")) {
                setButtonKey(statButton, this.keyMap.get(STAT_KEYS[i]), InputEvent.SHIFT_DOWN_MASK);
            } else {
                setButtonKey(statButton, this.keyMap.get(STAT_KEYS[i]), 0);
            }
        }
        formatFrame(statsMenu, statsMenuPanel, SCREENWIDTH - (BUTTON_HEIGHT * 4), SCREENHEIGHT - (BUTTON_HEIGHT * 2));
    }
    
    // Post: Opens a window with a button for every statistic available to select.
    private void statButtonFunction(Player play, String stat, Undo recent, JButton button, JFrame frame) {
    	play.add(true, stat);
    	if (play.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII)) {
    		if (stat.equals("Personal Foul")) {
        		this.teamFoulsInPeriod++;
    		}
    		if (!this.splitPane && getFouledOut() >= 1) {
                subNewPlayer(play, bench, players, true);
    		}
    		button.setEnabled(false);
    		button.setBackground(FOULED_OUT_BUTTON_COLOR);
    	} else if (this.numberStarters != 1) {
    		switch (stat) {
        	case "Made FG":
        	case "Made 3pt FG":
        		openNewStatWindow(recent, play, "Assist", "Assisted FG?", "Who got the Assist?", false);
    			break;
            case "Personal Foul":
            	this.teamFoulsInPeriod++;
            case "Technical Foul":
            case "Flagrant I Foul":
            case "Flagrant II Foul":
            	// Stop timer if foul is called
            	if (this.clock.isRunning()) {
            		this.clock.stopTimer();
                    this.startStop = !this.startStop;
                    this.buttonArray[START_BUTTON].setText("   Start");
                    setIcon(this.buttonArray, START_BUTTON, PLAY_BUTTON_ICON);
            	}
            	break;
            case "Missed FG":
            case "Missed 3pt FG":
            case "Missed Free Throw":
            	openNewStatWindow(recent, play, "Offensive Rebound", "Offensive Rebounded?", 
            			          "Who got the Offensive Rebound?", true);
            	break;
            case "Block":
            	openNewStatWindow(recent, play, "Defensive Rebound", "Defensive Rebounded?", 
            			          "Who got the Defensive Rebound?", true);
            	break;
        	}
    	}
        this.buttonArray[UNDO_BUTTON].setEnabled(true);
        this.undo.add(recent);
        updateFile(players);
        this.score.setText("Score: " + getTotal("PTS") + "    ");
    	this.teamFouls.setText("  Team Fouls: " + teamFoulsInPeriod);
    	frame.dispose();
    }
    
    // Post: In the case that the statistic chosen in the method addStatButton is a shot or a missed shot,
    //       a new window opens prompting the user:
    //       -For a shot:
    //       Was the shot assisted? (Yes/No)
    //       -For a missed shot:
    //       Did someone on the users team secure the offensive rebound? (Yes/No)
    // Parameters:
    // 'recent': The Undo object that will be used to chronologically archive this players statistical contribution
    //           to the game.
    // 'play': The player that got the statistic.
    // 'stat': The statistic in question.
    // 'frameText': The text the appears in the Yes/No frame title bar.
    // 'frameHeder': The text that appears in the frame that opens when 'Yes' is clicked in the 'assistFrame'.
    private void openNewStatWindow(Undo recent, Player play, String stat, String frameText, String frameHeader, boolean allPlayers) {
    	JFrame assistFrame = new JFrame(frameText);
        JPanel assistPanel = new JPanel(new GridLayout(2, 1));
        formatFrame(assistFrame, assistPanel, SCREENWIDTH / 3, SCREENHEIGHT / 2);
        JButton yes = new JButton("[Enter] ::: Yes");
        formatButton(yes, BUTTON_HEIGHT, BUTTON_HEIGHT, FONT_SIZE * 2 / 3, SETTINGS);
        yes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int size = players.size();
                if (!allPlayers) {
                	size -= 1;
                } 
                JPanel panel = new JPanel(new GridLayout(size, 1));
                JButton assistPlayerButton;
                int fontSize = FONT_SIZE / 2;
                if (players.size() == 2 && !stat.equals("Offensive Rebound") && !stat.equals("Defensive Rebound")) {
                	int index = players.indexOf(play);
                	int newIndex = 0;
                	if (index == 0) {
                		newIndex = 1;
                	}
                	if (!allPlayers) {
                        score.setText("Score: " + getTotal("PTS") + "    ");
                		players.get(newIndex).add(true, stat);
                    	recent.setAstPlayer(players.get(newIndex));
                	}
                    updateFile(players);
                } else {
                    JFrame frame = new JFrame(frameHeader);
                	int playerNumber = 1;
                    for (Player assistPlayer : players) {
                    	boolean check = assistPlayer != play;
                    	if (allPlayers) {
                    		check = true;
                    	}
                    	if (check && !assistPlayer.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII)) {
                            assistPlayerButton = new JButton("[" + playerNumber + "]  " + assistPlayer.toString());
                            formatButton(assistPlayerButton, BUTTON_HEIGHT, BUTTON_HEIGHT, fontSize, SETTINGS);
                            panel.add(assistPlayerButton);
                            assistPlayerButton.addActionListener(new AbstractAction() {
                                public void actionPerformed(ActionEvent e) {
                                    assistPlayer.add(true, stat);
                                    frame.dispose();
                                    score.setText("Score: " + getTotal("PTS") + "    ");
                                    switch (stat) {
                                    case "Offensive Rebound":
                                    case "Defensive Rebound":
                                		undo.add(new Undo(assistPlayer, stat, recent.getTimeOf()));
                                		break;
                                    default:
                                    	recent.setAstPlayer(assistPlayer);
                                    	break;
                                    }
                                    updateFile(players);
                                }
                            });
                            setButtonKey(assistPlayerButton, playerKeys.get(playerNumber), 0);
                            playerNumber++;
                        }
                    }
                    formatFrame(frame, panel, SCREENWIDTH / 3, SCREENHEIGHT / 2 + (SCREENHEIGHT / 6));
                }
                assistFrame.dispose();
            }
        });
        assistPanel.add(yes);
        noButton(assistFrame, assistPanel, FONT_SIZE * 2 / 3, BUTTON_HEIGHT, BUTTON_HEIGHT, "[ESC] ::: No", SETTINGS, false);
		yes.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					assistFrame.dispose();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}			
		});
        assistFrame.getRootPane().setDefaultButton(yes);
    }
    
    // Post: Updates the file with 'fileName' to have all the current statistics of each player and the time remaining
    //       and current period of the game.
    private void updateFile(List<Player> players) {
        CreateFile gameFile = new CreateFile(this.fileName);
        gameFile.openFile();
        gameFile.addGameConfig(SETTINGS);
        for (Player player : players){
            for (int column = 0; column < STATISTIC_ABBREVIATIONS.length; column++) {
            	gameFile.addInformation(STATISTIC_ABBREVIATIONS[column], player.getStat(STATISTIC_ABBREVIATIONS[column]));
            }
        	gameFile.addInformation("OnCourt", player.isStarter() + "");
        }
        gameFile.addUndo(this.undo);
        gameFile.closeFile();
    }
    
    // Post: Resets the window to have all the components of the standard ManagementPanel.
    private void setPane() {
    	this.frame.setTitle("Roster Management - " + this.fileName);
    	this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	if (this.splitPane) {
        	this.pane.setLayout(new BoxLayout(this.pane, BoxLayout.Y_AXIS));
        	this.pane.add(this.headerPanel);
        	this.pane.add(this.managementPanel);
    	} else {
    		this.pane.setLayout(new BorderLayout());
        	this.pane.add(this.headerPanel, BorderLayout.NORTH);
        	this.pane.add(this.managementPanel, BorderLayout.CENTER);
    	}
		this.pane.revalidate();
		this.pane.repaint();
    }
    
    // Post: Puts all the Components of the ManagementPanel into a window that opens when the user starts the game.
    public void frame() {
    	setPane();
    	this.frame.add(this.pane);
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.getRootPane().setDefaultButton(this.buttonArray[START_BUTTON]);
    }
    
    // Post: Returns a JPanel with all the components of a ManagementPanel. Used in the InstructionsPanel.
    public JPanel getManagementPanel() {
        JPanel panel = new JPanel();
        panel.add(this.headerPanel, BorderLayout.NORTH);
        JPanel managementP = panelfy(this.managementPanel, background, new GridLayout(1, 1), null);
        managementP.setPreferredSize(new Dimension(SCREENWIDTH - (BUTTON_HEIGHT * 5 / 2), 700));
        panel.setBorder(new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, DEFAULT_TEXT_BORDER_COLOR));
        panel.setPreferredSize(new Dimension(SCREENWIDTH - (BUTTON_HEIGHT * 5 / 2), 1100));
        panel.add(managementP);
        panel.setBackground(background);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }
    
    // Post: Returns the number of players who have fouled out of the game.
    private int getFouledOut() {
        int fouledOut = 0;
        for (Player player : this.bench) {
            if (!player.hasFouledOut(personalFouls, technicalFouls, flagrantI, flagrantII)) {
                fouledOut++;
            }
        }
        return fouledOut;
    }
    
    // Post: Returns the total of the statistic 'stat' for the entire team.
    private int getTotal(String stat) {
        int totalPoints = 0;
        for (Player play : this.total) {
            totalPoints += Integer.valueOf(play.getStat(stat));
        }
        return totalPoints;
    }
}

