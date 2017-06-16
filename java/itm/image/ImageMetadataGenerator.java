package itm.image;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import itm.model.ImageMedia;
import itm.model.MediaFactory;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * This class reads images of various formats and stores some basic image meta data to text files.
 * It can be called with 3 parameters, an input filename/directory, an output directory and an "overwrite" flag.
 * It will read the input image(s), retrieve some meta data and write it to a text file in the output directory.
 * The overwrite flag indicates whether the resulting output file should be overwritten or not.
 * <p>
 * If the input file or the output directory do not exist, an exception is thrown.
 */
public class ImageMetadataGenerator {
	private final static int COLOR_RED = 0;
	private final static int COLOR_GREEN = 1;
	private final static int COLOR_BLUE = 2;
	private final static int COLOR_GRAYSCALE = 3;

	/**
	 * Constructor.
	 */
	public ImageMetadataGenerator() {
	}


	/**
	 * Processes an image directory in a batch process.
	 *
	 * @param input     a reference to the input image directory
	 * @param output    a reference to the output directory
	 * @param overwrite indicates whether existing metadata files should be overwritten or not
	 * @return a list of the created media objects (images)
	 */
	public ArrayList<ImageMedia> batchProcessImages(File input, File output, boolean overwrite) throws IOException {
		if (!input.exists()) {
			throw new IOException("Input file " + input + " was not found!");
		}
		if (!output.exists()) {
			throw new IOException("Output directory " + output + " not found!");
		}
		if (!output.isDirectory()) {
			throw new IOException(output + " is not a directory!");
		}

		ArrayList<ImageMedia> ret = new ArrayList<ImageMedia>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {
				try {
					ImageMedia result = processImage(f, output, overwrite);
					System.out.println("converted " + f + " to " + output);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error converting " + input + " : " + e0.toString());
				}
			}
		} else {
			try {
				ImageMedia result = processImage(input, output, overwrite);
				System.out.println("converted " + input + " to " + output);
				ret.add(result);
			} catch (Exception e0) {
				System.err.println("Error converting " + input + " : " + e0.toString());
			}
		}
		return ret;
	}

	/**
	 * Processes the passed input image and stores the extracted metadata to a textfile in the output directory.
	 *
	 * @param input     a reference to the input image
	 * @param output    a reference to the output directory
	 * @param overwrite indicates whether existing metadata files should be overwritten or not
	 * @return the created image media object
	 */
	protected ImageMedia processImage(File input, File output, boolean overwrite) throws IOException, IllegalArgumentException {
		if (!input.exists()) {
			throw new IOException("Input file " + input + " was not found!");
		}
		if (input.isDirectory()) {
			throw new IOException("Input file " + input + " is a directory!");
		}
		if (!output.exists()) {
			throw new IOException("Output directory " + output + " not found!");
		}
		if (!output.isDirectory()) {
			throw new IOException(output + " is not a directory!");
		}

		// create outputfilename and check whether thumb already exists. All image
		// metadata files have to start with "img_" -  this is used by the mediafactory!
		File outputFile = new File(output, "img_" + input.getName() + ".txt");
		if (outputFile.exists()) {
			if (!overwrite) {
				// load from file
				ImageMedia media = new ImageMedia();
				media.readFromFile(outputFile);
				return media;
			}
		}


		// get metadata and store it to media object
		ImageMedia media = (ImageMedia) MediaFactory.createMedia(input);

		// ***************************************************************
		//  Fill in your code here!
		// ***************************************************************

		// load the input image
		BufferedImage img = null;
		try {
			img = ImageIO.read(input);
		} catch (IOException e) {
		}
		// set width and height of the image
		media.setWidth(img.getWidth());
		media.setHeight(img.getHeight());
		// add a tag "image" to the media
		media.addTag("image");
		// add a tag corresponding to the filename extension of the file to the media
		String extension = input.getName();
		int pos = extension.lastIndexOf(".");
		if (pos > 0) {
			extension = extension.substring(pos + 1, input.getName().length());
		}
		media.addTag(extension);
		// set orientation
		if (img.getWidth() > img.getHeight()) {
			media.setOrientation(0);
		} else media.setOrientation(1);
		// if there is a colormodel:
		if (img.getColorModel() != null) {
			media.setNumberOfImageComponents(img.getColorModel().getNumComponents());
			media.setNumberOfImageColorComponents(img.getColorModel().getNumColorComponents());
			// set color space type
			media.setColorSpaceType(img.getColorModel().getColorSpace().getType());

			// set pixel size
			media.setPixelSize(img.getColorModel().getPixelSize());
			// set transparency
			media.setTransparency(img.getColorModel().getTransparency());
			// set number of (color) components
			int focuscolor = getDominantColor(img);
			switch(focuscolor){
				case COLOR_RED:
					media.addTag("red");
					break;
				case COLOR_GREEN:
					media.addTag("green");
					break;
				case COLOR_BLUE:
					media.addTag("blue");
					break;
				default:
					media.addTag("red");
					media.addTag("green");
					media.addTag("blue");
					break;
			}
		}

		// store meta data
		try (PrintWriter out = new PrintWriter(outputFile)) {
			out.print(media.serializeObject());
		}

		return media;
	}

	private int getDominantColor(BufferedImage image) {
		int totalred = 0, totalgreen = 0, totalblue = 0;

		ColorModel model = ColorModel.getRGBdefault();

		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		int dominantcolor = 0;

		for(int i = 0; i < pixels.length; i++) {
			int current_red = model.getRed(pixels[i]);
			int current_green = model.getGreen(pixels[i]);
			int current_blue = model.getBlue(pixels[i]);

			dominantcolor = Math.max(current_red, Math.max(current_green, current_blue));

			if(current_red == current_green && current_red == current_blue){
				totalred++;
				totalgreen++;
				totalblue++;
			} else {
				if (dominantcolor == current_red) {
					totalred++;
				} else if (dominantcolor == current_green) {
					totalgreen++;
				} else {
					totalblue++;
				}
			}
		}

		if(totalred == totalgreen && totalred == totalblue)
			return COLOR_GRAYSCALE;
		else {
			dominantcolor = Math.max(totalred, Math.max(totalgreen, totalblue));

			if(dominantcolor == totalred) {
				return COLOR_RED;
			} else if(dominantcolor == totalgreen) {
				return COLOR_GREEN;
			} else {
				return COLOR_BLUE;
			}
		}
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage information if required.
	 */
	public static void main(String[] args) throws Exception {
		/*
		if (args.length < 2) {
			System.out.println("usage: java itm.image.ImageMetadataGenerator <input-image> <output-directory>");
			System.out.println("usage: java itm.image.ImageMetadataGenerator <input-directory> <output-directory>");
			System.exit(1);
		}
		*/
		File fi = new File("C:/Users/Mark Anthony/odrive/Google Work/17SS/ITM/ASS3/media/img/elephant14m.png");
		File fo = new File("C:/Users/Mark Anthony/odrive/Google Work/17SS/ITM/ASS3/media/img/");
		ImageMetadataGenerator img = new ImageMetadataGenerator();
		img.batchProcessImages(fi, fo, true);
	}
}