

import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;

public class Test extends GUISettings implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		//String line = "Jacob, Swierz, Jacob S., Alex, Eidt, Alex E., Made 3pt FG, 1st 3:34";
		//String line = "DONE, DONE, DONE, TEST, TEST, TEST, DONE, DONE";
		String line = "Markus, Schiffer, Markus S., TEST, TEST, TEST, Made Free Throw, OT3 1:21";
    	Pattern undoMatch = Pattern.compile("([A-Za-z\\.\\s]+,){6}\\s[A-Za-z\\s0-9]+,\\s[1-4]([a-z]{2})\\s[0-9]:[0-9]{2}");
        Matcher match = undoMatch.matcher(line);
		Path pathToDirectory = Paths.get("");
		String path = pathToDirectory.toAbsolutePath().toString();
		String result = "Boxscore_" + DAYNUM + "." + MONTH + "." + YEAR;
        System.out.println(getFileName(result));
		countLines();	
		System.out.println(Integer.MAX_VALUE);
		int[] playerKey = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5};
		for (int i : playerKey) {
			System.out.println(i);
		}
		/*
		JFrame frame = new JFrame();
		//frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.dispose();
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {}			
		});*/
	}
	
    public static String getFileName(String name) {
    	File file = new File(".");
    	List<String> names = new ArrayList<String>();
    	boolean found = false;
    	for (File f : file.listFiles()) {
    		if (f.getName().contains(name)) {
    			found = true;
    			break;
    		}
    	}
    	if (!found) {
    		return name;
    	} else {
    		for (File f : file.listFiles()) {
        		String n = f.getName().trim();
        		if (n.contains(FILETYPE)) {
        			n = n.replace("." + FILETYPE, "");
        			char[] c = n.toCharArray();
        			int l = c.length;
        			if (c[l - 2] == '_') {
                		names.add(c[l - 1] + "");
        			} else {
        				names.add(null);
        			}
        		}
        	}
        	if (!names.contains(null)) {
        		return name;
        	} else {
        		for (int i = 1; i <= names.size(); i++) {
        			if (names.contains(i + "")) {
        				continue;
        			} else {
        				return name + "_" + i;
        			}
        		}
        	}
    	}    	
    	return null;
    }
    
    // Post: Returns the number of .BBALL files in the directory.
	public static int countFiles() {
    	File file = new File(".");
        int countCopy = 0;
        for (File f : file.listFiles()) {
            if (f.getName().contains(FILETYPE)) {
            	countCopy++;
            }
        }
        return countCopy;
    }
	
	public static void countLines() {
		File newFile = new File(".");
		int count = 0;
		for (File file : newFile.listFiles()) {
			String fileName = file.getName();
			if (fileName.contains("src")) {
				for (File f : file.listFiles()) {
					String name = f.getName();
					if (name.endsWith(".java")) {
						Scanner scanner = null;
						try {
							scanner = new Scanner(f);
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
						while (scanner.hasNextLine()) {
							String line = scanner.nextLine();
							if (!line.isEmpty()) {
								count++;
							}
						}
						scanner.close();
					}
				}
				break;
			}
		}		
		System.out.println(count);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}
