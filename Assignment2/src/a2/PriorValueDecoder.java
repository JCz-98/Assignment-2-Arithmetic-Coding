package a2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import app.FreqCountIntegerSymbolModel;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorValueDecoder 
{
	
	public static void main(String[] args) throws InsufficientBitsLeftException, IOException 
	{
		String input_file_name = "vdata/raw_compressed.txt";
		String output_file_name = "vdata/raw_decompressed.txt";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] colors = new Integer[256];
		
		for (int i=0; i<256; i++) 
		{
			colors[i] = i;
		}
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) 
		{
			// Create new model with default count of 1 for all colors
			models[i] = new FreqCountIntegerSymbolModel(colors);
		}
		
		int range_bit_width = bit_source.next(8);
//		
//		System.out.println(range_bit_width);
		
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		
		int Frames[][] = new int[300][4096];
	
		
		int seconds = 0;
		int frames = 0;
		int pixels = 0;
		
		
		System.out.println("\nDecoding...");
		for(int i=0; i<Frames.length; i++)
		{
			if(i == 0)
			{
//				System.out.print("FRAME: " + i + "\n[ ");
				for(int j=0; j<Frames[i].length; j++)
				{
					
					int pixel = decoder.decode(model, bit_source);
					pixels++;
					Frames[i][j] = pixel;
					fos.write(pixel);
					
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
				
				for(int j=0; j<Frames[i].length; j++)
				{
					int last_pixel = Frames[i-1][j];
					model = models[last_pixel];
					
					
					int pixel = decoder.decode(model, bit_source);
					pixels++;
					Frames[i][j] = pixel;
					fos.write(pixel);
					
					// Update model used
					model.addToCount(pixel);			
					
				}
			}
			
			frames++;
			
			if (frames % 10 == 0)
			{
				seconds++;
			}
		}
		System.out.println("\nDecoded video stats---" );
		System.out.println("Resol: " + (int)Math.sqrt(pixels / frames) + " x " + (int)Math.sqrt(pixels / frames));
		System.out.println("Frames: " + frames);
		System.out.println("Seconds: " + seconds);
		//1,228,800
		System.out.println("Bites/Pixels: " + pixels);
		
		System.out.println("Done!");
		fos.flush();
		fos.close();
		fis.close();
		
		
		
	}

}
