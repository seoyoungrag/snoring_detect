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
		            fftData.setSize(fftSize);
		            double[] allFHAndDB = fftData.getItemsArray();
		            //��ü ���ļ�/���ú� ǥ�� ����
		            /*
		            if(isRecording==false) {
					    DecimalFormat df = new DecimalFormat("0.00");
					    if(i==0) { //�м� ������ �� ���ļ� ǥ��
					    	System.out.print("�ð�\t\t");
					    	Arrays.stream(tmpArray).forEach(e -> System.out.print(e + "\t" ));
					    	System.out.println();
					    }
						//�ð����� ���ļ�/���ú� ǥ��
						System.out.print(calcTime(times) + "s\t" );
					    Arrays.stream(allFHAndDB).forEach(e -> System.out.print(df.format(e) + "\t" ));
						System.out.println(" ");
		            }
		            */
					//��ü ���ļ�/���ú� ǥ�� ��
					i++; //�ð� ����
					
					//�ִ� ���ļ�, ���ú�, ���� ��������
					audioCalculator.setBytes(frameBytes);
					int amplitude = audioCalculator.getAmplitude();
					double decibel = audioCalculator.getDecibel();
					double frequency = audioCalculator.getFrequency();
					double sefrequency = audioCalculator.getFrequencySecondMax();
					
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
						recordStartingTIme = times;
						// baos = new ByteArrayOutputStream();
						isRecording = true;
					} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 500) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println("=====������ �м� ����, �м����� ����=====");
						System.out.println("�������� ����: "+ calcTime(times - recordStartingTIme));
						recordStartingTIme = 0;
						isRecording = false;
					} else if (isRecording == true && Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
						System.out.print(calcTime(times));
						System.out.print("("+String.format("%.2f", times) + "s)");
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
						System.out.println("=====������ �м� ����, �м����� ����=====");
						System.out.println("�������� ����: "+ calcTime(times - recordStartingTIme));
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
				    	//�ڰ��� ���İ� �߻�����.
				    	if(soundStartInRecording==false) {
			    			//System.out.println("���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ���¿��ٸ� ���� ���� ���·� ��ȯ");
				    		//���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ���¿��ٸ� ���� ���� ���·� ��ȯ
				    		soundStartInRecording = true;
				    		//�ڰ��� ī��Ʈ�� �ʱ�ȭ, ���� ���� �߿� ī��Ʈ�� ������ ���̴�.
				    		soundStartAndSnroingCnt = 0;
				    		//���Ľ��ۻ��¸� 0.5�� �������� üũ�ϱ� ���� ���� �Ҵ�
				    		soundStartInRecordingTimes = times;
				    		//���Ľ��۽ð��� �����ϱ� ���� ���vo�� ����
				    		StartEnd st = new StartEnd();
				    		st.start = times;
				    		snoringTermList.add(st);
				    		//�ִ� ���ú� ���� �����Ѵ�.(�ʱ�ȭ)
				    		maxDecibelAvg = decibel;
				    		lowFHDecibelAvg = -(31.5-allFHAndDB[0]/255*31.5);
				    	}else {
				    		//���� �߿� �Ҹ��� �߻��߰� ���� ���� ����
				    		if(times-soundStartInRecordingTimes>0.3) {
				    			//���� ���ۻ����̳� ���� üũ�� �ð��� 0.5�ʰ� ���������� Ȯ��, 0.5�ʸ��� ��� ġ�� üũ�Ѵ�.
			    				if(Math.abs(maxDecibelAvg-lowFHDecibelAvg) > Math.abs(maxDecibelAvg)*2) { 
				    				//���� ���ú� ��հ� ���� ���ļ� ����� ���� ���� ���ú��� ������ �Ѿ�� �ڰ��� ī��Ʈ ����
			    					soundStartAndSnroingCnt++;
			    				}else {
			    					//���
			    				}
				    			//0.3�ʰ� �������� üũ�ð��� ����
				    			soundStartInRecordingTimes = times;
				    		}else {
				    			//0.5�ʰ� ���� �ȹ��������� üũ �ð��� ���� // ��� �����ϸ� �ȵ�
					    		//soundStartInRecordingTimes = times;
				    		}
				    	}
				    	//�Ҹ� �߻������� ���� ���ú� ��հ� ���� ���ļ� ����� ���Ѵ�.
			    		maxDecibelAvg = (maxDecibelAvg+decibel)/2;
			    		lowFHDecibelAvg = (lowFHDecibelAvg+-(31.5-allFHAndDB[0]/255*31.5))/2;
				    }else {
				    	//�Ҹ��� �߻����� �ʾ�����, ���� �ڰ��� ���� �߻������� üũ �Ѵ�.
				    	if(soundStartInRecording==true) {
				    		//�ڰ��� �߻����̶��, ���� üũ���� üũ ���۽ð��� 1�ʸ� �Ѿ����� üũ�Ѵ�.
			    			if(times-snoringTermList.get(snoringTermList.size()-1).start>1){
			    				//���Ľ��۽ð����� 1�ʰ� �������ٸ� 
			    				soundStartInRecording = false;
			    				/*
			    				System.out.print(calcTime(snoringTermList.get(snoringTermList.size()-1).start)+"~");
			    				System.out.print(calcTime(times));
			    				System.out.print(" "+maxDecibelAvg);
			    				System.out.print(" "+lowFHDecibelAvg);
			    				System.out.println(" "+Math.abs(maxDecibelAvg-lowFHDecibelAvg)+ " vs "+ Math.abs(maxDecibelAvg)/2);
			    				*/
			    				if(soundStartAndSnroingCnt > 0) {
			    					//���vo�� ���� �ð��� ���
					    			//System.out.println("���� ���ú� ��հ� ���� ���ļ� ����� ���� ���� ���ú��� ������ �Ѿ�� ���vo�� ���� �ð��� ���");
				    				snoringTermList.get(snoringTermList.size()-1).end = times;
			    				}else {
				    				//�ƴϸ� ��� ���
			    					//��� ��� ��� �̰��̷� �ִ´�.
					    			//System.out.println("���� ���ú� ��հ� ���� ���ļ� ����� ���� ���� ���ú��� ������ �Ѿ�� ���vo�� ���� �ð��� ��� �ƴϸ� ��� ���");
				    				//snoringTermList.remove(snoringTermList.size()-1);
						    		StartEnd st = new StartEnd();
						    		st.start = snoringTermList.get(snoringTermList.size()-1).start;
						    		st.end = times;
						    		snoringTermList.remove(snoringTermList.size()-1);
						    		grindingTermList.add(st);
			    				}
			    			}else {
				    			//System.out.println("0.5�ʰ� ���������� ���Ľ��۽ð����� 0.5�ʰ� �������ٸ� ���vo ����");
			    				//0.5�ʰ� ���������� ���Ľ��۽ð����� 0.5�ʰ� �������ٸ� ���vo ����
			    				//snoringTermList.remove(snoringTermList.size()-1);
			    				//soundStartInRecording = false;
			    			}
				    	}
				    	//���Ŀ� �ش��ϴ� �Ҹ��� �߻����� �ʾҴ�. 
				    }
					// baos.write(frameBytes);

				}
				System.out.println("�м� ����");
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