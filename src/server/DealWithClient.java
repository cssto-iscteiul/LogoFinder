package server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class DealWithClient extends Thread {

	private BufferedReader in;
	private PrintWriter out;
	private Server server;
	private Socket socket;

	private LinkedList<String> tasks = new LinkedList<String>();
	private LinkedList<File> results = new LinkedList<File>();
	private File[] imagesToSearch;
	private byte[] imageBytes;
	private int taskCounter;
	private BufferedImage logo;
	private BufferedImage imageToSend;
	private CheckForResults checkForResults;
	private String SEARCH;
	private Timer timer;

	public DealWithClient(Socket socket, Server server) {
		this.server = server;
		this.socket = socket;
		this.checkForResults = new CheckForResults(this);
		this.timer = new Timer();
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				String str;
				str = in.readLine();
				System.out.println(str);
				if (str.contains("Users")) {
					String[] string = str.split(",");
					this.SEARCH = string[0];
					String PATH = string[1];
					imagesToSearch = new File(PATH).listFiles();
				}
				if (str.contains("CLIENT REQUEST")) {
					this.logo = ImageIO.read(socket.getInputStream());
					addTasks(SEARCH);
					timer.schedule(checkForResults, new Date(System.currentTimeMillis()), 20000);
				}
				if (str.contains("Disconnecting")) {
					server.getClients().remove(this);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void saveResult(File image) {
		this.taskCounter--;
		results.add(image);
	}

	public void updateSearch(String search) {
		out.println(search);
		out.flush();
	}

	public void sendResults() {
		if (taskCounter == 0 && !results.isEmpty()) {
			for (int i = 0; i != results.size(); i++) {
				// TODO
				out.flush();
				out.println("File name:" + results.get(i).getName());
				out.flush();
				try {
					imageToSend = ImageIO.read(results.get(i));
				} catch (IOException e) {
					System.out.println("ERROR: Couldn't draw image.");
					e.printStackTrace();
				}
				sendImage(imageToSend);
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					System.out.println("ERROR: Didn't wait for image transfer!");
					e.printStackTrace();
				}
			}
			results.removeAll(results);
			out.println("DONE.");
			out.flush();
		}
	}

	private synchronized void sendImage(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			imageBytes = baos.toByteArray();
			baos.close();
			OutputStream outStream = socket.getOutputStream();;
			outStream.write(imageBytes);
			outStream.flush();
		} catch (IOException e) {
			System.out.println("ERROR: failed sending image!");
			e.printStackTrace();
		}
	}

	private void addTasks(String search) {
		for (int i = 0; i != imagesToSearch.length; i++) {
			String task = imagesToSearch[i].getName() + "," + search;
			tasks.add(task);
			this.taskCounter++;
		}
	}

	public synchronized LinkedList<String> getTasks() {
		return tasks;
	}

	public synchronized File[] getFiles() {
		return imagesToSearch;
	}

	public synchronized BufferedImage getLogo() {
		return logo;
	}

}
