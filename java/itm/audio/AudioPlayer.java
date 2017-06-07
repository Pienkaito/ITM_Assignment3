package itm.audio;


import java.io.BufferedInputStream;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Plays an audio file using the system's default sound output device
 */
public class AudioPlayer {

	private final static int sampleSizeInBits = 16;

	/**
	 * Constructor
	 */
	public AudioPlayer() {

	}

	/**
	 * Plays audio data from a given input file to the system's default sound
	 * output device
	 *
	 * @param input the audio file
	 * @throws IOException general error when accessing audio file
	 */
	protected void playAudio(File input) throws IOException {

		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");

		AudioInputStream audio = null;
		try {
			audio = openAudioInputStream(input);
		} catch (UnsupportedAudioFileException e) {
			throw new IOException("could not open audio file " + input + ". Encoding / file format not supported");
		}

		try {
			rawplay(audio);
		} catch (LineUnavailableException e) {
			throw new IOException(
					"Error when playing sound from file " + input.getName() + ". Sound output device unavailable");
		}

		audio.close();

	}

	/**
	 * Decodes an encoded audio file and returns a PCM input stream
	 * <p>
	 * Supported encodings: MP3, OGG (requires SPIs to be in the classpath)
	 *
	 * @param input a reference to the input audio file
	 * @return a PCM AudioInputStream
	 * @throws UnsupportedAudioFileException an audio file's encoding is not supported
	 * @throws IOException                   general error when accessing audio file
	 */
	private AudioInputStream openAudioInputStream(File input) throws UnsupportedAudioFileException, IOException {

		AudioInputStream din = null;

		try {
			AudioInputStream readStream = AudioSystem.getAudioInputStream(input);

			AudioFormat baseFormat = readStream.getFormat();
			// sampleSizeInBits = The sample size indicates how many bits are
			// used to buffer (16 bit)
			// frameSize = Bytes per frame (In PCM; size of a frame (in bytes) =
			// the size of a sample (in bytes) times number of channels)
			// sample = "snapshot" of the sound pressure
			// a frame consists of set of samples
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
					sampleSizeInBits, baseFormat.getChannels(), (sampleSizeInBits / 8) * baseFormat.getChannels(),
					baseFormat.getSampleRate(), false);
			din = AudioSystem.getAudioInputStream(decodedFormat, readStream);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return din;
	}

	/**
	 * Writes audio data from an AudioInputStream to a SourceDataline
	 *
	 * @param audio the audio data
	 * @throws IOException              error when writing audio data to source data line
	 * @throws LineUnavailableException system's default source data line is not available
	 */
	private void rawplay(AudioInputStream audio) throws IOException, LineUnavailableException {

		AudioFormat audiof = audio.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audiof);
		SourceDataLine dataline = (SourceDataLine) AudioSystem.getLine(info);

		int bufferSize = (int) audiof.getFrameRate() * audiof.getFrameSize();
		byte[] buffer = new byte[bufferSize];

		dataline.open(audiof);

		if (dataline != null) {
			try {
				/*
				 * Load PCM data from AudioInputStream into the SourceDataLine
				 * buffer until the end of file is reached. After reaching EOF,
				 * close line and input stream
				 */
				dataline.start();
				int readbytes = 0;
				while (readbytes != -1) {
					readbytes = audio.read(buffer, 0, buffer.length);
					if (readbytes > 0) {
						dataline.write(buffer, 0, readbytes);
					}
				}
				// Stop
				dataline.drain();
				dataline.stop();
				dataline.close();
				audio.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("usage: java itm.audio.AudioPlayer <input-audioFile>");
			System.exit(1);
		}

		File fi = new File(args[0]);
		AudioPlayer player = new AudioPlayer();
		player.playAudio(fi);
		System.exit(0);
	}
}
