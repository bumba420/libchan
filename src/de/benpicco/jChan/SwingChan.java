package de.benpicco.jChan;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.ChanCrawler;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.LoggerBackend;
import de.benpicco.libchan.util.ThreadPool;

public class SwingChan {

	private JFrame			frmLibchan;
	private JTextField		urlField;
	private JTextField		targetField;
	private JTextField		voccTextField;
	private JTextField		textField_searchBoard;
	private JTextField		textField_SearchTerms;

	private final String	chanDir	= FileUtil.getJarLocation() + "chans";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingChan window = new SwingChan();
					window.frmLibchan.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingChan() {
		initialize();
		Logger.get().println(
				"Definitions for " + new ChanManager(chanDir).getSupported().size() + " imageboards present.");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLibchan = new JFrame();
		frmLibchan.setResizable(true);
		frmLibchan.setTitle("libChan " + ThreadArchiver.VERSION);
		frmLibchan.setBounds(100, 100, 600, 560);
		frmLibchan.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLibchan.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmLibchan.getContentPane().add(tabbedPane);

		final JPanel panel_archive = new JPanel();
		tabbedPane.addTab("Archive", null, panel_archive, null);
		SpringLayout sl_panel_archive = new SpringLayout();
		panel_archive.setLayout(sl_panel_archive);

		urlField = new JTextField();
		sl_panel_archive.putConstraint(SpringLayout.NORTH, urlField, 15, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, urlField, 22, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, urlField, 34, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, urlField, 414, SpringLayout.WEST, panel_archive);
		panel_archive.add(urlField);
		urlField.setText("http://");
		urlField.setColumns(10);

		final JButton btnOk = new JButton("ok");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, btnOk, 12, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, btnOk, 426, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, btnOk, 37, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, btnOk, 483, SpringLayout.WEST, panel_archive);
		panel_archive.add(btnOk);

		targetField = new JTextField();
		sl_panel_archive.putConstraint(SpringLayout.NORTH, targetField, 44, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, targetField, 22, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, targetField, 63, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, targetField, 414, SpringLayout.WEST, panel_archive);
		panel_archive.add(targetField);
		targetField.setColumns(10);

		targetField.setText(System.getProperty("user.home") + File.separator + "libChan");

