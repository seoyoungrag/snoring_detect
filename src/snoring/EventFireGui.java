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
	int frameByteSize = 1024;
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	static List<StartEnd> snoringTermList;
	static List<StartEnd> grindingTermList;
	static List<StartEnd> osaTermList;

	private AudioCalculator audioCalculator;

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
		            fftData.setSize(fftSize);
		            double[] allFHAndDB = fftData.getItemsArray();
		            //전체 주파수/데시벨 표시 시작
		            /*
		            if(isRecording==false) {
					    DecimalFormat df = new DecimalFormat("0.00");
					    if(i==0) { //분석 시작할 때 주파수 표시
					    	System.out.print("시간\t\t");
					    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
					    	System.out.println();
					    }
						//시간별로 주파수/데시벨 표시
						System.out.print(calcTime(times) + "s\t" );
					    Arrays.stream(allFHAndDB).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
		            }
		            */
					//전체 주파수/데시벨 표시 끝
					i++; //시간 증가
					
					//최대 주파수, 데시벨, 진폭 가져오기
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
					
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
						recordStartingTIme = times;
						// baos = new ByteArrayOutputStream();
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 종료! ");
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						System.out.println("녹음파일 길이: "+ calcTime(times - recordStartingTIme));
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true && Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!녹음 종료! ");
						System.out.println("=====녹음중 분석 종료, 분석정보 시작=====");
						System.out.println("녹음파일 길이: "+ calcTime(times - recordStartingTIme));
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
				    	//코골이 음파가 발생했음.
				    	if(soundStartInRecording==false) {
			    			//System.out.println("녹음 중에 소리가 발생했고 음파 시작은 아닌 상태였다면 음파 시작 상태로 변환");
				    		//녹음 중에 소리가 발생했고 음파 시작은 아닌 상태였다면 음파 시작 상태로 변환
				    		soundStartInRecording = true;
				    		//코골이 카운트를 초기화, 음파 진행 중에 카운트는 증가할 것이다.
				    		soundStartAndSnroingCnt = 0;
				    		//음파시작상태를 0.5초 간격으로 체크하기 위해 변수 할당
				    		soundStartInRecordingTimes = times;
				    		//음파시작시간을 보관하기 위해 기록vo를 생성
				    		StartEnd st = new StartEnd();
				    		st.start = times;
				    		snoringTermList.add(st);
				    		//최대 데시벨 값을 저장한다.(초기화)
				    		maxDecibelAvg = decibel;
				    		lowFHDecibelAvg = -(31.5-allFHAndDB[0]/255*31.5);
				    	}else {
				    		//녹음 중에 소리가 발생했고 음파 시작 상태
				    		if(times-soundStartInRecordingTimes>0.3) {
				    			//음파 시작상태이나 이전 체크된 시간과 0.5초가 벌어지는지 확인, 0.5초마다 평균 치를 체크한다.
			    				if(Math.abs(maxDecibelAvg-lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) { 
				    				//높은 데시벨 평균과 낮은 주파수 평균의 차가 높은 데시벨의 절반을 넘어가면 코골이 카운트 증가
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//통과
			    				}
				    			//0.3초가 지났으면 체크시간을 갱신
				    			soundStartInRecordingTimes = times;
				    		}else {
				    			//0.5초가 아직 안벌어졌으면 체크 시간을 갱신 // 계속 갱신하면 안됨
					    		//soundStartInRecordingTimes = times;
				    		}
				    	}
				    	//소리 발생했으면 높은 데시벨 평균과 낮은 주파수 평균을 구한다.
			    		maxDecibelAvg = (maxDecibelAvg+decibel)/2;
			    		lowFHDecibelAvg = (lowFHDecibelAvg+-(31.5-allFHAndDB[0]/255*31.5))/2;
				    }else {
				    	//소리가 발생하지 않았으면, 현재 코골이 음파 발생중인지 체크 한다.
				    	if(soundStartInRecording==true) {
				    		//코골이 발생중이라면, 지금 체크중인 체크 시작시간이 1초를 넘었는지 체크한다.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>1){
			    				//음파시작시간과는 1초가 벌어졌다면 
			    				soundStartInRecording = false;
			    				/*
			    				System.out.print(calcTime(snoringTermList.get(snoringTermList.size()-1).start)+"~");
			    				System.out.print(calcTime(times));
			    				System.out.print(" "+maxDecibelAvg);
			    				System.out.print(" "+lowFHDecibelAvg);
			    				System.out.println(" "+Math.abs(maxDecibelAvg-lowFHDecibelAvg)+ " vs "+ Math.abs(maxDecibelAvg)/2);
			    				*/
			    				if(soundStartAndSnroingCnt > 0) {
			    					//기록vo에 종료 시간을 기록
					    			//System.out.println("높은 데시벨 평균과 낮은 주파수 평균의 차가 높은 데시벨의 절반을 넘어가면 기록vo에 종료 시간을 기록");
				    				snoringTermList.get(snoringTermList.size()-1).end = times;
			    				}else {
				    				//아니면 기록 취소
			    					//기록 취소 대신 이갈이로 넣는다.
					    			//System.out.println("높은 데시벨 평균과 낮은 주파수 평균의 차가 높은 데시벨의 절반을 넘어가면 기록vo에 종료 시간을 기록 아니면 기록 취소");
				    				//snoringTermList.remove(snoringTermList.size()-1);
						    		StartEnd st = new StartEnd();
						    		st.start = snoringTermList.get(snoringTermList.size()-1).start;
						    		st.end = times;
						    		snoringTermList.remove(snoringTermList.size()-1);
						    		grindingTermList.add(st);
			    				}
			    			}else {
				    			//System.out.println("0.5초가 벌어졌으나 음파시작시간과도 0.5초가 벌어졌다면 기록vo 삭제");
			    				//0.5초가 벌어졌으나 음파시작시간과도 0.5초가 벌어졌다면 기록vo 삭제
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//음파에 해당하는 소리가 발생하지 않았다. 
				    }
					// baos.write(frameBytes);

				}
				System.out.println("분석 종료");
				for (StartEnd se : snoringTermList) {
					System.out.print(se.getTerm());
					System.out.println(se.printAnalysisRawDataList());
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
	double start;
	double end;
	List<AnalysisRawData> AnalysisRawDataList;

	public String getTerm() {
		return String.format("%.2f", start) + "~" + String.format("%.2f", end);
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