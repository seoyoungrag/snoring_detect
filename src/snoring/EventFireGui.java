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
				System.out.println("�м� ���� "+filePath);
				boolean isTitlePrint = false;
                AnalysisRawData maxARD = null;
                double timesForMaxArd = 0.0;
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					targetStream.close();
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					//�ִ� ���ļ�, ���ú�, ���� ��������
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
					
					//��ü ������ �����´�.
					//��ü ������ ���� ���ļ�, ���ļ��� ��=hzPerDataPoint
					//��ü ������ ���� ���ļ� ����Ʈ ����=fftSize
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
			            //��ü ���ļ�/���ú� ǥ�� ����
					    //TODO
						//��ü ���ļ�/���ú� ǥ�� ��
			            
					}
					i++; //�ð� ����
					

		            final String amp = String.valueOf(amplitude + "Amp");
		            final String db = String.valueOf(decibel + "db");
		            final String hz = String.valueOf(frequency + "Hz");
					//�Ҹ� �Ӱ�ġ�� �Ҹ��� �߻� ���θ� �����Ѵ�. 
					//�ʱ�ȭ ����
					SleepCheck.setMaxDB(decibel);
					SleepCheck.setMinDB(decibel);

					// �Ҹ��� �߻��ϸ� ������ �����ϰ�, 1���̻� �Ҹ��� �߻����� ������ ������ ���� �ʴ´�.
					//if (SleepCheck.noiseCheckForStart(decibel) >= 30 && isRecording == false
					if (isRecording == false
							&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times) ) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						//tmpMinDb = 99999;
						//tmpMaxDb = 0;
						recordStartingTIme = times;
						// baos = new ByteArrayOutputStream();
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println("=====������ �м� ����, �м����� ����=====");
						System.out.println("�������� ����: "+ calcTime(times - recordStartingTIme));
						System.out.println("tmpMinDb: "+tmpMinDb);
						System.out.println("tmpMaxDb: "+tmpMaxDb);
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true && Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println("=====������ �м� ����, �м����� ����=====");
						System.out.println("�������� ����: "+ calcTime(times - recordStartingTIme));
						System.out.println("tmpMinDb: "+tmpMinDb);
						System.out.println("tmpMaxDb: "+tmpMaxDb);
						recordStartingTIme = 0;
						isRecording = false;
					}
					if(isRecording==false) {
						continue;
					}
					//�̰��� ���İ� �ſ� ª�� ������, �ڰ����� ������ �и��ؾ��Ѵ�. �ڰ��̴� 0.16�� ������ �м�, �̰��̴� 0.01�ʷ� �м��ؾ���
					//�ڰ����� ���� ���� �� ���İ� �ƴ� ����� 1�� �������� ��� �ϰ� ��������, �ڰ��̰� �ƴ� ��쿡 �̰������� üũ�ϵ��� �Ѵ�.
					//�̰��̴� 1�� �̳��� ������ �߻��ϸ�, �߻��ÿ� 0.02~0.03���� ���ӵ� ª�� ���� ������ �߻��Ѵ�.�� ī��Ʈ�� 1�ʿ� 5ȸ �̸��� �͸� �̾Ƴ���. //
					//�׷��ٸ� �ð� ��� �ڰ��� Ƚ���� ����ؼ� ����ϸ� �ȴ�.
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
					//���İ� �߻��ϴ� ����, 
					//���İ� �߻��ϴ� �����̶� �ڰ��̰� �߻��ؼ� �ڰ��� 1ȸ�� ���۰� ���� �ǹ��Ѵ�.
					//���İ� �߻��ϴ� �������� �ڰ��̰� �߻��ߴ����� üũ �ؾ� �Ѵ�.
					//�÷��׷� ���İ� �߻��ϰ� �ִ����� �����Ѵ�.
					//�Ҹ��� �߻��ϰ�, 0.5�� �������� ���ӵǰ� �ִ��� üũ�Ѵ�.
					//���ӵ��� �ʴ� ���� ���İ� ���� ������ �����Ѵ�.
					//���İ� ������ ���� �ڰ��̰� �߻��ߴ��� üũ�ϰ�, �ڰ��̰� �߻����� ���� ���� �̰��̷� �����Ѵ�.
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
					//�ڰ��̴� �Ӱ�ġ�� �����ؼ� �ڰ����� ���� ���θ� �Ǵ��Ѵ�.
				    if(decibel > chkSnoringDb && tmpMaxDb>40) {
			            if(false) {
						    DecimalFormat df = new DecimalFormat("0.00");
						    //if(i==frameByteSizePer-1) { //�м� ������ �� ���ļ� ǥ��
						    if(isTitlePrint==false) { //�м� ������ �� ���ļ� ǥ��
						    	isTitlePrint = true;
						    	System.out.print("�ð�\t\t");
						    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
						    	System.out.println();
						    }
							//�ð����� ���ļ�/���ú� ǥ��
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
								//�ִ� �ּ�ġ�� 90���� ���, ����ȭ ����
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
				    	//�ڰ��� ���İ� �߻�����.
				    	if(soundStartInRecording==false) {
				    		//�ڰ��� �м� �� �̰��� ���� �ϱ����� ī��Ʈ �ʱ�ȭ, �̰��̶�� �� ī��Ʈ�� �ſ� ���Ƽ� �ȵȴ�.
				    		continueCntInChkTermForGrinding = 0;
				    		continueCntInChkTermForGrindingChange = 0;
				    		//TODO ���� �������� ���� ��� ���ú��� ������, ���ķ� ������ �Ҹ��� �ѹ��� ���� �Ѵ�.
				    		chkDBAgainInRecording = decibel;
				    		//���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ����, ���� ���� ���·� ��ȯ
				    		soundStartInRecording = true;
				    		//�ڰ��� ī��Ʈ�� �ʱ�ȭ(���� ���� �߿� ī��Ʈ ����)
				    		soundStartAndSnroingCnt = 0;
		    				//���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
	    					//���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
				    		soundStartAndSnroingOppCnt = 0;
				    		//���Ľ��ۻ��¸� 0.3�� �������� üũ�ϱ� ���� ���� �Ҵ�(�ʱ�ȭ)
				    		//0.3�� �̳��� ���������� ���İ� �߻��Ѵٸ� ���� ���� �߻��� �Ʒ� ������ 0.3�� �̻� ���̰� ���� �ʾƾ� �Ѵ�.
				    		soundStartInRecordingTimes = times;
				    		//���Ľ��۽ð��� �����ϱ� ���� ���vo�� ����
				    		StartEnd st = new StartEnd();
				    		st.start = times;
                            st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
				    		snoringTermList.add(st);
				    		//���İ� ����Ǵ� ���� �ִ� ���ú��� �����ļ��� ���ú��� ����� ����ϱ� ���� ���� �ʱ�ȭ �Ѵ�.
				    		//�ִ� ���ú� ���� �����ļ� ���ú� ���� �����Ѵ�.(�ʱ�ȭ)
				    		//maxDecibelAvg = decibel;
				    		//lowFHDecibelAvg = forChkSnroingDb1;
				    		//lowFHDecibelAvg = allFHAndDB[0];
                            firstDecibelAvg = 0;
                            secondDecibelAvg = 0;
                            snoringDbChkCnt = 0;
				    	}else {
				    		chkDBAgainInRecording = (chkDBAgainInRecording + decibel) /2;
				    		//���� �߿� �Ҹ��� �߻��߰� ���İ� ���� ���� ����
					    	//���� ���ú� ��հ� ���� ���ļ� ����� ����Ѵ�.
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
			    					//originalFftData�� allFHAndDb�� �ణ ���� �ٸ���. ������ ���� ������ ���� -31.5�� ������ ����
			    					/*
			    					System.out.print(decibel);
			    					System.out.print(" "+chkDBAgainInRecording);
			    					System.out.print(" "+calcforChkSnoringDbNotNomarlize(allFHAndDB, 11, 12));
			    					System.out.print(" "+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 4));
			    					System.out.println(" "+(calcforChkSnoringDbNotNomarlize(allFHAndDB, 11, 12) - calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 4)));
			    					*/
			    					if(Math.floor(decibel) >= Math.floor(chkDBAgainInRecording) && 
			    							calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18)>calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40)) {
			    						//������θ� ���ϱ� �Ұǵ�, ��ճ������� �󸶳� ���̰� �־����� ���غ�.. ���� �� ���� �ִ�.
			    						snoringDbChkCnt++;
			    					}
						    		firstDecibelAvg = (firstDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40))/2;
						    		secondDecibelAvg = (secondDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18))/2;
			    				}
					    		//System.out.println(calcTime(times)+"s firstDecibelAvg: "+firstDecibelAvg+" secondDecibelAvg: "+secondDecibelAvg);
			    			//}
				    		//lowFHDecibelAvg = (lowFHDecibelAvg+allFHAndDB[0])/2;
				    		/*
				    		//0.3�� ������ ��� ���ú��� ���Ѵ�. 
				    		if(times-soundStartInRecordingTimes>0.3) {
				    			//���� ���ۻ����̳� ���� üũ�� �ð��� 0.3�ʰ� ��������. 
			    				if(Math.abs(maxDecibelAvg-lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) {
			    					//0.3�ʰ� �������ٸ�, ���� ����� ������ �ִ� ���ú� ��հ� ��� ���ú��� ���̸� ���Ѵ�.
				    				//���� ���ú� ��հ� ���� ���ļ� ����� ���� ���� ���ú��� ������ �Ѿ�� �ڰ��� ī��Ʈ ����
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//ī��Ʈ ���� ���ϰ� ���
			    				}
				    			//0.3�ʰ� �������� üũ�ð��� ����
				    			soundStartInRecordingTimes = times;
				    		}else {
				    			//0.3�ʰ� ���� �ȹ��������� üũ �ð��� ���� // ��� �����ϸ� �ȵ�, 0.3�ʰ� �������ٰ� ��� ���� �ð��� �����ϸ� ������ �����Ѵ�.
					    		//soundStartInRecordingTimes = times;
				    		}
				    		*/
				    	}
				    }else {
				    	//�Ҹ��� �߻����� �ʾ�����, ���� �ڰ��� ���� �߻������� üũ �Ѵ�.
				    	if(soundStartInRecording==true) {
                            if(snoringTermList == null || snoringTermList.size()==0){
                                soundStartInRecording = false;
                                continue;
                            }
				    		//���� ���� ���̶��, ���� üũ���� üũ ���۽ð��� 1�ʸ� �Ѿ����� üũ�Ѵ�.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>0.16*7){
			    				//���Ľ��۽ð����� 1�ʰ� �������ٸ� , �м��� �ߴ��ϰ�, ���� �ڰ��� �߻� ī��Ʈ�� üũ�Ͽ� ����Ѵ�.
			    				soundStartInRecording = false;
			    				/*
			    				System.out.print(calcTime(snoringTermList.get(snoringTermList.size()-1).start)+"~");
			    				System.out.print(calcTime(times));
			    				System.out.print(" "+maxDecibelAvg);
			    				System.out.print(" "+lowFHDecibelAvg);
			    				System.out.println(" "+Math.abs(maxDecibelAvg-lowFHDecibelAvg)+ " vs "+ Math.abs(maxDecibelAvg)/2);
			    				*/
			    				//if(Math.abs(lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) {
			    				//���̴� max - low
			    				//double  diffMaxToLow = maxDecibelAvg - lowFHDecibelAvg;
			    				//�ι�° ���ú��� �� ũ�� ��Ÿ����.
			    				double  diffMaxToLow = Math.abs(secondDecibelAvg) - Math.abs(firstDecibelAvg);
			    				//���̰� �ƽø� ���ú��� ���� �̻��ΰ�
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
			    					//1�ʰ� �������ٸ�, ���� ����� ������ �ִ� ���ú� ��հ� ��� ���ú��� ���̸� ���Ѵ�.
				    				//���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
			    					//���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//���� ī��Ʈ ���� ���ϰ� ���
			    					//-> ����� ī��Ʈ ��� �ݴ� ī��Ʈ ����
			    					soundStartAndSnroingOppCnt++;
			    				}
			    				//1. 5~200 ���ļ��� ��� ���ú����� 43~80 ���ļ��� ��� ���ú��� �� Ŀ����
			    				//2. �ڰ��� ���� ī��Ʈ 1 ��, ����ī��Ʈ�� 3���� ũ�� �ȵȴ�.(
			    				
			    				if(soundStartAndSnroingCnt > 0 && soundStartAndSnroingOppCnt<soundStartAndSnroingCnt*3) {
			    					//�ڰ��� ī��Ʈ�� �����߾���, �ڰ��� ���vo�� ���� �ð��� ���
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
				    				//�ڰ��� ī��Ʈ�� ������ ���� ������. 
			    					//�ڰ��� ��� vo ��� �̰��� ��� vo�� �ִ´�.
			    					//�̰��̴� ���� ������� �Ѵ�.
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
			    							//grindingChange�� 3�̻��� ����, / �� 10���� ũ�� 12���� �۾ƾ���
			    							((continueCntInChkTermForGrindingChange >= 3 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange >= 10 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange <= 12)
			    							||
			    							//2������ ����, / �� 9���� �۾ƾ���
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
			    				//���� ���� ���̰�, �Ҹ��� �߻����� �ʾ����� ���� 1�ʰ� ������ �ʾҴ�.
		    					//���� ī��Ʈ ���� ���ϰ� ���
		    					//-> ����� ī��Ʈ ��� �ݴ� ī��Ʈ ����
		    					//���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
			    				soundStartAndSnroingOppCnt++;
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//�Ҹ��� �߻����� �ʾҰ�, ���İ� ���� ���� ���°� �ƴϴ�. 
			    	
					// baos.write(frameBytes);

				}
			    	allFHAndDB = null;
				    }else {
				    }
					
					if (decibel > SleepCheck.getMinDB()*0.45) {
						//�Ҹ��� �߻��߰�, �м� ���� ���� ���� true �� ��� �����Ѵ�.
						if(isOSATermTimeOccur) {
							//0.1�� ���� �Ҹ��� 70% �̻� �߻��� ��� �Ҹ��� �߻��� ������ ����.
								
							if(isOSATermCnt+isBreathTermCnt>90 && isOSATermCnt > 20 && isBreathTermCnt > 70) {
								//���������� �д�. 0.5�� ���� �������� �Ҹ��� �߻��ؾ� �Ѵ�.
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
						//��ȣ���� �����ϱ� ���� �м� ���� ���� �ʱ�ȭ
						//�ڰ��̰� �߻��ϰ� 5�ʰ� ��������� ��.
						if(snoringTermList.size() > 0 
								&& snoringTermList.get(snoringTermList.size()-1).end != 0
								&& times - snoringTermList.get(snoringTermList.size()-1).end > 0 
								&& times - snoringTermList.get(snoringTermList.size()-1).end < 5 
								&& !isOSATermTimeOccur) {
							//0.1�� ���� ������ 70% �̻� �߻��� ��� �Ҹ��� �߻��� ������ ����.
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
					//��ȣ�� �߻��� 3�е��� ������� �ʴ´ٸ� ���
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end==0 && times-osaTermList.get(osaTermList.size()-1).start > 180) {
						isOSATermTimeOccur = false;
						isOSATermCnt = 0;
						isBreathTerm = false;
						isBreathTermCnt = 0;
						OSAcurTermTime = 0.0;
						osaTermList.remove(osaTermList.size()-1);
					}

					//��ȣ�� ���� �� ������ �ð��� �ʹ� ª���� �����Ѵ�.
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
						if(osaTermList.get(osaTermList.size()-1).end - osaTermList.get(osaTermList.size()-1).start < 5 ){
							osaTermList.remove(osaTermList.size()-1);
	                    }
					}
					
					//��ȣ�� ���� �� 5�� �̳��� �ڰ��̰� �߻����� ������ ���
					//��ȣ�� ���� �� 5�� ���� �ڰ��� �߻����θ� üũ�Ѵ�.
					if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
						if(snoringTermList.size()>0 && isRecording == true){
							//�ڰ��̰� ���� ���̰� �Ǿ��� ��, üũ �÷��׸� ������Ʈ
	                        if(snoringTermList.get(snoringTermList.size() - 1).end==0){
	                        	osaTermList.get(osaTermList.size()-1).chk = 1;
	                        }
	                    }
					}
					//��ȣ�� ���� �� 5�ʰ� ���� ��� �÷��׸� üũ�ؼ� �ڰ��̸� �����Ѵ�.
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
                        //�ڰ��� ��Ͽ� vo ����
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
				System.out.println("�ڰ��� ����");
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
				System.out.println("�ڰ��� ��");
				System.out.println("�̰��� ����");
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
				System.out.println("�̰��� ��");
				System.out.println("��ȣ�� ����");
				for (StartEnd se : osaTermList) {
					System.out.print(se.getTerm());
					System.out.println();
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("��ȣ�� ��");
				System.out.println("�м� ����");
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
	    	//�Ҹ� �߻�üũ�ϴ� fft������ ��ü ���ļ� ���ú��� �������� fft ������ �޶�, ������ fft ���ú� ��ġ�� -31.5�� �°� �����Ѵ�.
	    	//���� fft ���� �� ���� ���� ��� -75~87������ ��ġ�� �߻��ϴ� �ͱ��� Ȯ����.
	    	//�̸� ����ȭ �ϱ� ���� 90�� �Ӱ�ġ�� -31.5 db�� ��ȯ�Ѵ�.
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