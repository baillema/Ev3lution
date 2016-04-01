package choice;

public class Automata {

	public enum State {
        WAIT,
        SEARCH,
        SEARCHD,
        SEARCHG,
        DETECT,
        CATCH,
        RETURN
    };
    
    public static void main(String[] args) {
		
		TouchSensor uTouch = new TouchSensor(SensorPort.S3);
		State current_state = State.WAIT;
		
		try 
		{
			System.out.println("Push the central button to START");
			Button.ENTER.waitForPress();
			current_state = State.SEARCH;
			
			while(current_state != State.RETURN)
			{
				switch (current_state) {
		        	case State.WAIT:
		        		//Attendre que l'on presse
		        		System.out.println("Push the central button to START");
						Button.ENTER.waitForPress();
						current_state = State.SEARCH;
		        		break;
		        		
		        	case State.SEARCH:
		        		//Detection de palet
		        			//Si on trouve un palet
		        				//current_state = State.DETECT;
		        			//Sinon
		        				//current_state = State.SEARCHD;
		                break;
		                
		        	case State.SEARCHD:  
		                //Detection de palet
		        			//Si on trouve un palet
		        				//current_state = State.DETECT;
		        			//Sinon
		        				//current_state = State.SEARCHG;
		                break;
		                
		        	case State.SEARCHG:
		        		//Detection de palet
		        			//Si on trouve un palet
		        				//current_state = State.DETECT;
		        			//Sinon
		        				//On s'arrête
		        				//current_state = State.RETURN;
		                break;
		                
		        	case State.DETECT: 
		        		//Avancer de X cm tant que l'on ne touche pas de palet
		        			//Si on touche un palet
		        				//current_state = State.CATCH	
		        		//Si on ne trouve pas
		        			//current_state = State.SEARCH;
		                break;
		                
		        	case State.CATCH:  
		        		//Demi-tour (rotate clockwise)
		        		//Avancer tant que l'on ne trouve pas le couleur blanc
		        			//Si on trouve le blanc
		        				//current_state = State.RETURN;
		                break;
		                
		       	 	case State.RETURN:
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
}
