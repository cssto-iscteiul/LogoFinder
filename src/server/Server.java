package server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {

	private ServerSocket ss;
	private Socket s;
	private final int PORT = 8080;
	private LinkedList<String> searchTypes = new LinkedList<String>();
	private LinkedList<DealWithWorker> workers = new LinkedList<DealWithWorker>();
	private LinkedList<DealWithClient> clients = new LinkedList<DealWithClient>();
	private BufferedReader in;
	private PrintWriter out;

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
					
					out.println("SERVER: you're connected!");
					out.flush();
					
					DealWithWorker dww = new DealWithWorker(s, this);
					dww.start();
					workers.add(dww);
				}
				if(str.contains("TYPE: CLIENT")) {
					
					if(!searchTypes.isEmpty()) {
						out.println(searchTypesToString());
						out.flush();						
					} else {
						out.println("SERVER: you're connected!");
						out.flush();
					}
					
					DealWithClient dwc = new DealWithClient(s, this);
					dwc.start();
					clients.add(dwc);
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized LinkedList<DealWithWorker> getWorkers(){
		return workers;
	}
	
	public synchronized LinkedList<DealWithClient> getClients(){
		return clients;
	}

	private synchronized void addSearchType(String search) {
		if(!searchTypes.contains(search)) {
			searchTypes.add(search);
			for(int i=0; i!= clients.size(); i++) {
				clients.get(i).updateSearch(searchTypesToString());
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
