package snoring;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RecordingThread extends Thread {
	int frameByteSizePer = 16;
	int frameByteSizeForSnoring = 1024 * frameByteSizePer;
	int frameByteSize = 1024;
	byte[] frameBytesForSnoring = new byte[frameByteSizeForSnoring];
	short[] tmpBytes = null;
	double[] allFHAndDB = null;

	RecordFragment recordFragment;

	public RecordingThread(RecordFragment recordFragment) {
		this.recordFragment = recordFragment;
	}

	public void run() {
		long recordStartingTIme = 0L;
		List<StartEnd> snoringTermList = new ArrayList<StartEnd>();
		List<StartEnd> grindingTermList = new ArrayList<StartEnd>();
		List<StartEnd> osaTermList = new ArrayList<StartEnd>();
		List<StartEnd> noiseTermListForOsaList = new ArrayList<StartEnd>();
		List<AnalysisRawData> AllAnalysisRawDataList = new ArrayList<AnalysisRawData>();
		double times = 0.0;
		int i = 0;
		boolean isRecording = false;
		int snoringBufferFilledCnt = 0;

		AnalysisRawData maxARD = null;
		double timesForMaxArd = 0.0;

		int recordingLength = 0;

		byte[] audioData;
		AudioCalculator audioCalculator;
		boolean isOSATermTimeOccur = false;

		double tmpMinDb = 99999;
		double tmpMaxDb = 0;

		File file = new File(recordFragment.getFilePath());
		audioData = new byte[(int) file.length()];
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(audioData);
			fis.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // read file into bytes[]
		InputStream targetStream = new ByteArrayInputStream(audioData);
		byte[] frameBytes = new byte[frameByteSize];
		audioCalculator = new AudioCalculator();
		FFTDataThread fftDataThread;

		int read = 0;
		try {
			while ((read = targetStream.read(frameBytes)) > 0) {
				if (frameBytes == null) {
					frameBytes = new byte[frameByteSize];
				}
	            times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
				targetStream.close();
				audioCalculator.setBytes(frameBytes);
				int amplitude = 0;
				double decibel = 0;
				double frequency = 0;
				try {
					// �Ҹ��� �߻��ϸ� ������ �����ϰ�, 1���̻� �Ҹ��� �߻����� ������ ������ ���� �ʴ´�.
					amplitude = audioCalculator.getAmplitude();
					decibel = audioCalculator.getDecibel();
					frequency = audioCalculator.getFrequency();
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					continue;
				}
				// ��ü ������ �����´�.
				// ��ü ������ ���� ���ļ�, ���ļ��� ��=hzPerDataPoint
				// ��ü ������ ���� ���ļ� ����Ʈ ����=fftSize
				if (snoringBufferFilledCnt < frameByteSizePer) {
					System.arraycopy(frameBytes, 0, frameBytesForSnoring, frameBytes.length * snoringBufferFilledCnt,
							frameBytes.length);
					snoringBufferFilledCnt++;
				}

				if (snoringBufferFilledCnt == frameByteSizePer) {
					snoringBufferFilledCnt = 0;
					tmpBytes = getAmplitudesFromBytesShort(frameBytesForSnoring);
					fftDataThread = new FFTDataThread(this);
					fftDataThread.setPriority(Thread.MAX_PRIORITY);
					fftDataThread.start();

				}
				i++; // �ð� ����

				// �Ҹ� �Ӱ�ġ�� �Ҹ��� �߻� ���θ� �����Ѵ�.
				// �ʱ�ȭ ����

				final String amp = String.valueOf(amplitude + "Amp");
				final String db = String.valueOf(decibel + "db");
				final String hz = String.valueOf(frequency + "Hz");
				// Log.v(LOG_TAG3,(calcTime(times)+" "+hz +" "+db+" "+amp+"
				// "+decibel+"vs"+SleepCheck.getMaxDB())+","+SleepCheck.getMinDB()+"
				// "+SleepCheck.noiseChkSum+" "+SleepCheck.noiseChkCnt);

				// �����δ� 1�� ���� �м��Ѵ�.
				if (i < 100) {
					continue;
				}

				SleepCheck.setMaxDB(decibel);
				SleepCheck.setMinDB(decibel);
	            tmpMinDb = SleepCheck.tmpMinDb;
	            tmpMaxDb = SleepCheck.tmpMaxDb;
	            
				if (SleepCheck.noiseCheckForStart(decibel) >= 1 && isRecording == false
						&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) != Math.floor(times)) {
					System.out.print(calcTime(times));
					System.out.print("(" + String.format("%.2f", times) + "s)");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
					// tmpMinDb = 99999;
					// tmpMaxDb = 0;
					recordStartingTIme = (long) times;
					// baos = new ByteArrayOutputStream();
					recordingLength = 0;
					isRecording = true;
					snoringTermList = new ArrayList<StartEnd>();
					grindingTermList = new ArrayList<StartEnd>();
					osaTermList = new ArrayList<StartEnd>();
					AllAnalysisRawDataList = new ArrayList<AnalysisRawData>();
					isOSATermTimeOccur = false;
				} else if (isRecording == true && SleepCheck.noiseCheck(decibel) <= 0) {
					System.out.print(calcTime(times));
					System.out.print("(" + String.format("%.2f", times) + "s)");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
					AllAnalysisRawDataList.add(maxARD);
					System.out.println("=====������ �м� ����, �м����� ����=====");
					System.out.println("�������� ����: " + calcTime(times - recordStartingTIme));
					System.out.println("tmpMinDb: " + tmpMinDb);
					System.out.println("tmpMaxDb: " + tmpMaxDb);
					recordStartingTIme = 0;
					isRecording = false;
				} else if (isRecording == true
						&& Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
					System.out.print(calcTime(times));
					System.out.print("(" + String.format("%.2f", times) + "s)");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!���� ����! ");
					AllAnalysisRawDataList.add(maxARD);
					System.out.println("=====������ �м� ����, �м����� ����=====");
					System.out.println("�������� ����: " + calcTime(times - recordStartingTIme));
					System.out.println("tmpMinDb: " + tmpMinDb);
					System.out.println("tmpMaxDb: " + tmpMaxDb);
					recordStartingTIme = 0;
					isRecording = false;
				}
				if (Math.floor((double) (audioData.length / (44100d * 16 * 1)) * 8) == Math.floor(times)) {
					recordFragment.setMShoudContinue(false);
				}
				if (isRecording == false) {
					continue;
				}
				if (allFHAndDB != null && tmpMaxDb > 40) {
					//System.out.println(calcTime(times) + " " + hz + " " + db + " " + amp + " " + decibel + ", 100db: " + tmpMaxDb + "db, max: " + SleepCheck.getMaxDB() + ", min: " + SleepCheck.getMinDB() + " " + SleepCheck.noiseChkSum + " " + SleepCheck.noiseChkCnt);
				}
				SleepCheck.snoringCheck(allFHAndDB, decibel, times, snoringTermList, grindingTermList, maxARD);
				if (SleepCheck.CHECKED_STATUS == SleepCheck.CHECKED_ERROR) { // �߻����� ���� �� ������ ���� ������ ���� 0 �����ϴ� ����ڵ带
																				// �����Ͽ���.
					continue;
				} else if (SleepCheck.CHECKED_STATUS == SleepCheck.allFHAndDb_NEED_INITIALIZE) { // allFHAndDB�� �ʱ�ȭ �Ǿ��
																									// �Ѵ�.
					allFHAndDB = null;
				}
				SleepCheck.osaCheck(decibel, times, osaTermList, snoringTermList,noiseTermListForOsaList );
				if (SleepCheck.CHECKED_STATUS == SleepCheck.CHECKED_ERROR) { // �߻����� ���� �� ������ ���� ������ ���� 0 �����ϴ� ����ڵ带
																				// �����Ͽ���.
					continue;
				}
				SleepCheck.someNoiseCheck(times, amplitude, noiseTermListForOsaList);
				if (SleepCheck.CHECKED_STATUS == SleepCheck.CHECKED_ERROR) { // �߻����� ���� �� ������ ���� ������ ���� 0 �����ϴ� ����ڵ带
																				// �����Ͽ���.
					continue;
				}

				if (maxARD != null) {
					if (decibel > maxARD.getDecibel()) {
						maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
					}
				} else {
					maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
					timesForMaxArd = Math.floor(times);
				}
				if (Math.floor(times) > timesForMaxArd) {
					// �ڰ��� ��Ͽ� vo ����
					if (maxARD.getDecibel() == 0) {
						maxARD.setDecibel(tmpMaxDb);
					}
					//System.out.println(calcTime(times)+" "+snoringTermList.size());
					if (snoringTermList.size() > 0 && isRecording == true) {
						if (snoringTermList.get(snoringTermList.size() - 1).end != 0) {
							if (snoringTermList.get(snoringTermList.size() - 1).end > times) {
								snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(maxARD);
							}
						} else {
							snoringTermList.get(snoringTermList.size() - 1).AnalysisRawDataList.add(maxARD);
						}
					}
					if (osaTermList.size() > 0 && isRecording == true && isOSATermTimeOccur) {
						if (osaTermList.get(osaTermList.size() - 1).end != 0) {
							if (osaTermList.get(osaTermList.size() - 1).end > times) {
								osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(maxARD);
							}
						} else {
							osaTermList.get(osaTermList.size() - 1).AnalysisRawDataList.add(maxARD);
						}
					}
					if (isRecording == true) {
						// AllAnalysisRawDataList.add(maxARD);
						int tmpTime = (int) Math.floor(times);
						// 1�� Ȥ�� 1�� ������ ���
						if (tmpTime < 31 || AllAnalysisRawDataList.size() < 31) {
							//System.out.println(calcTime(times) + " 1��");
							AllAnalysisRawDataList.add(maxARD);
						}

						// System.out.println(calcTime(times))+"
						// "+calcTime((System.currentTimeMillis()/1000)%60));
						if (tmpTime > 30 && AllAnalysisRawDataList.size() > 0) { // ������ ���� ������ �� ����Ʈ�� 0�� ����� ������ �߻��Ѵ�.
																					// �տ��� ����Ʈ�� 31 �̸��̸� ����Ʈ�� �߰��ϰ� �Ǿ������Ƿ�
																					// size�� 0�� ���� ����.

							double tmpCM = (times + (int) (recordFragment.getRecordStartDtl() / 1000) % 60);
							double tmpBeforeCM = (AllAnalysisRawDataList.get(AllAnalysisRawDataList.size() - 1)
									.getTimes() + (int) (recordFragment.getRecordStartDtl() / 1000) % 60);
							int tmpM = calcMinute(tmpCM);
							int tmpBeforeM = calcMinute(tmpBeforeCM);
							// System.out.println(calcTime(times)+" "+tmpCM+" "+tmpBeforeCM+" "+tmpM+"
							// "+tmpBeforeM));
							if (tmpM != tmpBeforeM) {
								//System.out.println(calcTime(times) + " 3�� " + tmpTime);
								AllAnalysisRawDataList.add(maxARD);
							}
						}
					}
					maxARD = new AnalysisRawData(times, amplitude, tmpMaxDb, frequency);
					timesForMaxArd = Math.floor(times);

					tmpMaxDb = 0;
					tmpMinDb = 99999;
				}
			}//while

	        System.out.println( "�ڰ��� ���� ����==========");
	        System.out.println( "�ڰ��� " + snoringTermList.size()+"ȸ �߻� ");
	        for(StartEnd se : snoringTermList) {
	            System.out.println(se.getTerm());
	        }
	        System.out.println( "�ڰ��� ���� ��==========");
	        System.out.println( "�̰��� ���� ����==========");
	        System.out.println( "�̰��� " + grindingTermList.size()+"ȸ �߻� ");
	        for(StartEnd se : grindingTermList) {
	            System.out.println(se.getTerm());
	        }
	        System.out.println( "�̰��� ���� ��==========");
	        System.out.println( "��ȣ�� ���� ����==========");
	        System.out.println( "��ȣ��" + osaTermList.size()+"ȸ �߻� ");
	        for(StartEnd se : osaTermList) {
	            System.out.println(se.getTerm());
	        }
	        System.out.println( "��ȣ�� ���� ��==========");

	        System.out.println( "���� ���� ����==========");
	        System.out.println( "����" + noiseTermListForOsaList.size()+"ȸ �߻� ");
	        for(StartEnd se : noiseTermListForOsaList) {
	            System.out.println(se.getTerm());
	        }
	        System.out.println( "���� ���� ��==========");
	        System.out.println(String.format("Recording  has stopped. Samples read: %d", read));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String calcTime(double times) {
		int seconds;
		int minutes;
		int hours;
		seconds = (int) times;
		hours = seconds / 3600;
		minutes = (seconds % 3600) / 60;
		double seconds_output = (times % 3600) % 60;
		seconds_output = Math.floor(seconds_output * 1000) / 1000;
		return hours + ":" + minutes + ":" + seconds_output + "";
	}

	private int calcMinute(double times) {
		int seconds;
		int minutes;
		seconds = (int) times;
		minutes = (seconds % 3600) / 60;
		double seconds_output = (times % 3600) % 60;
		seconds_output = Math.floor(seconds_output * 1000) / 1000;
		return minutes;
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

	public int getFrameBytesForSnoringLength() {
		return this.frameBytesForSnoring.length;
	}

	public short[] getTmpBytes() {
		return this.tmpBytes;
	}

	public void setAllFHAndDB(double[] allFHAndDB) {
		this.allFHAndDB = allFHAndDB;
	}

}