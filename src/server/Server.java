package server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {

	private ServerSocket serverSocket;
	private Socket socket;
	private final int PORT = 8080;
	private BufferedReader in;
	private PrintWriter out;
	private LinkedList<String> searchTypes = new LinkedList<String>();
	private LinkedList<DealWithWorker> workers = new LinkedList<DealWithWorker>();
	private LinkedList<DealWithClient> clients = new LinkedList<DealWithClient>();

	public static void main(String[] args) throws IOException {
		Server server = new Server(Integer.parseInt(args[0]));
		server.serve();
	}

	public Server(int PORT) {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("ERROR: failed creating server socket!");
			e.printStackTrace();
		}
	}

	public synchronized LinkedList<DealWithWorker> getWorkers() {
		return workers;
	}

	public synchronized LinkedList<DealWithClient> getClients() {
		return clients;
	}

	public void serve() {
		System.out.println("Waiting for connections.....");
		while (true) {
			try {
				socket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				String str;
				str = in.readLine();
				// TODO
				System.out.println(str);

				if (str.contains("TYPE: WORKER")) {
					String[] string = str.split(":");
					String SEARCHTYPE = string[2];
					addSearchType(SEARCHTYPE);
					out.println("SERVER: you're connected!");
					out.flush();
					DealWithWorker dww = new DealWithWorker(socket, this);
					dww.start();
					workers.add(dww);
				}
				if (str.contains("TYPE: CLIENT")) {
					if (!searchTypes.isEmpty()) {
						out.println(searchTypesToString());
						out.flush();
					} else {
						out.println("SERVER: you're connected!");
						out.flush();
					}
					DealWithClient dwc = new DealWithClient(socket, this);
					dwc.start();
					clients.add(dwc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized String searchTypesToString() {
		String str = "";

		for (int i = 0; i != searchTypes.size(); i++) {
			str = str + searchTypes.get(i);
			if (i != searchTypes.size() - 1) {
				str = str + ",";
			}
		}
		return str;
	}

	private synchronized void addSearchType(String search) {
		if (!searchTypes.contains(search)) {
			searchTypes.add(search);
			for (int i = 0; i != clients.size(); i++) {
				clients.get(i).updateSearch(searchTypesToString());
			}
		}
	}

}
