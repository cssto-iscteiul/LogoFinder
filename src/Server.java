import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class Server {

	private ServerSocket ss;
	private Socket s;
	private final int PORT = 8080;
	private LinkedList<String> searchTypes = new LinkedList<String>();
	private LinkedList<String> tasks = new LinkedList<String>();
	private LinkedList<PrintWriter> simpleSearchWorkers = new LinkedList<PrintWriter>();
	private LinkedList<PrintWriter> clients = new LinkedList<PrintWriter>();
	private String PATH;
	private BufferedReader in;
	private PrintWriter out;

	private File[] imagesToSearch;
	private BufferedImage logo;
	private BufferedImage imageToSearch;
	private byte[] imageBytes;

	public static void main(String[] args) throws IOException {

		Server server = new Server();
		server.serve();

	}

	public Server() {
		try {
			ss = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void serve() {
		System.out.println("Waiting for connections.....");
		while (true) {
			try {
				s = ss.accept();
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

				String str;
				str = in.readLine();
				System.out.println(str);

				if(str.contains("TYPE: WORKER")) {
					String[] string= str.split(":");
					String SEARCHTYPE = string[2];
					addSearchType(SEARCHTYPE);
					out.println("Server: You're connected!");
					out.flush();
					new DealWithWorker(s, SEARCHTYPE).start();
				}
				if(str.contains("TYPE: CLIENT")) {
					if(!searchTypes.isEmpty()) {
						out.println(searchTypesToString());
						out.flush();						
					}
					new DealWithClient(s).start();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class DealWithClient extends Thread {

		BufferedReader in;
		PrintWriter out;

		public DealWithClient(Socket s) {
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

				clients.add(out);

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
					
					out.write("Server: you're connected!");
					out.flush();

					if(str.contains("C:")) {
						String[] string= str.split(",");
						String SEARCH = string[0];
						PATH = string[1];
						addTasks(PATH, SEARCH);
						imagesToSearch = new File(PATH).listFiles();
					}
					if(str.contains("CLIENT REQUEST")) {
						logo = ImageIO.read(s.getInputStream());
					}

					if(str.contains("Disconnecting")) {
						clients.remove(out);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public class DealWithWorker extends Thread {

		BufferedReader in;
		PrintWriter out;
		String SEARCHTYPE;

		public DealWithWorker(Socket s, String SEARCHTYPE) {

			this.SEARCHTYPE = SEARCHTYPE;

			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

				if(SEARCHTYPE.equalsIgnoreCase("simple search")) {
					simpleSearchWorkers.add(out);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
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

					out.write("SERVER: You're connected!");
					out.flush();

					if(!tasks.isEmpty()) {
						String task[] = tasks.getLast().split("|");
						String imageName = task[0];
						String searchType = task[1];

						System.out.println("Gonna send a task");

						if(searchType.contains(SEARCHTYPE)) {

							for(int i=0; i!=imagesToSearch.length; i++) {
								if(imagesToSearch[i].getName().equalsIgnoreCase(imageName)) {

									imageToSearch = ImageIO.read(imagesToSearch[i]);
									out.write("SERVER: Sending image!");
									out.flush();
									sendImage(imageToSearch, s);

									out.write("SERVER: Sending logo!");
									out.flush();
									sendImage(logo, s);
								}

							}
						}
					}					
				} catch (IOException e) {
					System.out.println("Worker disconnected!");
					simpleSearchWorkers.remove(out);
					e.printStackTrace();
				}
			}
		}

	}

	private synchronized void sendImage(BufferedImage image, Socket socket) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			imageBytes = baos.toByteArray();
			baos.close();
			OutputStream outStream = socket.getOutputStream();
			outStream.write(imageBytes);
			outStream.flush();

			System.out.println("Sent the damn image");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private synchronized void addTasks(String path, String search) {
		File[] images = new File(path).listFiles();
		for(int i=0; i!= images.length; i++) {
			String task = images[i].getName()+"|"+search;
			tasks.add(task);
		}
	}

	private synchronized void addSearchType(String search) {
		if(!searchTypes.contains(search)) {
			searchTypes.add(search);
			for(int i=0; i!= clients.size(); i++) {
				clients.get(i).write(searchTypesToString());
				clients.get(i).flush();
			}
		}
	}

	private synchronized String searchTypesToString() {
		String str="";

		for(int i=0; i!=searchTypes.size(); i++) {
			str= str + searchTypes.get(i);
			if(i!=searchTypes.size()-1) {
				str = str + ",";
			} 			
		}
		return str;
	}

}
