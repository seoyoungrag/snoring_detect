package snoring;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import snoring.scichart.DoubleValues;
import snoring.scichart.Radix2FFT;
import snoring.scichart.ShortValues;

public class EventFireGui20190610 {

	byte[] audioData;
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;

	public EventFireGui20190610(String filePath) {
		InputStream fin;
		try {
			// 오디오 입력 테스트용, 이부분은 나중에 AudioRecord로 녹음하는 부분이 된다. sta
			fin = new FileInputStream(filePath);
			// 오디오 입력 테스트용, 이부분은 나중에 AudioRecord로 녹음하는 부분이 된다. end
			File file = new File(filePath);
			audioData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(audioData); // read file into bytes[]
			fis.close();
			int read = 0;
			InputStream targetStream = new ByteArrayInputStream(audioData);
			byte[] frameBytes = new byte[frameByteSize];
			audioCalculator = new AudioCalculator();
			frameBytes = new byte[frameByteSize];

			SleepCheck.checkTerm = 0;
			SleepCheck.checkTermSecond = 0;
			int osaCnt = 0;
			boolean grindingStart = false;
			boolean grindingContinue = false;
			int grindingRecordingContinueCnt = 0;
			boolean osaStart = false;
			boolean osaContinue = false;
			int osaRecordingExit = 0;
			int osaRecordingContinueCnt = 0;
			double osaStartTimes = 0.0;
			SleepCheck.grindingContinueAmpCnt = 0;
			SleepCheck.grindingContinueAmpOppCnt = 0;
			SleepCheck.grindingRepeatAmpCnt = 0;
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			boolean isRecording = false;
			@SuppressWarnings("unused")
			long recordStartingTIme = 0L;
			snoringTermList = new ArrayList<StartEnd>();
			grindingTermList = new ArrayList<StartEnd>();
			osaTermList = new ArrayList<StartEnd>();
			List<Analysis> ansList = new ArrayList<Analysis>();
			double times = 0.0;
			int i = 0;
		    int num = 0;
		    int[] tmpArray;
			try {
				targetStream = new ByteArrayInputStream(audioData);
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					audioCalculator.setBytes(frameBytes);
					//20~40 fhz의 진폭값중 가장 낮은 값을 가져온다.
					//20~40 fhz의 진폭값을 가져 오려면, 각각 주파수의 진폭 값을 계산해야한다.
					//1024 바이트에서 각 주파수별 진폭값을 가져와야 한다.
					/*
					int size = frameBytes.length;
					int[] shortArray = new int[size];
					for (int index = 0; index < size; index++) {
					    shortArray[index] = (int) frameBytes[index];
					}
					*/
					//1024바이트에서 주파수 리스트를 만든다.
					/*
					double[] freq = new double[1024];
					for (int index = 0; index < 1024; index++) {
						freq[index] = (double)index * 44100d / 1024d;
					}
					*/
					//int[] halfArray = getAmplitudesFromBytes(frameBytes);
					
					short[] halfArray = getAmplitudesFromBytesShort(frameBytes);
					
					//System.out.println(getRealDecibel(halfArray[1])+"v"+getRealDecibel(halfArray[3]));
					int bufferSize = frameBytes.length/2;
					Radix2FFT fft = new Radix2FFT(bufferSize);
				    double hzPerDataPoint = 44100d / bufferSize;
				    int fftSize = (int) ((44100d / 2) / (44100d / bufferSize))	;
				    tmpArray = new int[fftSize];
			        for (int k = 0; k < fftSize; k ++) {
			        	tmpArray[k] = (int) (k * hzPerDataPoint);
			        }
			        DoubleValues fftData = new DoubleValues();
			        ShortValues shortValues = new ShortValues(halfArray);
		            fft.run(shortValues, fftData);
		            fftData.setSize(fftSize);
		            
				    DecimalFormat df = new DecimalFormat("0.00");
				    if(i==0) {
					Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
					System.out.println();
				    }
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					System.out.println(calcTime(times) + "s" );
				    Arrays.stream(fftData.getItemsArray()).forEach(e -> System.out.print(df.format(e) + "\t" ));
				    /*num++;
					Arrays.stream(halfArray).forEach(e -> System.out.print(num + "\t" ));*/
					//Arrays.stream(halfArray).forEach(e -> System.out.print(df.format(getRealDecibel(e)) + "\t" ));
					//Arrays.stream(halfArray).forEach(e -> System.out.print(df.format(getRealDecibel(e)) + "\t" ));
					System.out.println(" ");
					i++;
					
					
					/*
					double[] freq2 = new double[512];
					for (int index = 0; index < 512; index++) {
						freq2[index] = (double)index * 44100d / 512d;
					}
					
					
			        System.out.println("You have read total: " + frameBytes.length + " bytes" );
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
					int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());
					i++;
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					targetStream.close();
					SleepCheck.curTermSecond = (int) Math.floor(times);
					SleepCheck.GrindingCheckTermSecond = times;

					final String amp = String.valueOf(amplitude + "Amp");
					final String db = String.valueOf(decibel + "db");
					final String hz = String.valueOf(frequency + "Hz");
					final String sehz = String.valueOf(sefrequency + "Hz(2th)");
					final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
					SleepCheck.setMaxDB(decibel);
					SleepCheck.setMinDB(decibel);
					//if(frequency<200) {
					
						System.out.print("시간 :\t"+calcTime(times) + "s\t" 
						+ hz + "\t" 
						+ db + "\t" 
						+ amp + "\t" 
						//+ sehz + " " 
						//+ seamp
						);
						*/
						/*
					    DecimalFormat df = new DecimalFormat("0.00");
					    System.out.println(" ");
					    System.out.print("DB:\t");
						Arrays.stream(audioCalculator.getDecibels()).forEach(e -> System.out.print(df.format(e) + "\t" ));
					    System.out.println(" ");
					    System.out.print("AMP:\t");
						Arrays.stream(audioCalculator.getAmplitudes()).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
						System.out.print("FQ:\t");
						Arrays.stream(audioCalculator.getFrequencyAll()).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    System.out.print("RDB:\t");
						Arrays.stream(audioCalculator.getRealDecibels()).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    System.out.print("1024DB:\t");
						Arrays.stream(shortArray).forEach(e -> System.out.print(df.format(getRealDecibel(e)) + "\t" ));
						System.out.println(" ");
					    System.out.print("1024DB-2:\t");
						Arrays.stream(shortArray).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    System.out.print("1024FH:\t");
						Arrays.stream(freq).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    System.out.print("512DB:\t");
						Arrays.stream(halfArray).forEach(e -> System.out.print(df.format(getRealDecibel(e)) + "\t" ));
						System.out.println(" ");
					    System.out.print("512DB-2:\t");
						Arrays.stream(halfArray).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    System.out.print("512FH:\t");
						Arrays.stream(freq2).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
					    
						*/
					//}
					

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} /*
			 * catch (UnsupportedAudioFileException e1) { // TODO Auto-generated catch block
			 * e1.printStackTrace(); }
			 */

	}

    private int[] getAmplitudesFromBytes(byte[] bytes) {
        int[] amps = new int[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short buff = bytes[i + 1];
            short buff2 = bytes[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short res = (short) (buff | buff2);
            amps[i == 0 ? 0 : i / 2] = (int) res;
        }
        return amps;
    }

    private short[] getAmplitudesFromBytesShort(byte[] bytes) {
    	short[] amps = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short buff = bytes[i + 1];
            short buff2 = bytes[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short res = (short) (buff | buff2);
            amps[i == 0 ? 0 : i / 2] = res;
        }
        return amps;
    }
    
    private double getRealDecibel(int amplitude) {
        if (amplitude < 0) amplitude *= -1;
        double amp = (((double) amplitude) / 32767.0d) * 100.0d;
        if (amp == 0.0d) {
            amp = 1.0d;
        }
        double decibel = Math.sqrt(100.0d / amp);
        decibel *= decibel;
        if (decibel > 100.0d) {
            decibel = 100.0d;
        }
        return ((-1.0d * decibel) + 1.0d) / Math.PI;
    }
    private double getRealDecibelDouble(double amplitude) {
        if (amplitude < 0) amplitude *= -1;
        double amp = (((double) amplitude) / 32767.0d) * 100.0d;
        if (amp == 0.0d) {
            amp = 1.0d;
        }
        double decibel = Math.sqrt(100.0d / amp);
        decibel *= decibel;
        if (decibel > 100.0d) {
            decibel = 100.0d;
        }
        return ((-1.0d * decibel) + 1.0d) / Math.PI;
    }
	private String calcTime(double times) {
        int seconds;
        int minutes ;
        int hours;
        seconds =  (int)times;
        hours = seconds / 3600;
        minutes = (seconds%3600)/60;
        double seconds_output = (times% 3600)%60;
        return hours  + ":" + minutes + ":" + seconds_output +""; 
	}

}

