package de.benpicco.jChan;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.benpicco.libchan.clichan.ArchiveOptions;
import de.benpicco.libchan.clichan.ChanManager;
import de.benpicco.libchan.clichan.ThreadArchiver;
import de.benpicco.libchan.util.FileUtil;
import de.benpicco.libchan.util.Logger;
import de.benpicco.libchan.util.LoggerBackend;

public class SwingChan {

	private JFrame		frmLibchan;
	private JTextField	urlField;
	private JTextField	targetField;
	private JTextField	voccTextField;

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
				"Definitions for "
						+ new ChanManager(FileUtil.getJarLocation(SwingChan.this) + "chans").getSupported().size()
						+ " imageboards present.");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLibchan = new JFrame();
		frmLibchan.setTitle("libChan " + ThreadArchiver.VERSION);
		frmLibchan.setBounds(100, 100, 520, 433);
		frmLibchan.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLibchan.getContentPane().setLayout(null);

		urlField = new JTextField();
		urlField.setText("http://");
		urlField.setBounds(12, 12, 392, 19);
		frmLibchan.getContentPane().add(urlField);
		urlField.setColumns(10);

		final JCheckBox chckbxDowloadImages = new JCheckBox("dowload Images");
		chckbxDowloadImages.setSelected(true);
		chckbxDowloadImages.setBounds(12, 68, 163, 23);
		frmLibchan.getContentPane().add(chckbxDowloadImages);

		final JCheckBox chckbxDownloadHtml = new JCheckBox("download html");
		chckbxDownloadHtml.setBounds(12, 95, 153, 23);
		frmLibchan.getContentPane().add(chckbxDownloadHtml);

		final JCheckBox chckbxGenerateStatistics = new JCheckBox("generate statistics");
		chckbxGenerateStatistics.setBounds(12, 146, 192, 23);
		frmLibchan.getContentPane().add(chckbxGenerateStatistics);

		final JCheckBox chckbxDownloadVocarooLinks = new JCheckBox("download Vocaroo links");
		chckbxDownloadVocarooLinks.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				voccTextField.setEnabled(chckbxDownloadVocarooLinks.isSelected());
			}
		});
		chckbxDownloadVocarooLinks.setBounds(233, 146, 218, 23);
		frmLibchan.getContentPane().add(chckbxDownloadVocarooLinks);

		final JCheckBox chckbxFollowNewThreads = new JCheckBox("follow new threads");
		chckbxFollowNewThreads.setSelected(true);
		chckbxFollowNewThreads.setBounds(12, 173, 171, 23);
		frmLibchan.getContentPane().add(chckbxFollowNewThreads);

		final JSpinner intervalSpinner = new JSpinner();
		intervalSpinner.setModel(new SpinnerNumberModel(new Integer(45), new Integer(1), null, new Integer(1)));
		intervalSpinner.setEnabled(false);
		intervalSpinner.setBounds(306, 97, 42, 20);
		frmLibchan.getContentPane().add(intervalSpinner);

		final JCheckBox chckbxMonitorThreads = new JCheckBox("monitor threads");
		chckbxMonitorThreads.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				intervalSpinner.setEnabled(chckbxMonitorThreads.isSelected());
			}
		});
		chckbxMonitorThreads.setBounds(231, 68, 171, 23);
		frmLibchan.getContentPane().add(chckbxMonitorThreads);

		final JLabel lblInterval = new JLabel("interval");
		lblInterval.setBounds(241, 99, 70, 15);
		frmLibchan.getContentPane().add(lblInterval);

		final JLabel lblSeconds = new JLabel("seconds");
		lblSeconds.setBounds(356, 96, 70, 21);
		frmLibchan.getContentPane().add(lblSeconds);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 218, 476, 173);
		frmLibchan.getContentPane().add(scrollPane);

		final JTextPane textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		textPane.setEditable(false);

		Logger.add(new TextPaneLogger(textPane));

		targetField = new JTextField();
		targetField.setBounds(12, 41, 392, 19);
		frmLibchan.getContentPane().add(targetField);
		targetField.setColumns(10);

		final JButton button = new JButton("...");
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
		button.setBounds(416, 38, 35, 25);
		frmLibchan.getContentPane().add(button);

		targetField.setText(System.getProperty("user.home") + File.separator + "libChan");

		final JCheckBox chckbxSeperateFolderFor = new JCheckBox("seperate folder for threads");
		chckbxSeperateFolderFor.setSelected(true);
		chckbxSeperateFolderFor.setBounds(234, 119, 239, 23);
		frmLibchan.getContentPane().add(chckbxSeperateFolderFor);

		final JCheckBox chckbxDeleteDeletedImages = new JCheckBox("delete deleted images");
		chckbxDeleteDeletedImages.setBounds(22, 119, 218, 23);
		frmLibchan.getContentPane().add(chckbxDeleteDeletedImages);

		final JButton btnOk = new JButton("ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ArchiveOptions opts = new ArchiveOptions();
				opts.interval = chckbxMonitorThreads.isSelected() ? (Integer) intervalSpinner.getValue() : 0;
				opts.chanConfig = FileUtil.getJarLocation(SwingChan.this) + "chans" + File.separator;
				opts.htmlTemplate = FileUtil.getJarLocation(SwingChan.this) + "template" + File.separator;
				opts.delete = chckbxDeleteDeletedImages.isSelected();
				opts.followUpTag = chckbxFollowNewThreads.isSelected() ? "NEW THREAD" : null;
				opts.recordStats = chckbxGenerateStatistics.isSelected();
				opts.saveHtml = chckbxDownloadHtml.isSelected();
				opts.saveImages = chckbxDowloadImages.isSelected();
				opts.target = targetField.getText();
				opts.threadFolders = chckbxSeperateFolderFor.isSelected();
				opts.vocaroo = chckbxDownloadVocarooLinks.isSelected() ? voccTextField.getText().split(",") : null;

				for (Component c : frmLibchan.getContentPane().getComponents())
					c.setEnabled(false);

				ThreadArchiver archiver = new ThreadArchiver(opts);
				archiver.addThread(urlField.getText());
				new Thread(new BackgroundThread(archiver)).start();
			}
		});
		btnOk.setBounds(416, 9, 57, 25);
		frmLibchan.getContentPane().add(btnOk);

		voccTextField = new JTextField();
		voccTextField.setEnabled(false);
		voccTextField.setBounds(256, 177, 232, 19);
		frmLibchan.getContentPane().add(voccTextField);
		voccTextField.setColumns(10);

		JLabel lblOnlyFrom = new JLabel("only by");
		lblOnlyFrom.setBounds(197, 177, 111, 15);
		frmLibchan.getContentPane().add(lblOnlyFrom);
	}

	class BackgroundThread implements Runnable {
		private final Runnable	task;

		public BackgroundThread(Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			task.run();

			for (Component c : frmLibchan.getContentPane().getComponents())
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