package itm.model;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import itm.audio.AudioMetadataGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class AudioMedia extends AbstractMedia {
	public class Duration{
		public int minutes = 0;
		public int seconds = 0;
		public Duration(int minutes, int seconds){
			this.minutes = minutes;
			this.seconds = seconds;
		}
		public Duration(int seconds){
			this.minutes = seconds/60;
			this.seconds = seconds - (minutes*60);
		}

		public Duration(double seconds){
			this.minutes = (int)seconds/60;
			this.seconds = (int)seconds - (minutes*60);
		}

		public String[] getStringArray() {
			String[] time = new String[2];
			time[0] = Integer.toString(this.minutes);
			time[1] = Integer.toString(this.seconds);
			return time;
		}

		@Override
		public String toString() {
			return minutes + ":" + seconds;
		}
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setDuration(String[] duration) {
		this.duration.minutes = Integer.parseInt(duration[0]);
		this.duration.seconds = Integer.parseInt(duration[1]);
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public void setComposer(String composer) {
		this.composer = composer;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
	public void setChannels(int channels) {
		this.channels = channels;
	}

	private String encoding = "";
	private Duration duration = new Duration(0,0);
	private String author = "";
	private String title = "";
	private String date = "";
	private String comment = "";
	private String album = "";
	private String track = "";
	private String composer = "";
	private String genre = "";
	private float frequency = 0;
	private int bitrate = 0;
	private int channels = 0;

	/**
	 * Constructor.
	 */
	public AudioMedia() {
		super();
	}

	/**
	 * Constructor.
	 */
	public AudioMedia(File instance) {
		super(instance);
	}

	/**
	 * Serializes this object to the passed file.
	 */
	@Override
	public StringBuffer serializeObject() throws IOException {
		StringWriter data = new StringWriter();
		PrintWriter out = new PrintWriter(data);
		out.println("type: audio");
		StringBuffer sup = super.serializeObject();
		out.print(sup);

		out.println("encoding: " + this.encoding);
		out.println("duration: " + this.duration);
		out.println("author: " + this.author);
		out.println("title: " + this.title);
		out.println("date: " + this.date);
		out.println("comment: " + this.comment);
		out.println("album: " + this.album);
		out.println("track: " + this.track);
		out.println("composer: " + this.composer);
		out.println("genre: " + this.genre);
		out.println("frequency: " + this.frequency);
		out.println("bitrate: " + this.bitrate);
		out.println("channels: " + this.channels);

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
			if (line.startsWith("encoding: ")) {
				this.setEncoding(line.substring("encoding: ".length()));
			} else if (line.startsWith("duration: ")) {
				this.setDuration(line.substring("duration: ".length()).split("\\:"));
			} else if (line.startsWith("author: ")) {
				this.setAuthor(line.substring("author: ".length()));
			} else if (line.startsWith("title: ")) {
				this.setTitle(line.substring("title: ".length()));
			} else if (line.startsWith("date: ")) {
				this.setDate(line.substring("date: ".length()));
			} else if (line.startsWith("comment: ")) {
				this.setComment(line.substring("comment: ".length()));
			} else if (line.startsWith("album: ")) {
				this.setAlbum(line.substring("album: ".length()));
			} else if (line.startsWith("track: ")) {
				this.setTrack(line.substring("track: ".length()));
			} else if (line.startsWith("composer: ")) {
				this.setComposer(line.substring("composer: ".length()));
			} else if (line.startsWith("genre: ")) {
				this.setGenre(line.substring("genre: ".length()));
			} else if (line.startsWith("frequency: ")) {
				this.setFrequency(Float.parseFloat(line.substring("frequency: ".length())));
			} else if (line.startsWith("bitrate: ")) {
				this.setBitrate(Integer.parseInt(line.substring("bitrate: ".length())));
			} else if (line.startsWith("channels: ")) {
				this.setChannels(Integer.parseInt(line.substring("channels: ".length())));
			}
		}
	}
}
