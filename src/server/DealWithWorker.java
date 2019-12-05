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

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;



public class DealWithWorker extends Thread {

	private BufferedReader in;
	private PrintWriter out;
	private Server server;
	private Socket socket;
	private DealWithClient client;
	private BufferedImage image;

	private byte[] imageBytes;

	public DealWithWorker(Socket socket, Server server) {

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
				
				if(str.contains("TASK REQUEST")) {
					findTask();
				}


			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendTask(BufferedImage image, BufferedImage logo, DealWithClient c) {

		this.client = c;

		out.println("SERVER: Sending logo!");
		out.flush();
		sendImage(logo);
		out.flush();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		out.println("SERVER: Sending image!");
		out.flush();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendImage(image);
	}

	private synchronized void sendImage(BufferedImage image) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			imageBytes = baos.toByteArray();
			baos.close();
			OutputStream outStream = socket.getOutputStream();
			outStream.flush();
			outStream.write(imageBytes);
			outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void findTask() {
		
		Boolean task=false;

		for(int i=0; i!=server.getClients().size(); i++) {
			if(!server.getClients().get(i).getTasks().isEmpty()) {
				String taskString[] = server.getClients().get(i).getTasks().getFirst().split("|");
				String fileName = taskString[0];
				File[] images = server.getClients().get(i).getFiles();
				this.client = server.getClients().get(i);
				task = true;
				for(int k=0; k!=images.length; k++) {
					if(images[k].getName().contains(fileName)) {
						try {
							this.image = ImageIO.read(images[k]);
						} catch (IOException e) {
							System.out.println("Failed getting image!");
							e.printStackTrace();
						}
					}
				}
			}
			if(task) {
				System.out.println("There's a task");
				sendTask(image, client.getLogo(), client);
				break;
			}
		}
		System.out.println("No tasks yet!");
	}



}
