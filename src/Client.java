import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class Client {

	private InetAddress HOST;
	private final int PORT = 8080;
	private Socket s;
	private LinkedList<String> searchTypes = new LinkedList<String>();
	private PrintWriter out;
	private byte[] logoBytes;

	public static void main(String[] args) throws IOException {

		Client client = new Client();
		client.connectToServer();

	}

	public void connectToServer() {

		try {

			HOST = InetAddress.getLocalHost();
			s = new Socket(HOST, PORT);

			out = new PrintWriter(s.getOutputStream());
			out.println("TYPE: CLIENT");
			out.flush();

			Frame window = new Frame(this);
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
							System.out.println("Server: "+str);

							if(str.contains("Search")) {
								String[] string = str.split(",");
								updateList(string);
								window.updateSearchList(searchTypes);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateList(String[] data) {

		for(int i = 0; i < data.length; i++) {
			//search[i] = data[i];
			searchTypes.add(data[i]);
		}
	}

	public void requestSearch(BufferedImage logo, String path, String searchType) {
		out.println(searchType+","+path);
		out.flush();
		out.println("CLIENT REQUEST: Here's the image to look for!");
		out.flush();

		convertImageToArray(logo);
		try {
			OutputStream outStream = s.getOutputStream();
			outStream.write(logoBytes);
			outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void endConnection() {
		try {
			out.write("CLIENT: Disconnecting!");
			out.flush();
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
