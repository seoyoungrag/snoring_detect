package snoring;

import java.io.ByteArrayInputStream;
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

public class EventFireGui {

	byte[] audioData;
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;

	public EventFireGui(String filePath) {
		InputStream fin;
		try {
			// ����� �Է� �׽�Ʈ��, �̺κ��� ���߿� AudioRecord�� �����ϴ� �κ��� �ȴ�. sta
			fin = new FileInputStream(filePath);
			// ����� �Է� �׽�Ʈ��, �̺κ��� ���߿� AudioRecord�� �����ϴ� �κ��� �ȴ�. end
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
			try {
				targetStream = new ByteArrayInputStream(audioData);
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					audioCalculator.setBytes(frameBytes);
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

					/*
					 * if (i < 1000) { continue; }
					 */
					// �Ҹ��� �߻��� Ư�� db �̻������Ѵ�. ���ú��� -31.5~0 ���� ��ġȭ �ϰ� ����.
					// -10db�� �Ȱɸ� ���� �����ϱ�, ���� ���� ������ ��� ���ú����� ���������� �����ϸ鼭 ��� ���ú����� ���� �Ҹ��� �߻��ߴ��� üũ
					// �Ѵ�.
					// ��� ���ú� üũ�� 3�� �����Ѵ�.
					// �Ҹ��� �߻��ϸ� ������ �����ϰ�, 1���̻� �Ҹ��� �߻����� ������ ������ ���� �ʴ´�.
					if(decibel!=-31.5) {
					/*	
					*/
					}
					if(SleepCheck.noiseChkCnt>0) {

						//System.out.print(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+" "+sehz+" "+seamp);
						//System.out.print(" decibel:" +decibel +"vs" + SleepCheck.getMaxDB()+"vs" + SleepCheck.getMinDB());
						//System.out.print(" noiseCheckForStart:" +SleepCheck.noiseChkForStartCnt+", "+SleepCheck.noiseChkForStartSum+", "+SleepCheck.noiseNoneChkSum);
						//System.out.println(" noiseCheck:" +SleepCheck.noiseChkCnt+", "+SleepCheck.noiseChkSum);
					}else {
					}
					
					//if (decibel > SleepCheck.getMaxDB() && isRecording == false
					if (SleepCheck.noiseCheckForStart(decibel) >= 30 && isRecording == false
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times) // ����
																													// ����
																													// �׽�Ʈ��
					) {
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println(String.format("%.2f", times) + "s~");
						System.out.println(calcTime(times));
						
						recordStartingTIme = System.currentTimeMillis();
						// baos = new ByteArrayOutputStream();
						// baos.write(frameBytes);
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						//System.out.println(" !!!!!!!!!!!!!!!!!!!!!!!!!" +SleepCheck.noiseCheck(decibel));
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println(String.format("%.2f", times) + "s ");
						System.out.println(calcTime(times));
						SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
						String fileName = dayTime.format(new Date(recordStartingTIme));
						dayTime = new SimpleDateFormat("dd_hhmm");
						long time = System.currentTimeMillis();
						fileName += "-" + dayTime.format(new Date(time));
						// byte[] waveData = baos.toByteArray();
						// TODO ������ ������ ����Ǵ� ����
						// filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
						System.out.println("=====������ �м� ����, �м����� ����=====");
						//System.out.println("�������� ����(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
						System.out.println("�������� ����(s): "+ (time - recordStartingTIme));
						Analysis ans = new Analysis();
						ans.setAnalysisStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(recordStartingTIme),
								ZoneId.systemDefault()));
						ans.setAnalysisEndDt(
								LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
						ans.setAnalysisFileAppPath("raw/raw_convert/");
						ans.setAnalysisFileNm("event-" + fileName + "_" + System.currentTimeMillis() + ".wav");
						List<AnalysisDetails> ansDList = new ArrayList<AnalysisDetails>();
						AnalysisDetails ansd = new AnalysisDetails();
						for (StartEnd se : snoringTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200101);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						for (StartEnd se : grindingTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200102);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						for (StartEnd se : osaTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200103);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						ans.setAnalysisDetailsList(ansDList);
						ansList.add(ans);
						/*
						 * System.out.println("analysisStartDt: "+dayTimeT.format(new
						 * Date(recordStartingTIme)));
						 * System.out.println("analysisEndDt: "+dayTimeT.format(new Date(time)));
						 * System.out.println("analysisFileNm: "+"event-"+fileName+"_"+System.
						 * currentTimeMillis()+".wav");
						 * System.out.println("analysisFileAppPath: raw/raw_convert/");
						 * System.out.println("analysisDetailsList ����, ����Ʈ, ����: "+snoringTermList.size()
						 * + grindingTermList.size()+osaTermList.size()); for(StartEnd se :
						 * snoringTermList) { System.out.println(se.getTermForRequest(200101,
						 * recordStartingTIme)); } for(StartEnd se : grindingTermList) {
						 * System.out.println(se.getTermForRequest(200102, recordStartingTIme)); }
						 * for(StartEnd se : osaTermList) {
						 * System.out.println(se.getTermForRequest(200103, recordStartingTIme)); }
						 */
						System.out.println("=====������ �м� ����, �м����� ��=====");
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print("���� ����!(���� ���� �׽�Ʈ��) ");
						System.out.println(String.format("%.2f", times) + "s ");
						SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
						String fileName = dayTime.format(new Date(recordStartingTIme));
						dayTime = new SimpleDateFormat("dd_hhmm");
						long time = System.currentTimeMillis();
						fileName += "-" + dayTime.format(new Date(time));
						// byte[] waveData = baos.toByteArray();
						// TODO ������ ������ ����Ǵ� ����
						// filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
						SimpleDateFormat dayTimeT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						System.out.println("=====������ �м� ����, �м����� ����=====");
						System.out.println("�������� ����(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
						Analysis ans = new Analysis();
						ans.setAnalysisStartDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(recordStartingTIme),
								ZoneId.systemDefault()));
						ans.setAnalysisEndDt(
								LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
						ans.setAnalysisFileAppPath("raw/raw_convert/");
						ans.setAnalysisFileNm("event-" + fileName + "_" + System.currentTimeMillis() + ".wav");
						List<AnalysisDetails> ansDList = new ArrayList<AnalysisDetails>();
						AnalysisDetails ansd = new AnalysisDetails();
						if (grindingTermList.size() > 0 && grindingStart == true) {
							grindingTermList.get(grindingTermList.size() - 1).end = times;
						}
						if (osaTermList.size() > 0 && osaStart) {
							osaTermList.get(osaTermList.size() - 1).end = times;
						}
						for (StartEnd se : snoringTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200101);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						for (StartEnd se : grindingTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200102);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						for (StartEnd se : osaTermList) {
							ansd = new AnalysisDetails();
							ansd.setTermTypeCd(200103);
							ansd.setTermStartDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.start * 1000)),
									ZoneId.systemDefault()));
							ansd.setTermEndDt(LocalDateTime.ofInstant(
									Instant.ofEpochMilli((long) (recordStartingTIme + se.end * 1000)),
									ZoneId.systemDefault()));
							ansDList.add(ansd);
						}
						ans.setAnalysisDetailsList(ansDList);
						ansList.add(ans);
						System.out.println("analysisStartDt: " + dayTimeT.format(new Date(recordStartingTIme)));
						System.out.println("analysisEndDt: " + dayTimeT.format(new Date(time)));
						System.out.println(
								"analysisFileNm: " + "event-" + fileName + "_" + System.currentTimeMillis() + ".wav");
						System.out.println("analysisFileAppPath: raw/raw_convert/");
						System.out.println("analysisDetailsList ����, ����Ʈ, ����: "
								+ (snoringTermList.size() + grindingTermList.size() + osaTermList.size()));
						for (StartEnd se : snoringTermList) {
							System.out.println(se.getTermForRequest(200101, recordStartingTIme));
							//System.out.println(se.getTerm());
							System.out.println(se.printAnalysisRawDataList());
						}
						for (StartEnd se : grindingTermList) {
							System.out.println(se.getTermForRequest(200102, recordStartingTIme));
							//System.out.println(se.getTerm());
							System.out.println(se.printAnalysisRawDataList());
						}
						for (StartEnd se : osaTermList) {
							System.out.println(se.getTermForRequest(200103, recordStartingTIme));
							System.out.println(se.getTerm());
							System.out.println(se.printAnalysisRawDataList());
						}
						System.out.println("=====������ �м� ����, �м����� ��=====");
						recordStartingTIme = 0;
						isRecording = false;
					}
					if (isRecording == false) {
						continue;
					}
					// baos.write(frameBytes);
					/*
					 * System.out.print("���� ��! "); System.out.println(String.format("%.2f",
					 * times)+"s ");
					 */
					// TODO �� ����� ���� ���̸� �� �ʴ��� �����͸� vo�� �Է��ؾ� �Ѵ�.
					// ����� �м����� ���� ������ ����Ʈ �ʵ尡 �ȴ�.
					// ����� ������ ��, �м��� vo�� �����Ѵ�. ���� ������ ����Ʈ �ʵ嵵 �ʱ�ȭ �� ���� �ð��� �����͸� �Է��Ѵ�.
					//
					// ������ ������ ���� �ڰ��̰� �߻��ߴ����� üũ�ؼ� ������ ������ �ڰ��� ������ �����Ѵ�. X
					// �ڰ��� ���θ� üũ�Ѵ�.
					int snoreChecked = SleepCheck.snoringCheck(decibel, frequency, sefrequency);
					// snorChecked = 1�̸� 0.01�ʿ� �ش��ϴ� ���ļ��� Ž����
					// snorChecked = 2�� 1�е��� �ڰ��̰� Ž���� ����
					if (snoreChecked == 2) {
						if (SleepCheck.isSnoringStart == true) {
							// �ڰ��̷� Ž���ؼ� �м��� �����ϰ� �ִ� ��
						} else {
							System.out.print("�ڰ��� ��ϸ� �����Ѵ�.");
							System.out.println(
									String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s");
							snoringTermList.add(new StartEnd());
							snoringTermList.get(snoringTermList.size() - 1).start = times;
							// TODO ���⼭ �ʴ� ������ ����Ʈ �ʵ带 �����ϸ� �ȴ�.
							snoringTermList
									.get(snoringTermList.size() - 1).AnalysisRawDataList = new ArrayList<AnalysisRawData>();
							snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(
									times, amplitude, decibel, frequency, sefrequency, sefamplitude));
							// System.out.println(String.format("%.0f", times)+"s "+hz +" "+db+" "+amp+"
							// "+sehz+" "+seamp);
							SleepCheck.isSnoringStart = true;
						}
					} else if (snoreChecked == 3) {
						if (SleepCheck.isSnoringStart == true) {
							System.out.println("�ڰ��� ��� ����");
							System.out.println(
									String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s");
							snoringTermList.get(snoringTermList.size() - 1).end = times;
							SleepCheck.isSnoringStart = false;
						} else {
							// �ڰ��̷� ��Ž��, ó���� ���� ����.
						}
					} else {
						// 0�� ���� ���� �м��ϰ� 1���� �ȵ� ����, �� �� 1���� �ȵ� ������ �̰��� ź��.

					}
					if (SleepCheck.isSnoringStart == true) {
						try {
							String tmpTime =
									String.format("%.0f", 
									snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.get(snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.size()-1).getTimes()
									);
							if(!tmpTime.equals(String.format("%.0f",times))){
								snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(
										times, amplitude, decibel, frequency, sefrequency, sefamplitude));
							}
						} catch (IndexOutOfBoundsException e) {
							System.out.println("293");// log4j�� �ϸ� ���γѹ��� ���� �� �ִ�. ���⼭�� �ϵ� �ڵ�.
						}
					}

					if (SleepCheck.grindingRepeatAmpCnt != 0) {
						// System.out.println(SleepCheck.curTermSecond+"vs"+SleepCheck.GrindingCheckStartTermSecond);
					}

					// �̰��̴� ���� ������� üũ�ؼ�, ��� �������� �߻��ߴ��� üũ�Ѵ�.
					SleepCheck.grindingCheck(times, decibel, sefamplitude, frequency, sefrequency);
					// �̰��� ��ȣ�� �߻��ϰ�, �̰��� üũ ���°� �ƴϸ� �̰��� üũ�� �����Ѵ�.
					// System.out.println(grindingStart+" "+SleepCheck.curTermSecond + "
					// "+SleepCheck.GrindingCheckStartTermSecond+"
					// "+SleepCheck.grindingRepeatAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt);
					if (SleepCheck.grindingRepeatAmpCnt == 2 && grindingStart == false) {
						System.out.print("�̰��� ����� �����Ѵ�.");
						System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s "
								+ SleepCheck.grindingContinueAmpCnt + " " + SleepCheck.grindingContinueAmpOppCnt + " "
								+ SleepCheck.grindingRepeatAmpCnt);
						// �̰��� üũ ������ ���� ���� �ð��� ���� �ð����� �Է��Ѵ�.
						SleepCheck.GrindingCheckStartTermSecond = times;
						grindingTermList.add(new StartEnd());
						// grindingTermList.get(grindingTermList.size()-1).start=times;
						grindingTermList.get(grindingTermList.size() - 1).start = times - 2;
						grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList = new ArrayList<AnalysisRawData>();
						grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(times, amplitude, decibel, frequency, sefrequency, sefamplitude));
						grindingStart = true;
						grindingContinue = false;
						// �̰��� üũ �߿� 1�ʰ������� ��ȿ ī��Ʈ�� ���������� �߻������� ��� üũ�Ѵ�.
						// ���� �� - �� ���� �ð�
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt >= 3 && grindingStart == true) {
						if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
							System.out.print("�̰��� ����(������ ����).");
							/*
							 * System.out.print("�̰��� ����."); System.out.println(String.format("%.2f", times)
							 * + "~" + String.format("%.2f", times + 1) + "s " +
							 * SleepCheck.grindingContinueAmpCnt + " " +
							 * SleepCheck.grindingContinueAmpOppCnt + " " +
							 * SleepCheck.grindingRepeatAmpCnt);
							 */
							SleepCheck.grindingRepeatAmpCnt = 0;
							grindingTermList.get(grindingTermList.size() - 1).end = times;
							grindingStart = false;
							grindingContinue = false;
							grindingRecordingContinueCnt = 0;
						}
						System.out.print("�̰��� ��.");
						/*
						 * System.out.print("�̰��� ��."); System.out.println(String.format("%.2f", times) +
						 * "~" + String.format("%.2f", times + 1) + "s " +
						 * SleepCheck.grindingContinueAmpCnt + " " +
						 * SleepCheck.grindingContinueAmpOppCnt + " " +
						 * SleepCheck.grindingRepeatAmpCnt);
						 */
						grindingRecordingContinueCnt = 0;
						grindingContinue = true;
						// �̰��� üũ �߿� 1�ʰ������� ��ȿ ī��Ʈ�� ���������� �߻����� ������ üũ�� ����Ѵ�.
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingStart == true
							&& grindingContinue == false) {
						// 1�� ���� �߻��ϴ� �̰��̵� ������� ���� ī��Ʈ�� �ִ´�. 1�ʸ� �ѹ��� üũ��.
						if (grindingRecordingContinueCnt >= SleepCheck.GRINDING_RECORDING_CONTINUE_CNT) {
							System.out.println("�̰��� �ƴ�, üũ ���.");
							/*
							 * System.out.print("�̰��� �ƴ�, üũ ���."); System.out.println(String.format("%.2f",
							 * times) + "~" + String.format("%.2f", times + 1) + "s " +
							 * SleepCheck.grindingContinueAmpCnt + " " +
							 * SleepCheck.grindingContinueAmpOppCnt + " " +
							 * SleepCheck.grindingRepeatAmpCnt);
							 */
							SleepCheck.grindingRepeatAmpCnt = 0;
							grindingTermList.remove(grindingTermList.size() - 1);
							grindingStart = false;
							grindingRecordingContinueCnt = 0;
						} else {
							System.out.print("�̰��� üũ�� ������� �ʰ� �����Ѵ�.(1�� ����)");
							/*
							 * System.out.print("�̰��� üũ�� ������� �ʰ� �����Ѵ�.(1�� ����)");
							 * System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f",
							 * times + 1) + "s " + SleepCheck.grindingContinueAmpCnt + " " +
							 * SleepCheck.grindingContinueAmpOppCnt + " " +
							 * SleepCheck.grindingRepeatAmpCnt);
							 */
							grindingRecordingContinueCnt++;
						}
						// �̰��� üũ �߿� 1�ʰ������� ��ȿī��Ʈ�� ���̻� �߻����� ������ ������ �߻��ߴ���� ���� üũ�ϴ� �̰��̴� ��ȿ��.
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingContinue == true) {
						System.out.print("�̰��� ����.");
						/*
						 * System.out.print("�̰��� ����."); System.out.println(String.format("%.2f", times)
						 * + "~" + String.format("%.2f", times + 1) + "s " +
						 * SleepCheck.grindingContinueAmpCnt + " " +
						 * SleepCheck.grindingContinueAmpOppCnt + " " +
						 * SleepCheck.grindingRepeatAmpCnt);
						 */
						SleepCheck.grindingRepeatAmpCnt = 0;
						grindingTermList.get(grindingTermList.size() - 1).end = times;
						grindingStart = false;
						grindingContinue = false;
						grindingRecordingContinueCnt = 0;
					} else if (SleepCheck.curTermSecond - SleepCheck.checkTermSecond == 1) {
						if (grindingStart) {
							/*
							 * System.out.println(String.format("%.2f", times) + "s �̰��� �� " + grindingStart
							 * + " " + grindingContinue + " " + grindingRecordingContinueCnt);
							 */
						}
					}
					
					if (grindingStart) {
						try {

							String tmpTime =
									String.format("%.0f", 
											grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList.get(grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList.size()-1).getTimes()
									);
							if(!tmpTime.equals(String.format("%.0f",times))){
								grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(
										times, amplitude, decibel, frequency, sefrequency, sefamplitude));
							}
						} catch (IndexOutOfBoundsException e) {
							System.out.println("397");// log4j�� �ϸ� ���γѹ��� ���� �� �ִ�. ���⼭�� �ϵ� �ڵ�.
						}
					}
					
					// ��ȣ�� ���� ������� üũ�ؼ�, ��� �������� �߻��ߴ��� üũ�Ѵ�.
					osaCnt = SleepCheck.OSACheck(times, decibel, sefamplitude, frequency, sefrequency);
					osaRecordingContinueCnt += osaCnt;
					// ��ȣ�� ī��Ʈ�� �߻��ϰ�, üũ ���°� �ƴϸ� üũ�� �����Ѵ�.
					if (osaRecordingExit > 0) {
						osaRecordingExit--;
					}
					if (osaCnt > 0 && osaStart == false) {
						System.out.print("��ȣ�� ����� �����Ѵ�.");
						System.out.println(String.format("%.2f", times) + "s~" + SleepCheck.isOSATerm + " "
								+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
						osaStart = true;
						osaContinue = false;
						osaRecordingExit = 0;
						osaStartTimes = times;
					} else if (times - osaStartTimes < 5 && osaStart == true) {
						// ��ȣ�� ���� �� 5�� �̳��� ȣ���� �߻��ϸ�, ��ȣ���� �ƴ� ������ ����.
						if (osaRecordingContinueCnt < 5) {
							System.out.print("��ȣ�� üũ ���. " + osaRecordingContinueCnt + ", ");
							System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 0.01)
									+ "s " + SleepCheck.isOSATerm + " " + SleepCheck.isBreathTerm + " "
									+ SleepCheck.isOSATermCnt);
							osaStart = false;
							osaRecordingContinueCnt = 0;
							osaTermList.remove(osaTermList.size() - 1);
						} else {
							if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
								System.out.print("��ȣ�� ��.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								osaStart = false;
								osaRecordingContinueCnt = 0;
							}
							osaContinue = true;
							/*
							 * System.out.print("��ȣ�� ��."); System.out.println(String.format("%.2f", times) +
							 * "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
							 * + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							 */
						}
						// ��ȣ�� ���� �� 5�� �� �Ŀ� �Ҹ��� �߻��ϸ�, ���� �Ҹ��� �߻��� �������� üũ�Ѵ�.
					} else if (times - osaStartTimes > 5 && osaStart == true) {
						if (SleepCheck.isBreathTerm == true) { // ������ ������ �Ǿ�����, üũ ��� �÷��׸� ������Ʈ
							if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
								System.out.print("��ȣ�� ��.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								osaStart = false;
								osaRecordingContinueCnt = 0;
							}
							osaContinue = true;
							/*
							 * System.out.print("��ȣ�� ��.2 "); System.out.println(String.format("%.2f", times)
							 * + "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm +
							 * " " + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							 */
						} else {
							if (osaContinue == true && osaRecordingExit == 1) {
								/*
								 * System.out.print("��ȣ�� ��."); System.out.println(String.format("%.2f", times) +
								 * "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
								 * + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
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
							 * System.out.print("��ȣ�� ��"); System.out.println(String.format("%.2f", times) +
							 * "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
							 * + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							 */
						}
					}

					if(SleepCheck.isOSAAnsStart == true) {
						try {
							String tmpTime =
									String.format("%.0f", 
											osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.get(osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.size()-1).getTimes()
									);
							if(!tmpTime.equals(String.format("%.0f",times))){
								osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(
										new AnalysisRawData(times, amplitude, decibel, frequency, sefrequency, sefamplitude));
							}
						} catch (IndexOutOfBoundsException e) {
							//System.out.println("540");// log4j�� �ϸ� ���γѹ��� ���� �� �ִ�. ���⼭�� �ϵ� �ڵ�.
						} 
						/*catch (NullPointerException e1) {
							System.out.println("NULL="+times);
							//System.out.println(SleepCheck.isOSATermTimeOccur+" "+);
						}*/
					}else {
						//System.out.println(times);
					}
					if (osaStart) {
						/*
						try {
							String tmpTime =
									String.format("%.0f", 
											osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.get(osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.size()-1).getTimes()
									);
							if(!tmpTime.equals(String.format("%.0f",times))){
								osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(
										new AnalysisRawData(times, amplitude, decibel, frequency, sefrequency, sefamplitude));
							}
						} catch (IndexOutOfBoundsException e) {
							//System.out.println("540");// log4j�� �ϸ� ���γѹ��� ���� �� �ִ�. ���⼭�� �ϵ� �ڵ�.
						}
						*/
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
				 * System.out.println("audio length(s): " + ((double) (audioData.length /
				 * (44100d * 16 * 1))) * 8); System.out.println( "�ڰ��� ���� " +
				 * SleepCheck.snoringContinue); System.out.println( "�̰��� " +
				 * grindingTermList.size()+"ȸ �߻� "); System.out.println( "�̰��� ����==========");
				 * for(StartEnd se : grindingTermList) { System.out.println(se.getTerm()); }
				 * System.out.println( "=================="); System.out.println( "��ȣ��" +
				 * osaTermList.size()+"ȸ �߻� "); System.out.println( "��ȣ�� ����==========");
				 * for(StartEnd se : osaTermList) { System.out.println(se.getTerm()); }
				 * System.out.println( "==================");
				 */

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

	private String calcTime(double times) {
        int seconds;
        int minutes ;
        int hours;
        seconds =  (int)times;
        hours = seconds / 3600;
        minutes = (seconds%3600)/60;
        int seconds_output = (seconds% 3600)%60;
        return hours  + ":" + minutes + ":" + seconds_output +""; 
	}

}

class StartEnd {
	double start;
	double end;
	List<AnalysisRawData> AnalysisRawDataList;

	public String getTerm() {
		return String.format("%.0f", start) + "~" + String.format("%.0f", end);
	}

	public String getTermForRequest(int termCd, long recordStartingTIme) {
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return "termTypeCd: " + termCd + ", termStartDt: "
				+ dayTime.format(new Date((long) (recordStartingTIme + this.start * 1000))) + ",termEndDt: "
				+ dayTime.format(new Date((long) (recordStartingTIme + this.end * 1000)));
	}
	
	public String printAnalysisRawDataList() {
		String rtn = "";
		if(this.AnalysisRawDataList!=null) {
			for(AnalysisRawData d : this.AnalysisRawDataList) {
				rtn+=d.toString()+"\r\n";
			}
		}
		return rtn;
	}
	
}