		final JButton button = new JButton("...");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, button, 41, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, button, 426, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, button, 66, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, button, 461, SpringLayout.WEST, panel_archive);
		panel_archive.add(button);

		final JCheckBox chckbxDowloadImages = new JCheckBox("dowload Images");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxDowloadImages, 71, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxDowloadImages, 22, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxDowloadImages, 94, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxDowloadImages, 185, SpringLayout.WEST, panel_archive);
		panel_archive.add(chckbxDowloadImages);
		chckbxDowloadImages.setSelected(true);

		final JCheckBox chckbxMonitorThreads = new JCheckBox("monitor threads");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxMonitorThreads, 71, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxMonitorThreads, 241, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxMonitorThreads, 94, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxMonitorThreads, 412, SpringLayout.WEST, panel_archive);
		panel_archive.add(chckbxMonitorThreads);

		final JCheckBox chckbxDownloadHtml = new JCheckBox("download html");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxDownloadHtml, 98, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxDownloadHtml, 22, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxDownloadHtml, 121, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxDownloadHtml, 175, SpringLayout.WEST, panel_archive);
		panel_archive.add(chckbxDownloadHtml);

		final JLabel lblInterval = new JLabel("interval");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, lblInterval, 102, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, lblInterval, 251, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, lblInterval, 117, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, lblInterval, 321, SpringLayout.WEST, panel_archive);
		panel_archive.add(lblInterval);

		final JSpinner intervalSpinner = new JSpinner();
		sl_panel_archive.putConstraint(SpringLayout.NORTH, intervalSpinner, 100, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, intervalSpinner, 316, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, intervalSpinner, 358, SpringLayout.WEST, panel_archive);
		panel_archive.add(intervalSpinner);
		intervalSpinner.setModel(new SpinnerNumberModel(new Integer(45), new Integer(1), null, new Integer(1)));
		intervalSpinner.setEnabled(false);

		final JLabel lblSeconds = new JLabel("seconds");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, lblSeconds, 99, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, lblSeconds, 366, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, lblSeconds, 120, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, lblSeconds, 436, SpringLayout.WEST, panel_archive);
		panel_archive.add(lblSeconds);

		final JCheckBox chckbxSeperateFolderFor = new JCheckBox("separate folders for threads");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxSeperateFolderFor, 122, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxSeperateFolderFor, 244, SpringLayout.WEST,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxSeperateFolderFor, 145, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxSeperateFolderFor, 483, SpringLayout.WEST,
				panel_archive);
		panel_archive.add(chckbxSeperateFolderFor);
		chckbxSeperateFolderFor.setSelected(true);

		final JCheckBox chckbxDeleteDeletedImages = new JCheckBox("delete deleted images");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxDeleteDeletedImages, 125, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxDeleteDeletedImages, 22, SpringLayout.WEST,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxDeleteDeletedImages, 148, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxDeleteDeletedImages, 240, SpringLayout.WEST,
				panel_archive);
		panel_archive.add(chckbxDeleteDeletedImages);

		final JCheckBox chckbxGenerateStatistics = new JCheckBox("generate statistics");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxGenerateStatistics, 149, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxGenerateStatistics, 22, SpringLayout.WEST,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxGenerateStatistics, 172, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxGenerateStatistics, 214, SpringLayout.WEST,
				panel_archive);
		panel_archive.add(chckbxGenerateStatistics);

		final JCheckBox chckbxFollowNewThreads = new JCheckBox("follow new threads");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxFollowNewThreads, 176, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxFollowNewThreads, 22, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxFollowNewThreads, 199, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive
				.putConstraint(SpringLayout.EAST, chckbxFollowNewThreads, 193, SpringLayout.WEST, panel_archive);
		panel_archive.add(chckbxFollowNewThreads);
		chckbxFollowNewThreads.setSelected(true);

		voccTextField = new JTextField();
		sl_panel_archive.putConstraint(SpringLayout.NORTH, voccTextField, 180, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, voccTextField, 266, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, voccTextField, 199, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, voccTextField, 498, SpringLayout.WEST, panel_archive);
		panel_archive.add(voccTextField);
		voccTextField.setEnabled(false);
		voccTextField.setColumns(10);

		JLabel lblOnlyFrom = new JLabel("only by");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, lblOnlyFrom, 180, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, lblOnlyFrom, 207, SpringLayout.WEST, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, lblOnlyFrom, 199, SpringLayout.NORTH, panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, lblOnlyFrom, 270, SpringLayout.WEST, panel_archive);
		panel_archive.add(lblOnlyFrom);

		final JCheckBox chckbxDownloadVocarooLinks = new JCheckBox("download Vocaroo links");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, chckbxDownloadVocarooLinks, 149, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.WEST, chckbxDownloadVocarooLinks, 243, SpringLayout.WEST,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.SOUTH, chckbxDownloadVocarooLinks, 172, SpringLayout.NORTH,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, chckbxDownloadVocarooLinks, 461, SpringLayout.WEST,
				panel_archive);
		panel_archive.add(chckbxDownloadVocarooLinks);

		JLabel lblParallelDownloads = new JLabel("parallel downloads");
		sl_panel_archive.putConstraint(SpringLayout.NORTH, lblParallelDownloads, 6, SpringLayout.SOUTH,
				chckbxFollowNewThreads);
		sl_panel_archive.putConstraint(SpringLayout.WEST, lblParallelDownloads, -178, SpringLayout.WEST, lblInterval);
		sl_panel_archive.putConstraint(SpringLayout.EAST, lblParallelDownloads, 0, SpringLayout.EAST,
				chckbxGenerateStatistics);
		panel_archive.add(lblParallelDownloads);

		final JSpinner spinnerParallelDownloads = new JSpinner();
		sl_panel_archive.putConstraint(SpringLayout.NORTH, spinnerParallelDownloads, 6, SpringLayout.SOUTH,
				chckbxFollowNewThreads);
		sl_panel_archive.putConstraint(SpringLayout.WEST, spinnerParallelDownloads, 22, SpringLayout.WEST,
				panel_archive);
		sl_panel_archive.putConstraint(SpringLayout.EAST, spinnerParallelDownloads, -6, SpringLayout.WEST,
				lblParallelDownloads);
		spinnerParallelDownloads
				.setModel(new SpinnerNumberModel(new Integer(20), new Integer(1), null, new Integer(1)));
		panel_archive.add(spinnerParallelDownloads);
		chckbxDownloadVocarooLinks.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				voccTextField.setEnabled(chckbxDownloadVocarooLinks.isSelected());
			}
		});
		chckbxMonitorThreads.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				intervalSpinner.setEnabled(chckbxMonitorThreads.isSelected());
			}
		});
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(targetField.getText());
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showOpenDialog(frmLibchan) == JFileChooser.APPROVE_OPTION) {
					String target = fc.getSelectedFile().toString().trim();
					if (target.length() > 0)
						targetField.setText(target);
				}
			}
		});
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ThreadPool.setPoolSize((Integer) spinnerParallelDownloads.getValue());

				ArchiveOptions opts = new ArchiveOptions();
				opts.interval = chckbxMonitorThreads.isSelected() ? (Integer) intervalSpinner.getValue() * 1000 : 0;
				opts.chanConfig = chanDir;
				opts.htmlTemplate = FileUtil.getJarLocation() + "template" + File.separator;
				opts.delete = chckbxDeleteDeletedImages.isSelected();
				opts.followUpTag = chckbxFollowNewThreads.isSelected() ? "NEW THREAD" : null;
				opts.recordStats = chckbxGenerateStatistics.isSelected();
				opts.saveHtml = chckbxDownloadHtml.isSelected();
				opts.saveImages = chckbxDowloadImages.isSelected();
				opts.target = targetField.getText();
				opts.threadFolders = chckbxSeperateFolderFor.isSelected();
				opts.vocaroo = chckbxDownloadVocarooLinks.isSelected() ? split(voccTextField.getText()) : null;

				ThreadArchiver archiver = new ThreadArchiver(opts);
				archiver.addThread(urlField.getText());
				new Thread(new BackgroundThread(archiver, panel_archive)).start();
			}
		});

		final JPanel panel_search = new JPanel();
		tabbedPane.addTab("Search", null, panel_search, null);
		panel_search.setLayout(null);

		textField_searchBoard = new JTextField();
		textField_searchBoard.setBounds(61, 4, 347, 24);
		textField_searchBoard.setText("http://");
		textField_searchBoard.setColumns(10);
		panel_search.add(textField_searchBoard);

		JLabel lblBoard = new JLabel("Board:");
		lblBoard.setBounds(10, 7, 62, 18);
		panel_search.add(lblBoard);

		JLabel lblPages = new JLabel("Pages:");
		lblPages.setBounds(10, 46, 62, 18);
		panel_search.add(lblPages);

		final JSpinner spinnerStartPage = new JSpinner();
		spinnerStartPage.setBounds(61, 46, 46, 20);
		spinnerStartPage.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		panel_search.add(spinnerStartPage);

		final JSpinner spinnerEndPage = new JSpinner();
		spinnerEndPage.setBounds(148, 46, 56, 20);
		spinnerEndPage.setModel(new SpinnerNumberModel(new Integer(15), new Integer(0), null, new Integer(1)));
		panel_search.add(spinnerEndPage);

		JLabel lblSearchFor = new JLabel("search for:");
		lblSearchFor.setBounds(10, 80, 97, 18);
		panel_search.add(lblSearchFor);

		textField_SearchTerms = new JTextField();
		textField_SearchTerms.setBounds(95, 78, 313, 22);
		panel_search.add(textField_SearchTerms);
		textField_SearchTerms.setColumns(10);

		JLabel lblTo = new JLabel("to");
		lblTo.setBounds(113, 46, 38, 18);
		panel_search.add(lblTo);

		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(436, 2, 97, 28);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final int start = (Integer) spinnerStartPage.getValue();
				final int end = (Integer) spinnerEndPage.getValue();
				final String board = textField_searchBoard.getText();
				final String[] names = split(textField_SearchTerms.getText());
				if (names.length > 0) {
					Runnable runnable = new Runnable() {

						@Override
						public void run() {
							ChanCrawler.lookFor(names, board, start, end, chanDir);
						}
					};
					new Thread(new BackgroundThread(runnable, panel_search)).start();
				}
			}
		});
		panel_search.add(btnSearch);

		final JScrollPane scrollPane = new JScrollPane();
		frmLibchan.getContentPane().add(scrollPane);

		final JTextPane textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		textPane.setEditable(false);

		Logger.add(new TextPaneLogger(textPane));
	}

	private String[] split(String s) {
		String[] split = s.split(",");
		if (split.length == 1 && split[0].trim().length() == 0)
			return new String[0];
		else
			for (int i = 0; i < split.length; ++i)
				split[i] = split[i].trim();
		return split;
	}

	class BackgroundThread implements Runnable {
		private final Runnable	task;
		private final JPanel	panel;

		public BackgroundThread(Runnable task, JPanel panel) {
			this.task = task;
			this.panel = panel;
		}

		@Override
		public void run() {
			for (Component c : panel.getComponents())
				c.setEnabled(false);
			task.run();

			for (Component c : panel.getComponents())
				c.setEnabled(true);
		}
	}
}

