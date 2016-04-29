package Choice;

import java.awt.Point;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;
import Sensor.TouchSensor;

public class Camera {

	//Etat courrant de l'automate
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
    static int SPEED = 500;
    
    //Position courante du robot
    static Point robot = new Point();
    
    //Position courante du palet en vue
    static Point palet = new Point();
    
    //Objectif adverse
    static Point obj = new Point();
       
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
						
						obj = find_goal();
						
						forward_till_touch();
						
						//System.out.println("Je suis en X : "+ robot.x + " Y : "+ robot.y);
						//System.out.println("Objectif en X : "+ obj.x + " Y : "+ obj.y);
						
						current_state = State.GOAL;	
		        		break;
		        		
		        	case SEARCH:
		        		//Cherche le palet le plus proche via les positions
		        		palet = search_palet();
		        		
		        		System.out.println("Palet proche : "+ palet.x + " Y : "+ palet.y);
		        		
		        		//S'il n'y a plus que les deux robots sur le plateau
		        		//if(palet.x<0 && palet.y <0) current_state = State.RETURN;
		        		
		        		//else current_state = State.DETECT;
		        		
		       	 		Button.ENTER.waitForPress();
		        		current_state = State.RETURN;
		        		
		                break;
		              		                
		        	case DETECT: 
		        		//Determiner le mouvement à effectuer pour atteindre le but
		        		travel_to_palet();
		        	
		        		current_state = State.CATCH;
		        		
		        		break;
		                
		        	case CATCH:  
		        		//Fermer les pinces
		        		close_arms();
		        		current_state = State.GOAL;
		                break;
		                
		        	case GOAL:
		        		travel_from_to_obj();
		        		backward(2);
		        		current_state = State.RETURN;
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

	private static Point maj_position() {
		Point[] tab = new Point[15];
		Point res = new Point();
		
		init_tab_point(tab);
		
		tab = receive_socket();
		
		
		
		for(int i = 0; i<tab.length; i++)
		{
			if(robot.x-3 < tab[i].x && tab[i].x < robot.x+3)
			{
				if(robot.y-3 < tab[i].y && tab[i].y < robot.y+3)
				{
					//Ce point n'a pas bougé
					//System.out.println("Points initial " + i + " X " + tab[i].x + " Y " + tab[i].y);

					//System.out.println("Points move" + i + " X " + tab2[i].x + " Y " + tab2[i].y);
				}
				else
				{
					//System.out.println("Difference trouvee.");
					res = new Point(tab[i].x, tab[i].y);
				}
			}
		}
		
		return res;
	}

	private static void travel_from_to_obj() {
		if(current_orientation!=0)
		{
			if(current_orientation<0)
			{
				rotate(current_orientation);
			}
			else
			{
				rotate(-current_orientation);
			}
		}
		
		forward_distance(Math.abs(robot.y-obj.y));
	}

	private static Point find_goal() {
		Point res = new Point();
		
		if(robot.y>150)
		{
			//Est en 200
			res.x = 100;
			res.y = 0;
		}
		
		else
		{
			//Est en 0
			res.x = 100;
			res.y = 200;
		}
		
		return res;
	}

	private static void travel_to_palet() 
	{
		int diffX = robot.x-palet.x;
		int diffY = robot.y-palet.y;
		
		if(Math.abs(diffX)<3)
		{
			//Sur la même ligne X
			if(diffX<0)
			{
				//Est derrière, demi tour
				rotate(180);
				current_orientation=(current_orientation+180)%360;
			}
			
			forward_distance(diffX);
		}
		
		if(Math.abs(diffY)<3)
		{
			//Sur la même ligne Y
			//Dépend de l'orientation
			//TODO trouver le modèle
			if(diffY<0)
			{
				//A gauche
			}
			
			else
			{
				//A droite
			}
		}
	}

	private static void forward_distance(int diffX) {
		Motor.C.setSpeed(0);
		Motor.B.setSpeed(0);
		
		Motor.C.forward();
		Motor.B.forward();
		
		for(int j=0; j<SPEED;j+=(SPEED/10))
		{
			Motor.C.setSpeed(j);
			Motor.B.setSpeed(j);
			Delay.msDelay(1);
		}
		
		Delay.msDelay(diffX*1000);
		
		stop();
		
	}

