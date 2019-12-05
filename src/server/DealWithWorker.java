package server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
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
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;



public class DealWithWorker extends Thread {

	private BufferedReader in;
	private PrintWriter out;
	private Server server;
	private Socket socket;
	private DealWithClient client;
	private BufferedImage image;
	private String fileName;
	private LinkedList<Point> coordinates = new LinkedList<Point>();

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
				if(str.contains("RESULTS")) {
					String[] results = str.split(":");
					getPoints(results[1]);
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
			e.printStackTrace();
		}
	}

	private void getPoints(String coords) {
		String[] pairs = coords.split(";");
		String[] point;
		LinkedList<Integer> points = new LinkedList<Integer>(); 
		for(int i=0; i!= pairs.length; i++) {
			String newS = pairs[i].replace("(", "");
			newS = newS.replace(")", "");
			point = newS.split(",");
			points.add(Integer.parseInt(point[0]));
			points.add(Integer.parseInt(point[1]));
		}	
		for(int j=0; j!= points.size(); j+=2) {
			Point p = new Point(points.get(j),points.get(j+1));
			coordinates.add(p);
		}
		for(int k=0; k!= coordinates.size(); k+=2) {
			System.out.println(coordinates.get(k).toString());
			drawRectangle(coordinates.get(k),coordinates.get(k+1));
		}
		
		coordinates.removeAll(coordinates);
		File newImage = new File(fileName);
		try {
			ImageIO.write(image, "png", newImage);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		client.saveResult(newImage);
	}

	private void drawRectangle(Point top, Point bottom) {
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.PINK);
		g2d.drawRect(top.x, top.y, bottom.x, bottom.y);
		g2d.dispose();
	}

	private void findTask() {

		Boolean task=false;

		for(int i=0; i!=server.getClients().size(); i++) {
			if(!server.getClients().get(i).getTasks().isEmpty()) {
				String taskString[] = server.getClients().get(i).getTasks().getFirst().split(",");
				server.getClients().get(i).getTasks().removeFirst();
				for(int j=0; j!=taskString.length; j++) {
					System.out.println(taskString[j]);
				}
				String name = taskString[0];
				this.fileName = taskString[0];
				File[] images = server.getClients().get(i).getFiles();
				this.client = server.getClients().get(i);
				task = true;
				for(int k=0; k!=images.length; k++) {
					if(images[k].getName().contains(name)) {
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
				sendTask(image, client.getLogo(), client);
				break;
			}
			System.out.println("No tasks.");
		}
	}



}
