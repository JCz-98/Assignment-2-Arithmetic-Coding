package a2;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import app.FreqCountIntegerSymbolModel;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

public class PriorValueEncoder 
{
	
	public static void main(String[] args) throws IOException, InsufficientBitsLeftException 
	{
		String input_file_name = "vdata/out.dat";
		String output_file_name = "vdata/raw_compressed.txt";

		int range_bit_width = 40;

		System.out.println("Encoding file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);


		Integer[] colors = new Integer[256];
		
		for (int i=0; i<256; i++) 
		{
			colors[i] = i;
		}
		
		// Create 256 models. Model chosen depends on value of colors prior to 
		// a pixel being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) 
		{
			// Create new model with default count of 1 for all colors
			models[i] = new FreqCountIntegerSymbolModel(colors);
		}
		
		
		
		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);
		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
		// First byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);
		
		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];	
		
		int Frames[][] = new int[300][4096];
	
		
		int seconds = 0;
		int frames = 0;
		int bytes = 0;
		
		
		System.out.println("\nEncoding...");
		for(int i=0; i<Frames.length; i++)
		{
			if(i == 0)
			{
//				System.out.print("FRAME: " + i + "\n[ ");
				for(int j=0; j<Frames[i].length; j++)
				{
					
					int pixel = fis.read();
					bytes++;
					Frames[i][j] = pixel;
					
					encoder.encode(pixel, model, bit_sink);
					
					// Update model used
					model.addToCount(pixel);
					
					// Set up next model based on symbol just encoded
					model = models[pixel];
					
//					if(j<192)
//					{
//						System.out.print(pixel + ", ");
//						if(j==63)
//							System.out.println();
//						if(j==126)
//							System.out.println();
//					}
				}
//				System.out.println(" ] ");
				
			}
			else
			{
				//System.out.print("FRAME: " + i + "\n[ ");
				for(int j=0; j<Frames[i].length; j++)
				{
					int pixel = fis.read();
					bytes++;
					Frames[i][j] = pixel;
					
					
					int last_pixel = Frames[i-1][j];
					// use model of pixel in previous frame
					model = models[last_pixel];
					//encode using previous model
					encoder.encode(pixel, model, bit_sink);
					
					// Update model used
					model.addToCount(pixel);	
					
				}
				//System.out.println(" ] ");
			}
			
			frames++;
			
			if (frames % 10 == 0)
			{
				seconds++;
			}
		}
		System.out.println("\nEcoded video stats---" );
		System.out.println("Resol: " + (int)Math.sqrt(bytes / frames) + " x " + (int)Math.sqrt(bytes / frames));
		System.out.println("Frames: " + frames);
		System.out.println("Seconds: " + seconds);
		//1,228,800
		System.out.println("Bites: " + bytes);
		
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
			
		
		
	}
	

}
