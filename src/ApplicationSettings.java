import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.google.gson.*;
import com.google.gson.annotations.Expose;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ApplicationSettings {
	JFrame settingsFrame;
	JTabbedPane settingsPane = new JTabbedPane();
	@Expose
	InterfaceSettings interfaceSettings;
	@Expose
	AdvancedSettings advancedSettings;
	FileSearchManager fileMgr;
	private final int FRAME_WIDTH = 750;
	private final int FRAME_HEIGHT = 280;
	public static final String SETTINGS_FILE = "settings.json";
	JButton okButton = new JButton("OK");
	JButton cancelButton;
	static boolean wasChanged;
	boolean firstTime = true;
	
	public ApplicationSettings(FileSearchManager fileMgr) {
		this.fileMgr = fileMgr;
	}
	
	public static void notifyChange() {
		wasChanged = true;
	}
	
	public void showSettings() throws UnsupportedEncodingException, HeadlessException, SecurityException {
		if (!firstTime) {
			settingsFrame.setVisible(true);
			settingsFrame.toFront();
			//settingsFrame.requestFocus();
		} else {
			firstTime = false;
			settingsFrame = new JFrame(new String(FileSearchManager.prop.getProperty("settingsConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
			settingsFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			settingsFrame.setLayout(new BorderLayout());
			URL faviconURL = getClass().getResource(FileSearchManager.FAVICON_URL);
	        if (faviconURL != null) {
		        ImageIcon favicon = new ImageIcon(faviconURL);
		        settingsFrame.setIconImage(favicon.getImage());
	        }
	        settingsFrame.toFront();
	        //settingsFrame.requestFocus();
	        settingsFrame.setState(JFrame.NORMAL);
	        //settingsFrame.setAlwaysOnTop(true);
			/*try {
				UIManager.setLookAndFeel(interfaceSettings.theme);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}*/
			settingsFrame.add(settingsPane, BorderLayout.CENTER);
			//interfaceSettings.initPanel();
			settingsPane.addTab(new String(FileSearchManager.prop.getProperty("interfaceConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), interfaceSettings.getPanel());
			settingsPane.addTab(new String(FileSearchManager.prop.getProperty("advancedSettingsConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), advancedSettings.getPanel());
			JPanel buttonPanel = new JPanel(new FlowLayout());
			
			okButton.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (wasChanged) {
						try {
							saveSettings(new File(SETTINGS_FILE));
							wasChanged = false;
							JOptionPane.showMessageDialog(settingsFrame, new String(FileSearchManager.prop.getProperty("changesAppliedMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("ordinaryTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.INFORMATION_MESSAGE);
							settingsFrame.setVisible(false);
							//settingsFrame.dispatchEvent(new WindowEvent(settingsFrame, WindowEvent.WINDOW_CLOSING));
						} catch (IOException e) {
							e.printStackTrace();
						}
						wasChanged = false;
					}
						settingsFrame.setVisible(false);
						//settingsFrame.dispatchEvent(new WindowEvent(settingsFrame, WindowEvent.WINDOW_CLOSING));
				}
				
			});
			//okButton.setPreferredSize(new Dimension(50, 20));
			buttonPanel.add(okButton);
			cancelButton = new JButton(new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)));
			cancelButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (wasChanged) {
						try {
							Object[] customAlt = {new String(FileSearchManager.prop.getProperty("yesConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("noConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING))};
							int answ = JOptionPane.showOptionDialog(settingsFrame, new String(FileSearchManager.prop.getProperty("settingsChangedOnExitMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)),
					                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					                null, customAlt, null);
							//int answ = JOptionPane.showConfirmDialog(settingsFrame, new String(FileSearchManager.prop.getProperty("settingsChangedOnExitMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							if (answ == JOptionPane.YES_OPTION) {
								try {
									saveSettings(new File(SETTINGS_FILE));
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
							wasChanged = false;
						} catch (UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
					settingsFrame.setVisible(false);
					//settingsFrame.dispatchEvent(new WindowEvent(settingsFrame, WindowEvent.WINDOW_CLOSING));
				}
			});
			//cancelButton.setPreferredSize(new Dimension(50, 20));
			buttonPanel.add(cancelButton);
			settingsFrame.add(buttonPanel, BorderLayout.SOUTH);

			settingsFrame.setLocationRelativeTo(null);
			settingsFrame.setVisible(true);
		}
	}
	
	public Font getFont() {
		return interfaceSettings.font;
	}
	
	public boolean hasSsdDrives() {
		return advancedSettings.advancedSearch;
	}
	
	/*public File[] getSsdDrives() {
		int len = advancedSettings.ssdDrives.size();
		File[] ssdDrives = new File[len];
		for (int i = 0; i < len; i++) {
			ssdDrives[i] = new File(advancedSettings.ssdDrives.get(i));
		}
		return ssdDrives;
	}*/
	
	public void initSettings(File jsonFile) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		if (jsonFile.exists() && jsonFile.length() > 0) {
			FileReader fr = new FileReader(jsonFile);
			BufferedReader br = new BufferedReader(fr);
			GsonBuilder builder = new GsonBuilder();
			builder.excludeFieldsWithoutExposeAnnotation();
			Gson gson = builder.create();
			
			String json = br.readLine();
			interfaceSettings = gson.fromJson(json, InterfaceSettings.class);
			interfaceSettings.initSettings();
			json = br.readLine();
			advancedSettings = gson.fromJson(json, AdvancedSettings.class);
			if (advancedSettings != null) {
				advancedSettings.initPanel();
			} else {
				advancedSettings = new AdvancedSettings();
				advancedSettings.advancedSearch = false;
			}
			wasChanged = false;
			br.close();			
		} else {
			interfaceSettings = new InterfaceSettings();
			interfaceSettings.font = new Font("Serif", Font.PLAIN, 18);
			interfaceSettings.fontSize = 18;
			interfaceSettings.theme = UIManager.getSystemLookAndFeelClassName();
			interfaceSettings.language = "ru";
			advancedSettings = new AdvancedSettings();
			advancedSettings.advancedSearch = false;
			interfaceSettings.initSettings();
			wasChanged = false;
			UIManager.setLookAndFeel(interfaceSettings.theme);
			saveSettings(jsonFile);
		}
	}
	
	public void saveSettings(File jsonFile) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		Gson gson = builder.create();
		String interfaceJson = gson.toJson(interfaceSettings);
		String advancedJson = gson.toJson(advancedSettings);
		
		FileWriter fw = new FileWriter(jsonFile, false);
		fw.write(interfaceJson+"\n");
		fw.write(advancedJson+"\n");
		fw.flush();
		fw.close();
	}
}

interface Settings {
	public void initPanel();
	public JPanel getPanel();
}

class InterfaceSettings implements Settings {
	@Expose
	String fontName;
	Font font;
	@Expose
	int fontSize;
	@Expose
	String theme;
	@Expose
	String language;
	PropertiesHolder langPropsHolder;
	final static String LANGUAGE_PROPERTIES = "language.properties";
	Locale currentLocale;
	JList<String> fontsList;
	JList<Integer> fontSizeList = new JList<Integer>();
	JComboBox<String> themeBox = new JComboBox<String>();
	JComboBox<String> langBox = new JComboBox<String>();
	JTextField sampleField;
	JPanel panel = new JPanel(new BorderLayout());
	JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	JPanel fontPanel = new JPanel(new BorderLayout());
	
	public InterfaceSettings() {}
	
	public void initSettings() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, IOException {
		System.out.println(language);
		Properties langProps = new Properties();
		langProps.load(new FileInputStream(LANGUAGE_PROPERTIES));
		Properties props = null;
		langPropsHolder = new PropertiesHolder(langProps);
		Set <Entry<Object,Object>> langEntries = langProps.entrySet();
		for (Entry<Object,Object> entry: langEntries) {
			langBox.addItem(/*new String(*/new String(((String)entry.getValue()).getBytes(FileSearchManager.PROPERTY_ENCODING)))/*.getBytes(FileSearchManager.PROPERTY_ENCODING)*/;
			if (entry.getKey().equals(language)) {
				props = new Properties();
				props.load(new FileInputStream("MessagesBundle_"+language+"_"+language.toUpperCase()+".properties"));
				langBox.setSelectedItem(new String(((String)entry.getValue()).getBytes(FileSearchManager.PROPERTY_ENCODING)));
				//break;
			}
		}
		/*switch (language) {
			case "ru": {
				props.load(new FileInputStream("MessagesBundle_ru_RU.properties"));
				//currentLocale = new Locale("ru", "RU");
				break;
			}
			case "en": {
				props.load(new FileInputStream("MessagesBundle_en_US.properties"));
				//currentLocale = new Locale("en", "US");
				break;
			} default: {
				props.load(new FileInputStream("MessagesBundle_en_US.properties"));
				break;
			}
		}*/
		if (props != null) {
			FileSearchManager.prop = new PropertiesHolder(props);
		} else {
			props = new Properties();
			props.load(new FileInputStream("MessagesBundle_ru_RU.properties"));
			FileSearchManager.prop = new PropertiesHolder(props);
		}
		//System.out.println(new String(FileSearchManager.prop.getProperty("myPc").getBytes(FileSearchManager.PROPERTY_ENCODING)));
		initPanel();
		font = new Font(fontName, Font.PLAIN, fontSize);
		fontsList.setSelectedValue(fontName, false);
		fontSizeList.setSelectedValue(fontSize, false);
		fontsList.ensureIndexIsVisible(fontsList.getSelectedIndex());
		fontSizeList.ensureIndexIsVisible(fontSizeList.getSelectedIndex());
		UIManager.setLookAndFeel(theme);
		themeBox.setSelectedItem(theme);
		/*switch (language) {
		case "en": {
			langBox.setSelectedItem("English");
			break;
		}
		case "ru": {
			langBox.setSelectedItem("Русский");
			break;	
		}
		}*/
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void initPanel() {
		panel.setLayout(new GridLayout(2,2));
		//JPanel txtPanel = new JPanel(new BorderLayout());
		try {
			sampleField = new JTextField(new String(FileSearchManager.prop.getProperty("sampleText").getBytes(FileSearchManager.PROPERTY_ENCODING)));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		sampleField.setEditable(false);
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontsList = new JList<String>(fontNames);
        fontsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontsList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				font = new Font(fontsList.getSelectedValue(), Font.PLAIN, fontSize);
				sampleField.setFont(font);
				fontName = font.getFontName();
				ApplicationSettings.notifyChange();
			}
        	
        });
		JScrollPane areaScrollPane = new JScrollPane(fontsList);
		/*fontsList.setPreferredSize(new Dimension(200, 400));
		areaScrollPane.setPreferredSize(new Dimension(200, 400));*/
        
        DefaultListModel<Integer> listModel = new DefaultListModel<Integer>();
        for (int size = 12; size <= 28; size += 2) {
        	listModel.addElement(size);
        }
        listModel.addElement(36);
        listModel.addElement(48);
        listModel.addElement(72);
        fontSizeList = new JList<Integer>(listModel);
        fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontSizeList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				fontSize = fontSizeList.getSelectedValue();
				font = new Font(font.getFontName(), Font.PLAIN, fontSize);
				sampleField.setFont(font);
				ApplicationSettings.notifyChange();
			}
		});
        JScrollPane sizeScrollPane = new JScrollPane(fontSizeList);
        /*fontSizeList.setPreferredSize(new Dimension(50, 400));
        sizeScrollPane.setPreferredSize(new Dimension(50, 400));*/
        
        sampleField.setPreferredSize(new Dimension(150, 25));
        upperPanel.add(sampleField);
        //panel.add(sampleField, BorderLayout.NORTH);
        
        LookAndFeelInfo lnfInfos[] = UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo lnfInfo: lnfInfos) {
        	themeBox.addItem(lnfInfo.getClassName());
        }
        /*BufferedReader br = new BufferedReader(new FileReader("languages.file"));
        String buff = "";*/
        /*while ((buff = new String(br.readLine().getBytes(FileSearchManager.PROPERTY_ENCODING))) != null) {
        	langBox.addItem(buff);
        }
        br.close();*/
        themeBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				theme = (String) themeBox.getSelectedItem();
				ApplicationSettings.notifyChange();
			}
		});
        langBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedLang = (String) langBox.getSelectedItem();
				Properties langProps = langPropsHolder.getProperties();
				try {
					for (Object key: langProps.keySet()) {
						System.out.println(key.toString());
						String value;
						System.out.println(value = new String(langPropsHolder.getProperty(key.toString()).toString().getBytes(FileSearchManager.PROPERTY_ENCODING)));
						if (value.equals(selectedLang)) {
							language = key.toString();
							System.out.println(language);
							break;
						}
					}
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
				/*try {
					if (selectedLang.equals()) {
						language = "en";   
					} else if (selectedLang.equals(new String(FileSearchManager.prop.getProperty("ruLang").getBytes(FileSearchManager.PROPERTY_ENCODING)))) {
						language = "ru";
					}
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}*/
				//language = selectedLang;
				ApplicationSettings.notifyChange();
			}
		});
        themeBox.setPreferredSize(new Dimension(300, 25));
        langBox.setPreferredSize(new Dimension(100, 25));
        try {
	        upperPanel.add(new JLabel(new String(FileSearchManager.prop.getProperty("appTheme").getBytes(FileSearchManager.PROPERTY_ENCODING))+":"));
	        upperPanel.add(themeBox);
	        upperPanel.add(new JLabel(new String(FileSearchManager.prop.getProperty("languageConst").getBytes(FileSearchManager.PROPERTY_ENCODING))+":"));
	        upperPanel.add(langBox);
        } catch (UnsupportedEncodingException ex) {
        	ex.printStackTrace();
        }
        /*txtPanel.add(sampleField, BorderLayout.NORTH);
		txtPanel.add(areaScrollPane, BorderLayout.WEST);
		txtPanel.add(sizeScrollPane, BorderLayout.EAST);*/
        upperPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        fontPanel.add(areaScrollPane, BorderLayout.WEST);
        fontPanel.add(sizeScrollPane, BorderLayout.CENTER);
		panel.add(upperPanel, BorderLayout.NORTH);
		panel.add(fontPanel, BorderLayout.NORTH);
        //panel.add(themeBox);
	}
}

