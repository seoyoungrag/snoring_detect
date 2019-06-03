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
					// 소리의 발생은 특정 db 이상으로한다. 데시벨은 -31.5~0 으로 수치화 하고 있음.
					// -10db에 안걸릴 수도 잇으니까, 현재 녹음 상태의 평균 데시벨값을 지속적으로 갱신하면서 평균 데시벨보다 높은 소리가 발생했는지 체크
					// 한다.
					// 평균 데시벨 체크는 3초 동안한다.
					// 소리가 발생하면 녹음을 시작하고, 1분이상 소리가 발생하지 않으면 녹음을 하지 않는다.
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
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times) // 사운드
																													// 파일
																													// 테스트용
					) {
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 시작! ");
						System.out.println(String.format("%.2f", times) + "s~");
						System.out.println(calcTime(times));
						
						recordStartingTIme = System.currentTimeMillis();
						// baos = new ByteArrayOutputStream();
						// baos.write(frameBytes);
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						//System.out.println(" !!!!!!!!!!!!!!!!!!!!!!!!!" +SleepCheck.noiseCheck(decibel));
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 종료! ");
						System.out.println(String.format("%.2f", times) + "s ");
						System.out.println(calcTime(times));
						SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
						String fileName = dayTime.format(new Date(recordStartingTIme));
						dayTime = new SimpleDateFormat("dd_hhmm");
						long time = System.currentTimeMillis();
						fileName += "-" + dayTime.format(new Date(time));
						// byte[] waveData = baos.toByteArray();
						// TODO 녹음된 파일이 저장되는 시점
						// filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						//System.out.println("녹음파일 길이(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
						System.out.println("녹음파일 길이(s): "+ (time - recordStartingTIme));
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
						 * System.out.println("analysisDetailsList 시작, 리스트, 길이: "+snoringTermList.size()
						 * + grindingTermList.size()+osaTermList.size()); for(StartEnd se :
						 * snoringTermList) { System.out.println(se.getTermForRequest(200101,
						 * recordStartingTIme)); } for(StartEnd se : grindingTermList) {
						 * System.out.println(se.getTermForRequest(200102, recordStartingTIme)); }
						 * for(StartEnd se : osaTermList) {
						 * System.out.println(se.getTermForRequest(200103, recordStartingTIme)); }
						 */
						System.out.println("=====녹음중 분석 종료, 분석정보 끝=====");
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print("녹음 종료!(사운드 파일 테스트용) ");
						System.out.println(String.format("%.2f", times) + "s ");
						SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmdd_hhmm");
						String fileName = dayTime.format(new Date(recordStartingTIme));
						dayTime = new SimpleDateFormat("dd_hhmm");
						long time = System.currentTimeMillis();
						fileName += "-" + dayTime.format(new Date(time));
						// byte[] waveData = baos.toByteArray();
						// TODO 녹음된 파일이 저장되는 시점
						// filePath = WaveFormatConverter.saveLongTermWave(waveData, fileName);
						SimpleDateFormat dayTimeT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						System.out.println("녹음파일 길이(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
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
						System.out.println("analysisDetailsList 시작, 리스트, 길이: "
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
						System.out.println("=====녹음중 분석 종료, 분석정보 끝=====");
						recordStartingTIme = 0;
						isRecording = false;
					}
					if (isRecording == false) {
						continue;
					}
					// baos.write(frameBytes);
					/*
					 * System.out.print("녹음 중! "); System.out.println(String.format("%.2f",
					 * times)+"s ");
					 */
					// TODO 각 기록이 진행 중이면 매 초당의 데이터를 vo에 입력해야 한다.
					// 기록은 분석상세의 매초 데이터 리스트 필드가 된다.
					// 기록이 시작할 때, 분석상세 vo를 생성한다. 매초 데이터 리스트 필드도 초기화 및 시작 시간의 데이터를 입력한다.
					//
					// 녹음이 끝나고 나면 코골이가 발생했는지를 체크해서 녹음된 파일의 코골이 유무를 결정한다. X
					// 코골이 여부를 체크한다.
					int snoreChecked = SleepCheck.snoringCheck(decibel, frequency, sefrequency);
					// snorChecked = 1이면 0.01초에 해당하는 주파수만 탐지됨
					// snorChecked = 2는 1분동안 코골이가 탐지된 상태
					if (snoreChecked == 2) {
						if (SleepCheck.isSnoringStart == true) {
							// 코골이로 탐지해서 분석을 진행하고 있는 중
						} else {
							System.out.print("코골이 기록를 시작한다.");
							System.out.println(
									String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s");
							snoringTermList.add(new StartEnd());
							snoringTermList.get(snoringTermList.size() - 1).start = times;
							// TODO 여기서 초당 데이터 리스트 필드를 생성하면 된다.
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
							System.out.println("코골이 기록 종료");
							System.out.println(
									String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s");
							snoringTermList.get(snoringTermList.size() - 1).end = times;
							SleepCheck.isSnoringStart = false;
						} else {
							// 코골이로 미탐지, 처리할 내용 없음.
						}
					} else {
						// 0일 때는 아직 분석하고 1분이 안된 상태, 즉 각 1분이 안된 때마다 이곳을 탄다.

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
							System.out.println("293");// log4j로 하면 라인넘버를 남길 수 있다. 여기서는 하드 코딩.
						}
					}

					if (SleepCheck.grindingRepeatAmpCnt != 0) {
						// System.out.println(SleepCheck.curTermSecond+"vs"+SleepCheck.GrindingCheckStartTermSecond);
					}

					// 이갈이는 기존 로직대로 체크해서, 어디 구간에서 발생했는지 체크한다.
					SleepCheck.grindingCheck(times, decibel, sefamplitude, frequency, sefrequency);
					// 이갈이 신호가 발생하고, 이갈이 체크 상태가 아니면 이갈이 체크를 시작한다.
					// System.out.println(grindingStart+" "+SleepCheck.curTermSecond + "
					// "+SleepCheck.GrindingCheckStartTermSecond+"
					// "+SleepCheck.grindingRepeatAmpCnt+" "+SleepCheck.grindingContinueAmpOppCnt);
					if (SleepCheck.grindingRepeatAmpCnt == 2 && grindingStart == false) {
						System.out.print("이갈이 기록을 시작한다.");
						System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1) + "s "
								+ SleepCheck.grindingContinueAmpCnt + " " + SleepCheck.grindingContinueAmpOppCnt + " "
								+ SleepCheck.grindingRepeatAmpCnt);
						// 이갈이 체크 간격을 위한 기준 시간을 현재 시간으로 입력한다.
						SleepCheck.GrindingCheckStartTermSecond = times;
						grindingTermList.add(new StartEnd());
						// grindingTermList.get(grindingTermList.size()-1).start=times;
						grindingTermList.get(grindingTermList.size() - 1).start = times - 2;
						grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList = new ArrayList<AnalysisRawData>();
						grindingTermList.get(grindingTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(times, amplitude, decibel, frequency, sefrequency, sefamplitude));
						grindingStart = true;
						grindingContinue = false;
						// 이갈이 체크 중에 1초간격으로 유효 카운트가 연속적으로 발생했으면 계속 체크한다.
						// 현재 초 - 비교 기준 시간
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt >= 3 && grindingStart == true) {
						if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
							System.out.print("이갈이 종료(녹음이 끝남).");
							/*
							 * System.out.print("이갈이 종료."); System.out.println(String.format("%.2f", times)
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
						System.out.print("이갈이 중.");
						/*
						 * System.out.print("이갈이 중."); System.out.println(String.format("%.2f", times) +
						 * "~" + String.format("%.2f", times + 1) + "s " +
						 * SleepCheck.grindingContinueAmpCnt + " " +
						 * SleepCheck.grindingContinueAmpOppCnt + " " +
						 * SleepCheck.grindingRepeatAmpCnt);
						 */
						grindingRecordingContinueCnt = 0;
						grindingContinue = true;
						// 이갈이 체크 중에 1초간격으로 유효 카운트가 연속적으로 발생하지 않으면 체크를 취소한다.
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingStart == true
							&& grindingContinue == false) {
						// 1초 단위 발생하는 이갈이도 잡기위해 유예 카운트를 넣는다. 1초만 한번더 체크함.
						if (grindingRecordingContinueCnt >= SleepCheck.GRINDING_RECORDING_CONTINUE_CNT) {
							System.out.println("이갈이 아님, 체크 취소.");
							/*
							 * System.out.print("이갈이 아님, 체크 취소."); System.out.println(String.format("%.2f",
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
							System.out.print("이갈이 체크를 취소하지 않고 진행한다.(1초 유예)");
							/*
							 * System.out.print("이갈이 체크를 취소하지 않고 진행한다.(1초 유예)");
							 * System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f",
							 * times + 1) + "s " + SleepCheck.grindingContinueAmpCnt + " " +
							 * SleepCheck.grindingContinueAmpOppCnt + " " +
							 * SleepCheck.grindingRepeatAmpCnt);
							 */
							grindingRecordingContinueCnt++;
						}
						// 이갈이 체크 중에 1초간격으로 유효카운트가 더이상 발생하지 않으나 이전에 발생했더라면 현재 체크하는 이갈이는 유효함.
					} else if (Math.floor(
							(SleepCheck.GrindingCheckTermSecond - SleepCheck.GrindingCheckStartTermSecond) * 100) == 101
							&& SleepCheck.grindingRepeatAmpCnt == 0 && grindingContinue == true) {
						System.out.print("이갈이 종료.");
						/*
						 * System.out.print("이갈이 종료."); System.out.println(String.format("%.2f", times)
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
							 * System.out.println(String.format("%.2f", times) + "s 이갈이 중 " + grindingStart
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
							System.out.println("397");// log4j로 하면 라인넘버를 남길 수 있다. 여기서는 하드 코딩.
						}
					}
					
					// 무호흡도 기존 로직대로 체크해서, 어디 구간에서 발생했는지 체크한다.
					osaCnt = SleepCheck.OSACheck(times, decibel, sefamplitude, frequency, sefrequency);
					osaRecordingContinueCnt += osaCnt;
					// 무호흡 카운트가 발생하고, 체크 상태가 아니면 체크를 시작한다.
					if (osaRecordingExit > 0) {
						osaRecordingExit--;
					}
					if (osaCnt > 0 && osaStart == false) {
						System.out.print("무호흡 기록을 시작한다.");
						System.out.println(String.format("%.2f", times) + "s~" + SleepCheck.isOSATerm + " "
								+ SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
						osaStart = true;
						osaContinue = false;
						osaRecordingExit = 0;
						osaStartTimes = times;
					} else if (times - osaStartTimes < 5 && osaStart == true) {
						// 무호흡 녹음 중 5초 이내에 호흡이 발생하면, 무호흡이 아닌 것으로 본다.
						if (osaRecordingContinueCnt < 5) {
							System.out.print("무호흡 체크 취소. " + osaRecordingContinueCnt + ", ");
							System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 0.01)
									+ "s " + SleepCheck.isOSATerm + " " + SleepCheck.isBreathTerm + " "
									+ SleepCheck.isOSATermCnt);
							osaStart = false;
							osaRecordingContinueCnt = 0;
							osaTermList.remove(osaTermList.size() - 1);
						} else {
							if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
								System.out.print("무호흡 끝.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								osaStart = false;
								osaRecordingContinueCnt = 0;
							}
							osaContinue = true;
							/*
							 * System.out.print("무호흡 중."); System.out.println(String.format("%.2f", times) +
							 * "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm + " "
							 * + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							 */
						}
						// 무호흡 녹음 중 5초 이 후에 소리가 발생하면, 다음 소리가 발생한 구간까지 체크한다.
					} else if (times - osaStartTimes > 5 && osaStart == true) {
						if (SleepCheck.isBreathTerm == true) { // 숨쉬는 구간이 되었으면, 체크 계속 플래그를 업데이트
							if (((double) (audioData.length / (44100d * 16 * 1))) * 8 < times + 1) {
								System.out.print("무호흡 끝.");
								System.out.println(String.format("%.2f", times) + "~" + String.format("%.2f", times + 1)
										+ "s " + SleepCheck.grindingContinueAmpCnt + " "
										+ SleepCheck.grindingContinueAmpOppCnt + " " + SleepCheck.grindingRepeatAmpCnt);
								osaStart = false;
								osaRecordingContinueCnt = 0;
							}
							osaContinue = true;
							/*
							 * System.out.print("무호흡 중.2 "); System.out.println(String.format("%.2f", times)
							 * + "~" + String.format("%.2f", times + 0.01) + "s " + SleepCheck.isOSATerm +
							 * " " + SleepCheck.isBreathTerm + " " + SleepCheck.isOSATermCnt);
							 */
						} else {
							if (osaContinue == true && osaRecordingExit == 1) {
								/*
								 * System.out.print("무호흡 끝."); System.out.println(String.format("%.2f", times) +
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
							 * System.out.print("무호흡 중"); System.out.println(String.format("%.2f", times) +
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
							//System.out.println("540");// log4j로 하면 라인넘버를 남길 수 있다. 여기서는 하드 코딩.
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
							//System.out.println("540");// log4j로 하면 라인넘버를 남길 수 있다. 여기서는 하드 코딩.
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
				 * (44100d * 16 * 1))) * 8); System.out.println( "코골이 여부 " +
				 * SleepCheck.snoringContinue); System.out.println( "이갈이 " +
				 * grindingTermList.size()+"회 발생 "); System.out.println( "이갈이 구간==========");
				 * for(StartEnd se : grindingTermList) { System.out.println(se.getTerm()); }
				 * System.out.println( "=================="); System.out.println( "무호흡" +
				 * osaTermList.size()+"회 발생 "); System.out.println( "무호흡 구간==========");
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