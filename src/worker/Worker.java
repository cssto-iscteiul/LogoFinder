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

/**
 * 
 * To run this class you must add the port and search type in "Run Configurations > Arguments"!
 * For search types please use one of the following:
 * "180 º Search" ; "90 º Search" or "Simple Search".
 * Thank you! :)
 *
 * @author Catarina Teodoro
 */

public class Worker {

	private InetAddress HOST;
	private int PORT;
	private Socket s;
	private PrintWriter out;
	private Timer timer;
	private String SEARCHTYPE;
	private BufferedImage logo;
	private BufferedImage image;
	private RequestTask request;
	private LinkedList<Point> coordinates = new LinkedList<Point>();

	public static void main(String[] args) throws IOException {
		Worker worker = new Worker(Integer.parseInt(args[0]), args[1]);
	}

	public Worker(int PORT, String SEARCHTYPE) throws IOException {

		this.request = new RequestTask(this);
		this.SEARCHTYPE = SEARCHTYPE;
		this.timer = new Timer();
		this.PORT = PORT;
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

						if (str.contains("SERVER: Sending image!")) {
							InputStream input1 = s.getInputStream();
							image = ImageIO.read(input1);
							if (SEARCHTYPE.contains("Simple")) {
								simpleSearch();
							}
							if (SEARCHTYPE.contains("90")) {
								ninetyDegreeSearch();
							}
							if (SEARCHTYPE.contains("180")) {
								oneEightyDegreeSearch();
							}
						}
						if (str.contains("SERVER: Sending logo!")) {
							timer.cancel();
							InputStream input2 = s.getInputStream();
							logo = ImageIO.read(input2);
						}
						if (str.contains("connected")) {
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
			out.println("TYPE: WORKER, SEARCH:" + SEARCHTYPE);
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR: failed connecting to server!");
			e.printStackTrace();
		}
	}

	public static boolean areAllTrue(boolean[] array) {
		for (boolean b : array) {
			if (!b) {
				return false;
			}
		}
		return true;
	}

	private void simpleSearch() {
		Point p;
		int width = image.getWidth();
		int height = image.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();
		coordinates.removeAll(coordinates);
		boolean[] match = new boolean[logoWidth * logoHeight];
		int i = 0;
		int logosFound = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.getRGB(x, y) == logo.getRGB(0, 0)) {
					p = new Point(x, y);
					match = new boolean[logoWidth * logoHeight];
					i = 0;
					if (x + logoWidth < width && y + logoHeight < height) {
						for (int j = 0; j < logoHeight; j++) {
							for (int k = 0; k < logoWidth; k++) {
								if (image.getRGB(x + k, y + j) == logo.getRGB(k, j)) {
									match[i] = true;
								} else {
									match[i] = false;
									break;
								}
								if (!match[i]) {
									break;
								}
								i++;
							}
						}
						if (areAllTrue(match)) {
							coordinates.add(p);
							coordinates.add(new Point(logoWidth, logoHeight));
							logosFound++;
						}
					}
				}
			}
		}
		if (logosFound != 0) {
			sendCoordinates();
		} else {
			setTimedTask();
		}
	}

	private void ninetyDegreeSearch() {
		Point p;
		int width = image.getWidth();
		int height = image.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();
		coordinates.removeAll(coordinates);
		boolean[] match = new boolean[logoWidth * logoHeight];
		int i = 0;
		int logosFound = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.getRGB(x, y) == logo.getRGB(0, logoHeight - 1)) {
					p = new Point(x, y);
					match = new boolean[logoWidth * logoHeight];
					i = 0;
					if (x + logoHeight < width && y + logoWidth < height) {
						int yValue = y;
						int xValue = x;
						for (int k = 0; k < logoWidth; k++) {
							xValue = x;
							for (int j = logoHeight - 1; j > -1; j--) {
								if (image.getRGB(xValue, yValue) == logo.getRGB(k, j)) {
									match[i] = true;
								} else {
									match[i] = false;
									break;
								}
								if (!match[i]) {
									break;
								}
								i++;
								xValue++;
							}
							yValue++;
						}
						if (areAllTrue(match)) {
							coordinates.add(p);
							coordinates.add(new Point(logoHeight, logoWidth));
							logosFound++;
						}
					}
				}
			}
		}
		if (logosFound != 0) {
			sendCoordinates();
		} else {
			setTimedTask();
		}
	}

	private void oneEightyDegreeSearch() {
		Point p;
		int width = image.getWidth();
		int height = image.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();
		coordinates.removeAll(coordinates);
		boolean[] match = new boolean[logoWidth * logoHeight];
		int i = 0;
		int logosFound = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.getRGB(x, y) == logo.getRGB(logoWidth - 1, logoHeight - 1)) {
					p = new Point(x, y);
					match = new boolean[logoWidth * logoHeight];
					i = 0;
					if (x + logoWidth < width && y + logoHeight < height) {
						int yValue = y;
						int xValue = x;
						for (int j = logoHeight - 1; j > -1; j--) {
							xValue = x;
							for (int k = logoWidth - 1; k > -1; k--) {
								if (image.getRGB(xValue, yValue) == logo.getRGB(k, j)) {
									match[i] = true;
								} else {
									match[i] = false;
									break;
								}
								if (!match[i]) {
									break;
								}
								i++;
								xValue++;
							}
							yValue++;
						}
						if (areAllTrue(match)) {
							coordinates.add(p);
							coordinates.add(new Point(logoWidth, logoHeight));
							logosFound++;
						}
					}
				}
			}
		}
		if (logosFound != 0) {
			sendCoordinates();
		} else {
			setTimedTask();
		}
	}

	public void sendCoordinates() {
		String coordinatesMessage = "RESULTS:";

		for (int i = 0; i != coordinates.size(); i++) {
			if (i == coordinates.size() - 1) {
				coordinatesMessage += "(" + coordinates.get(i).x + "," + coordinates.get(i).y + ")";
			} else {
				coordinatesMessage += "(" + coordinates.get(i).x + "," + coordinates.get(i).y + ");";
			}
		}
		out.println(coordinatesMessage);
		out.flush();
		setTimedTask();
	}

	private void setTimedTask() {
		try {
			TimeUnit.SECONDS.sleep(20);
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
