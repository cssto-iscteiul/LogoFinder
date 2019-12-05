package server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class DealWithClient extends Thread {

	private BufferedReader in;
	private PrintWriter out;
	private Server server;
	private Socket socket;
	
	private LinkedList<String> tasks = new LinkedList<String>();
	private File[] imagesToSearch;
	private BufferedImage logo;
	private LinkedList<BufferedImage> results = new LinkedList<BufferedImage>();
	private String SEARCH;

	public DealWithClient(Socket socket, Server server) {
		
		this.server = server;
		this.socket = socket;
		
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while(true) {
			try {

				String str;
				str = in.readLine();
				System.out.println(str);

				if(str.contains("C:")) {
					String[] string= str.split(",");
					this.SEARCH = string[0];
					String PATH = string[1];
					imagesToSearch = new File(PATH).listFiles();
				}
				if(str.contains("CLIENT REQUEST")) {
					this.logo = ImageIO.read(socket.getInputStream());
					addTasks(SEARCH);
				}

				if(str.contains("Disconnecting")) {
					server.getClients().remove(this);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void saveResult(BufferedImage image) {
		results.add(image);
	}
	
	public void updateSearch(String search) {
		out.println(search);
		out.flush();
	}
	
	private void addTasks(String search) {
		for(int i=0; i!= imagesToSearch.length; i++) {
			String task = imagesToSearch[i].getName()+"|"+search;
			tasks.add(task);
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
