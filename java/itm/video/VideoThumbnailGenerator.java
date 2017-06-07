package itm.video;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import itm.util.ImageCompare;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
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
 * This class reads video files, extracts metadata for both the audio and the
 * video track, and writes these metadata to a file.
 * 
 * It can be called with 3 parameters, an input filename/directory, an output
 * directory and an "overwrite" flag. It will read the input video file(s),
 * retrieve the metadata and write it to a text file in the output directory.
 * The overwrite flag indicates whether the resulting output file should be
 * overwritten or not.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */
public class VideoThumbnailGenerator {

	/**
	 * Constructor.
	 */
	public VideoThumbnailGenerator() {
	}

	/**
	 * Processes a video file directory in a batch process.
	 * 
	 * @param input
	 *            a reference to the video file directory
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing output files should be overwritten
	 *            or not
	 * @return a list of the created media objects (videos)
	 */
	public ArrayList<File> batchProcessVideoFiles(File input, File output, boolean overwrite, int timespan) throws IOException {
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

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4"))
					try {
						File result = processVideo(f, output, overwrite, timespan);
						System.out.println("processed file " + f + " to " + output);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error processing file " + input + " : " + e0.toString());
					}
			}
		} else {

			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4"))
				try {
					File result = processVideo(input, output, overwrite, timespan);
					System.out.println("processed " + input + " to " + result);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error when creating processing file " + input + " : " + e0.toString());
				}

		}
		return ret;
	}

	/**
	 * Processes the passed input video file and stores a thumbnail of it to the
	 * output directory.
	 * 
	 * @param input
	 *            a reference to the input video file
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing files should be overwritten or not
	 * @return the created video media object
	 */
	protected File processVideo(File input, File output, boolean overwrite, int timespan) throws Exception {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		// create output file and check whether it already exists.
		File outputFile = new File(output, input.getName() + "_thumb.avi");

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************
		//Quellen:https://github.com/artclarke/xuggle-xuggler/tree/master/src/com/xuggle/xuggler/demos
		// extract frames from input video
		if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler" + " support) for this demo to work");
	    //creating the container
	    IContainer container = IContainer.make();
	    
	    if (container.open(input.getPath(), IContainer.Type.READ, null) < 0) throw new IllegalArgumentException("could not open file: " + input.getName());
        
	    boolean wrotePic= false;
	    //some Metadata
	    int numStreams = container.getNumStreams();
		long duration = container.getDuration();
		long fileSize = container.getFileSize();
		long bitRate = container.getBitRate();
		int vidWidth=0;
		int vidHeight=0;
		//finding the videostream
	    int videoStreamId = -1;
	    IStreamCoder videoCoder = null;
	    for(int i = 0; i < numStreams; i++){
	    	IStream stream = container.getStream(i);
	    	IStreamCoder coder = stream.getStreamCoder();
	    	if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO){
	    		videoStreamId = i;
	            videoCoder = coder;
	            vidHeight=coder.getHeight();
	            vidWidth= coder.getWidth();	           
	            break;
	    	}
	    }
	    if (videoStreamId == -1)throw new RuntimeException("could not find video stream in container: "+input.getName());
	    if (videoCoder.open() < 0)throw new RuntimeException("could not open video decoder for container: " + input.getName());
	    //resampling if needed
	    IVideoResampler resampler = null;
	    if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24){
	        resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
	                						 videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
	        if (resampler == null)throw new RuntimeException("could not create color space resampler for: " + input.getName());
	    }
	    //loading the watermark
	    BufferedImage wm = ImageIO.read(new URL("http://i.imgur.com/8XmfJHS.jpg"));
	    IPacket packet = IPacket.make();
	    ArrayList <BufferedImage> frames= new ArrayList<BufferedImage>();
	    long lastPointCaptured=0;
	    //iterating through the packets and grab the videostream packets
	    while(container.readNextPacket(packet) >= 0){
	    	if (packet.getStreamIndex() == videoStreamId){
	            IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),videoCoder.getWidth(), videoCoder.getHeight());
	            int offset = 0;
	            while(offset < packet.getSize()){
	            	int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
	            	if (bytesDecoded < 0)throw new RuntimeException("got error decoding video in: " + input.getName());
	            	offset += bytesDecoded;
	            	//if the picture is completed we resample it if needed
	            	if (picture.isComplete()){
	            		IVideoPicture newPic = picture;
	            		if (resampler != null){
	                        newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(),picture.getHeight());
	                        if (resampler.resample(newPic, picture) < 0) throw new RuntimeException("could not resample video from: " + input.getName());
	            		}
	                    if (newPic.getPixelType() != IPixelFormat.Type.BGR24)throw new RuntimeException("could not decode video as BGR 24 bit data in: " + input.getName());
	                    
	                    boolean writePic= false;
	                    //if the interval requirement is met we write the picture We always write the first picture
	                    if(picture.getPts()>lastPointCaptured+timespan*1000000){writePic=true;}
	                    if (writePic || !wrotePic){
	                    	lastPointCaptured=picture.getPts();
	                    	//we draw the buffered image and add the watermark
	                    	BufferedImage img = Utils.videoPictureToImage(newPic);
	                		Graphics2D w = img.createGraphics();	
	                		w.drawRenderedImage(wm, null);
	                		w.dispose();
	                    	//System.out.println("timestamp: " + picture.getPts()/1000000);
	                    	//if the timespan given is 0 we always write a pic. In this case we have to compare the frames we can only compare if we already wrote a picture
	                    	if(timespan==0 && wrotePic){
	                    		//we compare the image with the last one
	                    		ImageCompare compare= new ImageCompare(frames.get(frames.size()-1),img);
	                    		//not sure what the best parameters are, those gave me a decent amount of frames so i igured it would be ok
	                    		compare.setParameters(12, 12, 10, 10);
	                    		compare.compare();
	                    		//if they are different from each other we add them to the list.
	                    		if(!compare.match()){
	                    			frames.add(img);
	                    		}
	                    	}
	                    	//we add the captured frames to the list.
	                    	else frames.add(img);
	                    	wrotePic=true;
	                    	writePic=false;
	                    }
	            	}
	            }
	    	}
	    	//we drop the rest of the packages
	        else{
	          do {} while(false);
	        }
	    	
	    }
	    //we close the coder and container
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
	    //we write the captured images to a videoStream
	    long startTime = System.nanoTime();
	    System.out.println("amount of frames captured: " + frames.size());
	    IMediaWriter writer = ToolFactory.makeWriter(outputFile.getPath());
	    writer.addVideoStream(0, 0,ICodec.ID.CODEC_ID_MPEG4, vidWidth, vidHeight);
	    //System.out.println(writer.getDefaultPixelType());
	    int f=0;
	    int fps=50;
	    for(int i=0; i<frames.size()*fps;i++){
	    	
	    	if(f<frames.size()-1){
	    		if((f+1)*fps<i)
	    			f++;}
	    	writer.encodeVideo(0, frames.get(f), System.nanoTime()-startTime,TimeUnit.NANOSECONDS);
	    	Thread.sleep((long) (1000/fps));
	    	

	    }
	    writer.close();


		// add a watermark of your choice and paste it to the image
        // e.g. text or a graphic

		// create a video writer

		// add a stream with the proper width, height and frame rate
		
		// if timespan is set to zero, compare the frames to use and add 
		// only frames with significant changes to the final video

		// loop: get the frame image, encode the image to the video stream
		
		// Close the writer

		return outputFile;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
            System.out.println("usage: java itm.video.VideoThumbnailGenerator <input-video> <output-directory> <timespan>");
            System.out.println("usage: java itm.video.VideoThumbnailGenerator <input-directory> <output-directory> <timespan>");
            System.exit(1);
        }
        File fi = new File(args[0]);
        File fo = new File(args[1]);
        int timespan = 5;
        if(args.length == 3)
            timespan = Integer.parseInt(args[2]);
        
        VideoThumbnailGenerator videoMd = new VideoThumbnailGenerator();
        videoMd.batchProcessVideoFiles(fi, fo, true, timespan);
	}
}