class TextPaneLogger implements LoggerBackend {
	static SimpleAttributeSet		INFO	= new SimpleAttributeSet();
	static SimpleAttributeSet		ERROR	= new SimpleAttributeSet();
	static SimpleAttributeSet		TIME	= new SimpleAttributeSet();

	{
		StyleConstants.setForeground(INFO, Color.black);
		StyleConstants.setFontFamily(INFO, "Courier New");
		StyleConstants.setFontSize(INFO, 14);

		StyleConstants.setForeground(ERROR, Color.red);
		StyleConstants.setFontFamily(ERROR, "Courier New");
		StyleConstants.setBold(ERROR, true);
		StyleConstants.setFontSize(ERROR, 14);

		StyleConstants.setForeground(TIME, Color.blue);
		StyleConstants.setFontFamily(TIME, "Courier New");
		StyleConstants.setFontSize(TIME, 13);
	}

	private final SimpleDateFormat	format	= new SimpleDateFormat("[HH:mm:ss] ");
	private final JTextPane			textPane;

	private String time() {
		return format.format(Calendar.getInstance().getTime());
	}

	public TextPaneLogger(JTextPane textPane) {
		this.textPane = textPane;
	}

	@Override
	public void println(String msg) {
		try {
			textPane.getDocument().insertString(textPane.getDocument().getLength(), time(), TIME);
			textPane.getDocument().insertString(textPane.getDocument().getLength(), msg + "\n", INFO);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void error(String msg) {
		try {
			textPane.getDocument().insertString(textPane.getDocument().getLength(), time(), TIME);
			textPane.getDocument().insertString(textPane.getDocument().getLength(), msg + "\n", ERROR);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
