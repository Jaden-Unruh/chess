package org.j3lsmp.chessengine;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Entry class for org.j3lsmp.chessengine
 * 
 * @author Jaden
 * @since 0.0.1
 */
@SpringBootApplication
public class ChessEngineApplication {
	
	/**
	 * The current board
	 */
	static ChessBoard board = new ChessBoard();
	
	/**
	 * The back-end application, running with Spring
	 */
	static SpringApplication app;
	
	/**
	 * {@link #app} interface context
	 */
	static ConfigurableApplicationContext context;
	
	/**
	 * Entry method for org.j3lsmp.chessengine
	 * @param args unused
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ChessEngineApplication.class);
		app.setHeadless(false);
		context = app.run(args);
		
		openWindow();
		
		board.resetBoard();
	}
	
	/**
	 * Open the small backend application window directing user to browser (this is why we can't run headless)
	 */
	static void openWindow() {
		JFrame programWindow = new JFrame("Chess by Jaden");
		programWindow.setLayout(new GridBagLayout());
		programWindow.add(new JLabel("<html>To play chess, open a browser and navigate to `localhost:8080`, or press the button below.<br>To exit, press the button below or close this window.</html>"),
				new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		JButton openPage = new JButton("Open player");
		openPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
					try {
						Desktop.getDesktop().browse(new URI("http://localhost:8080"));
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
						warnCannotOpenBrowser();
					}
				else
					warnCannotOpenBrowser();
			}
		});
		JButton closeApp = new JButton("Close application");
		closeApp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shutDownAndClose();
			}
		});
		programWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		programWindow.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				shutDownAndClose();
			}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		});
		programWindow.add(openPage,
				new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		programWindow.add(closeApp,
				new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		programWindow.pack();
		programWindow.setVisible(true);
	}
	
	/**
	 * Open warning dialog that we couldn't detect browser to open localhost in, user must manually navigate.
	 */
	static void warnCannotOpenBrowser() {
		JOptionPane.showMessageDialog(new JFrame(), "Cannot detect a browser to open chess interface. Please manually navigate to `localhost:8080` in a browser", "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Gracefully close and shut down web server and swing application
	 */
	static void shutDownAndClose() {
		System.exit(SpringApplication.exit(context));
	}
}