class AdvancedSettings implements Settings {
	@Expose
	boolean advancedSearch;
	@Expose
	ArrayList<String> ssdDrives = new ArrayList<>();
	JCheckBox searchCheckBox = new JCheckBox();
	JPanel panel = new JPanel(); 
	
	public void initPanel() {
		searchCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (searchCheckBox.isSelected()) {
					JPanel dialogPanel = new JPanel();
					JPanel localDrivesPanel = new JPanel();
					localDrivesPanel.setLayout(new BoxLayout(localDrivesPanel, BoxLayout.Y_AXIS));
					ArrayList<JCheckBox> driveBoxes = new ArrayList<>();
					JCheckBox anotherBox;
					try {
						dialogPanel.add(new JLabel(new String(FileSearchManager.prop.getProperty("checkLocalDisks").getBytes(FileSearchManager.PROPERTY_ENCODING))), BorderLayout.NORTH);
						for (Drive drive: FileSearchManager.LOCAL_DRIVES) {
							anotherBox = new JCheckBox(drive.drive.getAbsolutePath());
							driveBoxes.add(anotherBox);
							localDrivesPanel.add(anotherBox);
						}
						dialogPanel.add(localDrivesPanel, BorderLayout.CENTER);
						
						int answ = JOptionPane.showConfirmDialog(null, dialogPanel, new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.OK_CANCEL_OPTION);
						switch (answ) {
						case JOptionPane.OK_OPTION: {
							for (int i = 0; i < driveBoxes.size(); i++) {
								if (driveBoxes.get(i).isSelected()) {
									FileSearchManager.LOCAL_DRIVES[i].isSsd = true;
								}
							}
							ApplicationSettings.notifyChange();
							break;
						}
						case JOptionPane.CANCEL_OPTION: {
							searchCheckBox.setSelected(false);
							break;
						} default: {
							searchCheckBox.setSelected(false);
						}
						}
					} catch (HeadlessException | UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				} else {
					advancedSearch = false;
				}
			}
			
		});
		if (advancedSearch) {
			searchCheckBox.setSelected(true);
		} else {
			searchCheckBox.setSelected(false);
		}
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		try {
			boxPanel.add(new JLabel(new String(FileSearchManager.prop.getProperty("advSearchConst").getBytes(FileSearchManager.PROPERTY_ENCODING))));
			boxPanel.add(searchCheckBox);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		//boxPanel.add(searchCheckBox);
		panel.add(boxPanel, BorderLayout.CENTER);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}
}