import sensor.TouchSensor;
import sun.management.Sensor;
import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class testJerome {

	private static SampleProvider sampler;
	
	public static void main(String[] args) {
		//test
		try 
		{
			System.out.println("Push the central button to START");
			Button.ENTER.waitForPress();
			TouchSensor uTouch = new TouchSensor(SensorPort.S3);
			SensorModes sensor = new EV3UltrasonicSensor(SensorPort.S2);
			sampler = sensor.getMode("Distance");
			arms_init();
	        float[] sample = new float[sampler.sampleSize()];
	        boolean turn=true;
	        rotate1Clockwise();
	        while(turn==true)        // We take 10 samples at 2 seconds intervals and log them
	        {
	            sampler.fetchSample(sample, 0);
	            float t1 = (float)sample[0];
	            if((float)sample[0]<0.8)
	            {
	            	Delay.msDelay((long) 1.00);
	            	sampler.fetchSample(sample, 0);
	            	if((float)sample[0]-t1>0.50)
	            	{
	            		turn=false;
	            	}
	            }
	        }
	        Delay.msDelay(10);
	        rotate1antiClockwise();
	        Delay.msDelay(20);
	        stop();
	        Delay.msDelay(5);
	        forward();
	        while (uTouch.isPressed()==false)
			{	
			}
			stop();
			close_arms();
			Delay.msDelay(500);
			open_arms();
	        System.out.println("Program Exiting");
			Button.ENTER.waitForPress();
			
			//forward();
			
			//Infra.close();
			//uTouch.close();
		} 
		catch (Throwable t) 
		{
			t.printStackTrace();
			Delay.msDelay(10000);
			System.exit(0);
			
		}
	}
	
	private static void waitForTouch(TouchSensor uTouch)
    {
        //log("Waiting for press on Touch Sensor");

        while (! uTouch.isPressed())
        {
        	Motor.B.forward();
        	Motor.C.forward();
            //Delay.msDelay(1000);
        }
        
        Motor.B.stop();
    	Motor.C.stop();
    	close_arms();
    	

        //log("Touch Sensor pressed.");
    }
	
	public static void arms_init()
	{
		Motor.A.setSpeed(700);
	}
	
	public static void change_speed(int a)
	{
		Motor.B.setSpeed(a);
		Motor.C.setSpeed(a);
	}
	
	public static void open_arms()
	{
		Motor.A.forward();
		Delay.msDelay(550);
		Motor.A.stop();
	}
	
	public static void close_arms()
	{
		Motor.A.backward();
		Delay.msDelay(550);
		Motor.A.stop();
	}
	
	public static void forward()
	{
		Motor.B.forward();
		Motor.C.forward();
	}
	
	public static void forward_to(int a)
	{
		forward();
		Delay.msDelay(a);
		stop();
	}
	
	public static void backward_to(int a)
	{
		backward();
		Delay.msDelay(a);
		stop();
	}
	
	public static void stop()
	{
		Motor.B.stop();
		Motor.C.stop();
	}
	
	public static void backward()
	{
		Motor.B.backward();
		Motor.C.backward();		
	}
	
	public static void rotateClockwise()
    {
        Motor.C.forward();
        Motor.B.backward();
    }


    public static void rotateCounterClockwise()
    {
        Motor.C.backward();
        Motor.B.forward();
    }
    
    public static void rotate1Clockwise()
    {
    	Motor.B.forward();
    }
    
    public static void rotate1antiClockwise()
    {
    	Motor.C.forward();
    }
    public static void halfTurn()
    {
    	change_speed(300);
    	rotateClockwise();
    	Delay.msDelay(1000);
    	stop();
    	change_speed(600);
    }

}
