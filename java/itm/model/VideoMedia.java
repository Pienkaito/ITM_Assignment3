package itm.model;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;

public class VideoMedia extends AbstractMedia {

	// ***************************************************************
	// Fill in your code here!
	// ***************************************************************

	/* video format metadata */
	String videoCodec;
	String vidcodecID; 
	double videoFrameRate;
	long videoLength; 
	int videoHeight; 
	int videoWidth;


	/* audio format metadata */
	String audioCodec;
	String audcodecID;
	int audioChannels=0;
	int audioSampleRate;
	int audioBitRate;
	/**
	 * Constructor.
	 */
	public VideoMedia() {
		super();
	}

	/**
	 * Constructor.
	 */
	public VideoMedia(File instance) {
		super(instance);
	}

	/* GET / SET methods */

	// ***************************************************************
	// Fill in your code here!
	// ***************************************************************
	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public String getVidcodecID() {
		return vidcodecID;
	}

	public void setVidcodecID(String vidcodecID) {
		this.vidcodecID = vidcodecID;
	}

	public double getVideoFrameRate() {
		return videoFrameRate;
	}

	public void setVideoFrameRate(double videoFrameRate) {
		this.videoFrameRate = videoFrameRate;
	}

	public long getVideoLength() {
		return videoLength;
	}

	public void setVideoLength(long videoLength) {
		this.videoLength = videoLength;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public String getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	public String getAudcodecID() {
		return audcodecID;
	}

	public void setAudcodecID(String audcodecID) {
		this.audcodecID = audcodecID;
	}

	public int getAudioChannels() {
		return audioChannels;
	}

	public void setAudioChannels(int audioChannels) {
		this.audioChannels = audioChannels;
	}

	public int getAudioSampleRate() {
		return audioSampleRate;
	}

	public void setAudioSampleRate(int audioSampleRate) {
		this.audioSampleRate = audioSampleRate;
	}

	public int getAudioBitRate() {
		return audioBitRate;
	}

	public void setAudioBitRate(int audioBitRate) {
		this.audioBitRate = audioBitRate;
	}
	/* (de-)serialization */

	/**
	 * Serializes this object to the passed file.
	 * 
	 */
	@Override
	public StringBuffer serializeObject() throws IOException {
		StringWriter data = new StringWriter();
		PrintWriter out = new PrintWriter(data);
		out.println("type: video");
		StringBuffer sup = super.serializeObject();
		out.print(sup);
		out.println("AudiocodecID: " + getAudcodecID());
		out.println("audioChannels: " + getAudioChannels());
		out.println("audioSampleRate: " + getAudioSampleRate());
		out.println("audioBitRate: " + getAudioBitRate());
		/* video fields */
		out.println("VideocodecID: " + getVidcodecID());
		out.println("VideoCodec: " + getVideoCodec());
		out.println("VideoFrameRate: " + getVideoFrameRate());
		out.println("VideoLength: " + getVideoLength());
		out.println("VideoHeight: " + getVideoHeight());
		out.println("VideoWidth: " + getVideoWidth());

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************
		return data.getBuffer();
	}



	/**
	 * Deserializes this object from the passed string buffer.
	 */
	@Override
	public void deserializeObject(String data) throws IOException {
		super.deserializeObject(data);

		StringReader sr = new StringReader(data);
		BufferedReader br = new BufferedReader(sr);
		String line = null;
		while ((line = br.readLine()) != null) {
	        while ( ( line = br.readLine() ) != null ) {
	            if ( line.startsWith( "file: " ) ) {
	                File f = new File( line.substring( "file: ".length() ) );
	                setInstance( f );
	                } else
	            if ( line.startsWith( "name: " ) ) 
	                setName( line.substring( "name: ".length() ) );
	                else
	            if ( line.startsWith( "size: " ) ) ;
	                // no need to set size - will be calculated from media file directly
	                else
	            if ( line.startsWith( "tags: " ) ) {
	                StringTokenizer st = new StringTokenizer( line.substring( "tags: ".length() ), "," );
	                while ( st.hasMoreTokens() ) {
	                    String tag = st.nextToken().trim();
	                    addTag( tag );
	                }
	            }
	            if ( line.startsWith( "AudiocodecID: " ) ) {
	                setAudcodecID(line.substring("AudiocodecID: ".length()));
	                } else
	            if ( line.startsWith( "audioChannels: " ) ) 
	            	 setAudioChannels(Integer.parseInt( line.substring("audioChannels: ".length())));
	                else
	            if ( line.startsWith( "audioSampleRate: " ) ) 
	                  setAudioSampleRate(Integer.parseInt( line.substring("audioSampleRate: ".length())));
	            if ( line.startsWith( "audioBitRate: " ) ) 
	                setAudioBitRate(Integer.parseInt( line.substring("audioBitRate: ".length())));
	            if ( line.startsWith( "VideocodecID: " ) ) 
	                setVidcodecID(line.substring("VideocodecID: ".length()));
	            if ( line.startsWith( "VideoCodec: " ) ) 
	                setVideoCodec(line.substring("VideoCodec: ".length()));
	            if ( line.startsWith( "VideoFrameRate: " ) ) 
	                setVideoFrameRate(Integer.parseInt(line.substring("VideoFrameRate ".length())));
	            if ( line.startsWith( "VideoLength:  " ) ) 
	                setVideoLength(Integer.parseInt(line.substring("VideoLength: ".length())));
	            if ( line.startsWith( "VideoHeight: " ) ) 
	                setVideoHeight(Integer.parseInt(line.substring("VideoHeight: ".length())));
	            if ( line.startsWith( "VideoWidth: " ) ) 
	                setVideoWidth(Integer.parseInt(line.substring("VideoWidth: ".length())));
	                                      
	        }
			/* video fields */
			// ***************************************************************
			// Fill in your code here!
			// ***************************************************************
		}
	}

}
