package snoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.musicg.api.DetectionApi;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

public class SnoringApi extends DetectionApi {

	private WaveHeader waveHear;
	private byte[] data;
	private double[] amplitudes;
	private double threshold_E;
	private double threshold_ZCR;
	private double[] E = null;
	private double[] ZCR = null;
	private double MAX_ZCR;
	private double MIN_ZCR;
	private double AVER_ZCR;
	private double MAX_E;
	private double MIN_E;
	private double AVER_E;
	private int sampleRange = 7;

	public SnoringApi(WaveHeader waveHeader) {
		super(waveHeader);
		this.waveHeader = waveHeader;
	}

	protected void init() {
		// settings for detecting a whistle
		//minFrequency = 0.0f;
		minFrequency = 0.0f;
		//maxFrequency = 7500.0f;// Double.MAX_VALUE;
		maxFrequency = 20.0f;

		minIntensity = 100.0f;
		maxIntensity = 100000.0f;

		minStandardDeviation = 0.01f;
		maxStandardDeviation = 29.98f;
		// 4238740267052 2599952140684
		//highPass = 100;
		highPass = 0;
		//lowPass = 10000;
		lowPass = 1900;

		minNumZeroCross = 0;
		maxNumZeroCross = 1267;

		numRobust = 10;
	}

