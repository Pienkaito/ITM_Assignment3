package itm.audio;

/*******************************************************************************
 This file is part of the ITM course 2017
 (c) University of Vienna 2009-2017
 *******************************************************************************/

import itm.model.AudioMedia;
import itm.model.MediaFactory;
import itm.util.IOUtil;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads audio files of various formats and stores some basic audio
 * metadata to text files. It can be called with 3 parameters, an input
 * filename/directory, an output directory and an "overwrite" flag. It will read
 * the input audio file(s), retrieve some metadata and write it to a text file
 * in the output directory. The overwrite flag indicates whether the resulting
 * output file should be overwritten or not.
 * <p>
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */
public class AudioMetadataGenerator {
	public class Duration {
		public int minutes = 0;
		public int seconds = 0;

		public Duration(int minutes, int seconds) {
			this.minutes = minutes;
			this.seconds = seconds;
		}

		public Duration(int seconds) {
			this.minutes = seconds / 60;
			this.seconds = seconds - (minutes * 60);
		}

		public Duration(double seconds) {
			this.minutes = (int) seconds / 60;
			this.seconds = (int) seconds - (minutes * 60);
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

	/**
	 * Constructor.
	 */
	public AudioMetadataGenerator() {
	}

	/**
	 * Processes an audio file directory in a batch process.
	 *
	 * @param input     a reference to the audio file directory
	 * @param output    a reference to the output directory
	 * @param overwrite indicates whether existing metadata files should be
	 *                  overwritten or not
	 * @return a list of the created media objects (images)
	 */
	public ArrayList<AudioMedia> batchProcessAudio(File input, File output,
												   boolean overwrite) throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<AudioMedia> ret = new ArrayList<AudioMedia>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {

				String ext = f.getName().substring(
						f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
					try {
						AudioMedia result = processAudio(f, output, overwrite);
						System.out.println("created metadata for file " + f
								+ " in " + output);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error when creating metadata from file " + input + " : " + e0.toString());
					}

				}

			}
		} else {

			String ext = input.getName().substring(
					input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
				try {
					AudioMedia result = processAudio(input, output, overwrite);
					System.out.println("created metadata for file " + input
							+ " in " + output);
					ret.add(result);
				} catch (Exception e0) {
					System.err
							.println("Error when creating metadata from file "
									+ input + " : " + e0.toString());
				}

			}

		}
		return ret;
	}

	/**
	 * Processes the passed input audio file and stores the extracted metadata
	 * to a textfile in the output directory.
	 *
	 * @param input     a reference to the input audio file
	 * @param output    a reference to the output directory
	 * @param overwrite indicates whether existing metadata files should be
	 *                  overwritten or not
	 * @return the created image media object
	 */
	protected AudioMedia processAudio(File input, File output, boolean overwrite)
			throws IOException, IllegalArgumentException, UnsupportedAudioFileException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		// create outputfilename and check whether thumb already exists. All
		// image metadata files have to start with "aud_" - this is used by the
		// mediafactory!
		File outputFile = new File(output, "aud_" + input.getName() + ".txt");
		if (outputFile.exists())
			if (!overwrite) {
				// load from file
				AudioMedia media = new AudioMedia();
				media.readFromFile(outputFile);
				return media;
			}

		AudioMedia media = (AudioMedia) MediaFactory.createMedia(input);

		AudioInputStream originalStream = null;
		AudioFormat baseFormat = null;
		AudioFileFormat fileFormat = null;
		Map<String, Object> allProperties = null;
		HashMap<String, String> properties = new HashMap<String, String>();

		originalStream = AudioSystem.getAudioInputStream(input);
		fileFormat = AudioSystem.getAudioFileFormat(input);
		baseFormat = originalStream.getFormat();

		allProperties = fileFormat.properties();
		byte fileContent[] = null;
		byte footerContent[] = null;

