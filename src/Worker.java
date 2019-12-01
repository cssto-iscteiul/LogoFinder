import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import javax.imageio.ImageIO;

public class Worker {

	private InetAddress HOST;
	private final int PORT = 8080;
	private Socket s;
	private BufferedImage logoToSearch;
	private BufferedImage imageToCompare;
	private List<Point> coordinates;
	private final String SEARCHTYPE = "Simple Search";
	private PrintWriter out;


	public static void main(String[] args) throws IOException {

		Worker worker = new Worker();
		worker.connectToServer();

	}

	public void connectToServer() {

		try {

			HOST = InetAddress.getLocalHost();
			s = new Socket(HOST, PORT);

			out = new PrintWriter(s.getOutputStream());
			out.println("TYPE: CLIENT, SEARCH: "+SEARCHTYPE);
			out.flush();


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

							if(str.contains("SERVER: Sending image!")) {
								imageToCompare = ImageIO.read(s.getInputStream());
							}

							if(str.contains("SERVER: Sending logo!")) {
								logoToSearch = ImageIO.read(s.getInputStream());
								search();
							}

							for(int i=0; i!=coordinates.size(); i++) {
								System.out.println(coordinates.get(i).toString());
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


	public static boolean areAllTrue(boolean[] array)
	{
		for(boolean b : array) {
			if(!b) {
				return false;
			}
		}
		return true;
	}

	private void search() {

		int width = imageToCompare.getWidth();
		int height = imageToCompare.getHeight();
		int logoWidth = logoToSearch.getWidth();
		int logoHeight = logoToSearch.getHeight();
		Point p;

		boolean[] match = new boolean[logoWidth*logoHeight];
		int i = 0;

		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				if (imageToCompare.getRGB(x, y) == logoToSearch.getRGB(0, 0)) {
					p = new Point(x,y);
					match = new boolean[logoWidth*logoHeight];
					i=0;
					if(x+logoWidth<width && y+logoHeight<height) {
						for(int j=0; j<logoHeight; j++) {
							for(int k=0; k<logoWidth; k++) {
								if (imageToCompare.getRGB(x+k, y+j) == logoToSearch.getRGB(k, j)) {
									match[i] = true;
								} else {
									match[i] = false;
									break;
								}
								if(!match[i]) {
									break;
								}
								i++;
							}
						}
						if(areAllTrue(match)) {
							coordinates.add(p);
							coordinates.add(new Point(logoWidth,logoHeight));
						}	
					}
				}
			}
		}
	}

}
