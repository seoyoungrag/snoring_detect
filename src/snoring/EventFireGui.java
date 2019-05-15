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
import java.util.LinkedList;
import java.util.List;

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

public class EventFireGui extends JFrame {

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
	
	public EventFireGui(String filePath) {
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
			//String filePath = "raw/180335__sankalp__snoring-3.wav"; //코골이 인식함
			//String filePath = "raw/425582__deleted-user-2731495__freesound-mix-recording-1.wav";//코골이 아닌데 인식한지 모르겠음, 여러 소리가 섞여있음.
			//String filePath = "raw/50292__freesound__144-fake-rain-normalized.wav"; //코골이 아니며, 아니라고 인식함.
			//String filePath = "raw/25263__freesound__orgasm-ses2.wav"; //Invalid Wave Header
			//String filePath = "raw/114609__daxter31__snoring.wav"; //WaveHeader: only supports bitsPerSample 8 or 162
			//String filePath = "raw/182758__poorenglishjuggler__snoring.wav"; //코골이 인식함
			//String filePath = "raw/432996__mattyharm__male-snore-2.wav"; //Invalid Wave Header0
			//String filePath = "raw/466792__hoganthelogan__snoring.wav";//Invalid Wave Header
			//String filePath = "raw/345687__inspectorj__comedic-whistle-b.wav"; //코골이 아니며, 아니라고 인식함.
			//String filePath = "raw/234867__mlsulli__snoring.wav";
			//String filePath = "raw/340995__filmscore__young-woman-snoring.wav";
			//String filePath = "raw/341042__vikuserro__snore.wav";
			//44100 16 1 
			/*filePath = "raw/235873__delphidebrain__sjuulke-snoring-1.wav";
			

			filePath = "raw/401334__ckvoiceover__man-coughing.wav";
			filePath = "raw/178997__bigtexan7213__coughing.wav";
			filePath = "raw/252240__reitanna__real-cough.wav";
			filePath = "raw/41386__sandyrb__db-cough-002.wav";
			filePath = "raw/348364__frostyfrost__cough.wav";
			filePath = "raw/155858__rutgermuller__footsteps-in-factory-hall-on-wood-and-concrete.wav";
			filePath = "raw/194825__macphage__gravel3.wav";
			filePath = "raw/180535__suz-soundcreations__footsteps-snow-mono-44-16.wav";
			filePath = "raw/259639__stevious42__footsteps-in-street-woman.wav";
			filePath = "raw/267499__purplewalrus23__footsteps-on-rough-gravel.wav";
			filePath = "raw/175954__freefire66__footsteps.wav";
			filePath = "raw/195132__philter137__walking-from-wood-to-path.wav";
			filePath = "raw/259646__stevious42__footsteps-in-the-street.wav";
			filePath = "raw/48212__slothrop__footsteps.wav";
			filePath = "raw/198962__mydo1__footsteps-on-wood.wav";
			filePath = "raw/223152__yoyodaman234__glass-footstep-1.wav";
			filePath = "raw/238608__shart69__talking-creature.wav";
			filePath = "raw/428777__pauliperez1999__bimbo-girl-3.wav";
			filePath = "raw/460651__noamp2003__roomtone-kids-talking.wav";
			filePath = "raw/164606__steveukguy__class-a-female-talking-1.wav";
			filePath = "raw/61036__timtube__talking-3.wav";
			filePath = "raw/361928__toiletrolltube__161009-0085-lw-radio.wav";
			filePath = "raw/61945__noisecollector__radio.wav";
			filePath = "raw/156220__framixo__radionoize-0.wav";
			filePath = "raw/69012__lex0myko1__am-147-305mhz.wav";
			filePath = "raw/431118__inspectorj__door-front-closing-a.wav";
			filePath = "raw/205951__ryding__alarm-01.wav";
			filePath = "raw/93639__benboncan__personal-alarm.wav";
			filePath = "raw/244917__kwahmah-02__house-alarm.wav";
			filePath = "raw/345230__embracetheart__ceiling-fan-indoor.wav";
			filePath = "raw/435518__sromon__swtch-and-start-the-fan-ambiance.wav";
			filePath = "raw/40621__acclivity__sleepingbeauty.wav";
			filePath = "raw/77267__sagetyrtle__catsnores.wav";
			filePath = "raw/180535__suz-soundcreations__footsteps-snow-mono-44-16.wav";
			filePath = "raw/259639__stevious42__footsteps-in-street-woman.wav";
			filePath = "raw/63103__robinhood76__00555-snoring-1-heavy-breath.wav";
			filePath = "raw/69329__robinhood76__00966-baby-snoring-2.wav";
			filePath = "raw/61605__andune__schnauf.wav";
			filePath = "raw/235873__delphidebrain__sjuulke-snoring-1.wav";
			filePath = "raw/40621__acclivity__sleepingbeauty.wav";
			filePath = "raw/77267__sagetyrtle__catsnores.wav";
			filePath = "raw/20545__sirplus__snore.wav";
			filePath = "raw/377119__ejking17__20170112-the-zzz.wav";*/
			fin = new FileInputStream(filePath); 		
			Wave wave = new Wave(fin);
			waveHeader = wave.getWaveHeader();
			/*
			System.out.println(waveHeader.getChannels());
			System.out.println(waveHeader.getBitsPerSample());
			System.out.println(waveHeader.getSampleRate());
			*/
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
	            /*
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
			            if(frequency>1000) {
		            	System.out.println(String.format("%.2f", times)+"s "
		            			+ hz +" "+db+" "+amp+" "
		            			//+sehz+" "+seamp
		            			);
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
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }*/
	            //이갈이, 이갈이는 높은 주파수가 굉장히 짧은 간격으로 여러번 나타난다.
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
	            int findedTimeCnt = 0;
	            int unchcekdFindedTimeCnt = 0;
	            List<GrinderClass> gcl = new ArrayList<GrinderClass>();
				//List<String> findedTimeAr = new ArrayList<String>();
				//List<Integer> findedTimeCntAr = new ArrayList<Integer>();
				//List<Double> findedHzAr = new ArrayList<Double>();
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
		            int findFzTerm = 100;
		            //가장 첫 포먼트는 요상한 값이라 첫번째 진폭값은 무시한다. 
		            //녹음할떄는 아래 로직 무시해도 됨.
		            if(sumCnt ==0) {
		            	sumCnt++;
		            	continue;
		            }
		            else if(maxAmp != 0.0 && maxAmp/sumCnt *2 < amplitude) {
		            	maxAmp+=amplitude;
		            }
		            //특정 주파수대역에서 연속적으로 발생해야 한다.
		            //분석하는 소리의 길이는 0.01초.
		            //아래는 0.01초 데이터를 0.1초 단위로 분석
		            
		            //소리가 평균 크기보다 클 때 분석한다.
		            //-> 이갈이는 소리 폭이 너무 커서 평균 측정 소리 때문에 측정이 안될 수 도 있다.
		            if(amplitude > (maxAmp/sumCnt)) {
		            //if(amplitude > 1000) {
		            	//계속 발생되는 주파수의 0.1초간격으로 비교하여 비슷한 주파수 일때(+-50)가 연속되는 경우에 카운팅 한다.
		            	
		            	//아직 기준 시간이 없으면 초기화
		            	if(curTermTime == 0.0) {
		            		curTermTime = times;
		            		curTermHz = frequency;
		            		curTermSecondHz = sefrequency;
		            	}
		            	
		            	//기준 분석 시간으로부터 0.1초 간격인가
		            	//if( times-curTermTime<0.1 ) {
		            	if( times-curTermTime<0.1 ) { //1초로 변경하고 3번이상 유지되었는지를 계산
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
		            			gcl.add(new GrinderClass(String.format("%.2f", curTermTime),findedTimeCnt ,curTermHz, curTermSecondHz)); 
		            			/*
		            			findedHzAr.add(curTermHz);
			            		findedTimeAr.add(String.format("%.2f", curTermTime));
			            		findedTimeCntAr.add(findedTimeCnt);
			            		*/
		            		}
		            		curTermTime = 0.0;
		            		curTermHz = 0.0;
		            		curTermSecondHz = 0.0;
		            		findedTimeCnt = 0;
		            		unchcekdFindedTimeCnt = 0;
		            		
		            	}
		            }
			        targetStream.close();
			        i++;
			        sumCnt++;
				}
				System.out.println("audio length(s): "+((double)(audioData.length/(44100d*16*1)))*8); 
				System.out.println("grindArr: "+Arrays.toString(gcl.toArray()));
				//int sumfindedTimeCnt = 0;
				for(GrinderClass gc : gcl) {
					//sumfindedTimeCnt+=gc.getFindedTimeCnt();
						//System.out.println("all :"+gc.toString());
				}
				System.out.println("grindCnt: "+gcl.size());
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
						System.out.println("lower :"+gc.toString());
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
						System.out.println("higher :"+gc.toString());
					}
				}
				/*System.out.println("findedTimeAr: "+Arrays.toString(findedTimeAr.toArray()));
				System.out.println("findedTimeCntAr: "+Arrays.toString(findedTimeCntAr.toArray()));
				System.out.println("findedHzAr: "+Arrays.toString(findedHzAr.toArray()));
				*/
				System.out.println("lower range :"+lowerMinHz+"~"+lowerMaxHz);
				System.out.println("higher range :"+higherMinHz+"~"+higherMaxHz);
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