	public int isSnoring(byte[] audioBytes) {
		// return isSpecificSound(audioBytes);
		int cnt = 0;
		this.data = audioBytes; // byte, AudioRecord에서 녹음한 로우데이터, 인코딩은 PCM_16BIT, 채널은 MONO 방식.
		Wave wave = new Wave(waveHeader, audioBytes); // audio bytes of this // 헤더와 녹음 로우데이터를 이용해 Wave 객체를 생성
														// frame
		// this.amplitudes = wave.getSampleAmplitudes();
		this.amplitudes = wave.getNormalizedAmplitudes();// amplitues는 double[]임.
		// normalized amplitues는 정규화된 진폭이란 의민데, 뭔진 잘 모르겠고 2차원 배열로 double값들이 담긴다.
		//System.out.println("[yrseo]this.amplitudes = wave.getNormalizedAmplitudes();: " + amplitudes.length);
		//System.out.println("[yrseo]normalized amplitues는 정규화된 진폭이란 의민데, 아마 버퍼의 진폭값들인 것 같고, 2차원 배열로 double값들이 담긴다.");
		//System.out.println("[yrseo]waveHeader.getSampleRate(): " + waveHeader.getSampleRate());

		setE_ZCRArray(100, 50);
		//System.out.println("[yrseo]this.E와 this.ZCR에 값을 할당하는데, this.E에는 amplitudes[i,j]의 제곱값(pow(n,2))들이 누적되고,");
		//System.out.println("[yrseo]this.ZCR에는 amplitudes[i,j]가 amplitudes[i,j+1] 보다 값이 큰 경우에만 1씩 증가한다.");
		//System.out.println("[yrseo]this.E.length: " + this.E.length);
		//System.out.println("[yrseo]this.ZCR.length: " + this.ZCR.length);
		try {
			//System.out.println("[yrseo]this.E[0]: " + this.E[0]);
			//System.out.println("[yrseo]this.ZCR[0]: " + this.ZCR[0]);
			//System.out.println("[yrseo]리스트 this.E: " + Arrays.toString(this.E));
			//System.out.println("[yrseo]리스트 this.ZCR: " + Arrays.toString(this.ZCR));
			//System.out.println("[yrseo]this.E[this.E.length]: " + this.E[this.E.length - 1]);
			//System.out.println("[yrseo]this.ZCR[this.ZCR.length]: " + this.ZCR[this.ZCR.length - 1]);
		} catch (Exception e) {

		}
		// amplitudes[i,j]에 대한 반복문을 돌릴때 i는 for (int i = 4; i < this.amplitudes.length -
		// length; i += length - overlap) {
		// this.amplitudes.length 712548 수치가 나옴(테스트폰에서)
		// length와 overlap 은
		// int length = (sampleRate / 1000) * length_time; //44100 / 1000 * 100 = 4410
		// int overlap = (sampleRate / 1000) * overlap_time; //44100 / 1000 * 50 = 2205
		// -> for ( int i = 4 < 712548 - 4410; i += 4410-2205)
		// j는 for (int j = 0; j < length; j++) {
		// -> for ( int j = 0; j < 4410 ; j++)
		//System.out.println("[yrseo]max_e;: " + this.getMAX_E());
		//System.out.println("[yrseo]min_e;: " + this.getMIN_E());

		cal_threshold();
		// setE_ZCRArray에서 this.E[]의 max값과 min값이 결정되고
		//System.out.println("[yrseo]threshold_E는, 0.02 * (this.getMAX_E() - this.getMIN_E()) + this.getMIN_E();");
		//System.out.println("[yrseo]threshold_ZCR는 1 * this.getAVER_ZCR() 이 된다.");
		//System.out.println("[yrseo]threshold_E: " + this.getThreshold_E());
		//System.out.println("[yrseo]threshold_ZCR: " + this.getThreshold_ZCR());

		float[] res = getSnoring();
		//System.out.println("[yrseo]this.E[]가 threshold_E보다 크고 this.ZCR이 threshold_ZCR보다 작은 경우가");
		// //System.out.println("[yrseo]가장 많이 반복되는 횟수를 저장 및 체크된 인덱스 리스트를 저장하고 리스트를
		// 반환한다.");
		//System.out.println("[yrseo]연속된 횟수의 리스트를 생성한다.리스트 중 0.0은 있을 수 있다.");
		//System.out.println("[yrseo]리스트: " + Arrays.toString(res));

		// //System.out.println(Arrays.toString(res));
		int num = res.length / 2;
		//System.out.println("[yrseo]AlarmStaticVariables.snoringCount :" + AlarmStaticVariables.snoringCount);
		//System.out.println("[yrseo]이후 로직에서는 상단의 리스트 중 값이 sampleRange(7)보다 큰 횟수가 sampleCount(2)보다 많은 경우이면 AlarmStaticVariables.snoringCount에 4를 할당한다.");
		//System.out.println("[yrseo]sampleRange: " + sampleRange);
		//System.out.println("[yrseo]AlarmStaticVariables.sampleCount: " + AlarmStaticVariables.sampleCount);
		//System.out.println("[yrseo]위 로직 이후 다시 이 메소드를 실행하면 AlarmStaticVariables.snoringCount가 4가 된 경우일 수 있고, 이 때는 if의 상단 부분 로직을 수행한다.");
		//System.out.println("[yrseo]AlarmStaticVariables.snoringCount를 증가시키거나 초기화하기 위한 로직이 수행된다.");
		if (AlarmStaticVariables.snoringCount > 0) {
			boolean ctn = true;
			for (int i = 0; i < res.length; i++) {
				// //System.out.println("[yrseo]ctn :"+ctn);
				// //System.out.println("[yrseo]res[i] :"+res[i]);
				// //System.out.println("[yrseo]sampleRange :"+sampleRange);
				if (ctn && res[i] >= sampleRange) {
					AlarmStaticVariables.snoringCount++;
					if (AlarmStaticVariables.snoringCount >= AlarmStaticVariables.sampleCount) {
						//System.out.println("[yrseo]return here");
						//return 4;
						return 1;
					}
				} else if (!ctn && res[i] >= sampleRange) {
					cnt++;
					if (cnt >= AlarmStaticVariables.sampleCount) {
						//System.out.println("[yrseo]return 5 here");
						//return 4;
						return 1;
					}
				} else if (res[i] < sampleRange) {
					ctn = false;
					AlarmStaticVariables.snoringCount = 0;
					cnt = 0;
				}
			}
		} else {
			for (int i = 0; i < res.length; i++) {
				if (res[i] >= sampleRange) {
					cnt++;
					if (cnt >= AlarmStaticVariables.sampleCount) {
						//System.out.println("[yrseo]return 5 here");
						//return 4;
						return 1;
					}
				} else
					cnt = 0;
			}
		}

		//System.out.println("cnt=" + cnt);
		return cnt;
	}

