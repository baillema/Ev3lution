package sensor;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;

public class ForwardTillTouch
{
    public static void main(String[] args)
    {
        try {
        	log("Program Starting");
        	
        Button.ENTER.waitForPress();
        Motor.A.forward();
    	Delay.msDelay(2000);
    	Motor.A.stop();
        TouchSensor uTouch = new TouchSensor(SensorPort.S3);
        waitForTouch(uTouch);

        log("Program Ending");
        
        } catch (Throwable t) {
			t.printStackTrace();
			Delay.msDelay(10000);
			System.exit(0);
		}
    }
   

    private static void waitForTouch(TouchSensor uTouch)
    {
        log("Waiting for press on Touch Sensor");

        while (! uTouch.isPressed())
        {
        	Motor.B.forward();
        	Motor.C.forward();
            //Delay.msDelay(1000);
        }
        
        Motor.B.stop();
    	Motor.C.stop();
    	
    	Motor.A.backward();
    	Delay.msDelay(2000);
    	Motor.A.stop();
    	
    	Motor.B.backward();
    	Motor.C.backward();
    	Delay.msDelay(1000);

        log("Touch Sensor pressed.");
    }


    private static void log(String msg)
    {
        System.out.println("log>\t" + msg);
    }
}
