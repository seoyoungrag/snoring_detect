package snoring;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import snoring.scichart.DoubleValues;
import snoring.scichart.Radix2FFT;
import snoring.scichart.ShortValues;

public class EventFireGui {

	byte[] audioData;
	int frameByteSizePer = 16;
	int frameByteSizeForSnoring = 1024*frameByteSizePer;
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;
	static double tmpMinDb = 99999;
	static double tmpMaxDb = 0;
	static double firstDecibelAvg = 0.0;
	static double secondDecibelAvg = 0.0;
	static double snoringDbChkCnt = 0;
	static int soundStartAndSnroingCnt = 0;
	static int soundStartAndSnroingOppCnt = 0;

	static boolean isBreathTerm = false;
	static boolean isOSATermTimeOccur = false;
	static int isBreathTermCnt = 0;
	static double OSAcurTermTime = 0.0;
	static int isOSATermCnt = 0;
	static int osaContinueCnt = 0;

	int l = 0;
	
	@SuppressWarnings("unused")
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
			byte[] frameBytesForSnoring = new byte[frameByteSizeForSnoring];
			
			SleepCheck.checkTerm = 0;
			SleepCheck.checkTermSecond = 0;
			SleepCheck.grindingContinueAmpCnt = 0;
			SleepCheck.grindingContinueAmpOppCnt = 0;
			SleepCheck.grindingRepeatAmpCnt = 0;
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			@SuppressWarnings("unused")
			double recordStartingTIme = 0;
			snoringTermList = new ArrayList<StartEnd>();
			grindingTermList = new ArrayList<StartEnd>();
			osaTermList = new ArrayList<StartEnd>();
			double times = 0.0;
			int i = 0;
		    int[] tmpArray = null;
			boolean isRecording = false;
			boolean soundStartInRecording = false;
			double soundStartInRecordingTimes = 0.0;
			double maxDecibelAvg = 0.0;
			double lowFHDecibelAvg = 0.0;
			int snoringBufferFilledCnt = 0;
			double[] allFHAndDB = null;
			int grindingRepeatOnceAmpCnt = 0;
			int grindingRepeatAmpCnt = 0;
			int grindingContinueAmpCnt = 0;
			int grindingContinueAmpOppCnt = 0;
			double GrindingCheckTermSecond = 0;
			double GrindingCheckStartTermSecond = 0;
			double GrindingCheckStartTermDecibel = 0;
			boolean grindingStart = false;
			boolean grindingContinue = false;
			int grindingRecordingContinueCnt = 0;
			int GRINDING_RECORDING_CONTINUE_CNT = 1;
			
			double chkDBAgainInRecording = 0.0;
			int continueCntInChkTermForGrinding = 0;
			int continueCntInChkTermForGrindingChange = 0;

