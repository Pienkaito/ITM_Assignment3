package itm.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * This class creates acoustic thumbnails from various types of audio files. It
 * can be called with 3 parameters, an input filename/directory and an output
 * directory, and the desired length of the thumbnail in seconds. It will read
 * MP3 or OGG encoded input audio files(s), cut the contained audio data to a
 * given length (in seconds) and saves the acoustic thumbnails to a certain
 * length.
 * <p>
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */

public class AudioThumbGenerator {

	private int thumbNailLength = 10; // 10 sec default
	private final static int sampleSizeInBits = 16;

	/**
	 * Constructor.
	 */
	public AudioThumbGenerator(int thumbNailLength) {
		this.thumbNailLength = thumbNailLength;
	}

	/**
	 * Processes the passed input audio file / audio file directory and stores
	 * the processed files to the output directory.
	 *
	 * @param input  a reference to the input audio file / input directory
	 * @param output a reference to the output directory
	 */
	public ArrayList<File> batchProcessAudioFiles(File input, File output) throws IOException {
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
				if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
					try {
						File result = processAudio(f, output);
						System.out.println("converted " + f + " to " + result);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error converting " + f + " : " + e0.toString());
					}

				}

			}
		} else {
			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
				try {
					File result = processAudio(input, output);
					System.out.println("converted " + input + " to " + result);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error converting " + input + " : " + e0.toString());
				}

			}

		}
		return ret;
	}

	/**
	 * Processes the passed audio file and stores the processed file to the
	 * output directory.
	 *
	 * @param input  a reference to the input audio File
	 * @param output a reference to the output directory
	 */
	protected File processAudio(File input, File output) throws IOException, IllegalArgumentException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		File outputFile = new File(output, input.getName() + ".wav");

		ByteArrayInputStream finaldataStream = null;
		ByteArrayOutputStream rawdataStream = null;
		AudioInputStream oldreadStream = null;
		AudioInputStream convertedStream = null;

		try {
			oldreadStream = AudioSystem.getAudioInputStream(input);
			AudioFormat baseFormat = oldreadStream.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
					sampleSizeInBits, baseFormat.getChannels(), (sampleSizeInBits / 8) * baseFormat.getChannels(),
					baseFormat.getSampleRate(), false);
			convertedStream = AudioSystem.getAudioInputStream(decodedFormat, oldreadStream);

			rawdataStream = new ByteArrayOutputStream();

			int bufferSize = (int) decodedFormat.getFrameRate() * decodedFormat.getFrameSize();
			byte[] buffer = new byte[bufferSize];
			int readbytes = 0;
			int totalbytes = 0;

			while (readbytes != -1) {
				readbytes = convertedStream.read(buffer, 0, buffer.length);
				if (readbytes > 0) {
					totalbytes += readbytes;
				}
				if (totalbytes > bufferSize * thumbNailLength) {
					totalbytes -= readbytes;
					break;
				} else {
					rawdataStream.write(buffer, 0, readbytes);
				}
			}

			totalbytes -= 8;

			byte[] header = generateHeader(totalbytes, (long) decodedFormat.getSampleRate(), decodedFormat.getChannels());
			finaldataStream = new ByteArrayInputStream(combinebytes(header, rawdataStream.toByteArray()));

			AudioInputStream newreadstream = AudioSystem.getAudioInputStream(finaldataStream);
			AudioSystem.write(newreadstream, AudioFileFormat.Type.WAVE, outputFile);

		} catch (Exception ex) {
			return null;
		}

		return outputFile;
	}

	/**
	 * Combines to byte arrays into one
	 *
	 * @param x first half of the new byte array
	 * @param y second half of the new byte array
	 * @return Combined byte array
	 */
	private byte[] combinebytes(byte[] x, byte[] y) {
		byte[] combined = new byte[x.length + y.length];
		for (int i = 0; i < combined.length; ++i) {
			combined[i] = i < x.length ? x[i] : y[i - x.length];
		}
		return combined;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {
		//args = new String[]{"./media/audio", "./test", "10"};

		if (args.length < 3) {
			System.out.println("usage: java itm.audio.AudioThumbGenerator <input-audioFile> <output-directory> <length>");
			System.out.println("usage: java itm.audio.AudioThumbGenerator <input-directory> <output-directory> <length>");
			System.exit(1);
		}

		File fi = new File(args[0]);
		File fo = new File(args[1]);
		Integer length = new Integer(args[2]);

		AudioThumbGenerator audioThumb = new AudioThumbGenerator(length);
		audioThumb.batchProcessAudioFiles(fi, fo);
	}

	/**
	 * Generates the RIFF header which is important for WAV files
	 * Source: http://soundfile.sapp.org/doc/WaveFormat/
	 *
	 * @param audiobytes File size of the raw audio file
	 * @param sample     Sample rate of the audio
	 * @param channels   Amount of channels of audio
	 * @return complete RIFF header
	 */
	private byte[] generateHeader(long audiobytes, long sample, int channels) {
		byte[] header = new byte[44];
		long filesize = audiobytes + 36;
		long byteRate = sample * 2 * channels;

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (filesize & 0xff);
		header[5] = (byte) ((filesize >> 8) & 0xff);
		header[6] = (byte) ((filesize >> 16) & 0xff);
		header[7] = (byte) ((filesize >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (sample & 0xff);
		header[25] = (byte) ((sample >> 8) & 0xff);
		header[26] = (byte) ((sample >> 16) & 0xff);
		header[27] = (byte) ((sample >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) ((sampleSizeInBits / 8) * channels); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (audiobytes & 0xff);
		header[41] = (byte) ((audiobytes >> 8) & 0xff);
		header[42] = (byte) ((audiobytes >> 16) & 0xff);
		header[43] = (byte) ((audiobytes >> 24) & 0xff);

		return header;
	}
}
