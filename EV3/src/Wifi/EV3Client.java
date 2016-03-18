import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class EV3Client {
	public static void main(String args[]) {
		try {
			int port = 8888;
			// Create a socket to listen on the port.
			DatagramSocket dsocket = new DatagramSocket(port);
			// Create a buffer to read datagrams into. If a
			// packet is larger than this buffer, the
			// excess will simply be discarded!
			byte[] buffer = new byte[2048];
			// Create a packet to receive data into the buffer
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			// Now loop forever, waiting to receive packets and printing them.
			while (true) {
				// Wait to receive a datagram
				dsocket.receive(packet);
				// Convert the contents to a string, and display them
				String msg = new String(buffer, 0, packet.getLength());
				System.out.println("packet.getLength = " + packet.getLength() + " msg = " + msg);
				String[] palets = msg.split("\n");
				for (int i = 0; i < palets.length; i++) {
					String[] coord = palets[i].split(";");
					if (coord.length > 2) {
						int x = Integer.parseInt(coord[1]);
						int y = Integer.parseInt(coord[2]);
						System.out.println(i + " / " + Integer.toString(x) + " / " + Integer.toString(y));
					}
				}
				// Reset the length of the packet before reusing it.
				packet.setLength(buffer.length);
				packet.setData(buffer);
			}
			dsocket.close();
		}

		catch (Exception e) {
			System.err.println(e);
		}
	}
}
