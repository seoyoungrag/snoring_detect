package snoring;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.sun.media.sound.WaveFileWriter;

public class WaveFormatConverter {

	static public void  saveWave(String filePath, byte[] audioData, InputStream fin, FileInputStream fis, File file, Wave wave, WaveHeader waveHeader, String fileName) throws UnsupportedAudioFileException, IOException{
		System.out.println(fileName+ "----------------------save start");
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
		BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
		audioInputStream = new AudioInputStream(bufferedInputStream, audioInputStream.getFormat(), audioInputStream.getFrameLength());
		AudioFormat format = audioInputStream.getFormat();
		WaveFileWriter wfw = new WaveFileWriter();
		AudioFormat monoFormat = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize(), format.getFrameRate(), format.isBigEndian());

		int length = audioData.length;
		ByteArrayInputStream bais = new ByteArrayInputStream(audioData);

		AudioInputStream stereoStream = new AudioInputStream(bais, format, length);
		AudioInputStream monoStream = new AudioInputStream(stereoStream, monoFormat, length);

		String destFilePath = "raw/raw_convert/test-save-"+fileName+"_"+System.currentTimeMillis()+".wav";
		File destFile = new File(destFilePath);
		if(destFile.exists()) {
			destFile.delete();
		}
		wfw.write(monoStream, javax.sound.sampled.AudioFileFormat.Type.WAVE,
				new File(destFilePath));

/*
        ReadWAV2Array audioTest = new ReadWAV2Array(destFilePath, true);
        double[] rawData = audioTest.getByteArray();
        length = rawData.length;

        //initialize parameters for FFT
        int WS = 2048; //WS = window size
        int OF = 8;    //OF = overlap factor

        //calculate FFT parameters
        double SR = audioTest.getSR();
        double time_resolution = WS/SR;
        double frequency_resolution = SR/WS;
        double highest_detectable_frequency = SR/2.0;
        double lowest_detectable_frequency = 5.0*SR/WS;
        System.out.println("SampleRate:                   " + SR + " ");
        System.out.println("time_resolution:              " + time_resolution*1000 + " ms");
        System.out.println("frequency_resolution:         " + frequency_resolution + " Hz");
        System.out.println("highest_detectable_frequency: " + highest_detectable_frequency + " Hz");
        System.out.println("lowest_detectable_frequency:  " + lowest_detectable_frequency + " Hz");

		System.out.println("audio length(s): "+((double)(audioData.length/(44100d*16*1)))*8); 
        */
		System.out.println(fileName+ "----------------------save end");
	}
	static public void  stereoToMono(String filePath, byte[] audioData, InputStream fin, FileInputStream fis, File file, Wave wave, WaveHeader waveHeader) throws UnsupportedAudioFileException, IOException{

		System.out.println("convert start");
		
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
		BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
		audioInputStream = new AudioInputStream(bufferedInputStream, audioInputStream.getFormat(), audioInputStream.getFrameLength());
		AudioFormat format = audioInputStream.getFormat();
		WaveFileWriter wfw = new WaveFileWriter();
		AudioFormat monoFormat = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize(), format.getFrameRate(), format.isBigEndian());

		//byte[] audioData = IOUtils.toByteArray(fin);

		int length = audioData.length;
		ByteArrayInputStream bais = new ByteArrayInputStream(audioData);

		AudioInputStream stereoStream = new AudioInputStream(bais, format, length);
		AudioInputStream monoStream = new AudioInputStream(stereoStream, monoFormat, length);

		String destFilePath = "raw/raw_convert/un-ogged.wav";
		File destFile = new File(destFilePath);
		if(destFile.exists()) {
			destFile.delete();
		}
		wfw.write(monoStream, javax.sound.sampled.AudioFileFormat.Type.WAVE,
				new File(destFilePath));

		fin = new FileInputStream(destFilePath);		
		fis = new FileInputStream(file);
		fis.read(audioData); // read file into bytes[]
		fis.close();	
		wave = new Wave(fin);
		waveHeader = wave.getWaveHeader();
		waveHeader = new WaveHeader();
		//waveHeader.setChannels(1);
		waveHeader.setChannels(waveHeader.getChannels());
		waveHeader.setBitsPerSample(waveHeader.getBitsPerSample());
		waveHeader.setSampleRate(waveHeader.getSampleRate());
		/*
		System.out.println(waveHeader.getChannels());
		System.out.println(waveHeader.getBitsPerSample());
		System.out.println(waveHeader.getSampleRate());
		*/
		/*
		 * byte[] audioData = inputStreamToByteArray(fin); int length =
		 * audioData.length; AudioFormat af = new AudioFormat( Encoding.PCM_SIGNED,
		 * waveHeader.getSampleRate(), waveHeader.getBitsPerSample(),
		 * waveHeader.getChannels(), waveHeader.getChannels()*2,
		 * waveHeader.getBitsPerSample(), false); AudioInputStream monoStream = new
		 * AudioInputStream(fin,af,length/2); WaveFileWriter wfw = new WaveFileWriter();
		 * wfw.write(monoStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, new
		 * File("raw/raw_convert/un-ogged.wav"));
		 */

		System.out.println("convert end");
	}
}
