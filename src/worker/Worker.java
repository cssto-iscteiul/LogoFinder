package worker;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;


public class Worker {

	private InetAddress HOST;
	private final int PORT = 8080;
	private Socket s;
	private PrintWriter out;
	private String SEARCHTYPE;
	private BufferedImage image;
	private BufferedImage logo;
	private LinkedList<Point> coordinates = new LinkedList<Point>();
	private RequestTask request;
	private Timer timer;


	public static void main(String[] args) throws IOException {

		Worker worker = new Worker("Simple Search");

	}

	public Worker(String SEARCHTYPE) throws IOException {

		this.SEARCHTYPE = SEARCHTYPE;
		this.request = new RequestTask(this);
		this.timer = new Timer();
		connectToServer();


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

						if(str.contains("SERVER: Sending image!")) {
							InputStream input1 = s.getInputStream();
							image = ImageIO.read(input1);
							search();
						}

						if(str.contains("SERVER: Sending logo!")) {
							timer.cancel();
							InputStream input2 = s.getInputStream();
							logo = ImageIO.read(input2);
						}

						if(str.contains("connected")) {
							timer.schedule(request, new Date(System.currentTimeMillis()), 20000);
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
			out.println("TYPE: WORKER, SEARCH:"+SEARCHTYPE);
			out.flush();

		} catch (IOException e) {
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

		int width = image.getWidth();
		int height = image.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();
		Point p;
		coordinates.removeAll(coordinates);

		boolean[] match = new boolean[logoWidth*logoHeight];
		int i = 0;

		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				if (image.getRGB(x, y) == logo.getRGB(0, 0)) {
					p = new Point(x,y);
					match = new boolean[logoWidth*logoHeight];
					i=0;
					if(x+logoWidth<width && y+logoHeight<height) {
						for(int j=0; j<logoHeight; j++) {
							for(int k=0; k<logoWidth; k++) {
								if (image.getRGB(x+k, y+j) == logo.getRGB(k, j)) {
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
		sendCoordinates();
	}

	public void sendCoordinates() {
		String coordinatesMessage = "RESULTS:";

		for(int i=0; i!=coordinates.size(); i++) {
			if(i == coordinates.size()-1) {
				coordinatesMessage += "("+coordinates.get(i).x+","+coordinates.get(i).y+")";
			} else {
				coordinatesMessage += "("+coordinates.get(i).x+","+coordinates.get(i).y+");";
			}
		}
		out.println(coordinatesMessage);
		out.flush();

		setTimedTask();
	}

	private void setTimedTask() {
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {
			System.out.println("ERROR: Pause failed!");
			e.printStackTrace();
		}
		this.request = new RequestTask(this);
		this.timer = new Timer();
		timer.schedule(request, new Date(System.currentTimeMillis()), 20000);
	}

	public void requestTask() {
		out.println("WORKER TASK REQUEST");
		out.flush();
	}

}
