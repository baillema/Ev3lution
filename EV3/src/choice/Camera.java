package Choice;

import java.awt.Point;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;
import Moving.MyChassis;
import Sensor.TouchSensor;

public class Camera {

	//Etat courent de l'automate
	public enum State {
        WAIT,
        SEARCH,
        DETECT,
        CATCH,
        GOAL,
        RETURN
    };
    
    //Orientation relative du robot
    static int current_orientation = 0;
	static State current_state = State.WAIT;
    static TouchSensor uTouch = new TouchSensor(SensorPort.S3);
    
    //Moteur
    MyChassis chass = new MyChassis();
    
    //Position courante du robot
    static Point robot = new Point();
    
    //Position courante du palet en vue
    static Point palet = new Point();
       
    public static void main(String[] args) {	
		try 
		{
			while(current_state != State.RETURN)
			{
				switch (current_state) {
		        	case WAIT:
		        		//Attendre que l'on presse
		        		System.out.println("Push the central button to START");
						Button.ENTER.waitForPress();
						
						//open_arms();
						robot = search_position();
						current_state = State.SEARCH;	
		        		break;
		        		
		        	case SEARCH:
		        		//Cherche le palet le plus proche via les positions
		        		palet = search_palet();
		        		
		        		System.out.println("Je suis en X : "+ robot.x + " Y : "+robot.y);
		        		
		        		//S'il n'y a plus que les deux robots sur le plateau
		        		//if(palet.x<0 && palet.y <0) current_state = State.RETURN;
		        		Button.ENTER.waitForPress();
		        		current_state = State.RETURN;
		                break;
		              		                
		        	case DETECT: 
		        		//Determiner le mouvement à effectuer pour atteindre le but
		        		forward_till_touch();
		        		
		        		//En cas de mouvement du palet que l'on attaque, changement de cible
		        		
		        		current_state = State.CATCH;
		        		
		        		break;
		                
		        	case CATCH:  
		        		//Fermer les pinces
		        		close_arms();
		        		current_state = State.RETURN;
		        		//Demi-tour (rotate clockwise)
		        		//Avancer tant que l'on ne trouve pas le couleur blanc
		        			//Si on trouve le blanc
		        				//current_state = State.RETURN;
		                break;
		                
		        	case GOAL:
		        		
		        		break;
		        		
		       	 	case RETURN:
		       	 		System.out.println("Push the central button to START");
		       	 		Button.ENTER.waitForPress();
		                break;
		
		        default: current_state = State.RETURN;
		                break;
		    	}
        	}
        
		} 
		catch (Throwable t) 
		{
			t.printStackTrace();
			Delay.msDelay(10000);
			System.exit(0);
		}
	}

	private static Point search_position()
	{
		Point[] tab = new Point[15];
		Point[] tab2 = new Point[15];
		
		init_tab_point(tab);
		init_tab_point(tab);
			
		Point res = new Point();
		
		tab = receive_socket();
		forward(5);
		tab2 = receive_socket();
		
		res = compare_point(tab, tab2);
		
		return res;
	}

	private static Point compare_point(Point[] tab, Point[] tab2) {
		
		// TODO Auto-generated method stub
		Point res = new Point();
		for (int i = 0; i<tab.length && i<tab2.length; i++)
		{
			if(tab2[i].x-3 < tab[i].x && tab[i].x < tab2[i].x+3)
			{
				if(tab2[i].y-3 < tab[i].y && tab[i].y < tab2[i].y+3)
				{
					//Ce point n'a pas bougé
					System.out.println("Points initial " + i + " X " + tab[i].x + " Y " + tab[i].y);

					System.out.println("Points move" + i + " X " + tab2[i].x + " Y " + tab2[i].y);
				}
				else
				{
					System.out.println("Difference trouvee.");
					res = new Point(tab2[i].x, tab2[i].y);
				}
			}
				
		}
		
		return res;
	}

	private static Point[] receive_socket(){
		Point[] tab = new Point[15];
		init_tab_point(tab);
		
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
			
			for(int boucle = 0; boucle<5; boucle++) {
				
				dsocket.receive(packet);
				// Convert the contents to a string, and display them
				String msg = new String(buffer, 0, packet.getLength());
				//System.out.println("packet.getLength = " + packet.getLength() + " msg = " + msg);
				String[] palets = msg.split("\n");
				for (int i = 0; i < palets.length; i++) {
					String[] coord = palets[i].split(";");
					if (coord.length > 2) {
						int x = Integer.parseInt(coord[1]);
						int y = Integer.parseInt(coord[2]);
						tab[i].x = x;
						tab[i].y = y;
						//System.out.println(i + " / " + Integer.toString(x) + " / " + Integer.toString(y));
					}
				}
				
				packet.setLength(buffer.length);
				packet.setData(buffer);
			}
			dsocket.close();
		}

		catch (Exception e) {
			System.err.println(e);
		}
		return tab;
		
	}

	private static void init_tab_point(Point[] tab) {
		// TODO Auto-generated method stub
		for(int i = 0; i<tab.length; i++) tab[i] = new Point();
	}

	private static Point search_palet()
	{
		Point[] res = new Point[15];
		Point min = new Point();
		
		init_tab_point(res);
		
		res = receive_socket();
		
		min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		//TODO A tester
		for(int i = 0; i<res.length; i++)
		{
			if(Math.abs(robot.x-res[i].x)+Math.abs(robot.y-res[i].y)<Math.abs(robot.x-min.x)+Math.abs(robot.y-min.y))
			{
				//Test si ce n'est pas la position du robot
				if(Math.abs(robot.x-res[i].x)+Math.abs(robot.y-res[i].y)<3)
				{
					//C'est le robot
				}
				else
				{
					min = res[i];
				}
			}
		}
		
		return min;
	}

	private static void open_arms() {
		// TODO Auto-generated method stub
		Motor.A.setSpeed(1000);
		Motor.A.forward();
		Delay.msDelay(550);
		Motor.A.stop();
	}

	private static void close_arms() {
		// TODO Auto-generated method stub
		Motor.A.setSpeed(1000);
		Motor.A.backward();
		Delay.msDelay(550);
		Motor.A.stop();
	}

	private static void forward(int i) {
		// TODO Auto-generated method stub
		Motor.C.setSpeed(500);
		Motor.B.setSpeed(500);

		Motor.C.forward();
		Motor.B.forward();
		
		Delay.msDelay(i*1000);
		
		Motor.B.stop();
    	Motor.C.stop();
	}
	
	private static void backward(int i) {
		// TODO Auto-generated method stub
		Motor.C.setSpeed(500);
		Motor.B.setSpeed(500);

		Motor.C.backward();
		Motor.B.backward();
		
		Delay.msDelay(i*1000);
		
		Motor.B.stop();
    	Motor.C.stop();
	}
	
	private static void forward_till_touch() {
		// TODO Auto-generated method stub
		Motor.C.setSpeed(500);
		Motor.B.setSpeed(500);
		while(!uTouch.isPressed())
		{
			Motor.C.forward();
			Motor.B.forward();
		}
		
		Motor.B.stop();
    	Motor.C.stop();
	}

	private static void rotate_right(int t) {
		// TODO Auto-generated method stub
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		Motor.B.forward();
        Motor.C.backward();
        Delay.msDelay(t);
        Motor.C.stop();
        Motor.B.stop();
	}
	
	private static void rotate_left(int t) {
		// TODO Auto-generated method stub
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		Motor.C.forward();
        Motor.B.backward();
        Delay.msDelay(t);
        Motor.C.stop();
        Motor.B.stop();
	}
}
