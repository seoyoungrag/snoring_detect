package snoring;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.sun.media.sound.WaveFileWriter;

public class EventFireGui20190517 extends JFrame {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private SnoringApi snoringApi;
	private WaveHeader waveHeader;
	byte[] audioData;
	int frameByteSize = 1024; 
	byte[] buffer;
	byte[] totalBuf;
	int cnt;
	private LinkedList<Boolean> snoringResultList = new LinkedList<Boolean>();
	private int snoringCheckLength = 3;
	private int snoringPassScore = 3;
	private int failCnt = 0;
	private int successCnt = 0;

    private AudioCalculator audioCalculator;
	private void initBuffer() {
		AlarmStaticVariables.isSpecificSoundCnt = 0;
		snoringResultList.clear();
		
		// init the first frames
		for (int i = 0; i < snoringCheckLength; i++) {
			snoringResultList.add(false);
		}
		// end init the first frames
	}
	
	public EventFireGui20190517(String filePath) {
		super("Event Firer");
		AlarmStaticVariables.snoringCount = 0;
		setBounds(100, 100, 300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container contentPane = this.getContentPane();
		JPanel pane = new JPanel();
		JButton buttonStart = new JButton("Start");
		JTextField textPeriod = new JTextField(5);
		JLabel labelPeriod = new JLabel("Input period : ");
		JCheckBox checkboxIsRandom = new JCheckBox("Fire randomly");

		checkboxIsRandom.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					textPeriod.setText("Random");
					textPeriod.setEnabled(false);
				} else {
					textPeriod.setText("");
					textPeriod.setEnabled(true);
				}
			}
		});

		buttonStart.setMnemonic('S');

		pane.add(buttonStart);
		pane.add(labelPeriod);
		pane.add(textPeriod);
		pane.add(checkboxIsRandom);

		contentPane.add(pane);
		InputStream fin;
		try {
			fin = new FileInputStream(filePath); 		
			Wave wave = new Wave(fin);
			waveHeader = wave.getWaveHeader();
			File file = new File(filePath);
			audioData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(audioData); // read file into bytes[]
			fis.close();
			
			if (waveHeader.getChannels() != 1) {
				System.out.println("convert start");
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
				BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
				audioInputStream = new AudioInputStream(bufferedInputStream, audioInputStream.getFormat(), audioInputStream.getFrameLength());
				AudioFormat format = audioInputStream.getFormat();
				WaveFileWriter wfw = new WaveFileWriter();
				AudioFormat monoFormat = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize(), format.getFrameRate(), format.isBigEndian());

				//byte[] audioData = IOUtils.toByteArray(fin);

				int length = audioData.length;
				ByteArrayInputStream bais = new ByteArrayInputStream(audioData);

				AudioInputStream stereoStream = new AudioInputStream(bais, format, length);
				AudioInputStream monoStream = new AudioInputStream(stereoStream, monoFormat, length);

				String destFilePath = "raw/raw_convert/un-ogged.wav";
				File destFile = new File(destFilePath);
				if(destFile.exists()) {
					destFile.delete();
				}
				wfw.write(monoStream, javax.sound.sampled.AudioFileFormat.Type.WAVE,
						new File(destFilePath));

				fin = new FileInputStream(destFilePath);		
				fis = new FileInputStream(file);
				fis.read(audioData); // read file into bytes[]
				fis.close();	
				wave = new Wave(fin);
				waveHeader = wave.getWaveHeader();
				waveHeader = new WaveHeader();
				//waveHeader.setChannels(1);
				waveHeader.setChannels(waveHeader.getChannels());
				waveHeader.setBitsPerSample(waveHeader.getBitsPerSample());
				waveHeader.setSampleRate(waveHeader.getSampleRate());
				/*
				System.out.println(waveHeader.getChannels());
				System.out.println(waveHeader.getBitsPerSample());
				System.out.println(waveHeader.getSampleRate());
				*/
				/*
				 * byte[] audioData = inputStreamToByteArray(fin); int length =
				 * audioData.length; AudioFormat af = new AudioFormat( Encoding.PCM_SIGNED,
				 * waveHeader.getSampleRate(), waveHeader.getBitsPerSample(),
				 * waveHeader.getChannels(), waveHeader.getChannels()*2,
				 * waveHeader.getBitsPerSample(), false); AudioInputStream monoStream = new
				 * AudioInputStream(fin,af,length/2); WaveFileWriter wfw = new WaveFileWriter();
				 * wfw.write(monoStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, new
				 * File("raw/raw_convert/un-ogged.wav"));
				 */

				System.out.println("convert end");
			}
			snoringApi = new SnoringApi(waveHeader);
			//System.out.println("How many all bytes? " + audioData.length);
			//buffer = new byte[frameByteSize];
			totalBuf = new byte[AlarmStaticVariables.sampleSize * 2];
			cnt = 0;
			//snoringApi.isSnoring(getFrameBytes());
			int read = 0;
			InputStream targetStream = new ByteArrayInputStream(audioData);
			byte[] frameBytesForZCR = new byte[frameByteSize];
			byte[] frameBytes = new byte[frameByteSize];
		    try {
				while( ( read = targetStream.read( frameBytes ) ) > 0 ){
					//AlarmStaticVariables.snoringCount = 0;
					//AlarmStaticVariables.isSpecificSoundCnt = 0;
					if(frameBytes == null) {
						frameBytes = new byte[frameByteSize];
					}
					frameBytesForZCR = getFrameBytes(frameBytes);
					if(frameBytesForZCR != null) {
						AlarmStaticVariables.snoringCount += snoringApi.isSnoring(frameBytesForZCR);
					}else {
						frameBytesForZCR = new byte[frameByteSize];
					}
				}
		        targetStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*
			byte[] frameBytesForSpectogram = new byte[snoringApi.frameByteSize];
			frameBytes = new byte[snoringApi.frameByteSize];
		    try {
				targetStream = new ByteArrayInputStream(audioData);
				initBuffer();
				while( ( read = targetStream.read( frameBytes ) ) > 0 ){
					if(frameBytes == null) {
						frameBytes = new byte[snoringApi.frameByteSize];
					}
					frameBytesForSpectogram = snoringApi.getFrameBytes(frameBytes);
					if(frameBytesForSpectogram != null) {
						if(AlarmStaticVariables.snoringCount>0) {
							boolean isSnoring = snoringApi.isSpecificSound(frameBytesForSpectogram);
							if(snoringResultList.getFirst()) {
								AlarmStaticVariables.isSpecificSoundCnt--;
							}
							snoringResultList.removeFirst();
							snoringResultList.add(isSnoring);
							if (isSnoring) {
								AlarmStaticVariables.isSpecificSoundCnt++;
							}
							if (AlarmStaticVariables.isSpecificSoundCnt >= snoringPassScore) {
								successCnt++;
								System.out.println("[찾았음 start]");
								System.out.println("AlarmStaticVariables.snoringCount: " + AlarmStaticVariables.snoringCount);
								System.out.println("AlarmStaticVariables.isSpecificSoundCnt: " + AlarmStaticVariables.isSpecificSoundCnt);
								System.out.println("[찾았음 end]");
							}else {
								failCnt++;
								System.out.println("[못찾았음 start]");
								System.out.println("AlarmStaticVariables.snoringCount: " + AlarmStaticVariables.snoringCount);
								System.out.println("AlarmStaticVariables.isSpecificSoundCnt: " + AlarmStaticVariables.isSpecificSoundCnt);
								System.out.println("[못찾았음 end]");
							}
						}
						
					}else {
						frameBytesForSpectogram = new byte[snoringApi.frameByteSize];
					}
			        targetStream.close();
				}
		    } catch (IOException e) {
				e.printStackTrace();
			}
		    */
			/*
			AlarmStaticVariables.snoringCount = snoringApi.isSnoring(audioData);
			System.out.println("[yrseo]AlarmStaticVariables.snoringCount :" + AlarmStaticVariables.snoringCount);
			if(AlarmStaticVariables.snoringCount>0) {
				AlarmStaticVariables.isSpecificSoundCnt = snoringApi.isSnoringSpectogramChk(audioData);
			}
			*/
			/*System.out.println("[yrseo]실패 카운트: " + failCnt);
			System.out.println("[yrseo]성공 카운트: " + successCnt);
			System.out.println("[yrseo]성공 카운트가 실패카운트보다 높은가?: " + String.valueOf(successCnt>failCnt));*/
			try {

	            //get raw double array containing .WAV data
	            ReadWAV2Array audioTest = new ReadWAV2Array(filePath, true);
	            double[] rawData = audioTest.getByteArray();
	            int length = rawData.length;

	            //initialize parameters for FFT
	            int WS = 2048; //WS = window size
	            int OF = 8;    //OF = overlap factor
	            int windowStep = WS/OF;

	            //calculate FFT parameters
	            double SR = audioTest.getSR();
	            double time_resolution = WS/SR;
	            double frequency_resolution = SR/WS;
	            double highest_detectable_frequency = SR/2.0;
	            double lowest_detectable_frequency = 5.0*SR/WS;

	            System.out.println("SR:              " + SR + " ");
	            System.out.println("time_resolution:              " + time_resolution*1000 + " ms");
	            System.out.println("frequency_resolution:         " + frequency_resolution + " Hz");
	            System.out.println("highest_detectable_frequency: " + highest_detectable_frequency + " Hz");
	            System.out.println("lowest_detectable_frequency:  " + lowest_detectable_frequency + " Hz");

	            //initialize plotData array
	            /*int nX = (length-WS)/windowStep;
	            int nY = WS;
	            double[][] plotData = new double[nX][nY]; 

	            //apply FFT and find MAX and MIN amplitudes

	            double maxAmp = Double.MIN_VALUE;
	            double minAmp = Double.MAX_VALUE;

	            double amp_square;

	            double[] inputImag = new double[length];

	            for (int i = 0; i < nX; i++){
	                Arrays.fill(inputImag, 0.0);
	                double[] WS_array = FFT.fft(Arrays.copyOfRange(rawData, i*windowStep, i*windowStep+WS), inputImag, true);
	                for (int j = 0; j < nY; j++){
	                    amp_square = (WS_array[2*j]*WS_array[2*j]) + (WS_array[2*j+1]*WS_array[2*j+1]);
	                    if (amp_square == 0.0){
	                        plotData[i][j] = amp_square;
	                    }
	                    else{
	                        plotData[i][j] = 10 * Math.log10(amp_square);
	                    }

	                    //find MAX and MIN amplitude
	                    if (plotData[i][j] > maxAmp)
	                        maxAmp = plotData[i][j];
	                    else if (plotData[i][j] < minAmp)
	                        minAmp = plotData[i][j];

	                }
	            }

	            System.out.println("---------------------------------------------------");
	            System.out.println("Maximum amplitude: " + maxAmp);
	            System.out.println("Minimum amplitude: " + minAmp);
	            System.out.println("---------------------------------------------------");

	            //Normalization
	            double diff = maxAmp - minAmp;
	            for (int i = 0; i < nX; i++){
	                for (int j = 0; j < nY; j++){
	                    plotData[i][j] = (plotData[i][j]-minAmp)/diff;
	                }
	            }
*/
	            //plot image
	            /*
	            BufferedImage theImage = new BufferedImage(nX, nY, BufferedImage.TYPE_INT_RGB);
	            double ratio;
	            for(int x = 0; x<nX; x++){
	                for(int y = 0; y<nY; y++){
	                    ratio = plotData[x][y];

	                    //theImage.setRGB(x, y, new Color(red, green, 0).getRGB());
	                    //Color newColor = getColor(1.0-ratio);
	                    //theImage.setRGB(x, y, newColor.getRGB());
	                }
	            }
	            File outputfile = new File("saved.png");
	            ImageIO.write(theImage, "png", outputfile);
*/
	            audioCalculator = new AudioCalculator();
				frameBytes = new byte[snoringApi.frameByteSize];
			    try {
		            int count = 0;
					targetStream = new ByteArrayInputStream(audioData);
					initBuffer();
					int i = 1;
					while( ( read = targetStream.read( frameBytes ) ) > 0 ){
						if(frameBytes == null) {
							frameBytes = new byte[snoringApi.frameByteSize];
						}

			            audioCalculator.setBytes(frameBytes);
			            int amplitude = audioCalculator.getAmplitude();
			            double decibel = audioCalculator.getDecibel();
			            double frequency = audioCalculator.getFrequency();
			            double sefrequency = audioCalculator.getFrequencySecondMax();
			            int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());
			            //double frequency2Th = audioCalculator.getFrequency2Th();

			            final String amp = String.valueOf(amplitude + "Amp");
			            final String db = String.valueOf(decibel + "db");
			            final String hz = String.valueOf(frequency + "Hz");
			            final String sehz = String.valueOf(sefrequency + "Hz(2th)");
			            final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
			            //System.out.println(amp);
			            //System.out.println(db);
			            double times = (((double)(frameBytes.length/(44100d*16*1)))*8)*i;
			            System.out.println(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+" "+sehz+" "+seamp);
			            if(frequency>1000) {
			            	/*
		            	System.out.println(String.format("%.2f", times)+"s "
		            			+ hz +" "+db+" "+amp+" "
		            			//+sehz+" "+seamp
		            			);
		            			*/
			            }
			            
			            if(frequency>=150 && frequency<=250 && sefrequency>=950 &&sefrequency<1050 
			            		//&& amplitude < sefamplitude
			            		) {
			            	count++;
			            	//System.out.println(hz +" "+db+" "+amp+" "+sehz+" "+seamp);
			            }
				        targetStream.close();i++;
					}
					System.out.println("audio length(s): "+((double)(audioData.length/(44100d*16*1)))*8); 
					System.out.println("(frequency>=150 && frequency<=250 && sefrequency>=950 &&sefrequency<1050) CNT: "+count);
					System.out.println("AlarmStaticVariables.snoringCount: "+AlarmStaticVariables.snoringCount);
					
			    } catch (IOException e) {
					e.printStackTrace();
				}
	            //이갈이, 이갈이는 높은 주파수가 굉장히 짧은 간격으로 여러번 나타난다.
	            //아래 1,2,3 무시, 다시-> 이갈이는 0.02~0.07초 사이의 큰 진폭을 갖는다. 즉, 0.01초 단위로 분석해서 연속으로 2~7회의 진폭이 발생하는 것을 잡으면 됨.
	            //이 때, 진폭의 수치는 의미가 없을 수 있으므로 먼저 반복횟수로만 잡아본다.
	            //1. 특정 주파수보다 높은 주파수가 --> 특정주파수 a=? 
	            //2. 특정 시간동안 특정 횟수보다 많이 반복되는지 --> 특정시간 b=?, 특정횟수 c=?
	            //3. 체크되면 이갈이다.
            audioCalculator = new AudioCalculator();
			frameBytes = new byte[snoringApi.frameByteSize];
		    try {
				targetStream = new ByteArrayInputStream(audioData);
				int i = 1;
				int sumCnt = 0;
				int maxAmp = 0;
	            double curTermHz = 0.0;
	            double curTermSecondHz = 0.0;
	            double curTermTime = 0.0;
	            double curTermDb = 0.0;
	            int curTermAmp = 0;
	            int findedTimeCnt = 0;
	            int unchcekdFindedTimeCnt = 0;
	            int continueAmp = 0;
	            double chkDb = -10;
	            //double chkDb = -32;
	            int s = 0;
	            double termTime = 0.0;
	            List<GrinderClass> gcl = new ArrayList<GrinderClass>();
	            List<GrinderClass> gclOpp = new ArrayList<GrinderClass>();
				List<String> findedTimeAr = new ArrayList<String>();
				List<Integer> findedTimeCntAr = new ArrayList<Integer>();
				List<Double> findedHzAr = new ArrayList<Double>();
				while( ( read = targetStream.read( frameBytes ) ) > 0 ){
					if(frameBytes == null) {
						frameBytes = new byte[snoringApi.frameByteSize];
					}
		            audioCalculator.setBytes(frameBytes);
		            int amplitude = audioCalculator.getAmplitude();
		            double decibel = audioCalculator.getDecibel();
		            double frequency = audioCalculator.getFrequency();
		            double sefrequency = audioCalculator.getFrequencySecondMax();
		            int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());

		            final String amp = String.valueOf(amplitude + "Amp");
		            final String db = String.valueOf(decibel + "db");
		            final String hz = String.valueOf(frequency + "Hz");
		            final String sehz = String.valueOf(sefrequency + "Hz(2th)");
		            final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
		            double times = (((double)(frameBytes.length/(44100d*16*1)))*8)*i;
		            termTime = Math.floor(times);
            		//System.out.println(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+" "+sehz+" "+seamp);
		            int findFzTerm = 100;
		            //가장 첫 포먼트는 소리 녹음시 잘못 입력되는 값이라 첫번째 진폭값은 무시한다. 
		            if(sumCnt ==0) {
		            	sumCnt++;
		            	continue;
		            }
		            else if(maxAmp != 0.0 && maxAmp/sumCnt *2 < amplitude) {
		            	maxAmp+=amplitude;
		            }
		            //특정 주파수대역에서 연속적으로 발생해야 한다.
		            //분석하는 소리의 길이는 0.01초.
		            //아래는 0.01초 데이터를 0.1초 단위로 분석X

            		//System.out.println(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+" "+sehz+" "+seamp);
		            //소리가 평균 크기보다 클 때 분석한다.
		            //-> 이갈이는 소리 폭이 너무 커서 평균 측정 소리 때문에 측정이 안될 수 도 있다.-> 평균 소리보다 2배 이상 진폭이 크면 평균치에 합산 안함.
		            //if(amplitude > (maxAmp/sumCnt)) {
		            //if(amplitude > 1000) {
		            	//계속 발생되는 주파수의 0.1초간격으로 비교하여 비슷한 주파수 일때(+-50)가 연속되는 경우에 카운팅 한다.X
		            	//연속으로 높게 발생되는 진폭이 0.01초 단위로 2~7회 연속되는지 체크  
		            	
		            	//아직 기준 시간이 없으면 초기화
		            
		            	if(curTermTime == 0.0) {
		            		curTermTime = times;
		            		curTermDb = decibel;
		            		curTermAmp = amplitude;
		            		curTermHz = frequency;
		            		curTermSecondHz = sefrequency;
		            	}
		            
						if (decibel > chkDb  // 기준 데시벨 보다 커야 한다. 지금 이 수치는 -5.1이지만 다른 사운드를 테스트해보고 상대적인 수치로 바꿔야한다.
						) {
				            //System.out.println(times+" - "+termTime+" = "+String.valueOf(times-termTime));

							// 데시벨이 더 높고 주파수대역이 100의 자리에서 내림했을 때 동일하며, 0.02초 동안만 반복되어야 한다.(1번 반복)X
							if (
									//curTermDb >= decibel && // 비교기준이 되는 데시벨은 고점이어야 한다.X -> 고점에서 점차 데시벨이 내려오는것은 다른 사운드와 동일한 특징이다. 비슷한 대역의 소리가 계속 발생하는것을 찾아야한다.
									Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 //데시벨 오차 범위가 1db 이면 같은 이어지는 소리로 판단한다.
									
									||Math.floor(curTermSecondHz / 10) * 10 == Math.floor(sefrequency / 10) * 10
									||Math.floor(curTermSecondHz / 100) * 100 == Math.floor(sefrequency / 100) * 100
									
									||Math.floor(curTermHz / 10) * 10 == Math.floor(frequency / 10) * 10
									||Math.floor(curTermHz / 100) * 100 == Math.floor(frequency / 100) * 100
									
									||Math.floor(curTermAmp / 10) * 10 == Math.floor(amplitude / 10) * 10
									||Math.floor(curTermAmp / 100) * 100 == Math.floor(amplitude / 100) * 100 
									) { 
								continueAmp++;
								/*
								System.out.println(String.format("%.2f", times) + "s " 
								+ Math.abs(curTermDb) + ":" 
								+ Math.abs(decibel)+ ":" 
								+ Math.floor(curTermHz / 10) * 10 + ":" 
								+ Math.floor(frequency / 10) * 10 + ", " 
								
								+ Math.floor(curTermHz / 100) * 100 + ":" 
								+ Math.floor(frequency / 100) * 100  + ", "
								
								+ Math.floor(curTermAmp / 10) * 10+":"
								+ Math.floor(amplitude / 10) * 10+", "
								+ Math.floor(curTermAmp / 100) * 100+":"
								+Math.floor(amplitude / 100) * 100 +"  "+continueAmp );
								*/
								curTermTime = times;
								
								curTermDb = decibel;
								curTermAmp = amplitude;
								curTermHz = frequency;
								curTermSecondHz = sefrequency;
								//System.out.println(String.format("%.2f", times) + "s " + hz + " " + db + " " + amp + " " + sehz + " " + seamp+","+continueAmp);
								/*
								System.out.println(Math.abs(curTermDb));
								System.out.println(Math.abs(decibel));
								System.out.println(Math.abs(Math.abs(curTermDb) - Math.abs(decibel)));
								System.out.println(Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 5);
								*/
								// 주파수 대역이 100의 자리에서 내림했을 때 동일한가?
								
								if (Math.floor(curTermSecondHz / 10) * 10 == Math.floor(sefrequency / 10) * 10||
										Math.floor(curTermSecondHz / 100) * 100 == Math.floor(sefrequency / 100) * 100) {
									/*
									System.out.println("==========SECOND AMP CHECK STA==============");
									System.out.println(String.format("%.2f", times) + "s " + hz + " " + db + " " + amp + " " + sehz + " " + seamp);
									System.out.println( curTermDb + " " + Math.floor(sefrequency / 10) * 10);
									System.out.println( Math.floor(curTermSecondHz / 10) * 10 + " " + Math.floor(sefrequency / 10) * 10);
									System.out.println( Math.floor(curTermSecondHz / 100) * 100 + " " + Math.floor(sefrequency / 100) * 100);
									System.out.println("==========SECOND AMP CHECK END==============");
									*/
									continueAmp++;
								}else {
									if (continueAmp == 1) { // 특정 주파수로 1번 반복하는 것만 체크하자. 주파수가 동일한데 1번 연속되어야 하며 2번이상은 안된다.
										gcl.add(new GrinderClass(String.format("%.2f", curTermTime), continueAmp, curTermHz,
												curTermSecondHz, curTermDb, curTermAmp));
										findedTimeCnt++;
										continueAmp = 0;
									} else {
										continueAmp = 0;
									}
								}
								
							} else {
/*
								System.out.println("==========STARTING AMP CHECK STA==============");
								System.out.println(String.format("%.2f", times) + "s " + hz + " " + db + " " + amp + " " + sehz + " " + seamp);
								System.out.println( curTermDb + " " + decibel);
								System.out.println( Math.floor(curTermSecondHz / 10) * 10 + " " + Math.floor(sefrequency / 10) * 10);
								System.out.println( Math.floor(curTermSecondHz / 100) * 100 + " " + Math.floor(sefrequency / 100) * 100);
								System.out.println("==========STARTING AMP CHECK END==============");
								*/
								 //if(continueAmp<=7 && continueAmp>=2) { 
								if (continueAmp == 1 || continueAmp == 2) { // 진폭, 주파수, 데시벨 각각 or 조건으로 1번 반복하는 것만 체크하자. 주파수가 동일한데 1번 연속되어야 하며 2번이상은 안된다.
									gcl.add(new GrinderClass(String.format("%.2f", curTermTime), continueAmp, curTermHz, curTermSecondHz, curTermDb, curTermAmp));
								} else {
									gclOpp.add(new GrinderClass(String.format("%.2f", curTermTime), continueAmp, curTermHz, curTermSecondHz, curTermDb, curTermAmp));
								}
								continueAmp = 0;

							}
							//System.out.println(String.format("%.2f", times) + "s " + hz + " " + db + " " + amp + " " + sehz + " " + seamp);
							//System.out.println( Math.floor(curTermSecondHz / 10) * 10 + " " + Math.floor(sefrequency / 10) * 10);
						} else {
							// 특정 데시벨보다 낮음으로, 현재 분석된 데이터가 이갈이 인지 체크한다.
							 //if(continueAmp<=7 && continueAmp>=2) {
							if (continueAmp == 1 || continueAmp == 2) { // 진폭, 주파수, 데시벨 각각 or 조건으로 1번 반복하는 것만 체크하자. 주파수가 동일한데 1번 연속되어야 하며 2번이상은 안된다.
								gcl.add(new GrinderClass(String.format("%.2f", curTermTime), continueAmp, curTermHz, curTermSecondHz, curTermDb, curTermAmp));
							} else {
								gclOpp.add(new GrinderClass(String.format("%.2f", curTermTime), continueAmp, curTermHz, curTermSecondHz, curTermDb, curTermAmp));
							}
							continueAmp = 0;
						}

						// 연속된 진폭 카운트가 0 이면, 스팩토그램정보를 현재 스팩토그램 정보로 초기화한다.
						if (continueAmp == 0) {
							curTermTime = times;
							curTermDb = decibel;
							curTermAmp = amplitude;
							curTermHz = frequency;
							curTermSecondHz = sefrequency;
						}
		            	//기준 분석 시간으로부터 0.1초 간격인가X
		            	
		            	if( times-curTermTime<0.1 ) { //1초로 변경하고 3번이상 유지되었는지를 계산X
		            		//비슷한 주파수 인가(+- 50)
			            	if( 
			            			//frequency >15 && frequency <400 &&
			            			frequency > curTermHz-findFzTerm && frequency < curTermHz +findFzTerm 
			            			&&
			            			sefrequency > curTermSecondHz-findFzTerm && sefrequency < curTermSecondHz +findFzTerm
			            			&& frequency != sefrequency && frequency!=0.0 && sefrequency!=0
			            			) {
			            		findedTimeCnt++;
			            		
			            	}	
			            	else if(decibel<=-30) {
		            			//System.out.println(amplitude);
			            		unchcekdFindedTimeCnt++;	
			            	}
			            	else {
			            		//비슷한 주파수가 아니면 무시
			            	}
		            	}
		            	//기준시간보다 0.1초가 지나있다면 초기화
		            	else {
		            		//0.1초동안 비슷한 주파수 대역이 1번 이상 발생한 데이터 축적
		            		//System.out.println("findedTimeCnt vs unchcekdFindedTimeCnt: "+findedTimeCnt+" vs" +unchcekdFindedTimeCnt);
		            		if(findedTimeCnt<=2&& unchcekdFindedTimeCnt>findedTimeCnt) {
		            			gcl.add(new GrinderClass(String.format("%.2f", curTermTime),findedTimeCnt ,curTermHz, 
										curTermSecondHz, curTermDb, curTermAmp));
		            			
		            			findedHzAr.add(curTermHz);
			            		findedTimeAr.add(String.format("%.2f", curTermTime));
			            		findedTimeCntAr.add(findedTimeCnt);
			            		
		            		}
		            		curTermTime = 0.0;
		            		curTermHz = 0.0;
		            		curTermSecondHz = 0.0;
		            		findedTimeCnt = 0;
		            		unchcekdFindedTimeCnt = 0;
		            		
		            	}
		            
		            //}
			        targetStream.close();
			        i++;
			        sumCnt++;
				}
				System.out.println("audio length(s): "+((double)(audioData.length/(44100d*16*1)))*8);
				int audioLth = (int) (((double)(audioData.length/(44100d*16*1)))*8);
				System.out.println("grindArr: "+Arrays.toString(gcl.toArray()));
				int sumPerTime = 0;		
				int sumOppCnt = 0;
				termTime = 0.0;		
				s = 0;
				termTime = ++s * 1;
				HashMap<Integer, Integer> timesValue = new HashMap<Integer,Integer>();
				for(int j = 0 ; j <= audioLth ; j ++) {
					timesValue.put(j, 0);
				}
				for(GrinderClass gc : gcl) {
					//System.out.println("all :"+gc.toString());
				}
				for(GrinderClass gc : gcl) {
					sumPerTime = timesValue.get((int)Math.floor(Double.parseDouble(gc.getFindedTime())));
					timesValue.put((int) Math.floor(Double.parseDouble(gc.getFindedTime())), sumPerTime+1);
				}
				for (Object obj : timesValue.entrySet()) {
				    Map.Entry<Integer, Integer> entry = (Map.Entry) obj;
				    /*
				    System.out.print("~초: " + entry.getKey());
				    System.out.println(", 횟수: " + entry.getValue());
				    */
				}
				HashMap<Integer, Integer> oppTimesValue = new HashMap<Integer,Integer>();
				for(int j = 0 ; j <= audioLth ; j ++) {
					oppTimesValue.put(j, 0);
				}
				for(GrinderClass gc : gclOpp) {
					sumPerTime = oppTimesValue.get((int)Math.floor(Double.parseDouble(gc.getFindedTime())));
					oppTimesValue.put((int) Math.floor(Double.parseDouble(gc.getFindedTime())), sumPerTime+1);
				}

				for (Object obj : oppTimesValue.entrySet()) {
				    Map.Entry<Integer, Integer> entry = (Map.Entry) obj;
				    /*
				    System.out.print("~초: " + entry.getKey());
				    System.out.println(", 횟수: " + entry.getValue());
				    */
				}
				

				//1. 진폭, 데시벨, 주파수를 이용해서 0.01초 단위로 1번만 발생하거나 2번 연속발생한 포먼트를 측정하고,
				//2. 위 포먼트가 1초동안 얼만큼 발생했는지를 카운트를 체크, 기준치는 3으로 잡았다.
				//3. 3번 이상 연속발생한 포먼트의 카운트를 체크, 기준치는 69로 잡았다.
				//-> 즉 1번의 포먼트가 1초동안 3번 발생했는지 체크, 동시에 3회이상연속한 포먼트 카운트가 69를 넘어야함. 이 현상이 수초(2초) 동안 발생해야 함 
				//연속 카운트
				int continueCnt = 0 ;
				int classficationCnt = 0;
				
				for (Object obj : timesValue.entrySet()) {
				    Map.Entry<Integer, Integer> entry = (Map.Entry) obj;
				    /*System.out.println(entry.getValue()+","+oppTimesValue.get(entry.getKey()));*/
				    if(entry.getValue()>=2 && oppTimesValue.get(entry.getKey()) >= 60) {
				    	continueCnt++;
				    }else {
				    	if(continueCnt>1) {
				    		classficationCnt++;
				    		System.out.println("진폭, 데시벨, 주파수를 이용해서 0.01초 단위로 1번만 발생하거나 2번 연속발생한 포먼트는 ");
				    		System.out.println(entry.getKey()-continueCnt+"~"+entry.getKey()+"초,"+continueCnt+"발생");
				    	}
				    	continueCnt=0;
				    }
				}
				//System.out.println("grindCnt: "+gcl.size());
				System.out.println("classficationCnt: "+classficationCnt);
				//System.out.println("sumfindedTimeCnt: "+sumfindedTimeCnt/gcl.size());
				double lowerMinHz = 0.0;
				double lowerMaxHz = 0.0;
				double higherMinHz = 0.0;
				double higherMaxHz = 0.0;
				for(GrinderClass gc : gcl) {
					if(gc.getFindedHz()<1000 && gc.getFindedTimeCnt()>1) {
						if(lowerMinHz == 0.0) {
							lowerMinHz = gc.getFindedHz();	
						}
						if(lowerMaxHz == 0.0) {
							lowerMaxHz = gc.getFindedHz();	
						}
						if(gc.getFindedHz()>lowerMaxHz) {
							lowerMaxHz = gc.getFindedHz();
						}
						if(gc.getFindedHz()<=lowerMinHz) {
							lowerMinHz = gc.getFindedHz();
						}
						//System.out.println("lower :"+gc.toString());
					}
				}
				for(GrinderClass gc : gcl) {
					if(gc.getFindedHz()>=1000 && gc.getFindedTimeCnt()>1) {
						if(higherMinHz == 0.0) {
							higherMinHz = gc.getFindedHz();	
						}
						if(higherMaxHz == 0.0) {
							higherMaxHz = gc.getFindedHz();	
						}	
						if(gc.getFindedHz()>higherMaxHz) {
							higherMaxHz = gc.getFindedHz();
						}
						if(gc.getFindedHz()<=higherMinHz) {
							higherMinHz = gc.getFindedHz();
						}
						//System.out.println("higher :"+gc.toString());
					}
				}
				/*
				System.out.println("findedTimeAr: "+Arrays.toString(findedTimeAr.toArray()));
				System.out.println("findedTimeCntAr: "+Arrays.toString(findedTimeCntAr.toArray()));
				System.out.println("findedHzAr: "+Arrays.toString(findedHzAr.toArray()));
				
				System.out.println("lower range :"+lowerMinHz+"~"+lowerMaxHz);
				System.out.println("higher range :"+higherMinHz+"~"+higherMaxHz);
				*/
		    } catch (IOException e) {
				e.printStackTrace();
			}

	            /*무호흡증은 항상 코골이를 동반하며, 코골이의 시작하고 종료한 시간은 5~10초이내 숨을 쉬어야 하기 때문에 호흡이 가파른 느낌이 있다, 무호흡 코골이 시간이 종료한 후 숨을 멈추는 시간이 30~50초 이내이다.
	             * 1.db세기로 무호흡코골이 및 호흡으로 측정되는 부분을 특정한다. 
	             * 2. 호흡은 0.7초 간격은 0.2초
	             * 3. 2번이 유지되면 무호흡코골이 혹은 호흡하는 구간이다.
	             * 4. 3번을 유지하지 않는경우 일정 db이하로 30~50초 동안 유지되는지를 체크함으로써 무호흡 구간인지 특정한다. 
	             * */ 
				audioCalculator = new AudioCalculator();
				frameBytes = new byte[snoringApi.frameByteSize];
				try {
					targetStream = new ByteArrayInputStream(audioData);
					int i = 1;
					int sumCnt = 0;
					int maxAmp = 0;
					double curTermHz = 0.0;
					double curTermSecondHz = 0.0;
					double curTermTime = 0.0;
					double curTermDb = 0.0;
					int curTermAmp = 0;
					double chkDb = -9;
					boolean isBreathTerm = false;
					boolean isOSATerm = false;
					int isBreathTermCnt = 0;
					int isBreathTermCntOpp = 0;
					int isOSATermCnt = 0;
					int isOSATermCntOpp = 0;
					String beforeTermWord="";
					String BREATH = "breath";
					String OSA = "osa";
					while ((read = targetStream.read(frameBytes)) > 0) {
						if (frameBytes == null) {
							frameBytes = new byte[snoringApi.frameByteSize];
						}
						audioCalculator.setBytes(frameBytes);
						int amplitude = audioCalculator.getAmplitude();
						double decibel = audioCalculator.getDecibel();
						double frequency = audioCalculator.getFrequency();
						double sefrequency = audioCalculator.getFrequencySecondMax();
						int sefamplitude = audioCalculator.getAmplitudeNth(audioCalculator.getFreqSecondN());

						final String amp = String.valueOf(amplitude + "Amp");
						final String db = String.valueOf(decibel + "db");
						final String hz = String.valueOf(frequency + "Hz");
						final String sehz = String.valueOf(sefrequency + "Hz(2th)");
						final String seamp = String.valueOf(sefamplitude + "Amp(2th)");
						double times = (((double) (frameBytes.length / (44100d * 16 * 1))) * 8) * i;
						/*
						System.out.print(String.format("%.2f", times) + "s " + hz + " " + db + " " + amp + " " + sehz + " " + seamp);
						System.out.print(" isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+" ");
						System.out.println(" isBreathTermCnt: " + isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+" ");
						*/
						// 가장 첫 포먼트는 소리 녹음시 잘못 입력되는 값이라 첫번째 진폭값은 무시한다.
						if (sumCnt == 0) {
							sumCnt++;
							continue;
						}

						// 2. 기준 데시벨보다 높은 소리라면 호흡(혹은 코골이) 구간인지 체크한다.
						if (decibel > chkDb) {
							// 2-1. 데시벨을 이용해서 연속된 소리인지 체크한다.
							// 2-1-1. 연속된 소리인지 체크하기 위해서는 비슷한 데시벨인지만 체크한다.
							//        (주파수나 진폭은 0.01초 단위로 상이하기 때문에 팩터로 이용할 수 없음.)
							// 2-1-2. 비슷한 데시벨은 기준 정보의 데시벨에서 +-1db로 하며, 체크될 경우 숨쉬는 구간 카운트를 1씩 증가한다. 
							//        체크가 안되면  숨쉬기 아님 카운트를 1씩 증가한다. 숨쉬기구간은 true 된다.
							/*
							if (Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // 데시벨 오차 범위가 1db인지만 체크
							) {
								isBreathTermCnt++;
								isBreathTerm = true;
								// 2-1-2-1. 숨쉬기 구간이 true가 될 때, 무호흡 구간이 true인 경우, 무호흡 구간을 false로 바꾸며, 
								//          이때 기준 정보의 시간은 무호흡 시작시간, 종료시간은 무호흡 종료시간이 된다.
								//          무호흡 구간 카운트 로깅을 하고 무호흡 구간 카운트를 초기화 한다.
								if(isOSATerm == true) {
									System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
									curTermTime = times;
									isOSATerm = false;
									isOSATermCnt = 0;
								}
							} else {
								isBreathTermCntOpp++;
							}
							*/
							if(isOSATerm == true) {
								//무호흡에서 호흡으로 넘어오는 경우 오차범위가 3초는 넘어야 무호흡구간으로 본다.
								if(beforeTermWord.equals(BREATH) && isOSATermCnt>500 ) {
									/*
									if(beforeTermWord.equals(OSA)) {
										System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
									}else {
										System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
										curTermTime = times;
									}
									*/
									System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
									curTermTime = times;
									isOSATerm = false;
									isOSATermCnt = 0;
									isOSATermCntOpp = 0;
									//beforeTermWord=OSA;
								}else {
									//System.out.println("[ignore term, "+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
									//curTermTime = time;
									isOSATerm = false;
									isOSATermCnt = 0;
									isOSATermCntOpp = 0;
									//beforeTermWord=OSA;
								}
							}else {
								isBreathTermCnt++;
								isBreathTerm = true;
							}
							// 2-1-3. 숨쉬기 아님 카운트가 20을 넘으면(0.2초가 초과되면), 숨쉬는 구간 카운트가 70(0.7초) 미만일 시, 
							//        숨쉬는 구간 카운트, 숨쉬기 아님 카운트를 0으로 초기화 한다. 숨쉬기구간은 false가 된다.
						}else {
							// 3. 기준 데시벨보다 낮은 소리인 경우는 무호흡 중인지 체크한다.
							// 3-1. 숨쉬기 구간이 false인 경우, 무호흡 구간 카운트를 증가하며, 무호흡 구간은 true가 된다.
							if(isBreathTerm == false) {
								isOSATermCnt++;
								isOSATerm = true;
							}else {
							//3-1-2. 숨쉬기 구간이 true인 경우에는 숨쉬기 아님 카운트를 증가시킨다.
								isBreathTermCntOpp++;
								isOSATermCntOpp++; 
							}
						}

						if(isBreathTermCntOpp>20) {
							if(isBreathTermCnt<70) {
								//일정 데시벨 이상이고, 숨쉬기 카운트의 0.2초 오차가 발생한 데이터로 무시함.
								//System.out.println("[ignore term, "+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " + isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
								//curTermTime = time;
								isBreathTermCnt = 0;
								isBreathTermCntOpp = 0;
								isBreathTerm = false;
								//beforeTermWord=BREATH;
							}else {
								// 2-1-3-1. 기준 정보의 기준 시간이 숨쉬기 시작시간, 현재 시간은 숨쉬기 종료 시간
								//        (이 시간은 한번 호흡이지 무호흡 사이의 구간을 의미하지는 않는다.)
								/*
								if(beforeTermWord.equals(BREATH)) {
									System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " + isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
								}else {
									System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " + isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
									curTermTime = times;
								}
								*/
								//System.out.println("["+String.format("%.2f", curTermTime) + "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " + isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
								curTermTime = times;
								isBreathTermCnt = 0;
								isBreathTermCntOpp = 0;
								isBreathTerm = false;
								beforeTermWord=BREATH;
							}
						}
						//1. 연속된 소리가 되는 기준 정보를 초기화 한다.
						//1-1. 기준 정보의 기준 시간은 기준 시간이 0이거나, 숨쉬는 구간 카운트가 0이며, 숨쉬기 아님 카운트가 0일 경우 초기화 한다.
						if (curTermTime == 0 || (isBreathTermCnt == 0 && isOSATermCnt == 0)) {
							curTermTime = times;
						}
						// 1-1. 기준 정보의 데시벨만 계속 초기화 한다. (주파수나 진폭은 0.01초 단위로 상이하기 때문에 팩터로 이용할 수 없음, 로그용으로
						// 초기화)
						curTermDb = decibel;
						curTermAmp = amplitude;
						curTermHz = frequency;
						curTermSecondHz = sefrequency;
						targetStream.close();
						i++;
						sumCnt++;
					}
					System.out.println("audio length(s): " + ((double) (audioData.length / (44100d * 16 * 1))) * 8);
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

		// setVisible(true);
	}

	public static byte[] inputStreamToByteArray(InputStream is) {

		byte[] resBytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int read = -1;
		try {
			while ((read = is.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}

			resBytes = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resBytes;
	}

	public byte[] getFrameBytes(byte[] buffer) {

		// analyze sound
		int totalAbsValue = 0;
		short sample = 0;
		short[] tmp = new short[frameByteSize];
		// float averageAbsValue = 0.0f;
		AlarmStaticVariables.absValue = 0.0f;

		for (int i = 0; i < frameByteSize; i += 2) {
			sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
			tmp[i] = sample;
			totalAbsValue += Math.abs(sample);
		}
		AlarmStaticVariables.absValue = totalAbsValue / frameByteSize / 2;
/*
		Message msg = new Message();
		msg.obj = AlarmStaticVariables.absValue;
		showhandler.sendMessage(msg);
*/
		for (int i = 0; i < buffer.length; i++) {
			totalBuf[cnt++] = buffer[i];
		}

		// ----------save into buf----------------------
		short[] tmpBuf = new short[buffer.length
				/ AlarmStaticVariables.rateX];
		for (int i = 0, ii = 0; i < tmpBuf.length; i++, ii = i
				* AlarmStaticVariables.rateX) {
			tmpBuf[i] = tmp[ii];
		}
		synchronized (AlarmStaticVariables.inBuf) {//
			AlarmStaticVariables.inBuf.add(tmpBuf);// add data
		}
		// ----------save into buf----------------------

		//System.out.println(cnt + " vs " + AlarmStaticVariables.sampleSize);
		if (cnt > AlarmStaticVariables.sampleSize) {
			cnt = 0;
			return totalBuf;
		} else
			return null;
		// return buffer;
	}
}
