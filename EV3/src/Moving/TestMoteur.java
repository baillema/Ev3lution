package Moving;
import lejos.hardware.Button;
import lejos.utility.Delay;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;

public class TestMoteur
{
    private static NXTRegulatedMotor mLeftMotor = Motor.B;
    private static NXTRegulatedMotor mRightMotor = Motor.C;
    

    private final static int SPEED = 500;
	
    public static void main(String[] args)
    {
    	try
    	{
    		System.out.println("Test des moteurs, pressez le bouton central.");
    		
	    	Button.ENTER.waitForPress();
	    	
	    	mLeftMotor.setSpeed(SPEED);
	    	mRightMotor.setSpeed(SPEED);
	    	
	    	//Devant
	    	forward();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	//Derrière
	    	backward();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	//Rotation sens horaire
	    	rotateClockwise();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	//Rotation sens anti-horraire
	    	rotateCounterClockwise();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	//Tourner à gauche
	    	turnLeft();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	//Tourner à droite
	    	turnRight();
	    	Delay.msDelay(5000);
	    	stop();
	    	
	    	Delay.msDelay(10000);
    	}
    	
		catch (Throwable t) 
		{
			t.printStackTrace();
			Delay.msDelay(10000);
			System.exit(0);
		}
    }
   
    public static void forward()
    {
        mLeftMotor.forward();
        mRightMotor.forward();
    }
    
    public static void backward()
    {
        mLeftMotor.backward();
        mRightMotor.backward();
    }

    //Stop des moteurs de manière douce
    public static void stop()
    {
    	mLeftMotor.setSpeed(SPEED);
    	mRightMotor.setSpeed(SPEED);
    	
    	for(int i=mLeftMotor.getSpeed();i>=0; i--)
        {
    		mLeftMotor.setSpeed(i);
    		mRightMotor.setSpeed(i);
    		Delay.msDelay(10);
        }
        
    }
    
    public static void rotateClockwise()
    {
        mLeftMotor.forward();
        mRightMotor.backward();
    }


    public static void rotateCounterClockwise()
    {
        mLeftMotor.backward();
        mRightMotor.forward();
    }

    public static void turnLeft()
    {
        mLeftMotor.forward();
        mRightMotor.setSpeed(SPEED/2);
        mRightMotor.forward();
    }

    public static void turnRight()
    {
        mLeftMotor.backward();
        mLeftMotor.setSpeed(SPEED/2);
        mRightMotor.forward();
    }
}