    public int isSnoringSpectogramChk(byte[] audioBytes) {
    	int isSpecificSoundCnt = 0;
		int read = 0;
		InputStream targetStream = new ByteArrayInputStream(audioBytes);
		byte[] frameBytes = new byte[frameByteSize];
	    try {
			while( ( read = targetStream.read( frameBytes ) ) > 0 ){
				frameBytes = getFrameBytes(frameBytes);
				if(frameBytes != null) {
					//System.out.println("[yrseo]isSpecificSound(frameBytes): "+isSpecificSound(frameBytes));
					isSpecificSoundCnt++;
				}else {
					frameBytes = new byte[frameByteSize];
				}
			}
	        targetStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return isSpecificSoundCnt;
    }
	private int getMax(int[] tmpMax) {
		int m = 0;
		for (int i = 0; i < tmpMax.length; i++)
			if (tmpMax[i] > m)
				m = tmpMax[i];
		return m;
	}

	public void setThreshold_E(double value) {
		this.threshold_E = value;
	}

	public double getThreshold_E() {
		return this.threshold_E;
	}

	public void setThreshold_ZCR(double value) {
		this.threshold_ZCR = value;
	}

	public double getThreshold_ZCR() {
		return this.threshold_ZCR;
	}

	public void setMIN_ZCR(double value) {
		this.MIN_ZCR = value;
	}

	public void setMAX_ZCR(double value) {
		this.MAX_ZCR = value;
	}

	public void setAVER_ZCR() {
		double sum = 0;
		for (int i = 0; i < this.ZCR.length; i++)
			sum += this.ZCR[i];
		this.AVER_ZCR = sum / this.ZCR.length;
	}

	public double getMIN_ZCR() {
		return this.MIN_ZCR;
	}

	public double getMAX_ZCR() {
		return this.MAX_ZCR;
	}

	public double getAVER_ZCR() {
		return this.AVER_ZCR;
	}

	public void setMAX_E(double value) {
		this.MAX_E = value;
	}

	public void setMIN_E(double value) {
		this.MIN_E = value;
	}

	public void setAVER_E() {
		double sum = 0;
		for (int i = 0; i < this.E.length; i++)
			sum += this.E[i];
		this.AVER_E = sum / this.E.length;
	}

	public double getMAX_E() {
		return this.MAX_E;
	}

	public double getMIN_E() {
		return this.MIN_E;
	}

	public double getAVER_E() {
		return this.AVER_E;
	}

	public void setE_ZCRArray(int length_time, int overlap_time) {// ms
		// int test = this.waveHear.getSampleRate();
		int sampleRate;
		if (this.waveHeader == null) {
			sampleRate = 0;// never happened
		} else {
			sampleRate = this.waveHeader.getSampleRate();
		}
		// //System.out.println("sampleRate=" + sampleRate);
		int length = (sampleRate / 1000) * length_time; // 44100 / 1000 * 100
		int overlap = (sampleRate / 1000) * overlap_time; // 44100 / 1000 * 50
		int count_e = 0;
		int num_E = (this.data.length + (length - overlap) - overlap) / (length - overlap) + 1;
		// //System.out.println("[yrseo]num_E :"+num_E);
		double tmp_energy[] = new double[num_E];
		double tmp_ZCR[] = new double[num_E];
		// //System.out.println("[yrseo]this.amplitudes.length - length; i += length -
		// overlap) :"+ (this.amplitudes.length)+"-"+length);
		for (int i = 4; i < this.amplitudes.length - length; i += length - overlap) {
			double sum_slice = 0;
			double sum_ZCR = 0;
			for (int j = 0; j < length; j++) {
				sum_slice += Math.pow(this.amplitudes[i + j], 2);
				// //System.out.println("[yrseo]sum_slice :"+sum_slice);
				// //System.out.println("[yrseo]this.amplitudes[i + j] :"+this.amplitudes[i + j]);
				if ((this.amplitudes[i + j] > 0) != (this.amplitudes[i + j + 1] > 0))
					sum_ZCR++;
				// //System.out.println("[yrseo]sum_ZCR :"+sum_ZCR);
			}
			if (sum_slice == 0 && sum_ZCR == 0)
				continue;
			if (count_e == 0) {
				this.setMAX_E(sum_slice);
				this.setMIN_E(sum_slice);
				this.setMAX_ZCR(sum_ZCR);
				this.setMIN_E(sum_ZCR);
			} else {
				if (sum_slice > this.getMAX_E())
					this.setMAX_E(sum_slice);
				if (sum_slice < this.getMIN_E())
					this.setMIN_E(sum_slice);
				if (sum_ZCR > this.getMAX_ZCR())
					this.setMAX_ZCR(sum_ZCR);
				if (sum_ZCR < this.getMIN_ZCR())
					this.setMIN_ZCR(sum_ZCR);
			}
			tmp_energy[count_e] = sum_slice;
			// //System.out.println("[yrseo]tmp_energy[count_e] :"+count_e+" "+sum_slice);
			tmp_ZCR[count_e] = sum_ZCR;
			// //System.out.println("[yrseo]tmp_energy[count_e] :"+count_e+" "+sum_slice);
			count_e++;
		}
		this.E = new double[count_e];
		this.ZCR = new double[count_e];
		for (int i = 0; i < count_e; i++) {
			this.E[i] = tmp_energy[i];
			this.ZCR[i] = tmp_ZCR[i];
		}
		this.setAVER_E();
		this.setAVER_ZCR();
		// for(int i=0; i< this.E.length; i++)
		// //System.out.println(this.E[i]);
	}

	public void cal_threshold() {

		float a = (float) 0.02;
		float b = (float) 8000;
		float c = (float) 1;
		double I_1 = a * (this.getMAX_E() - this.getMIN_E()) + this.getMIN_E();
		double I_2 = b * this.getMIN_E();
		/*
		 * if(I_1<I_2) this.threshold_E = I_1; else this.threshold_E = I_2;
		 */
		this.setThreshold_E(I_1);
		this.setThreshold_ZCR(c * this.getAVER_ZCR());

		// for(int i=0; i<this.ZCR.length; i++)
		// //System.out.println(this.ZCR[i]);
		// //System.out.println(this.getThreshold_E());
		// //System.out.println(this.getThreshold_ZCR());
	}

	public float[] getSnoring() {
		ArrayList<Float> snoring_time = new ArrayList<Float>();// s
		boolean flag = false;
		int count = 0;
		for (int i = 0; i < this.E.length; i++) {
			if (this.E[i] > this.getThreshold_E() && this.ZCR[i] < this.getThreshold_ZCR()) {
				if (flag == false) {
					// snoring_time.add((float) (i / 20.0));
					flag = true;
					// //System.out.println(i);
				} else {
					count++;
				}
			} else {
				if (flag == true) {
					flag = false;
					snoring_time.add((float) (count));
					count = 0;
				}
			}
		}
		float[] res = new float[snoring_time.size()];
		for (int i = 0; i < snoring_time.size(); i++)
			res[i] = snoring_time.get(i);
		return res;
	}

	public boolean getSpectogram(byte[] audioBytes) {
		int bytesPerSample = this.waveHeader.getBitsPerSample() / 8;
		int numSamples = audioBytes.length / bytesPerSample;
		//System.out.println("[yrseo-numSamples]" + numSamples);
		//System.out.println("[yrseo-Integer.bitCount(numSamples)]" + Integer.bitCount(numSamples));
		/*
		 * if (numSamples <= 0 || Integer.bitCount(numSamples) != 1) {
		 * //System.out.println("The sample size must be a power of 2"); return false; }
		 */
		this.fftSampleSize = numSamples;
		this.numFrequencyUnit = this.fftSampleSize / 2;
		this.unitFrequency = (double) this.waveHeader.getSampleRate() / 2.0D / (double) this.numFrequencyUnit;
		this.lowerBoundary = (int) ((double) this.highPass / this.unitFrequency);
		this.upperBoundary = (int) ((double) this.lowPass / this.unitFrequency);
		Wave wave = new Wave(this.waveHeader, audioBytes);
		short[] amplitudes = wave.getSampleAmplitudes();
		Spectrogram spectrogram = wave.getSpectrogram(this.fftSampleSize, 0);
		double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();
		double[] spectrum = spectrogramData[0];
		////System.out.println("[yrseo-nSpectogram]" + Arrays.deepToString(spectrogram.getNormalizedSpectrogramData()));
		////System.out.println("[yrseo-aSpectogram]" + Arrays.deepToString(spectrogram.getAbsoluteSpectrogramData()));
		/*
		 * int frequencyUnitRange = this.upperBoundary - this.lowerBoundary + 1;
		 * double[] rangedSpectrum = new double[frequencyUnitRange];
		 * System.arraycopy(spectrum, this.lowerBoundary, rangedSpectrum, 0,
		 * rangedSpectrum.length); if (frequencyUnitRange <= spectrum.length) { if
		 * (this.isPassedIntensity(spectrum) &&
		 * this.isPassedStandardDeviation(spectrogramData) &&
		 * this.isPassedZeroCrossingRate(amplitudes) &&
		 * this.isPassedFrequency(rangedSpectrum)) { return true; } } else {
		 * System.err.println("is error: the wave needed to be higher sample rate"); }
		 */
		/*
		 * } else { //System.out.println("The sample size must be a power of 2"); }
		 */
		return false;
	}

    public boolean isSpecificSound(byte[] audioBytes) {
        int bytesPerSample = this.waveHeader.getBitsPerSample() / 8;
        int numSamples = audioBytes.length / bytesPerSample;
		//System.out.println("[yrseo-bytesPerSample]" + bytesPerSample);
		//System.out.println("[yrseo-numSamples]" + numSamples);
		//System.out.println("[yrseo-Integer.bitCount(numSamples)]" + Integer.bitCount(numSamples));
        if (numSamples > 0 && Integer.bitCount(numSamples) == 1) {
            this.fftSampleSize = numSamples;
    		//System.out.println("[yrseo-fftSampleSize]" + fftSampleSize);
            this.numFrequencyUnit = this.fftSampleSize / 2;
            this.unitFrequency = (double)this.waveHeader.getSampleRate() / 2.0D / (double)this.numFrequencyUnit;
    		//System.out.println("[yrseo-unitFrequency]" + unitFrequency);
            this.lowerBoundary = (int)((double)this.highPass / this.unitFrequency);
    		//System.out.println("[yrseo-lowerBoundary]" + lowerBoundary);
            this.upperBoundary = (int)((double)this.lowPass / this.unitFrequency);
    		//System.out.println("[yrseo-upperBoundary]" + upperBoundary);
            Wave wave = new Wave(this.waveHeader, audioBytes);
            short[] amplitudes = wave.getSampleAmplitudes();
            Spectrogram spectrogram = wave.getSpectrogram(this.fftSampleSize, 0);
            double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();
            double[] spectrum = spectrogramData[0];
            int frequencyUnitRange = this.upperBoundary - this.lowerBoundary + 1;
            double[] rangedSpectrum = new double[frequencyUnitRange];
    		//System.out.println("[yrseo-spectrum.length]" + spectrum.length);
    		//System.out.println("[yrseo-rangedSpectrum.length]" + rangedSpectrum.length);
            System.arraycopy(spectrum, this.lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);
            if (frequencyUnitRange <= spectrum.length) {
                ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
                double robustFrequency = (double)arrayRankDouble.getMaxValueIndex(spectrum) * this.unitFrequency;
                /*System.out.println("!!!!!!!!getMaxValueIndex"+(double)arrayRankDouble.getMaxValueIndex(spectrum));
                System.out.println("!!!!!!!!spectrum getMaxValueIndex"+spectrum[arrayRankDouble.getMaxValueIndex(spectrum)]);
                System.out.println("!!!!!!!!spectrum.length"+spectrum.length);
                System.out.println("!!!!!!!!this.unitFrequency"+this.unitFrequency);
                System.out.println("!!!!!!!!"+robustFrequency);*/
                if (this.isPassedIntensity(spectrum) && this.isPassedStandardDeviation(spectrogramData) && this.isPassedZeroCrossingRate(amplitudes) && this.isPassedFrequency(rangedSpectrum)) {
                    return true;
                }
            } else {
                System.err.println("is error: the wave needed to be higher sample rate");
            }
        } else {
            //System.out.println("The sample size must be a power of 2");
        }

        return false;
    }
	int frameByteSize = 1024; // for 1024 fft size (16bit sample size)
	public byte[] getFrameBytes(byte[] buffer){
		//audioRecord.read(buffer, 0, frameByteSize);
		
		// analyze sound
		int totalAbsValue = 0;
        short sample = 0; 
        float averageAbsValue = 0.0f;
        
        for (int i = 0; i < frameByteSize; i += 2) {
            sample = (short)((buffer[i]) | buffer[i + 1] << 8);
			////System.out.println("[yrseo-sample] "+sample);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;

        ////System.out.println(averageAbsValue);
        
        // no input
        if (averageAbsValue < 30){
        	return null;
        }
        
		return buffer;
	}
}
