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

public class EventFireGui20190607 {

	byte[] audioData;
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;
	double tmpMinDb = 99999;
	double tmpMaxDb = 0;

	@SuppressWarnings("unused")
	public EventFireGui20190607(String filePath) {
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
		    int[] tmpArray;
			boolean isRecording = false;
			boolean soundStartInRecording = false;
			double soundStartInRecordingTimes = 0.0;
			int soundStartAndSnroingCnt = 0;
			double maxDecibelAvg = 0.0;
			double lowFHDecibelAvg = 0.0;
			double firstDecibelAvg = 0.0;
			double secondDecibelAvg = 0.0;
			try {
				targetStream = new ByteArrayInputStream(audioData);
				System.out.println("분석 시작 "+filePath);
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					targetStream.close();
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					//전체 진폭을 가져온다.
					//전체 진폭에 대한 주파수, 주파수의 갭=hzPerDataPoint
					//전체 진폭에 대한 주파수 리스트 길이=fftSize
					short[] tmpBytes = getAmplitudesFromBytesShort(frameBytes);
					int bufferSize = frameBytes.length/2;
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
		            double[] allFHAndDB = fftData.getItemsArray();
		            //전체 주파수/데시벨 표시 시작
		            tmpMaxDb = 0;
		            tmpMinDb = 99999;
				    Arrays.stream(allFHAndDB).forEach(e ->{
					    if( e > tmpMaxDb) {
					    	tmpMaxDb = e;
					    }
					    if( e < tmpMinDb) {
					    	tmpMinDb = e;
					    }
				    }
				    );

					//최대 주파수, 데시벨, 진폭 가져오기
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
		            if(false) {
					    DecimalFormat df = new DecimalFormat("0.00");
					    if(i==0) { //분석 시작할 때 주파수 표시
					    	System.out.print("시간\t\t");
					    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
					    	System.out.println();
					    }
						//시간별로 주파수/데시벨 표시
						System.out.print(calcTime(times) + "s\t" );
						//System.out.print(frequency + "HZ\t" );
						//System.out.print(decibel + "DB\t" );
					    //Arrays.stream(allFHAndDB).forEach(e -> System.out.print(df.format(e) + "\t" ));
						Arrays.stream(allFHAndDB).forEach(e ->{
							//최대 최소치를 70으로 잡고, 정규화 하자
							if(Math.abs(e)>70) {
								e = 70;
							}
							//System.out.print(df.format( -(31.5-(Math.abs(e)/70)*31.5 )) + "\t" );
							System.out.print(-(31.5-(Math.abs(e)/70)*31.5 ) + "\t" );
							});
					    
						System.out.println(" ");
		            }
					//전체 주파수/데시벨 표시 끝
					i++; //시간 증가
					
					
					//소리 임계치로 소리의 발생 여부를 감지한다. 
					//초기화 설정
					SleepCheck.setMaxDB(decibel);
					SleepCheck.setMinDB(decibel);

					// 소리가 발생하면 녹음을 시작하고, 1분이상 소리가 발생하지 않으면 녹음을 하지 않는다.
					if (SleepCheck.noiseCheckForStart(decibel) >= 30 && isRecording == false
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
						chkSnoringDb = SleepCheck.getMinDB()/0.75;
					}else if(chkSnoringDb<=-10) {
						//chkSnoringDb = chkSnoringDb;
					}
					//코골이는 임계치를 보정해서 코골이의 음파 여부를 판단한다.
				    if(decibel > chkSnoringDb) {
				    	//소리 발생체크하는 fft로직과 전체 주파수 데시벨을 가져오는 fft 로직이 달라서, 후자의 fft 데시벨 수치를 -31.5에 맞게 보정한다.
				    	//샘플 fft 예제 및 실제 측정 결과 -75~87까지의 수치가 발생하는 것까지 확인함.
				    	//이를 평준화 하기 위해 70을 임계치로 -31.5 db로 변환한다.
						if(Math.abs(allFHAndDB[0])>70) {
							allFHAndDB[0] = 70;
						}
						if(Math.abs(allFHAndDB[1])>70) {
							allFHAndDB[1] = 70;
						}
						//최대 최소치를 70으로 잡고, 정규화 하자
						double forChkSnroingDb1 = -(31.5-(Math.abs((allFHAndDB[0]+allFHAndDB[1])/2)/70)*31.5 ); 
						double forChkSnroingDb2 = -(31.5-(Math.abs(allFHAndDB[2])/70)*31.5 ); 
				    	//코골이 음파가 발생했음.
				    	if(soundStartInRecording==false) {
				    		//녹음 중에 소리가 발생했고 음파 시작은 아닌 상태, 음파 시작 상태로 변환
				    		soundStartInRecording = true;
				    		//코골이 카운트를 초기화(음파 진행 중에 카운트 증가)
				    		soundStartAndSnroingCnt = 0;
				    		//음파시작상태를 0.3초 간격으로 체크하기 위해 변수 할당(초기화)
				    		//0.3초 이내에 지속적으로 음파가 발생한다면 이후 음파 발생시 아래 변수와 0.3초 이상 차이가 나지 않아야 한다.
				    		soundStartInRecordingTimes = times;
				    		//음파시작시간을 보관하기 위해 기록vo를 생성
				    		StartEnd st = new StartEnd();
				    		st.start = times;
				    		snoringTermList.add(st);
				    		//음파가 진행되는 동안 최대 데시벨과 저주파수의 데시벨의 평균을 계산하기 위해 값을 초기화 한다.
				    		//최대 데시벨 값과 저주파수 데시벨 값을 저장한다.(초기화)
				    		//maxDecibelAvg = decibel;
				    		//lowFHDecibelAvg = forChkSnroingDb1;
				    		firstDecibelAvg = 0;
				    		secondDecibelAvg = 0;
				    		//lowFHDecibelAvg = allFHAndDB[0];
				    	}else {
				    		//녹음 중에 소리가 발생했고 음파가 진행 중인 상태
					    	//높은 데시벨 평균과 낮은 주파수 평균을 계산한다.
				    		double timeGap = times-snoringTermList.get(snoringTermList.size()-1).start;
				    		//System.out.println(calcTime(times)+"s forChkSnroingDb1: "+forChkSnroingDb1+" forChkSnroingDb2: "+forChkSnroingDb2+" firstDecibelAvg: "+firstDecibelAvg+" secondDecibelAvg: "+secondDecibelAvg);
			    			if(timeGap > 0.2 && timeGap < 0.8){
					    		//maxDecibelAvg = (maxDecibelAvg+decibel)/2;
					    		//lowFHDecibelAvg = (lowFHDecibelAvg+forChkSnroingDb1)/2;
			    				if(firstDecibelAvg == 0 || secondDecibelAvg == 0) {
						    		firstDecibelAvg = forChkSnroingDb1;
						    		secondDecibelAvg = forChkSnroingDb2;
			    				}else {
						    		firstDecibelAvg = (firstDecibelAvg+forChkSnroingDb1)/2;
						    		secondDecibelAvg = (secondDecibelAvg+forChkSnroingDb2)/2;
			    				}
					    		//System.out.println(calcTime(times)+"s firstDecibelAvg: "+firstDecibelAvg+" secondDecibelAvg: "+secondDecibelAvg);
			    			}
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
				    		//음파 진행 중이라면, 지금 체크중인 체크 시작시간이 1초를 넘었는지 체크한다.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>1){
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
			    				//if(diffMaxToLow < 0 && Math.abs(diffMaxToLow) > (31.5-Math.abs(secondDecibelAvg)) / 2) {
			    				if(diffMaxToLow < 0 ) {
			    					//1초가 벌어졌다면, 음파 진행된 동안의 최대 데시벨 평균과 평균 데시벨의 차이를 비교한다.
				    				//낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//카운트 증가 안하고 통과
			    				}
			    				if(soundStartAndSnroingCnt > 0) {
			    					//코골이 카운트가 증가했었고, 코골이 기록vo에 종료 시간을 기록
				    				snoringTermList.get(snoringTermList.size()-1).end = times;
				    				snoringTermList.get(snoringTermList.size()-1).low = firstDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).max = secondDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).chk = chkSnoringDb;
			    				}else {
				    				//코골이 카운트가 증가한 적이 없었다. 
			    					//코골이 기록 vo 대신 이갈이 기록 vo로 넣는다.
						    		StartEnd st = new StartEnd();
						    		st.start = snoringTermList.get(snoringTermList.size()-1).start;
						    		st.end = times;
						    		st.max = secondDecibelAvg;
						    		st.low = firstDecibelAvg;
						    		st.chk = chkSnoringDb;
						    		snoringTermList.remove(snoringTermList.size()-1);
						    		grindingTermList.add(st);
			    				}
			    			}else {
			    				//음파 진행 중이고, 소리가 발생하지 않았으나 아직 1초가 지나지 않았다. 
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//소리가 발생하지 않았고, 음파가 진행 중인 상태가 아니다. 
				    }
					// baos.write(frameBytes);

				}

				System.out.println("코골이 시작");
				for (StartEnd se : snoringTermList) {
					System.out.print(se.getTerm());
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("코골이 끝");
				System.out.println("이갈이 시작");
				for (StartEnd se : grindingTermList) {
					System.out.print(se.getTerm());
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("이갈이 끝");
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