			int osaCnt = 0;
			int osaRecordingContinueCnt = 0;
			int osaRecordingExit = 0;
			boolean osaStart = false;
			boolean osaContinue = false;
			double osaStartTimes = 0.0;
			try {
				targetStream = new ByteArrayInputStream(audioData);
				System.out.println("분석 시작 "+filePath);
				boolean isTitlePrint = false;
                AnalysisRawData maxARD = null;
                double timesForMaxArd = 0.0;
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					targetStream.close();
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					//최대 주파수, 데시벨, 진폭 가져오기
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
					
					//전체 진폭을 가져온다.
					//전체 진폭에 대한 주파수, 주파수의 갭=hzPerDataPoint
					//전체 진폭에 대한 주파수 리스트 길이=fftSize
					if(snoringBufferFilledCnt < frameByteSizePer) {
						System.arraycopy(frameBytes,0,frameBytesForSnoring,frameBytes.length*snoringBufferFilledCnt,frameBytes.length);
						snoringBufferFilledCnt++;
					}
					if(snoringBufferFilledCnt == frameByteSizePer) {
						snoringBufferFilledCnt = 0;
						short[] tmpBytes = getAmplitudesFromBytesShort(frameBytesForSnoring);
						int bufferSize = frameBytesForSnoring.length/2;
						Radix2FFT fft = new Radix2FFT(bufferSize);
					    double hzPerDataPoint = 44100d / bufferSize;
					    int fftSize = (int) ((44100d / 2) / (44100d / bufferSize))	;
					    tmpArray = new int[fftSize];
				        for (int k = 0; k < fftSize; k ++) {
				        	tmpArray[k] = (int) (k * hzPerDataPoint);
				        }
				        DoubleValues fftData = new DoubleValues();
				        ShortValues shortValues = new ShortValues(tmpBytes);
			            fft.run(shortValues, fftData);
			            allFHAndDB = fftData.getItemsArray();
			            //전체 주파수/데시벨 표시 시작
					    //TODO
						//전체 주파수/데시벨 표시 끝
			            
					}
					i++; //시간 증가
					

		            final String amp = String.valueOf(amplitude + "Amp");
		            final String db = String.valueOf(decibel + "db");
		            final String hz = String.valueOf(frequency + "Hz");
					//소리 임계치로 소리의 발생 여부를 감지한다. 
					//초기화 설정
					SleepCheck.setMaxDB(decibel);
					SleepCheck.setMinDB(decibel);

					// 소리가 발생하면 녹음을 시작하고, 1분이상 소리가 발생하지 않으면 녹음을 하지 않는다.
					//if (SleepCheck.noiseCheckForStart(decibel) >= 30 && isRecording == false
					if (isRecording == false
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times) ) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 시작! ");
						//tmpMinDb = 99999;
						//tmpMaxDb = 0;
						recordStartingTIme = times;
						// baos = new ByteArrayOutputStream();
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 종료! ");
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						System.out.println("녹음파일 길이: "+ calcTime(times - recordStartingTIme));
						System.out.println("tmpMinDb: "+tmpMinDb);
						System.out.println("tmpMaxDb: "+tmpMaxDb);
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true && Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 종료! ");
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						System.out.println("녹음파일 길이: "+ calcTime(times - recordStartingTIme));
						System.out.println("tmpMinDb: "+tmpMinDb);
						System.out.println("tmpMaxDb: "+tmpMaxDb);
						recordStartingTIme = 0;
						isRecording = false;
					}
					if(isRecording==false) {
						continue;
					}
					//이갈이 음파가 매우 짧기 때문에, 코골이의 로직과 분리해야한다. 코골이는 0.16초 단위로 분석, 이갈이는 0.01초로 분석해야함
					//코골이의 음파 길이 및 음파가 아닌 경우의 1초 범위까지 기록 하고 있음으로, 코골이가 아닌 경우에 이갈이인지 체크하도록 한다.
					//이갈이는 1초 이내에 여러번 발생하며, 발생시에 0.02~0.03초의 연속된 짧고 높은 진폭이 발생한다.이 카운트가 1초에 5회 미만인 것만 뽑아낸다. //
					//그렇다면 시간 대비 코골이 횟수를 비례해서 계산하면 된다.
					double chkGrindingDb = SleepCheck.getMinDB();
					if(chkGrindingDb<=-30) {
						chkGrindingDb = SleepCheck.getMinDB()/1.5;
					}else if(chkGrindingDb<=-20) {
						chkGrindingDb = SleepCheck.getMinDB()/1.25;
					}else if(chkGrindingDb<=-10) {
						chkGrindingDb = SleepCheck.getMinDB()/1.1;
					}
				    if(decibel > chkGrindingDb) {
				    	grindingRepeatOnceAmpCnt++;
						//System.out.print(calcTime(times)+"s ");
						//System.out.println(" "+decibel+"vs"+chkGrindingDb+" "+grindingRepeatOnceAmpCnt);
				    }else {
				    	if( grindingRepeatOnceAmpCnt >= continueCntInChkTermForGrinding) {
				    		continueCntInChkTermForGrinding += grindingRepeatOnceAmpCnt;
				    		continueCntInChkTermForGrindingChange++;
				    	}
				    	grindingRepeatOnceAmpCnt = 0;
				    }
					//음파가 발생하는 구간, 
					//음파가 발생하는 구간이란 코골이가 발생해서 코골이 1회의 시작과 끝을 의미한다.
					//음파가 발생하는 구간동안 코골이가 발생했는지를 체크 해야 한다.
					//플래그로 음파가 발생하고 있는지를 관리한다.
					//소리가 발생하고, 0.5초 간격으로 연속되고 있는지 체크한다.
					//연속되지 않는 순간 음파가 끝난 것으로 간주한다.
					//음파가 끝나는 순간 코골이가 발생했는지 체크하고, 코골이가 발생하지 않은 것은 이갈이로 구분한다.
					double chkSnoringDb = SleepCheck.getMinDB();
					if(chkSnoringDb<=-30) {
						chkSnoringDb = SleepCheck.getMinDB()/2;
					}else if(chkSnoringDb<=-20) {
						chkSnoringDb = SleepCheck.getMinDB()/1.75;
					}else if(chkSnoringDb<=-10) {
						chkSnoringDb = SleepCheck.getMinDB()/1.5;
					}
					if(allFHAndDB!=null) {
					    Arrays.stream(allFHAndDB).forEach(e ->{
						    if( e > tmpMaxDb) {
						    	tmpMaxDb = e;
						    }
						    if( e < tmpMinDb) {
						    	tmpMinDb = e;
						    }
					    }
					    );
					    //System.out.println(calcTime(times) + " " + hz + " " + db + " " + amp + " " + decibel + ", 100db: " + tmpMaxDb + "db, max: " + SleepCheck.getMaxDB() + ", min: " + SleepCheck.getMinDB() + " " + SleepCheck.noiseChkSum + " " + SleepCheck.noiseChkCnt);
					//코골이는 임계치를 보정해서 코골이의 음파 여부를 판단한다.
				    if(decibel > chkSnoringDb && tmpMaxDb>40) {
			            if(false) {
						    DecimalFormat df = new DecimalFormat("0.00");
						    //if(i==frameByteSizePer-1) { //분석 시작할 때 주파수 표시
						    if(isTitlePrint==false) { //분석 시작할 때 주파수 표시
						    	isTitlePrint = true;
						    	System.out.print("시간\t\t");
						    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
						    	System.out.println();
						    }
							//시간별로 주파수/데시벨 표시
							System.out.print(calcTime(times) + "s\t" );
							//System.out.print(frequency + "HZ\t" );
							//System.out.print(decibel + "DB\t" );
						    //Arrays.stream(allFHAndDB).forEach(e -> System.out.print(df.format(e) + "\t" ));
							//System.out.print("avg:"+sumU10+"\t");
							System.out.print("db:"+decibel+"\t");
							l = 0;
							//System.out.print(calcforChkSnoringDb(allFHAndDB, 0, 9) + "\t" );
							//System.out.print(calcforChkSnoringDb(allFHAndDB, 10, 11) + "\t" );
							Arrays.stream(allFHAndDB).forEach(e ->{
								l++;
								//최대 최소치를 90으로 잡고, 정규화 하자
								if(Math.abs(e)>90) {
									e = 90;
								}
								//System.out.print(df.format( -(31.5-(Math.abs(e)/70)*31.5 )) + "\t" );
								if(l<100 && decibel > - 31) {
									System.out.print(-(31.5-(Math.abs(e)/90)*31.5 ) + "\t" );
								}
								});
							System.out.println(" ");
			            }
				    	//코골이 음파가 발생했음.
				    	if(soundStartInRecording==false) {
				    		//코골이 분석 중 이갈이 구별 하기위한 카운트 초기화, 이갈이라면 이 카운트가 매우 높아선 안된다.
				    		continueCntInChkTermForGrinding = 0;
				    		continueCntInChkTermForGrindingChange = 0;
				    		//TODO 음파 진행중일 떄의 평균 데시벨을 가지고, 음파로 인정할 소리를 한번더 구별 한다.
				    		chkDBAgainInRecording = decibel;
				    		//녹음 중에 소리가 발생했고 음파 시작은 아닌 상태, 음파 시작 상태로 변환
				    		soundStartInRecording = true;
				    		//코골이 카운트를 초기화(음파 진행 중에 카운트 증가)
				    		soundStartAndSnroingCnt = 0;
		    				//낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
	    					//음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
				    		soundStartAndSnroingOppCnt = 0;
				    		//음파시작상태를 0.3초 간격으로 체크하기 위해 변수 할당(초기화)
				    		//0.3초 이내에 지속적으로 음파가 발생한다면 이후 음파 발생시 아래 변수와 0.3초 이상 차이가 나지 않아야 한다.
				    		soundStartInRecordingTimes = times;
				    		//음파시작시간을 보관하기 위해 기록vo를 생성
				    		StartEnd st = new StartEnd();
				    		st.start = times;
                            st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
				    		snoringTermList.add(st);
				    		//음파가 진행되는 동안 최대 데시벨과 저주파수의 데시벨의 평균을 계산하기 위해 값을 초기화 한다.
				    		//최대 데시벨 값과 저주파수 데시벨 값을 저장한다.(초기화)
				    		//maxDecibelAvg = decibel;
				    		//lowFHDecibelAvg = forChkSnroingDb1;
				    		//lowFHDecibelAvg = allFHAndDB[0];
                            firstDecibelAvg = 0;
                            secondDecibelAvg = 0;
                            snoringDbChkCnt = 0;
				    	}else {
				    		chkDBAgainInRecording = (chkDBAgainInRecording + decibel) /2;
				    		//녹음 중에 소리가 발생했고 음파가 진행 중인 상태
					    	//높은 데시벨 평균과 낮은 주파수 평균을 계산한다.
				    		//double timeGap = times-snoringTermList.get(snoringTermList.size()-1).start;
				    		//System.out.println(calcTime(times)+"s forChkSnroingDb1: "+forChkSnroingDb1+" forChkSnroingDb2: "+forChkSnroingDb2+" firstDecibelAvg: "+firstDecibelAvg+" secondDecibelAvg: "+secondDecibelAvg);
			    			//if(timeGap > 0.3 && timeGap < 0.7){
					    		//maxDecibelAvg = (maxDecibelAvg+decibel)/2;
					    		//lowFHDecibelAvg = (lowFHDecibelAvg+forChkSnroingDb1)/2;
			    				if(firstDecibelAvg == 0 || secondDecibelAvg == 0) {
						    		firstDecibelAvg = calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40);
						    		secondDecibelAvg = calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18);
						    		snoringDbChkCnt = 0;
			    				}else {
			    					//originalFftData와 allFHAndDb는 약간 값이 다르다. 후자의 값은 전자의 값을 -31.5로 보정한 값임
			    					/*
			    					System.out.print(decibel);
			    					System.out.print(" "+chkDBAgainInRecording);
			    					System.out.print(" "+calcforChkSnoringDbNotNomarlize(allFHAndDB, 11, 12));
			    					System.out.print(" "+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 4));
			    					System.out.println(" "+(calcforChkSnoringDbNotNomarlize(allFHAndDB, 11, 12) - calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 4)));
			    					*/
			    					if(Math.floor(decibel) >= Math.floor(chkDBAgainInRecording) && 
			    							calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18)>calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40)) {
			    						//평균으로만 비교하긴 할건데, 평균낼때까지 얼마나 차이가 있었나도 비교해봄.. 값을 쓸 수도 있다.
			    						snoringDbChkCnt++;
			    					}
						    		firstDecibelAvg = (firstDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40))/2;
						    		secondDecibelAvg = (secondDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18))/2;
			    				}
					    		//System.out.println(calcTime(times)+"s firstDecibelAvg: "+firstDecibelAvg+" secondDecibelAvg: "+secondDecibelAvg);
			    			//}
				    		//lowFHDecibelAvg = (lowFHDecibelAvg+allFHAndDB[0])/2;
				    		/*
				    		//0.3초 동안의 평균 데시벨을 비교한다. 
				    		if(times-soundStartInRecordingTimes>0.3) {
				    			//음파 시작상태이나 이전 체크된 시간과 0.3초가 벌어졌음. 
			    				if(Math.abs(maxDecibelAvg-lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) {
			    					//0.3초가 벌어졌다면, 음파 진행된 동안의 최대 데시벨 평균과 평균 데시벨의 차이를 비교한다.
				    				//높은 데시벨 평균과 낮은 주파수 평균의 차가 높은 데시벨의 절반을 넘어가면 코골이 카운트 증가
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//카운트 증가 안하고 통과
			    				}
				    			//0.3초가 지났으면 체크시간을 갱신
				    			soundStartInRecordingTimes = times;
				    		}else {
				    			//0.3초가 아직 안벌어졌으면 체크 시간을 갱신 // 계속 갱신하면 안됨, 0.3초가 안지났다고 계속 기준 시간을 갱신하면 영원히 갱신한다.
					    		//soundStartInRecordingTimes = times;
				    		}
				    		*/
				    	}
				    }else {
				    	//소리가 발생하지 않았으면, 현재 코골이 음파 발생중인지 체크 한다.
				    	if(soundStartInRecording==true) {
                            if(snoringTermList == null || snoringTermList.size()==0){
                                soundStartInRecording = false;
                                continue;
                            }
				    		//음파 진행 중이라면, 지금 체크중인 체크 시작시간이 1초를 넘었는지 체크한다.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>0.16*7){
			    				//음파시작시간과는 1초가 벌어졌다면 , 분석을 중단하고, 이후 코골이 발생 카운트를 체크하여 기록한다.
			    				soundStartInRecording = false;
			    				/*
			    				System.out.print(calcTime(snoringTermList.get(snoringTermList.size()-1).start)+"~");
			    				System.out.print(calcTime(times));
			    				System.out.print(" "+maxDecibelAvg);
			    				System.out.print(" "+lowFHDecibelAvg);
			    				System.out.println(" "+Math.abs(maxDecibelAvg-lowFHDecibelAvg)+ " vs "+ Math.abs(maxDecibelAvg)/2);
			    				*/
			    				//if(Math.abs(lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) {
			    				//차이는 max - low
			    				//double  diffMaxToLow = maxDecibelAvg - lowFHDecibelAvg;
			    				//두번째 데시벨이 더 크게 나타난다.
			    				double  diffMaxToLow = Math.abs(secondDecibelAvg) - Math.abs(firstDecibelAvg);
			    				//차이가 맥시멈 데시벨의 절반 이상인가
			    				//if(lowFHDecibelAvg < maxDecibelAvg &&  diffMaxToLow > maxDecibelAvg*2) {
			    				//System.out.println(calcTime(times)+"s diffMaxToLow: "+diffMaxToLow+" Math.abs(diffMaxToLow): "+Math.abs(diffMaxToLow)+" (31.5-Math.abs(secondDecibelAvg)) / 2: "+(31.5-Math.abs(secondDecibelAvg)) / 2);
			    				/*
			    				System.out.print(calcTime(times)+"s \t"); 
			    				System.out.print(firstDecibelAvg+"\t");
			    				System.out.print(secondDecibelAvg+"\t");
			    				System.out.print(diffMaxToLow+"\t");
			    				System.out.println(snoringDbChkCnt+"\t");
			    				*/
			    				//if(diffMaxToLow < 0 && Math.abs(diffMaxToLow) > (31.5-Math.abs(secondDecibelAvg)) / 2) {
			    				//if(diffMaxToLow < 0 && Math.abs(diffMaxToLow) > ((31.5-Math.abs(secondDecibelAvg))) / 4) {
			    				if(diffMaxToLow > 0 ) {
			    					//1초가 벌어졌다면, 음파 진행된 동안의 최대 데시벨 평균과 평균 데시벨의 차이를 비교한다.
				    				//낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
			    					//음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//진행 카운트 증가 안하고 통과
			    					//-> 진행된 카운트 대신 반대 카운트 증가
			    					soundStartAndSnroingOppCnt++;
			    				}
			    				//1. 5~200 주파수의 평균 데시벨보다 43~80 주파수의 평균 데시벨이 더 커야함
			    				//2. 코골이 긍장 카운트 1 당, 부정카운트가 3보다 크면 안된다.(
			    				
			    				if(soundStartAndSnroingCnt > 0 && soundStartAndSnroingOppCnt<soundStartAndSnroingCnt*3) {
			    					//코골이 카운트가 증가했었고, 코골이 기록vo에 종료 시간을 기록
				    				snoringTermList.get(snoringTermList.size()-1).end = times;
				    				snoringTermList.get(snoringTermList.size()-1).first = firstDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).second = secondDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).chk = snoringDbChkCnt;
				    				snoringTermList.get(snoringTermList.size()-1).positiveCnt = soundStartAndSnroingCnt;
				    				snoringTermList.get(snoringTermList.size()-1).negitiveCnt = soundStartAndSnroingOppCnt;
                                    if(snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList!=null &&
                                            snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList.size() >0){
                                        double tmpTimes1 = snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList.get(
                                                snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList.size()-1
                                        ).getTimes();
                                        tmpTimes1 = Math.floor(tmpTimes1);
                                        double currentTimes1 = Math.floor(times);
                                        if(currentTimes1-1 == tmpTimes1){
                                            snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList.add(maxARD);
                                        }else if(currentTimes1-2 == tmpTimes1){
                                            AnalysisRawData tmpD = new AnalysisRawData(currentTimes1-1, maxARD.getAmplitude(), tmpMaxDb, maxARD.getFrequency());
                                            snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList.add(tmpD);
                                        }
                                    }
			    				}else {
				    				//코골이 카운트가 증가한 적이 없었다. 
			    					//코골이 기록 vo 대신 이갈이 기록 vo로 넣는다.
			    					//이갈이는 원본 로직대로 한다.
			    					/*
			    					System.out.print(calcTime(times-snoringTermList.get(snoringTermList.size()-1).start));
			    					//System.out.print(" "+continueCntInChkTermForGrinding/Math.round(times-snoringTermList.get(snoringTermList.size()-1).start));
			    					//System.out.print(" "+continueCntInChkTermForGrindingChange);
			    					//System.out.print(" "+chkDBAgainInRecording);
			    					System.out.print(" "+calcforChkSnoringDb(new double[] {firstDecibelAvg}, 0, 0));
			    					System.out.print(" "+firstDecibelAvg);
			    					System.out.print(" "+tmpMinDb);
			    					System.out.println(" "+tmpMaxDb);
			    					*/
			    					//System.out.println(" "+continueCntInChkTermForGrinding);
			    					
			    					//if(continueCntInChkTermForGrinding>1 && continueCntInChkTermForGrinding < 8) {
			    					//if(true) {
			    					if(continueCntInChkTermForGrindingChange > 0 && continueCntInChkTermForGrinding> 0 && 
			    							firstDecibelAvg > tmpMaxDb/2 &&
			    							Math.abs(firstDecibelAvg - secondDecibelAvg)<5 && 
			    							//grindingChange가 3이상일 때는, / 가 10보다 크고 12보다 작아야함
			    							((continueCntInChkTermForGrindingChange >= 3 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange >= 10 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange <= 12)
			    							||
			    							//2이하일 때는, / 가 9보다 작아야함
			    							(continueCntInChkTermForGrindingChange <=2 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange >= 6 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange <= 9)
			    							)) {
							    		StartEnd st = new StartEnd();
							    		st.start = snoringTermList.get(snoringTermList.size()-1).start;
                                        st.AnalysisRawDataList = snoringTermList.get(snoringTermList.size()-1).AnalysisRawDataList;
							    		st.end = times;
							    		st.second = secondDecibelAvg;
							    		st.first = firstDecibelAvg;
							    		st.chk = secondDecibelAvg-firstDecibelAvg;
							    		st.positiveCnt = continueCntInChkTermForGrinding;
							    		st.negitiveCnt = continueCntInChkTermForGrindingChange;
                                        if(st.AnalysisRawDataList!=null &&
                                                st.AnalysisRawDataList.size() >0){
                                            double tmpTimes1 = st.AnalysisRawDataList.get(st.AnalysisRawDataList.size()-1).getTimes();
                                            tmpTimes1 = Math.floor(tmpTimes1);
                                            double currentTimes1 = Math.floor(times);
                                            if(currentTimes1-1 == tmpTimes1){
                                                st.AnalysisRawDataList.add(maxARD);
                                            }else if(currentTimes1-2 == tmpTimes1){
                                                AnalysisRawData tmpD = new AnalysisRawData(currentTimes1-1, maxARD.getAmplitude(), tmpMaxDb, maxARD.getFrequency());
                                                st.AnalysisRawDataList.add(tmpD);
                                            }
                                        }
							    		snoringTermList.remove(snoringTermList.size()-1);
							    		grindingTermList.add(st);
			    					}else {
							    		snoringTermList.remove(snoringTermList.size()-1);
			    					}
			    				}
			    			}else {
			    				//음파 진행 중이고, 소리가 발생하지 않았으나 아직 1초가 지나지 않았다.
		    					//진행 카운트 증가 안하고 통과
		    					//-> 진행된 카운트 대신 반대 카운트 증가
		    					//음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
			    				soundStartAndSnroingOppCnt++;
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//소리가 발생하지 않았고, 음파가 진행 중인 상태가 아니다. 
			    	
					// baos.write(frameBytes);

				}
			    	allFHAndDB = null;
				    }else {
				    }
					
					if (decibel > SleepCheck.getMinDB()*0.45) {
						//소리가 발생했고, 분석 시작 변수 값이 true 인 경우 종료한다.
						if(isOSATermTimeOccur) {
							//0.1초 동안 소리가 70% 이상 발생한 경우 소리가 발생한 것으로 본다.
								
							if(isOSATermCnt+isBreathTermCnt>90 && isOSATermCnt > 20 && isBreathTermCnt > 70) {
								//오차범위를 둔다. 0.5초 동안 연속으로 소리가 발생해야 한다.
								if(osaContinueCnt > 4) {
									isOSATermTimeOccur = false;
									isBreathTermCnt = 0;
									isBreathTerm = true;
									osaTermList.get(osaTermList.size()-1).end=times;
									osaTermList.get(osaTermList.size()-1).chk=0;
									osaContinueCnt = 0;
								}else {
									if(osaContinueCnt!=0) {
										osaContinueCnt ++;	
									}else {
										osaContinueCnt = 1;
									}
								}
							}
						}else {
							
						}
						isBreathTermCnt++;
					}else {
						//무호흡을 측정하기 위한 분석 시작 변수 초기화
						//코골이가 발생하고 5초가 안지났어야 함.
						if(snoringTermList.size() > 0 
								&& snoringTermList.get(snoringTermList.size()-1).end != 0
								&& times - snoringTermList.get(snoringTermList.size()-1).end > 0 
								&& times - snoringTermList.get(snoringTermList.size()-1).end < 5 
								&& !isOSATermTimeOccur) {
							//0.1초 동안 묵음이 70% 이상 발생한 경우 소리가 발생한 것으로 본다.
							if(isOSATermCnt+isBreathTermCnt>90 && isBreathTermCnt > 70 && isBreathTermCnt > 20) {
							osaContinueCnt = 0;
							OSAcurTermTime = times;
							isOSATermTimeOccur = true;
							isBreathTerm = false;
								osaTermList.add(new StartEnd());
								osaTermList.get(osaTermList.size()-1).start=times;
								osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList = new ArrayList<AnalysisRawData>();
							}
						}
						isOSATermCnt++;
					}
					//무호흡 발생후 3분동안 종료되지 않는다면 취소
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end==0 && times-osaTermList.get(osaTermList.size()-1).start > 180) {
						isOSATermTimeOccur = false;
						isOSATermCnt = 0;
						isBreathTerm = false;
						isBreathTermCnt = 0;
						OSAcurTermTime = 0.0;
						osaTermList.remove(osaTermList.size()-1);
					}

					//무호흡 종료 후 녹음된 시간이 너무 짧으면 삭제한다.
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
						if(osaTermList.get(osaTermList.size()-1).end - osaTermList.get(osaTermList.size()-1).start < 5 ){
							osaTermList.remove(osaTermList.size()-1);
	                    }
					}
					
					//무호흡 종료 후 5초 이내에 코골이가 발생하지 않으면 취소
					//무호흡 종료 후 5초 동안 코골이 발생여부를 체크한다.
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
						if(snoringTermList.size()>0 && isRecording == true){
							//코골이가 녹음 중이게 되었을 때, 체크 플래그를 업데이트
	                        if(snoringTermList.get(snoringTermList.size() - 1).end==0){
	                        	osaTermList.get(osaTermList.size()-1).chk = 1;
	                        }
	                    }
					}
					//무호흡 종료 후 5초가 넘은 경우 플래그를 체크해서 코골이를 삭제한다.
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end > 5) {
						if(osaTermList.get(osaTermList.size()-1).chk==0) {
							osaTermList.remove(osaTermList.size()-1);
						}
					}
					
                    
                    if(maxARD!=null){
                        if(decibel > maxARD.getDecibel()){
                            maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
                        }
                    }else{
                        maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
                        timesForMaxArd = Math.floor(times);
                    }
                    if(Math.floor(times) > timesForMaxArd){
                        //코골이 기록용 vo 생성
                        if(maxARD.getDecibel()==0){
                            maxARD.setDecibel(tmpMaxDb);
                        }
                        //System.out.println(calcTime(times)+" "+snoringTermList.size()+" "+SleepCheck.isOSATerm+" "+SleepCheck.isBreathTerm+" "+SleepCheck.isOSAAnsStart);
                        if(snoringTermList.size()>0 && isRecording == true){
                            if(snoringTermList.get(snoringTermList.size() - 1).end!=0){
                                if(snoringTermList.get(snoringTermList.size() - 1).end > times){
                                    snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(maxARD);
                                }
                            }else {
                                snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(maxARD);
                            }
                        }
                        if(osaTermList.size()>0 && isRecording == true && isOSATermTimeOccur){
                            if(osaTermList.get(osaTermList.size() - 1).end!=0){
                                if(osaTermList.get(osaTermList.size() - 1).end > times){
                                    osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(maxARD);
                                }
                            }else {
                                osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(maxARD);
                            }
                        }
                        maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
                        timesForMaxArd = Math.floor(times);
                        
					tmpMaxDb = 0;
                    tmpMinDb = 99999;
                    }
				}
				System.out.println("코골이 시작");
				for ( int s = 0 ; s < snoringTermList.size() ; s ++) {
					if(s>0) {
						StartEnd se = snoringTermList.get(s);
						StartEnd bse = snoringTermList.get(s-1);
						double curStartTime = se.start;
						double beforeEndTime = bse.end;
						if(curStartTime - beforeEndTime <= 1) {
							bse.end = se.end;
							bse.negitiveCnt += se.negitiveCnt;
							bse.positiveCnt += se.positiveCnt;
							bse.first = (bse.first+se.first);
							bse.second = (bse.second+se.second);
							bse.chk += se.chk;
							bse.AnalysisRawDataList.addAll(se.AnalysisRawDataList);
							snoringTermList.remove(se);
							s--;
						}
					}
				}
				for (StartEnd se : snoringTermList) {
					System.out.print(se.getTerm());
					System.out.println();
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("코골이 끝");
				System.out.println("이갈이 시작");
				for ( int s = 0 ; s < grindingTermList.size() ; s ++) {
					if(s>0) {
						StartEnd se = grindingTermList.get(s);
						StartEnd bse = grindingTermList.get(s-1);
						double curStartTime = se.start;
						double beforeEndTime = bse.end;
						if(curStartTime - beforeEndTime <= 1) {
							bse.end = se.end;
							bse.negitiveCnt += se.negitiveCnt;
							bse.positiveCnt += se.positiveCnt;
							bse.first = (bse.first+se.first);
							bse.second = (bse.second+se.second);
							bse.chk += se.chk;
							bse.AnalysisRawDataList.addAll(se.AnalysisRawDataList);
							grindingTermList.remove(se);
							s--;
						}
					}
				}
				for (StartEnd se : grindingTermList) {
					System.out.print(se.getTerm());
					System.out.println();
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("이갈이 끝");
				System.out.println("무호흡 시작");
				for (StartEnd se : osaTermList) {
					System.out.print(se.getTerm());
					System.out.println();
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("무호흡 끝");
				System.out.println("분석 종료");
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

	private double calcforChkSnoringDbNotNomarlize(double[] allFHAndDB, int startN, int endN) {
		double forChkSnroingDb = 0;
		for (int i = 0; i <= endN - startN; i++) {
			forChkSnroingDb += allFHAndDB[startN+i];
		}
		forChkSnroingDb = Math.abs((forChkSnroingDb) / (endN - startN + 1));
		return forChkSnroingDb;
	}
	private double calcforChkSnoringDb(double[] allFHAndDB, int startN, int endN) {
		double forChkSnroingDb = 0;
		for (int i = 0; i <= endN - startN; i++) {
	    	//소리 발생체크하는 fft로직과 전체 주파수 데시벨을 가져오는 fft 로직이 달라서, 후자의 fft 데시벨 수치를 -31.5에 맞게 보정한다.
	    	//샘플 fft 예제 및 실제 측정 결과 -75~87까지의 수치가 발생하는 것까지 확인함.
	    	//이를 평준화 하기 위해 90을 임계치로 -31.5 db로 변환한다.
			if(Math.abs(allFHAndDB[startN+i])>90) {
				allFHAndDB[startN+i] = 90;
			}
			forChkSnroingDb += allFHAndDB[startN+i];
		}
		forChkSnroingDb = -(31.5 - (Math.abs((forChkSnroingDb) / (endN - startN + 1)) / 90) * 31.5);
		return forChkSnroingDb;
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
    
	private String calcTime(double times) {
        int seconds;
        int minutes ;
        int hours;
        seconds =  (int)times;
        hours = seconds / 3600;
        minutes = (seconds%3600)/60;
        double seconds_output = (times% 3600)%60;
        seconds_output = Math.floor(seconds_output*1000)/1000;
        return hours  + ":" + minutes + ":" + seconds_output +""; 
	}

}

class StartEnd {
	public int negitiveCnt;
	public int positiveCnt;
	double start;
	double end;
	List<AnalysisRawData> AnalysisRawDataList;
	double second;
	double first;
	double chk;

	public String getTerm() {
		return 
				String.format("%.2f", start) 
				+ "~" + String.format("%.2f", end)
				+ " second: " + String.format("%.2f", second)
				+ " first: " + String.format("%.2f", first)
				+ " chk: " + String.format("%.2f", chk)
		+ " positiveCnt: " + positiveCnt
		+ " negitiveCnt: " + negitiveCnt;
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