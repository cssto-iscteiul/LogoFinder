package client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class Frame {

	private JFrame frame;
	private TextField folder;
	private TextField image;
	private Button folderB;
	private Button imageB;
	private Button search;
	private JPanel bottomP;
	private JPanel bottom;
	private JLabel imageToSearch;
	private JList<File> rightList;
	private JList<String> leftList;
	private BufferedImage logoToSearch;
	private BufferedImage imageToCompare;
	private String[] data = new String[3];
	DefaultListModel<File> listModel = new DefaultListModel<>();
	//private File[] images = new File[50];
	private Client client;
	private String finalPath;

	public Frame(Client client) {

		this.client = client;
		frame = new JFrame("Logo Finder");
		bottomP = new JPanel();
		bottom = new JPanel();

		frame.setLayout(new BorderLayout());
		bottom.setLayout(new GridLayout(2,1,3,3));
		bottomP.setLayout(new GridLayout(2,2,3,3));

		leftList = new JList<String>(data);
		rightList = new JList<File>(listModel);
		folder = new TextField("No Folder has been selected.");
		image = new TextField("No Image has been selected.");
		search = new Button("Search");
		folderB = new Button("Select Folder");
		imageB = new Button("Select Image");
		imageToSearch = new JLabel("", SwingConstants.CENTER);


		imageToSearch.setIcon(null);
		imageToSearch.setText("No Image Selected.");

		bottomP.add(folder);
		bottomP.add(folderB);
		bottomP.add(image);
		bottomP.add(imageB);
		bottom.add(bottomP);
		bottom.add(search);

		frame.add(leftList, BorderLayout.WEST);
		frame.add(rightList, BorderLayout.EAST);
		frame.add(bottom, BorderLayout.SOUTH);
		frame.add(new JScrollPane(imageToSearch), BorderLayout.CENTER);

		leftList.setFixedCellWidth(100);
		leftList.setFixedCellHeight(20);
		rightList.setFixedCellWidth(100);
		rightList.setFixedCellHeight(20);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, 
						"Are you sure you want to close this window?", "Close Window?", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					client.endConnection();
					System.exit(0);
				}
			}
		});


		folderB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				// For Directory
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				int rVal = fileChooser.showOpenDialog(null);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					folder.setText(fileChooser.getSelectedFile().toString());
					String path = fileChooser.getSelectedFile().toString();
					setPath(path);
					/*
					if(!rightList.isSelectionEmpty()) {
						setImageToSearch();
					}
					 */
				}
			}
		});

		imageB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				// For Image
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				int rVal = fileChooser.showOpenDialog(null);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					image.setText(fileChooser.getSelectedFile().getName());
					try {
						logoToSearch = ImageIO.read(fileChooser.getSelectedFile());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(leftList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(frame, "Select search type!");
				} else if (folder.getText().equalsIgnoreCase("No Folder has been selected.")) {
					JOptionPane.showMessageDialog(frame, "Select image folder!");
				} else if (image.getText().equalsIgnoreCase("No Image has been selected.")) {
					JOptionPane.showMessageDialog(frame, "Select logo to find!");
				} else {
					client.requestSearch(logoToSearch, finalPath, leftList.getSelectedValue());
				}
			}
		});
	}

	public void setPath(String path) {

		finalPath = path.replaceAll("\\\\", "/");
	}

	public void updateList(LinkedList<File> files) {	

		class FileRenderer extends JLabel implements ListCellRenderer<File> {

			@Override
			public Component getListCellRendererComponent(JList<? extends File> list, File file, int index,
					boolean isSelected, boolean cellHasFocus) {

				setIcon(null);
				setText(file.getName());

				return this;
			}

		}

		for(int i=0; i!=files.size(); i++) {
			listModel.add(i,files.get(i));
		}

		rightList.setCellRenderer(new FileRenderer());
		rightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		frame.revalidate();
		frame.repaint();

		MouseListener ml = (MouseListener) new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1) {
					setImageToSearch();
				}
			}
		};
		rightList.addMouseListener(ml);

	}

	public void open(){
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1000, 800);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	private void setImageToSearch(){
		if(!rightList.isSelectionEmpty()){
			rightList.addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent event) {

							try {
								imageToCompare = ImageIO.read(listModel.get(rightList.getSelectedIndex()));
								imageToSearch.setText(null);
								imageToSearch.setIcon(new ImageIcon(imageToCompare));
							} catch (IOException e1) {
								System.out.println("ERROR: couldn't display image!");
								e1.printStackTrace();
							}
						}
					});
		}
	}

	public void updateSearchList(LinkedList<String> searchTypes) {
		for(int i = 0; i < searchTypes.size(); i++) {
			data[i] = searchTypes.get(i);
		}
		frame.revalidate();
		frame.repaint();
	}

	public String getPath() {
		return finalPath;
	}

}