		if (fileFormat.getType().toString().toLowerCase().equals("mp3") ||
				fileFormat.getType().toString().toLowerCase().equals("ogg")) {
			if (fileFormat.getType().toString().toLowerCase().equals("mp3")) {
				media.setComposer(allProperties.get("mp3.id3tag.composer").toString());
				media.setTrack(allProperties.get("mp3.id3tag.track").toString());
				media.setGenre(allProperties.get("mp3.id3tag.genre").toString());
			} else {
				media.setComposer(allProperties.get("ogg.comment.composer").toString());
				media.setTrack(allProperties.get("ogg.comment.track").toString());
				media.setGenre(allProperties.get("ogg.comment.genre").toString());
			}

			media.setTitle(allProperties.get("title").toString());
			media.setAuthor(allProperties.get("author").toString());
			media.setAlbum(allProperties.get("album").toString());
			media.setDate(allProperties.get("date").toString());
			media.setComment(allProperties.get("comment").toString());

			media.setEncoding(baseFormat.getEncoding().toString());
			media.setChannels(Integer.parseInt(String.valueOf(baseFormat.getChannels())));
			media.setBitrate(Integer.parseInt(allProperties.get("mp3.bitrate.nominal.bps").toString()));
			media.setFrequency(Float.parseFloat(String.valueOf(baseFormat.getFrameRate())));

			int milisecs = (int) (Long.parseLong(allProperties.get("duration").toString()) / 1000);
			int sec = (milisecs / 1000) % 60;
			int min = (milisecs / 1000) / 60;
			Duration duration = new Duration(min, sec);
			media.setDuration(duration.getStringArray());
		} else {
			FileInputStream fin = new FileInputStream(input);
			fileContent = new byte[(int) input.length()];
			fin.read(fileContent);

			int footerstart = (int) (originalStream.getFrameLength() * baseFormat.getFrameSize() + 46);
			footerContent = new byte[fileContent.length - footerstart];

			String footertext = "";
			String specialChars = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";

			int i = footerstart;
			for (; i < fileContent.length; i++) {
				int charid = (fileContent[i] & 0xFF);
				if (charid == 2)
					break;
				String symbol = "" + (char) charid;
				footertext += symbol;

				if (footertext.contains("INFO")) {
					i++;
					footertext = null;
					break;
				}
			}

			if (footertext == null) {
				footertext = "";
				for (; i < fileContent.length; i++) {
					int charid = (fileContent[i] & 0xFF);
					if (charid == 2)
						break;
					String symbol = "" + (char) charid;
					footertext += symbol;

					int[] currentindex = {i};
					if (checkPropertiesWAV(footertext, currentindex, fileContent, media)) {
						footertext = "";
					}
					i = currentindex[0];
				}
			} else {
				throw new UnsupportedAudioFileException();
			}

			media.setEncoding(baseFormat.getEncoding().toString());
			media.setChannels(Integer.parseInt(String.valueOf(baseFormat.getChannels())));
			media.setBitrate((int) (baseFormat.getSampleRate() * baseFormat.getSampleSizeInBits() * baseFormat.getChannels() * 0.125));
			media.setFrequency(Float.parseFloat(String.valueOf(baseFormat.getFrameRate())));

			Duration duration = new Duration(originalStream.getFrameLength() / baseFormat.getFrameRate());
			media.setDuration(duration.getStringArray());
		}
		media.addTag("audio");

		String name = input.getName();
		media.addTag(name.substring(name.lastIndexOf(".") + 1));
		originalStream.close();
		IOUtil.writeFile(media.serializeObject(), outputFile);


		return media;
	}

	/**
	 * source: http://www.robotplanet.dk/audio/wav_meta_data/
	 *
	 * @param footertext
	 * @param i
	 * @param fileContent
	 * @return
	 */
	private boolean checkPropertiesWAV(String footertext, int[] i, byte[] fileContent, AudioMedia media) {
		int len = 0;
		boolean foundprops = true;

		String[] cases = {"IPRD", "IART", "IGNR", "GENR", "INAM", "ITRK", "ICRD"};

		int type;
		for (type = 0; type < cases.length; type++)
			if (footertext.contains(cases[type])) break;

		switch (type) {
			case 0:
				//Producer
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}
				media.setAlbum(footertext);
				break;
			case 1:
				//Artist
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}

				media.setAuthor(footertext);
				break;
			case 2:
			case 3:
				//Genre
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}

				media.setGenre(footertext);
				break;
			case 4:
				//Title
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}
				media.setTitle(footertext);
				break;
			case 5:
				//Track
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}
				media.setTrack(footertext);
				break;
			case 6:
				//Date
				i[0]++;
				footertext = "";
				for (int j = i[0]; i[0] < j + 4; i[0]++) {
					len += fileContent[i[0]] & 0xFF;
				}
				for (int j = i[0]; i[0] <= j + len; i[0]++) {
					if ((fileContent[i[0]] & 0xFF) != 0)
						footertext += "" + (char) (fileContent[i[0]] & 0xFF);
					else {
						break;
					}
				}
				media.setDate(footertext);
				break;
			default:
				foundprops = false;
				break;
		}
		return foundprops;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {
		//args = new String[] { "./media/audio", "./media/md" };

		if (args.length < 2) {
			System.out
					.println("usage: java itm.image.AudioMetadataGenerator <input-image> <output-directory>");
			System.out
					.println("usage: java itm.image.AudioMetadataGenerator <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		AudioMetadataGenerator audioMd = new AudioMetadataGenerator();
		audioMd.batchProcessAudio(fi, fo, true);
	}
}
