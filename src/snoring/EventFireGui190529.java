package snoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

public class EventFireGui190529 {

	private WaveHeader waveHeader;
	byte[] audioData;
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;

	public EventFireGui190529(String filePath) {
		InputStream fin;
		try {
			// ����� �Է� �׽�Ʈ��, �̺κ��� ���߿� AudioRecord�� �����ϴ� �κ��� �ȴ�. sta
			fin = new FileInputStream(filePath);
			// ����� �Է� �׽�Ʈ��, �̺κ��� ���߿� AudioRecord�� �����ϴ� �κ��� �ȴ�. end
			Wave wave = new Wave(fin);
			waveHeader = wave.getWaveHeader();
			File file = new File(filePath);
			audioData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(audioData); // read file into bytes[]
			fis.close();
			// wave�� stereo�� �ƴ� ��� mono�� ����
			if (waveHeader.getChannels() != 1) {
				WaveFormatConverter.stereoToMono(filePath, audioData, fin, fis, file, wave, waveHeader);
			}
			int read = 0;
			InputStream targetStream = new ByteArrayInputStream(audioData);
			byte[] frameBytes = new byte[frameByteSize];
			try {
				// initialize parameters for FFT
				int WS = 2048; // WS = window size

				// ����� ���� ��� ���� ǥ��
				// get raw double array containing .WAV data
				ReadWAV2Array audioTest = new ReadWAV2Array(filePath, true);

				// calculate FFT parameters
				double SR = audioTest.getSR();
				double time_resolution = WS / SR;
				double frequency_resolution = SR / WS;
				double highest_detectable_frequency = SR / 2.0;
				double lowest_detectable_frequency = 5.0 * SR / WS;

				System.out.println("SampleRate:                   " + SR + " ");
				System.out.println("time_resolution:              " + time_resolution * 1000 + " ms");
				System.out.println("frequency_resolution:         " + frequency_resolution + " Hz");
				System.out.println("highest_detectable_frequency: " + highest_detectable_frequency + " Hz");
				System.out.println("lowest_detectable_frequency:  " + lowest_detectable_frequency + " Hz");

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
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				boolean isRecording = false;
				@SuppressWarnings("unused")
				long recordStartingTIme = 0L;
				snoringTermList = new ArrayList<StartEnd>();
				grindingTermList = new ArrayList<StartEnd>();
				osaTermList = new ArrayList<StartEnd>();
				List<Analysis> ansList = new ArrayList<Analysis>();
				try {
					targetStream = new ByteArrayInputStream(audioData);
					int i = 1;
					while ((read = targetStream.read(frameBytes)) > 0) {
						if (frameBytes == null) {
							frameBytes = new byte[frameByteSize];
						}
						audioCalculator.setBytes(frameBytes);
						// �Ҹ��� �߻��ϸ� ������ �����ϰ�, 1���̻� �Ҹ��� �߻����� ������ ������ ���� �ʴ´�.
						int amplitude = audioCalculator.getAmplitude();
						double decibel = audioCalculator.getDecibel();
						double frequency = audioCalculator.getFrequency();
						double sefrequency = audioCalculator.getFrequencySecondMax();
						int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());

						double times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
						i++;
						targetStream.close();
						SleepCheck.curTermSecond = (int) Math.floor(times);
						
			            final String amp = String.valueOf(amplitude + "Amp");
			            final String db = String.valueOf(decibel + "db");
			            final String hz = String.valueOf(frequency + "Hz");
			            final String sehz = String.valueOf(sefrequency + "Hz(2th)");
			            final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
						
						// �Ҹ��� �߻��� Ư�� db �̻������Ѵ�. ���ú��� -31.5~0 ���� ��ġȭ �ϰ� ����.
						// -10db�� �Ȱɸ� ���� �����ϱ�, ���� ���� ������ ��� ���ú����� ���������� �����ϸ鼭 ��� ���ú����� ���� �Ҹ��� �߻��ߴ��� üũ
						// �Ѵ�.
						// ��� ���ú� üũ�� 3�� �����Ѵ�.
						if (decibel > SleepCheck.NOISE_DB_INIT_VALUE && isRecording == false
								&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times) //���� ���� �׽�Ʈ��
								) {
							System.out.print("���� ����! ");
							System.out.println(String.format("%.2f", times)+"s~");
							recordStartingTIme = System.currentTimeMillis();
							baos = new ByteArrayOutputStream();
							baos.write(frameBytes);
							isRecording = true;
						} else if (isRecording == true && SleepCheck.noiseCheck(decibel)==0) {
							System.out.print("���� ����! ");
							System.out.println(String.format("%.2f", times)+"s ");
							baos = new ByteArrayOutputStream();
							baos.write(frameBytes);
							SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
							String fileName = dayTime.format(new Date(recordStartingTIme));
							dayTime = new SimpleDateFormat("dd_hhmm");
							long time = System.currentTimeMillis();
							fileName += "-" + dayTime.format(new Date(time));
							byte[] waveData = baos.toByteArray();
							//TODO ������ ������ ����Ǵ� ����
							filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
							System.out.println("=====������ �м� ����, �м����� ����=====");
							System.out.println("�������� ����(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
							Analysis ans = new Analysis();
							ans.setAnalysisStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(recordStartingTIme), ZoneId.systemDefault()));
							ans.setAnalysisEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
							ans.setAnalysisFileAppPath("raw/raw_convert/");
							ans.setAnalysisFileNm("event-"+fileName+"_"+System.currentTimeMillis()+".wav");
							List<AnalysisDetails> ansDList = new ArrayList<AnalysisDetails>();
							AnalysisDetails ansd = new AnalysisDetails();
							for(StartEnd se : snoringTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200101);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							for(StartEnd se : grindingTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200102);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							for(StartEnd se : osaTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200103);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							ans.setAnalysisDetailsList(ansDList);
							ansList.add(ans);
							/*
							System.out.println("analysisStartDt: "+dayTimeT.format(new Date(recordStartingTIme)));
							System.out.println("analysisEndDt: "+dayTimeT.format(new Date(time)));
							System.out.println("analysisFileNm: "+"event-"+fileName+"_"+System.currentTimeMillis()+".wav");
							System.out.println("analysisFileAppPath: raw/raw_convert/");
							System.out.println("analysisDetailsList ����, ����Ʈ, ����: "+snoringTermList.size()+ grindingTermList.size()+osaTermList.size());
							for(StartEnd se : snoringTermList) {
								System.out.println(se.getTermForRequest(200101, recordStartingTIme));
							}
							for(StartEnd se : grindingTermList) {
								System.out.println(se.getTermForRequest(200102, recordStartingTIme));
							}
							for(StartEnd se : osaTermList) {
								System.out.println(se.getTermForRequest(200103, recordStartingTIme));
							}
							*/
							System.out.println("=====������ �м� ����, �м����� ��=====");
							recordStartingTIme = 0;
							isRecording = false;
						}
						else if(isRecording == true && Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)){
							System.out.print("���� ����!(���� ���� �׽�Ʈ��) ");
							System.out.println(String.format("%.2f", times)+"s ");
							baos = new ByteArrayOutputStream();
							baos.write(frameBytes);
							SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
							String fileName = dayTime.format(new Date(recordStartingTIme));
							dayTime = new SimpleDateFormat("dd_hhmm");
							long time = System.currentTimeMillis();
							fileName += "-" + dayTime.format(new Date(time));
							byte[] waveData = baos.toByteArray();
							//TODO ������ ������ ����Ǵ� ����
							filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
							SimpleDateFormat dayTimeT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
							System.out.println("=====������ �м� ����, �м����� ����=====");
							System.out.println("�������� ����(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
							Analysis ans = new Analysis();
							ans.setAnalysisStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(recordStartingTIme), ZoneId.systemDefault()));
							ans.setAnalysisEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
							ans.setAnalysisFileAppPath("raw/raw_convert/");
							ans.setAnalysisFileNm("event-"+fileName+"_"+System.currentTimeMillis()+".wav");
							List<AnalysisDetails> ansDList = new ArrayList<AnalysisDetails>();
							AnalysisDetails ansd = new AnalysisDetails();
							for(StartEnd se : snoringTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200101);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							for(StartEnd se : grindingTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200102);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							for(StartEnd se : osaTermList) {
								ansd = new AnalysisDetails();
								ansd.setTermTypeCd(200103);
								ansd.setTermStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.start*1000)), ZoneId.systemDefault()));
								ansd.setTermEndDt(LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (recordStartingTIme+se.end*1000)), ZoneId.systemDefault()));
                                ansDList.add(ansd);
							}
							ans.setAnalysisDetailsList(ansDList);
							ansList.add(ans);
							System.out.println("analysisStartDt: "+dayTimeT.format(new Date(recordStartingTIme)));
							System.out.println("analysisEndDt: "+dayTimeT.format(new Date(time)));
							System.out.println("analysisFileNm: "+"event-"+fileName+"_"+System.currentTimeMillis()+".wav");
							System.out.println("analysisFileAppPath: raw/raw_convert/");
							System.out.println("analysisDetailsList ����, ����Ʈ, ����: "+snoringTermList.size()+ grindingTermList.size()+osaTermList.size());
							for(StartEnd se : snoringTermList) {
								System.out.println(se.getTermForRequest(200101, recordStartingTIme));
							}
							for(StartEnd se : grindingTermList) {
								System.out.println(se.getTermForRequest(200102, recordStartingTIme));
							}
							for(StartEnd se : osaTermList) {
								System.out.println(se.getTermForRequest(200103, recordStartingTIme));
							}
							System.out.println("=====������ �м� ����, �м����� ��=====");
							recordStartingTIme = 0;
							isRecording = false;
						}

						if (i == 1 || isRecording == false) {
							continue;
						}
						/*
						System.out.print("���� ��! ");
						System.out.println(String.format("%.2f", times)+"s ");
						*/
						
						// ������ ������ ���� �ڰ��̰� �߻��ߴ����� üũ�ؼ� ������ ������ �ڰ��� ������ �����Ѵ�. X
						// �ڰ��� ���θ� üũ�Ѵ�.
						int snoreChecked = SleepCheck.snoringCheck(decibel, frequency, sefrequency);
						if(snoreChecked==1) {
							if(snoringTermList.size()>0) {
								double beforeTime = snoringTermList.get(snoringTermList.size()-1).start;
								if(Math.floor(beforeTime)+100<Math.floor(times)) {
									snoringTermList.add(new StartEnd());
									snoringTermList.get(snoringTermList.size()-1).start=times;
									snoringTermList.get(snoringTermList.size()-1).end=times;		
								}
							}else {
								snoringTermList.add(new StartEnd());	
								snoringTermList.get(0).start=times;
								snoringTermList.get(0).end=times;
							}
						}
						
						// �̰��̴� ���� ������� üũ�ؼ�, ��� �������� �߻��ߴ��� üũ�Ѵ�.
						SleepCheck.grindingCheck(times, decibel, sefamplitude, frequency, sefrequency);
						// �̰��� ��ȣ�� �߻��ϰ�, �̰��� üũ ���°� �ƴϸ� �̰��� üũ�� �����Ѵ�.
						if (SleepCheck.grindingRepeatAmpCnt == 1 && grindingStart == false) {
							/*
							System.out.print("�̰��� üũ�� �����Ѵ�.");
							System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
									+ "s " + SleepCheck.grindingContinueAmpCnt + " "
									+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
							*/
							grindingTermList.add(new StartEnd());
							grindingTermList.get(grindingTermList.size()-1).start=times;
							grindingStart = true;
							grindingContinue = false;
							// �̰��� üũ �߿� 1�ʰ������� ��ȿ ī��Ʈ�� ���������� �߻������� ��� üũ�Ѵ�.
						} else if (SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1
								&& SleepCheck.grindingRepeatAmpCnt >= 3 && grindingStart == true) {
							if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
								/*
								System.out.print("�̰��� ����.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								*/
								grindingTermList.get(grindingTermList.size()-1).end=times;
								grindingStart = false;
								grindingContinue = false;
								grindingRecordingContinueCnt = 0;
							}
							/*
							System.out.print("�̰��� ��.");
							System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
									+ "s " + SleepCheck.grindingContinueAmpCnt + " "
									+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
							*/
							grindingRecordingContinueCnt = 0;
							grindingContinue = true;
							// �̰��� üũ �߿� 1�ʰ������� ��ȿ ī��Ʈ�� ���������� �߻����� ������ üũ�� ����Ѵ�.
						} else if (SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1
								&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingStart == true
								&& grindingContinue == false) {
							// 1�� ���� �߻��ϴ� �̰��̵� ������� ���� ī��Ʈ�� �ִ´�. 1�ʸ� �ѹ��� üũ��.
							if (grindingRecordingContinueCnt >= SleepCheck.GRINDING_RECORDING_CONTINUE_CNT) {
								/*
								System.out.print("�̰��� �ƴ�, üũ ���.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								*/
								grindingTermList.remove(grindingTermList.size()-1);
								grindingStart = false;
								grindingRecordingContinueCnt = 0;
							} else {
								/*
								System.out.print("�̰��� üũ�� ������� �ʰ� �����Ѵ�.(1�� ����)");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								*/
								grindingRecordingContinueCnt++;
							}
							// �̰��� üũ �߿� 1�ʰ������� ��ȿī��Ʈ�� ���̻� �߻����� ������ ������ �߻��ߴ���� ���� üũ�ϴ� �̰��̴� ��ȿ��.
						} else if (SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1
								&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingContinue == true) {
							/*
							System.out.print("�̰��� ����.");
							System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
									+ "s " + SleepCheck.grindingContinueAmpCnt + " "
									+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
							*/
							grindingTermList.get(grindingTermList.size()-1).end=times;
							grindingStart = false;
							grindingContinue = false;
							grindingRecordingContinueCnt = 0;
						} else if (SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1) {
							if (grindingStart) {
								/*
								System.out.println(String.format("%.2f", times) + "s �̰��� �� " + grindingStart + " "
										+ grindingContinue + " " + grindingRecordingContinueCnt);
								*/
							}
						}
						// ��ȣ���� ���� ������� üũ�ؼ�, ��� �������� �߻��ߴ��� üũ�Ѵ�.
						osaCnt = SleepCheck.OSACheck(times, decibel, sefamplitude, frequency, sefrequency);
						osaRecordingContinueCnt += osaCnt;
						// ��ȣ�� ī��Ʈ�� �߻��ϰ�, üũ ���°� �ƴϸ� üũ�� �����Ѵ�.
						if (osaRecordingExit > 0) {
							osaRecordingExit--;
						}
						if (osaCnt > 0 && osaStart == false) {
							/*
							System.out.print("��ȣ�� üũ�� �����Ѵ�.");
							System.out.println(String.format("%.2f", times) + "s~" + SleepCheck.isOSATerm + " "
									+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							*/
							osaStart = true;
							osaContinue = false;
							osaRecordingExit = 0;
							osaStartTimes = times;
						} else if (times - osaStartTimes < 5 && osaStart == true) {
							// ��ȣ�� ���� �� 5�� �̳��� ȣ���� �߻��ϸ�, ��ȣ���� �ƴ� ������ ����.
							if (osaRecordingContinueCnt < 5) {
								/*
								System.out.print("��ȣ�� üũ ���. " + osaRecordingContinueCnt + ", ");
								System.out.println(String.format("%.2f", times) + "~"
										+ String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
										+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
								*/
								osaStart = false;
								osaRecordingContinueCnt = 0;
							} else {
								if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
									/*
									System.out.print("��ȣ�� ��.");
									System.out.println(
											String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s "
													+ SleepCheck.grindingContinueAmpCnt + " "
													+ SleepCheck.grindingContinueAmpOppCnt + " "
													+ SleepCheck.grindingRepeatAmpCnt);
									*/
									osaStart = false;
									osaRecordingContinueCnt = 0;
								}
								osaContinue = true;
								/*
								System.out.print("��ȣ�� ��.");
								System.out.println(String.format("%.2f", times) + "~"
										+ String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
										+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
								*/
							}
							// ��ȣ�� ���� �� 5�� �� �Ŀ� �Ҹ��� �߻��ϸ�, ���� �Ҹ��� �߻��� �������� üũ�Ѵ�.
						} else if (times - osaStartTimes > 5 && osaStart == true) {
							if (SleepCheck.isBreathTerm == true) { // ������ ������ �Ǿ�����, üũ ��� �÷��׸� ������Ʈ
								if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
									/*
									System.out.print("��ȣ�� ��.");
									System.out.println(
											String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s "
													+ SleepCheck.grindingContinueAmpCnt + " "
													+ SleepCheck.grindingContinueAmpOppCnt + " "
													+ SleepCheck.grindingRepeatAmpCnt);
									*/
									osaStart = false;
									osaRecordingContinueCnt = 0;
								}
								osaContinue = true;
								/*
								System.out.print("��ȣ�� ��.2 ");
								System.out.println(String.format("%.2f", times) + "~"
										+ String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
										+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
								*/
							} else {
								if (osaContinue == true && osaRecordingExit == 1) {
									/*
									System.out.print("��ȣ�� ��.");
									System.out.println(String.format("%.2f", times) + "~"
											+ String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
											+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
									*/
									osaStart = false;
									osaRecordingContinueCnt = 0;
								}
								if (osaCnt > 0) {
									osaRecordingExit = 1000;
								}
								osaCnt = 0;
							}
						} else {
							if (osaStart) {
								/*
								System.out.print("��ȣ�� ��");
								System.out.println(String.format("%.2f", times) + "~"
										+ String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
										+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
								*/
							}
						}
						SleepCheck.curTermTime = times;
						SleepCheck.curTermDb = decibel;
						SleepCheck.curTermAmp = amplitude;
						SleepCheck.curTermHz = frequency;
						SleepCheck.curTermSecondHz = sefrequency;

						SleepCheck.checkTerm++;
						SleepCheck.checkTermSecond = (int) Math.floor(times);

					}
					/*
					System.out.println("audio length(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
					System.out.println( "�ڰ��� ���� " + SleepCheck.snoringContinue);
					System.out.println( "�̰��� " + grindingTermList.size()+"ȸ �߻� ");
					System.out.println( "�̰��� ����==========");
					for(StartEnd se : grindingTermList) {
						System.out.println(se.getTerm());
					}
					System.out.println( "==================");
					System.out.println( "��ȣ��" + osaTermList.size()+"ȸ �߻� ");
					System.out.println( "��ȣ�� ����==========");
					for(StartEnd se : osaTermList) {
						System.out.println(se.getTerm());
					}
					System.out.println( "==================");
					*/

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

	}

}
