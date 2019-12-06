package client;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class Client {

	private InetAddress HOST;
	private final int PORT = 8080;
	private Socket s;
	private Frame window;
	private LinkedList<String> searchTypes = new LinkedList<String>();
	private PrintWriter out;
	private byte[] logoBytes;
	private LinkedList<File> files = new LinkedList<File>();

	public static void main(String[] args) throws IOException {

		Client client = new Client();

	}

	public Client() throws IOException {
		connectToServer();

		window = new Frame(this);
		window.open();			

		new Thread(new Runnable() {

			@Override
			public void run() {
				InputStreamReader in;
				BufferedReader bf;

				while (true) {
					try {
						in = new InputStreamReader(s.getInputStream());
						bf = new BufferedReader(in);

						String str = bf.readLine();
						System.out.println(str);

						if(str.contains("Search")) {
							String[] string = str.split(",");
							updateList(string);
							window.updateSearchList(searchTypes);
						}

						if(str.contains("File name")) {
							String[] string = str.split(":");
							String fileName = string[1];
							InputStream input1 = s.getInputStream();
							BufferedImage img = ImageIO.read(input1);
							ImageIO.write(img, "png", new File(window.getPath()+"\\result-"+fileName));
							File image = new File(fileName);
							files.add(image);
						}

						if(str.contains("DONE.")) {
							//System.out.println(window.getPath());
							window.updateList(window.getPath()+"\\results");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void connectToServer() {

		try {

			HOST = InetAddress.getLocalHost();
			s = new Socket(HOST, PORT);

			out = new PrintWriter(s.getOutputStream());
			out.println("TYPE: CLIENT");
			out.flush();

		} catch (IOException e) {
			System.out.println("ERROR: Failed connecting to server!");
			e.printStackTrace();
		}
	}

	public void updateList(String[] data) {

		for(int i = 0; i < data.length; i++) {
			searchTypes.add(data[i]);
		}
	}

	public void requestSearch(BufferedImage logo, String path, String searchType) {
		out.println(searchType+","+path);
		out.flush();
		out.println("CLIENT REQUEST");
		out.flush();

		convertImageToArray(logo);
		try {
			OutputStream outStream = s.getOutputStream();
			outStream.write(logoBytes);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void convertImageToArray(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			logoBytes = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endConnection() {
		try {
			out.write("CLIENT: Disconnecting!");
			out.flush();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