	private static Point search_position()
	{
		Point[] tab = new Point[15];
		Point[] tab2 = new Point[15];
		
		init_tab_point(tab);
		init_tab_point(tab);
			
		Point res = new Point();
		
		tab = receive_socket();
		forward(1);
		tab2 = receive_socket();
		
		res = compare_point(tab, tab2);
		
		return res;
	}

	private static Point compare_point(Point[] tab, Point[] tab2) {
		Point res = new Point();
		for (int i = 0; i<tab.length && i<tab2.length; i++)
		{
			if(tab2[i].x-3 < tab[i].x && tab[i].x < tab2[i].x+3)
			{
				if(tab2[i].y-3 < tab[i].y && tab[i].y < tab2[i].y+3)
				{
					//Ce point n'a pas bougé
					//System.out.println("Points initial " + i + " X " + tab[i].x + " Y " + tab[i].y);

					//System.out.println("Points move" + i + " X " + tab2[i].x + " Y " + tab2[i].y);
				}
				else
				{
					//System.out.println("Difference trouvee.");
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
		for(int i = 0; i<tab.length; i++) tab[i] = new Point();
	}

	private static Point search_palet()
	{
		Point[] res = new Point[15];
		Point min = new Point();
		
		init_tab_point(res);
		
		res = receive_socket();
		
		min = new Point(300, 300);
		
		//TODO A tester
		for(int i = 0; i<res.length; i++)
		{
			int diff = Math.abs(res[i].x-robot.x)+Math.abs(res[i].y-robot.y);
			
			int diffMin = Math.abs(min.x-robot.x)+Math.abs(min.y-robot.y);
			
			//System.out.println("Diff "+ diff+ " DiffMin"+diffMin);
			
			if(diff<3)
			{
				//Detection de sa propre position
			}
			
			else if((diff)<(diffMin))
			{
				min = res[i];
			}

		}
		
		//S'il ne reste pas de palet en jeu
		//TODO Faire un test pour savoir s'il reste un robot autre que le notre
		if(min.x==300)
		{
			min = new Point(-1,-1);
		}
		
		return min;
	}

	private static void open_arms() {
		Motor.A.setSpeed(1000);
		Motor.A.forward();
		Delay.msDelay(550);
		Motor.A.stop();
	}

	private static void close_arms() {
		Motor.A.setSpeed(1000);
		Motor.A.backward();
		Delay.msDelay(550);
		Motor.A.stop();
	}

	private static void forward(int i) {
		Motor.C.setSpeed(0);
		Motor.B.setSpeed(0);
		
		Motor.C.forward();
		Motor.B.forward();
		
		for(int j=0; j<SPEED;j+=(SPEED/10))
		{
			Motor.C.setSpeed(j);
			Motor.B.setSpeed(j);
			maj_position();
		}
		
		int delay = (i*1000);
		
		for(int j=0; j<delay; j+=(delay/10))
		{
			Delay.msDelay(delay/10);
			maj_position();
		}
		
		stop();
	}
	
	private static void stop() {
		for(int j=0; j<SPEED;j+=(SPEED/10))
		{
			Motor.C.setSpeed(SPEED-j);
			Motor.B.setSpeed(SPEED-j);
			Delay.msDelay(1);
		}
	}

	private static void backward(int i) {
		Motor.C.setSpeed(0);
		Motor.B.setSpeed(0);
		
		Motor.C.backward();
		Motor.B.backward();
		
		for(int j=0; j<SPEED;j+=(SPEED/10))
		{
			Motor.C.setSpeed(j);
			Motor.B.setSpeed(j);
			robot = maj_position();
		}
		
		int delay = (i*1000);
		
		for(int j=0; j<delay; j+=(delay/10))
		{
			Delay.msDelay(delay/10);
			robot = maj_position();
		}
		
		stop();
	}
	
	private static void forward_till_touch() {
		Motor.C.setSpeed(0);
		Motor.B.setSpeed(0);
		
		Motor.C.forward();
		Motor.B.forward();
		
		for(int j=0; j<SPEED;j+=(SPEED/10))
		{
			Motor.C.setSpeed(j);
			Motor.B.setSpeed(j);
			maj_position();
		}
				
		while(!uTouch.isPressed()){maj_position();}
		
		close_arms();

    	stop();
	}

	private static void rotate(int t) {
		//Motor.C.rotate(t);
		Motor.B.rotate(t);
		
		current_orientation = current_orientation+t;
		
		stop();
	}
}
