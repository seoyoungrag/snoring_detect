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
				System.out.println("�м� ���� "+filePath);
				while ((read = targetStream.read(frameBytes)) > 0) {
					if (frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					targetStream.close();
					times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
					//��ü ������ �����´�.
					//��ü ������ ���� ���ļ�, ���ļ��� ��=hzPerDataPoint
					//��ü ������ ���� ���ļ� ����Ʈ ����=fftSize
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
		            //��ü ���ļ�/���ú� ǥ�� ����
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

					//�ִ� ���ļ�, ���ú�, ���� ��������
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
		            if(false) {
					    DecimalFormat df = new DecimalFormat("0.00");
					    if(i==0) { //�м� ������ �� ���ļ� ǥ��
					    	System.out.print("�ð�\t\t");
					    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
					    	System.out.println();
					    }
						//�ð����� ���ļ�/���ú� ǥ��
						System.out.print(calcTime(times) + "s\t" );
						//System.out.print(frequency + "HZ\t" );
						//System.out.print(decibel + "DB\t" );
					    //Arrays.stream(allFHAndDB).forEach(e -> System.out.print(df.format(e) + "\t" ));
						Arrays.stream(allFHAndDB).forEach(e ->{
							//�ִ� �ּ�ġ�� 70���� ���, ����ȭ ����
							if(Math.abs(e)>70) {
								e = 70;
							}
							//System.out.print(df.format( -(31.5-(Math.abs(e)/70)*31.5 )) + "\t" );
							System.out.print(-(31.5-(Math.abs(e)/70)*31.5 ) + "\t" );
							});
					    
						System.out.println(" ");
		            }
					//��ü ���ļ�/���ú� ǥ�� ��
					i++; //�ð� ����
					
					
					//�Ҹ� �Ӱ�ġ�� �Ҹ��� �߻� ���θ� �����Ѵ�. 
					//�ʱ�ȭ ����
					SleepCheck.setMaxDB(decibel);
					SleepCheck.setMinDB(decibel);

					// �Ҹ��� �߻��ϸ� ������ �����ϰ�, 1���̻� �Ҹ��� �߻����� ������ ������ ���� �ʴ´�.
					if (SleepCheck.noiseCheckForStart(decibel) >= 30 && isRecording == false
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
						chkSnoringDb = SleepCheck.getMinDB()/0.75;
					}else if(chkSnoringDb<=-10) {
						//chkSnoringDb = chkSnoringDb;
					}
					//�ڰ��̴� �Ӱ�ġ�� �����ؼ� �ڰ����� ���� ���θ� �Ǵ��Ѵ�.
				    if(decibel > chkSnoringDb) {
				    	//�Ҹ� �߻�üũ�ϴ� fft������ ��ü ���ļ� ���ú��� �������� fft ������ �޶�, ������ fft ���ú� ��ġ�� -31.5�� �°� �����Ѵ�.
				    	//���� fft ���� �� ���� ���� ��� -75~87������ ��ġ�� �߻��ϴ� �ͱ��� Ȯ����.
				    	//�̸� ����ȭ �ϱ� ���� 70�� �Ӱ�ġ�� -31.5 db�� ��ȯ�Ѵ�.
						if(Math.abs(allFHAndDB[0])>70) {
							allFHAndDB[0] = 70;
						}
						if(Math.abs(allFHAndDB[1])>70) {
							allFHAndDB[1] = 70;
						}
						//�ִ� �ּ�ġ�� 70���� ���, ����ȭ ����
						double forChkSnroingDb1 = -(31.5-(Math.abs((allFHAndDB[0]+allFHAndDB[1])/2)/70)*31.5 ); 
						double forChkSnroingDb2 = -(31.5-(Math.abs(allFHAndDB[2])/70)*31.5 ); 
				    	//�ڰ��� ���İ� �߻�����.
				    	if(soundStartInRecording==false) {
				    		//���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ����, ���� ���� ���·� ��ȯ
				    		soundStartInRecording = true;
				    		//�ڰ��� ī��Ʈ�� �ʱ�ȭ(���� ���� �߿� ī��Ʈ ����)
				    		soundStartAndSnroingCnt = 0;
				    		//���Ľ��ۻ��¸� 0.3�� �������� üũ�ϱ� ���� ���� �Ҵ�(�ʱ�ȭ)
				    		//0.3�� �̳��� ���������� ���İ� �߻��Ѵٸ� ���� ���� �߻��� �Ʒ� ������ 0.3�� �̻� ���̰� ���� �ʾƾ� �Ѵ�.
				    		soundStartInRecordingTimes = times;
				    		//���Ľ��۽ð��� �����ϱ� ���� ���vo�� ����
				    		StartEnd st = new StartEnd();
				    		st.start = times;
				    		snoringTermList.add(st);
				    		//���İ� ����Ǵ� ���� �ִ� ���ú��� �����ļ��� ���ú��� ����� ����ϱ� ���� ���� �ʱ�ȭ �Ѵ�.
				    		//�ִ� ���ú� ���� �����ļ� ���ú� ���� �����Ѵ�.(�ʱ�ȭ)
				    		//maxDecibelAvg = decibel;
				    		//lowFHDecibelAvg = forChkSnroingDb1;
				    		firstDecibelAvg = 0;
				    		secondDecibelAvg = 0;
				    		//lowFHDecibelAvg = allFHAndDB[0];
				    	}else {
				    		//���� �߿� �Ҹ��� �߻��߰� ���İ� ���� ���� ����
					    	//���� ���ú� ��հ� ���� ���ļ� ����� ����Ѵ�.
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
				    		//���� ���� ���̶��, ���� üũ���� üũ ���۽ð��� 1�ʸ� �Ѿ����� üũ�Ѵ�.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>1){
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
			    				//if(diffMaxToLow < 0 && Math.abs(diffMaxToLow) > (31.5-Math.abs(secondDecibelAvg)) / 2) {
			    				if(diffMaxToLow < 0 ) {
			    					//1�ʰ� �������ٸ�, ���� ����� ������ �ִ� ���ú� ��հ� ��� ���ú��� ���̸� ���Ѵ�.
				    				//���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//ī��Ʈ ���� ���ϰ� ���
			    				}
			    				if(soundStartAndSnroingCnt > 0) {
			    					//�ڰ��� ī��Ʈ�� �����߾���, �ڰ��� ���vo�� ���� �ð��� ���
				    				snoringTermList.get(snoringTermList.size()-1).end = times;
				    				snoringTermList.get(snoringTermList.size()-1).low = firstDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).max = secondDecibelAvg;
				    				snoringTermList.get(snoringTermList.size()-1).chk = chkSnoringDb;
			    				}else {
				    				//�ڰ��� ī��Ʈ�� ������ ���� ������. 
			    					//�ڰ��� ��� vo ��� �̰��� ��� vo�� �ִ´�.
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
			    				//���� ���� ���̰�, �Ҹ��� �߻����� �ʾ����� ���� 1�ʰ� ������ �ʾҴ�. 
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//�Ҹ��� �߻����� �ʾҰ�, ���İ� ���� ���� ���°� �ƴϴ�. 
				    }
					// baos.write(frameBytes);

				}

				System.out.println("�ڰ��� ����");
				for (StartEnd se : snoringTermList) {
					System.out.print(se.getTerm());
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("�ڰ��� ��");
				System.out.println("�̰��� ����");
				for (StartEnd se : grindingTermList) {
					System.out.print(se.getTerm());
					System.out.println(se.printAnalysisRawDataList());
				}
				System.out.println("�̰��� ��");
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

