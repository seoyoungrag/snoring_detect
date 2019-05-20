package snoring;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.sun.media.sound.WaveFileWriter;

public class EventFireGui {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private SnoringApi snoringApi;
	private WaveHeader waveHeader;
	byte[] audioData;
	int frameByteSize = 1024; 
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	private LinkedList<Boolean> snoringResultList = new LinkedList<Boolean>();
	private int snoringCheckLength = 3;
	private int snoringPassScore = 3;
	private int failCnt = 0;
	private int successCnt = 0;

    private AudioCalculator audioCalculator;
	private void initBuffer() {
		AlarmStaticVariables.isSpecificSoundCnt = 0;
		snoringResultList.clear();
		
		// init the first frames
		for (int i = 0; i < snoringCheckLength; i++) {
			snoringResultList.add(false);
		}
		// end init the first frames
	}
	
	public EventFireGui(String filePath) {
		AlarmStaticVariables.snoringCount = 0;

		InputStream fin;
		try {
			fin = new FileInputStream(filePath); 		
			Wave wave = new Wave(fin);
			waveHeader = wave.getWaveHeader();
			File file = new File(filePath);
			audioData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(audioData); // read file into bytes[]
			fis.close();
			
			//wave가 stereo가 아닌 경우 mono로 변경
			if (waveHeader.getChannels() != 1) {
				WaveFormatConverter.stereoToMono(filePath, audioData, fin, fis, file, wave, waveHeader);
			}
			snoringApi = new SnoringApi(waveHeader);
			int read = 0;
			InputStream targetStream = new ByteArrayInputStream(audioData);
			byte[] frameBytes = new byte[frameByteSize];
			try {
				//오디오 파일 요약 정보 표시
	            //get raw double array containing .WAV data
	            ReadWAV2Array audioTest = new ReadWAV2Array(filePath, true);
	            double[] rawData = audioTest.getByteArray();
	            int length = rawData.length;

	            //initialize parameters for FFT
	            int WS = 2048; //WS = window size
	            int OF = 8;    //OF = overlap factor
	            int windowStep = WS/OF;

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

	            audioCalculator = new AudioCalculator();
				frameBytes = new byte[snoringApi.frameByteSize];
				
				SleepCheck.checkTerm = 0;
				SleepCheck.checkTermSecond = 0;
				int grindCnt = 0;
				int snoreCnt = 0;
				int osaCnt = 0;
				boolean grindingStart = false;
				boolean grindingContinue = false;
				int grindingRecordingContinueCnt = 0;
				boolean osaStart = false;
				boolean osaContinue = false;
				int osaRecordingExit = 0;
				int osaRecordingContinueCnt = 0;
				double osaStartTimes = 0.0;
				boolean snoringStart = false;
				boolean snoringContinue = false;
				int snoringRecordingExit = 0;
				int snoringRecordingContinueCnt = 0;
				double snoreStartTimes = 0.0;
				SleepCheck.grindingContinueAmpCnt = 0;
				SleepCheck.grindingContinueAmpOppCnt = 0;
				SleepCheck.grindingRepeatAmpCnt = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    try {
		            int count = 0;
					targetStream = new ByteArrayInputStream(audioData);
					initBuffer();
					int i = 1;
					while( ( read = targetStream.read( frameBytes ) ) > 0 ){
						if(frameBytes == null) {
							frameBytes = new byte[snoringApi.frameByteSize];
						}
						baos.write(frameBytes);
			            audioCalculator.setBytes(frameBytes);
			            int amplitude = audioCalculator.getAmplitude();
			            double decibel = audioCalculator.getDecibel();
			            double frequency = audioCalculator.getFrequency();
			            double sefrequency = audioCalculator.getFrequencySecondMax();
			            int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());
			            //double frequency2Th = audioCalculator.getFrequency2Th();

			            final String amp = String.valueOf(amplitude + "Amp");
			            final String db = String.valueOf(decibel + "db");
			            final String hz = String.valueOf(frequency + "Hz");
			            final String sehz = String.valueOf(sefrequency + "Hz(2th)");
			            final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
			            //System.out.println(amp);
			            //System.out.println(db);
			            double times = (((double)(frameBytes.length/(44100d*16*1)))*8)*i;
						SleepCheck.curTermSecond = (int) Math.floor(times);

						//무호흡인지 먼저 체크한 후, 이갈이 체크, 그 다음 코골이 체크.
						//무호흡인 장시간 호흡이 없는 경우를 체크한다. 가장 체크범위가 넓다.
						//이갈이는 3초간의 이갈이 현상이 있는지 체크한다. 코골이보다는 범위가 넓다. 
						//코골이는 1.5초 내에 코골이 주파수가 발생하는지 체크한다. 가장 범위가 적다.
			            //무호흡 체크
						/*
		            	osaCnt = SleepCheck.OSACheck(times, decibel, sefamplitude, frequency, sefrequency);
	            		osaRecordingContinueCnt += osaCnt;
		            	//무호흡 카운트가 발생하고, 녹음 상태가 아니면 녹음을 시작한다.
	            		if(osaRecordingExit>0) {
	            			osaRecordingExit--;
	            		}
		            	if(osaCnt>0 && osaStart == false) {
		            		System.out.print("무호흡 녹음을 시작한다.");
		        			System.out.println(String.format("%.2f", times)+"s~"+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSATermCnt);
		        			baos = new ByteArrayOutputStream();
		        			osaStart = true;
		        			osaContinue = false;
		        			osaRecordingExit = 0;
		        			osaStartTimes = times;
		            	}else if(times-osaStartTimes<5 && osaStart == true) {
		            		//무호흡 녹음 중 5초 이내에 호흡이 발생하면, 무호흡이 아닌 것으로 본다.
		            		if(osaRecordingContinueCnt<5) {
		            			System.out.print("무호흡 녹음을 취소한다. "+osaRecordingContinueCnt+", ");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+0.01)+"s "+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSATermCnt);
			        			baos = new ByteArrayOutputStream();
			        			osaStart = false;
			        			osaRecordingContinueCnt = 0;
		            		}else {
		            			if(((double)(audioData.length/(44100d*16*1)))*8 < times+1) {
			        				System.out.print("무호흡 녹음을 중단한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        				byte[] c = baos.toByteArray();
				        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "OSA");
				        			osaStart = false;
				        			osaRecordingContinueCnt = 0;
			        			}
		            			osaContinue = true;
			            		System.out.print("무호흡 녹음을 계속한다.1 ");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+0.01)+"s "+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSATermCnt);
		            		}
		        		//무호흡 녹음 중 5초 이 후에 호흡이 발생하면, 숨쉬는 구간까지 녹음한다.
		            	}else if(times-osaStartTimes>5 && osaStart == true) {
		            		if(SleepCheck.isBreathTerm==true) { //숨쉬는 구간이 되었으면, 계속 녹음 플래그를 업데이트
		            			if(((double)(audioData.length/(44100d*16*1)))*8 < times+1) {
			        				System.out.print("무호흡 녹음을 중단한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        				byte[] c = baos.toByteArray();
				        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "OSA");
				        			osaStart = false;
				        			osaRecordingContinueCnt = 0;
			        			}
		            			osaContinue = true;
			            		System.out.print("무호흡 녹음을 계속한다.2 ");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+0.01)+"s "+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSATermCnt);
		            		}else {
		            			if(osaContinue==true && osaRecordingExit == 1) {
		            				System.out.print("무호흡 녹음을 중단한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+0.01)+"s "+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSATermCnt);
				        			byte[] c = baos.toByteArray();
				        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "OSA");
				        			osaStart = false;
				        			osaRecordingContinueCnt = 0;
		            			}
		            			if(osaCnt > 0) {
		            				osaRecordingExit = 1000;
		            			}
	            				osaCnt = 0;
		            		}
	            		}else {
	            			//System.out.println(times-osaStartTimes+" "+osaStart);
	            			//System.out.println(times+" "+osaStartTimes+" "+osaStart+ " "+osaCnt);
	            		}
		            	
		            	if(osaStart == false) {
				            //이갈이 체크
			            	SleepCheck.grindingCheck(times, decibel, sefamplitude, frequency, sefrequency);
			            	//이갈이 신호가 발생하고, 녹음 상태가 아니면 녹음을 시작한다.
			        		if(SleepCheck.grindingRepeatAmpCnt==1 && grindingStart == false) {
			        			System.out.print("이갈이 녹음을 시작한다.");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        			baos = new ByteArrayOutputStream();
			        			grindingStart = true;
			        			grindingContinue = false;
		        			//이갈이 녹음 중에 1초간격으로 유효 카운트가 연속적으로 발생했으면 계속 녹음한다.
			        		}else if(SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1 && SleepCheck.grindingRepeatAmpCnt>=3 && grindingStart == true) {
			        			if(((double)(audioData.length/(44100d*16*1)))*8 < times+1) {
			        				System.out.print("이갈이 녹음을 중단한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        				byte[] c = baos.toByteArray();
				        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "grinding");
				        			grindingStart = false;
				        			grindingContinue = false;
				        			grindingRecordingContinueCnt = 0;
			        			}
			        			System.out.print("이갈이 녹음을 계속한다.");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        			grindingRecordingContinueCnt = 0;
			        			grindingContinue = true;
		        			//이갈이 녹음 중에 1초간격으로 유효 카운트가 연속적으로 발생하지 않으면 녹음을 취소한다.
			        		}else if(SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1 && SleepCheck.grindingRepeatAmpCnt==0 && grindingStart==true && grindingContinue == false) {
			        			//1초 단위 발생하는 이갈이도 잡기위해 유예 카운트를 넣는다. 1초만 한번더 체크함.
			        			if(grindingRecordingContinueCnt >= SleepCheck.GRINDING_RECORDING_CONTINUE_CNT) {
				        			System.out.print("이갈이 녹음을 취소한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
				        			baos = new ByteArrayOutputStream();
				        			grindingStart = false;
				        			grindingRecordingContinueCnt = 0;
			        			}else {
			        				System.out.print("이갈이 녹음을 취소하지 않고 진행한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        				grindingRecordingContinueCnt++;
			        			}
		        			//이갈이 녹음 중에 1초간격으로 유효카운트가 더이상 발생하지 않으나 이전에 발생했더라면 기존의 녹음을 저장한다.
			        		}else if(SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1 && SleepCheck.grindingRepeatAmpCnt==0 && grindingContinue == true) {
			        			System.out.print("이갈이 녹음을 중단한다.");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
		        				byte[] c = baos.toByteArray();
			        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "grinding");
			        			grindingStart = false;
			        			grindingContinue = false;
			        			grindingRecordingContinueCnt = 0;
			        		}else if(SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1) {
			        			if(grindingStart) {
			        				System.out.println(String.format("%.2f", times)+"s 이갈이 녹음 중 "+grindingStart+" "+grindingContinue+" "+grindingRecordingContinueCnt);
			        			}
			        		}
		            	}
		            	*/
			            //코골이 체크
		            	if(osaStart == false && grindingStart == false) {
		            		SleepCheck.snoringCheck(decibel, frequency, sefrequency);
		            		//코골이 신호가 발생하고 3초 동안 동일하게 반복되면 저장한다.
		            		if(snoreStartTimes == 0.0) {
			            		snoreStartTimes = times;
			            		SleepCheck.snoringContinue = 0;
			            		SleepCheck.snoringContinueOpp = 0;
			            		snoringRecordingExit = 0;
		            		}

		            		System.out.println(String.format("%.3f", times)+" "+frequency+" "+sefrequency+" "+SleepCheck.snoringContinue+" "+SleepCheck.snoringContinueOpp);
		            		//시간이 3초 간격으로 주파수 단위를 체크해서 시작한다.
		            		if(times - snoreStartTimes >=3 && SleepCheck.snoringContinue >= 1 && SleepCheck.snoringContinueOpp <= 240 && snoringStart==false) {
		            			System.out.print("코골이 녹음을 시작한다.");
			        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
			        			baos = new ByteArrayOutputStream();
			        			snoringStart = true;
			        			snoringContinue = false;
			            		snoreStartTimes = times;
			            		snoringRecordingExit = 500;
		            		//5초가 지나는 동안 주파수가 발생하지 않을 수도 있어서, 한번 유예할 수 있도록 한다.
		            		}else if(times - snoreStartTimes >=3 && !(SleepCheck.snoringContinue >= 60 && SleepCheck.snoringContinueOpp <= 240) && snoringStart==true) {
		            			if(snoringRecordingExit==1) {
		            				System.out.print("코골이 녹음을 중단한다.");
				        			System.out.println(String.format("%.2f", times)+"~"+String.format("%.2f", times+1)+"s "+SleepCheck.grindingContinueAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt+" "+SleepCheck.grindingRepeatAmpCnt);
				        			byte[] c = baos.toByteArray();
				        			WaveFormatConverter.saveWave(filePath, c, fin, fis, file, wave, waveHeader, "snoring");
				        			snoringStart = false;
				        			snoringRecordingExit = 0;
		            			}else {
		            				snoringRecordingExit--;
		            			}
		            		//5초가 지나는 동안 주파수가 발생한 경우, 유예를 해제한다.
		            		}else if(times - snoreStartTimes >=3 && SleepCheck.snoringContinue >= 60 && SleepCheck.snoringContinueOpp <= 240 && snoringStart==true) {
		            			snoringRecordingExit = 500;
		            		}
		            		/*
		            		if(snoringRecordingExit>0) {
				            	snoringRecordingExit--;
		            		}
		            		*/
		            	}
		        		SleepCheck.curTermTime = times;
		        		SleepCheck.curTermDb = decibel;
		        		SleepCheck.curTermAmp = amplitude;
		        		SleepCheck.curTermHz = frequency;
		        		SleepCheck.curTermSecondHz = sefrequency;
		        		
			            if(grindingStart) {
			            	//System.out.println(String.format("%.2f", times) +"s "+ "이갈이: "+grindingStart);
			            }else if(snoreCnt > 0){
			            	//System.out.println(String.format("%.2f", times) +"s "+ "코골이: "+snoreCnt);
			            }else if(osaCnt > 0){
			            	//System.out.println(String.format("%.2f", times) +"s "+ "무호흡: "+osaCnt);
			            }
			            if(grindCnt > 0 || snoreCnt > 0 || osaCnt > 0) {
			            	SleepCheck.checkTerm = 0;
			            }
			            //count += SleepCheck.snoringCheck(frequency, sefrequency);
			            targetStream.close();
			            SleepCheck.checkTerm++;
						SleepCheck.checkTermSecond = (int) Math.floor(times);
				        i++;

					}
					System.out.println("audio length(s): "+((double)(audioData.length/(44100d*16*1)))*8); 
					System.out.println("(frequency>=150 && frequency<=250 && sefrequency>=950 &&sefrequency<1050) CNT: "+count);
					System.out.println("AlarmStaticVariables.snoringCount: "+AlarmStaticVariables.snoringCount);
					
			    } catch (IOException e) {
					e.printStackTrace();
				}
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// setVisible(true);
	}

	public static byte[] inputStreamToByteArray(InputStream is) {

		byte[] resBytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int read = -1;
		try {
			while ((read = is.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}

			resBytes = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resBytes;
	}

	public byte[] getFrameBytes(byte[] buffer) {

		// analyze sound
		int totalAbsValue = 0;
		short sample = 0;
		short[] tmp = new short[frameByteSize];
		// float averageAbsValue = 0.0f;
		AlarmStaticVariables.absValue = 0.0f;

		for (int i = 0; i < frameByteSize; i += 2) {
			sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
			tmp[i] = sample;
			totalAbsValue += Math.abs(sample);
		}
		AlarmStaticVariables.absValue = totalAbsValue / frameByteSize / 2;
/*
		Message msg = new Message();
		msg.obj = AlarmStaticVariables.absValue;
		showhandler.sendMessage(msg);
*/
		for (int i = 0; i < buffer.length; i++) {
			totalBuf[cnt++] = buffer[i];
		}

		// ----------save into buf----------------------
		short[] tmpBuf = new short[buffer.length
				/ AlarmStaticVariables.rateX];
		for (int i = 0, ii = 0; i < tmpBuf.length; i++, ii = i
				* AlarmStaticVariables.rateX) {
			tmpBuf[i] = tmp[ii];
		}
		synchronized (AlarmStaticVariables.inBuf) {//
			AlarmStaticVariables.inBuf.add(tmpBuf);// add data
		}
		// ----------save into buf----------------------

		//System.out.println(cnt + " vs " + AlarmStaticVariables.sampleSize);
		if (cnt > AlarmStaticVariables.sampleSize) {
			cnt = 0;
			return totalBuf;
		} else
			return null;
		// return buffer;
	}
}
