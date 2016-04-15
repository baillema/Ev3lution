package sensor;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadColors {

public static boolean goMessage() {
		
		GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
		g.clear();
		g.drawString("Color Sensor", 5, 0, 0);
		g.setFont(Font.getSmallFont());
		 
		g.drawString("The code for this sample     ", 2, 20, 0);
		g.drawString("shows how to work with the ", 2, 30, 0);
		g.drawString("Color Sensor ", 2, 40, 0);
		g.drawString("To run the ", 2, 60, 0);
		g.drawString("code one needs an EV3 ", 2, 70, 0);
		g.drawString("brick with a EV3 color sensor", 2, 80, 0); 
		g.drawString("attached to port 1.", 2, 90, 0);
		  
		// Quit GUI button:
		g.setFont(Font.getSmallFont()); // can also get specific size using Font.getFont()
		int y_quit = 100;
		int width_quit = 45;
		int height_quit = width_quit/2;
		int arc_diam = 6;
		g.drawString("QUIT", 9, y_quit+7, 0);
		g.drawLine(0, y_quit,  45, y_quit); // top line
		g.drawLine(0, y_quit,  0, y_quit+height_quit-arc_diam/2); // left line
		g.drawLine(width_quit, y_quit,  width_quit, y_quit+height_quit/2); // right line
		g.drawLine(0+arc_diam/2, y_quit+height_quit,  width_quit-10, y_quit+height_quit); // bottom line
		g.drawLine(width_quit-10, y_quit+height_quit, width_quit, y_quit+height_quit/2); // diagonal
		g.drawArc(0, y_quit+height_quit-arc_diam, arc_diam, arc_diam, 180, 90);
		
		// Enter GUI button:
		g.fillRect(width_quit+10, y_quit, height_quit, height_quit);
		g.drawString("GO", width_quit+15, y_quit+7, 0,true);
		
		Button.waitForAnyPress();
		if(Button.ESCAPE.isDown()) {
			return false;
		}
		else {
			g.clear();
			return true;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			boolean again = true;
			
			if (!goMessage()) System.exit(0);
								
			//Gestion des logs de la couleur
			File file = new File("colors.out");
		
			FileInputStream input = new FileInputStream(file);
 			DataInputStream data = new DataInputStream(input);
						
			Port port = LocalEV3.get().getPort("S1");
			EV3ColorSensor colorSensor = new EV3ColorSensor(port);
			SampleProvider average = new MeanFilter(colorSensor.getRGBMode(), 1);
			colorSensor.setFloodlight(Color.WHITE);
			
			float[] black = new float[average.sampleSize()];
			float[] red = new float[average.sampleSize()];
			float[] blue = new float[average.sampleSize()];
			float[] green = new float[average.sampleSize()];
			float[] yellow = new float[average.sampleSize()];
			float[] white = new float[average.sampleSize()];
			char temp = new char;
			
			ReadInFile(black, data);
			ReadInFile(temp, data);
			ReadInFile(red, data);
			ReadInFile(temp, data);
			ReadInFile(blue, data);
			ReadInFile(temp, data);
			ReadInFile(green, data);
			ReadInFile(temp, data);
			ReadInFile(yellow, data);
			ReadInFile(temp, data);
			ReadInFile(white, data);
			
			//Gestion des logs de la couleur
			File file2 = new File("colors2.out");
			FileWriter fileWriter = new FileWriter(file2);
			
			WriteInFile(black, fileWriter);
			WriteInFile(red, fileWriter);
			WriteInFile(blue, fileWriter);
			WriteInFile(green, fileWriter);
			WriteInFile(yellow, fileWriter);
			WriteInFile(white, fileWriter);
			
			while (again) {
				float[] sample = new float[average.sampleSize()];
				System.out.println("\nPress enter to detect a color...");
				Button.ENTER.waitForPressAndRelease();
				average.fetchSample(sample, 0);
				double minscal = Double.MAX_VALUE;
				String color = "";
				
				double scalaire = SaveColors.scalaire(sample, blue);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "blue";
				}
				
				scalaire = SaveColors.scalaire(sample, red);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "red";
				}
				
				scalaire = SaveColors.scalaire(sample, green);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "green";
				}
				
				scalaire = SaveColors.scalaire(sample, black);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "black";
				}
				
				scalaire = SaveColors.scalaire(sample, yellow);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "yellow";
				}
				
				scalaire = SaveColors.scalaire(sample, white);
				if (scalaire < minscal) {
					minscal = scalaire;
					color = "white";
				}
				
				System.out.println("The color is " + color + " \n");
				System.out.println("Press ENTER to continue \n");
				System.out.println("ESCAPE to exit");
				Button.waitForAnyPress();
				
				
				if(Button.ESCAPE.isDown()) {
					colorSensor.setFloodlight(false);
					again = false;
					input.close();
					fileWriter.close();
				}
			}
				
		} catch (Throwable t) {
			t.printStackTrace();
			Delay.msDelay(10000);
			System.exit(0);
		}
	}
	
	public static double scalaire(float[] v1, float[] v2) {
		return Math.sqrt (Math.pow(v1[0] - v2[0], 2.0) +
				Math.pow(v1[1] - v2[1], 2.0) +
				Math.pow(v1[2] - v2[2], 2.0));
	}
	
	public static void ReadInFile(float[] color, DataInputStream f) throws IOException
	{
		for(int i = 0; i<color.length; i++)
		{
			color[i] = f.readFloat();
		}
	}
	
		public static void WriteInFile(float[] color, FileWriter f) throws IOException
	{
		for(int i = 0; i<color.length; i++)
		{
			f.write(Float.toString(color[i]));
			f.write(" ");
		}
		
		f.write("\n");
	}
}
