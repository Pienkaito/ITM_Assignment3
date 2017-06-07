package itm.video;

import java.awt.image.BufferedImage;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

/**
 * 
 * This class creates JPEG thumbnails from from video frames grabbed from the
 * middle of a video stream It can be called with 2 parameters, an input
 * filename/directory and an output directory.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */

public class VideoFrameGrabber {
	/**
	 * Constructor.
	 */
	public VideoFrameGrabber() {
	}

	/**
	 * Processes the passed input video file / video file directory and stores
	 * the processed files in the output directory.
	 * 
	 * @param input
	 *            a reference to the input video file / input directory
	 * @param output
	 *            a reference to the output directory
	 */
	public ArrayList<File> batchProcessVideoFiles(File input, File output) throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<File> ret = new ArrayList<File>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {
				if (f.isDirectory())
					continue;

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4")) {
					File result = processVideo(f, output);
					System.out.println("converted " + f + " to " + result);
					ret.add(result);
				}

			}

		} else {
			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4")) {
				File result = processVideo(input, output);
				System.out.println("converted " + input + " to " + result);
				ret.add(result);
			}
		}
		return ret;
	}

	/**
	 * Processes the passed audio file and stores the processed file to the
	 * output directory.
	 * 
	 * @param input
	 *            a reference to the input audio File
	 * @param output
	 *            a reference to the output directory
	 */
	protected File processVideo(File input, File output) throws IOException, IllegalArgumentException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		File outputFile = new File(output, input.getName() + "_thumb.jpg");
		// load the input video file

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************
		//Quellen:https://github.com/artclarke/xuggle-xuggler/tree/master/src/com/xuggle/xuggler/demos

	    if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler" + " support) for this demo to work");
	    //Opening the container
	    IContainer container = IContainer.make();	    
	    if (container.open(input.getPath(), IContainer.Type.READ, null) < 0) throw new IllegalArgumentException("could not open file: " + input.getName());
        //testvariable to determine if i already wrote a pic
	    boolean wrotePic= false;
	    //some stream metadata
	    int numStreams = container.getNumStreams();
		long duration = container.getDuration();
		long fileSize = container.getFileSize();
		long bitRate = container.getBitRate();
		//looking for the video stream
	    int videoStreamId = -1;
	    IStreamCoder videoCoder = null;
	    for(int i = 0; i < numStreams; i++){
	    	IStream stream = container.getStream(i);
	    	IStreamCoder coder = stream.getStreamCoder();
	    	if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO){
	    		videoStreamId = i;
	            videoCoder = coder;
	            break;
	    	}
	    }
	    if (videoStreamId == -1)throw new RuntimeException("could not find video stream in container: "+input.getName());
	    if (videoCoder.open() < 0)throw new RuntimeException("could not open video decoder for container: " + input.getName());
	    IVideoResampler resampler = null;
	    //creating resampler if needed
	    if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24){
	        resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
	                						 videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
	        if (resampler == null)throw new RuntimeException("could not create color space resampler for: " + input.getName());
	    }
	    
	    IPacket packet = IPacket.make();
	    //iterating through packets. If they belong to the videostream we grab them.
	    while(container.readNextPacket(packet) >= 0){
	    	if (packet.getStreamIndex() == videoStreamId){
	            IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),videoCoder.getWidth(), videoCoder.getHeight());
	            int offset = 0;
	            while(offset < packet.getSize()){
	            	int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
	            	if (bytesDecoded < 0)throw new RuntimeException("got error decoding video in: " + input.getName());
	            	offset += bytesDecoded;
	            	//if we got a complete picture we test for resampling
	            	if (picture.isComplete()){
	            		IVideoPicture newPic = picture;
	            		//we resample the picture if needed
	            		if (resampler != null){
	                        newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(),picture.getHeight());
	                        if (resampler.resample(newPic, picture) < 0) throw new RuntimeException("could not resample video from: " + input.getName());
	            		}
	                    if (newPic.getPixelType() != IPixelFormat.Type.BGR24)throw new RuntimeException("could not decode video as BGR 24 bit data in: " + input.getName());
	                    BufferedImage img = Utils.videoPictureToImage(newPic);
	                    boolean writePic= false;
	                    //we test if we already wrote a picture. if the picture is in the middle of the stream then we write it and set wrotePic to true hence we are finished
	                    if(picture.getPts()>duration/2){writePic=true;}
	                    if (writePic&& !wrotePic){
	                    	wrotePic=true;
	                    	System.out.println("timestamp: " + picture.getPts()/1000000);
	            			//Writing the JPEG with my method from ImageConverter
	            			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
	            	    	ImageWriteParam param = writer.getDefaultWriteParam();
	            	    	param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	            	    	float quality=1;
	            	    	param.setCompressionQuality(quality);         
	            	        ImageOutputStream outputStream = ImageIO.createImageOutputStream(outputFile);
	            	    	writer.setOutput(outputStream);
	            	    	try{writer.write(null, new IIOImage(img, null, null), param);}
	            	    		catch(IOException e){        		
	            	    	}
	                    }
	            	}
	            }
	    	}
	    	//dropping the rest of the packages
	        else{
	          do {} while(false);
	        }
	    	
	    }
	    if (videoCoder != null)
	    {
	      videoCoder.close();
	      videoCoder = null;
	    }
	    if (container !=null)
	    {
	      container.close();
	      container = null;
	    }
		return outputFile;

	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		// args = new String[] { "./media/video", "./test" };

		if (args.length < 2) {
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-videoFile> <output-directory>");
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		VideoFrameGrabber grabber = new VideoFrameGrabber();
		grabber.batchProcessVideoFiles(fi, fo);
	}

}
