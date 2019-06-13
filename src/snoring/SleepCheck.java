package snoring;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SleepCheck {

	static double curTermHz = 0.0;
	static double curTermSecondHz = 0.0;
	static double curTermTime = 0.0;
	static double OSAcurTermTime = 0.0;
	static double curTermDb = 0.0;
	static int curTermAmp = 0;
	static double grindChkDb = -10;

	static double chkOSADb = -9;
	static boolean isBreathTerm = false;
	static boolean isOSATerm = false;
	static int isBreathTermCnt = 0;
	static int isBreathTermCntOpp = 0;
	static int isOSATermCnt = 0;
	static int isOSATermCntOpp = 0;
	static String beforeTermWord = "";
	static String BREATH = "breath";
	static String OSA = "osa";

	static int checkTerm = 0; // 1�� 0.01��
	static int grindingRepeatOnceAmpCnt = 0;
	static int grindingRepeatAmpCnt = 0;
	static int grindingContinueAmpCnt = 0;
	static int grindingContinueAmpOppCnt = 0;

	static int snoringCheckCnt = 0;
	static int snoringContinue = 0;
	static int snoringContinueOpp = 0;
	
	static int checkTermSecond = 0;
	static int curTermSecond = 0;

	static int GRINDING_RECORDING_CONTINUE_CNT = 1;
	
	static double decibelSum = 0;
	static double decibelSumCnt = 0;
	
	static int EXCEPTION_DB_FOR_AVR_DB = -10;
	static int AVR_DB_CHECK_TERM = 2000;
	static double MAX_DB_CRIT_VALUE = -31.5;
	static double MIN_DB_CRIT_VALUE = -(31.5-(31.5*35/120)); //http://www.noiseinfo.or.kr/about/info.jsp?pageNo=942 ������ ����(���鿡 ���� ���� ����) 35, 40���� ��������
	static int NOISE_DB_INIT_VALUE = -10;
	static int NOISE_DB_CHECK_TERM = 1*100*60;

	static int noiseChkSum = 0;
	static int noiseNoneChkSum = 0;
	static int noiseChkCnt = 0;
	static int noiseChkForStartSum = 0;
	static int noiseNoneChkForStartSum = 0;
	static int noiseChkForStartCnt = 0;
	
	static double GrindingCheckTermSecond = 0;
	static double GrindingCheckStartTermSecond = 0;
	static double GrindingCheckStartTermDecibel = 0;
	static boolean GrindTermCheckBoolean = false;
	
	static double MAX_DB = -31.5;
	static double MIN_DB = 0;
	
	static boolean isSnoringStart = false;
	
	static boolean isOSATermTimeOccur = false;
	static boolean isOSAAnsStart = false;
	/*
	static double getAvrDB(double decibel) {
		double avrDB = -AVR_DB_INIT_VALUE;
		if (decibelSumCnt >= AVR_DB_CHECK_TERM || decibelSumCnt == 0) {
			decibelSum = 0;
			decibelSumCnt = 0;
		}
		if (decibel < EXCEPTION_DB_FOR_AVR_DB) {
			decibelSum += decibel;
			decibelSumCnt++;
		}
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		return avrDB;
	}
*/
	/*
	static double getAvrDB() {
		double avrDB = -AVR_DB_INIT_VALUE;
		if (decibelSumCnt >= AVR_DB_CHECK_TERM || decibelSumCnt == 0) {
			decibelSum = 0;
			decibelSumCnt = 0;
		}
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		return avrDB;
	}
	 */

	static double getMinDB() {
		/*
		double avrDB = -AVR_DB_INIT_VALUE;
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		//System.out.print(decibelSum+" "+decibelSumCnt+" "+avrDB+" ");
		*/
		return MIN_DB/2 > MIN_DB_CRIT_VALUE ? Math.floor(MIN_DB_CRIT_VALUE) : MIN_DB/2;
	}

	static double setMinDB(double decibel) {
		//10�и��� ��� ���ú��� �ٽ� ����Ѵ�.
		if(Math.abs(decibel) != 0 && decibel < MIN_DB) {
			MIN_DB = decibel;
		}
		/*
		if (decibelSumCnt >= AVR_DB_CHECK_TERM) {
			decibelSum = 0;
			decibelSumCnt = 0;
		}
		double avrDB = -AVR_DB_INIT_VALUE;
		decibelSum += decibel;
		decibelSumCnt ++;
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		*/
		return MIN_DB/2 > MIN_DB_CRIT_VALUE ? Math.floor(MIN_DB_CRIT_VALUE) : MIN_DB/2;
	}
	static double getMaxDB() {
		/*
		double avrDB = -AVR_DB_INIT_VALUE;
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		//System.out.print(decibelSum+" "+decibelSumCnt+" "+avrDB+" ");
		*/
		return MAX_DB*2 < MAX_DB_CRIT_VALUE ? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
	}

	static double setMaxDB(double decibel) {
		//10�и��� ��� ���ú��� �ٽ� ����Ѵ�.
		if(Math.abs(decibel) != 0 && decibel > MAX_DB) {
			MAX_DB = decibel-1;
		}
		if (decibelSumCnt >= AVR_DB_CHECK_TERM) {
			decibelSumCnt = 0;
			MAX_DB = -31.5;
			MIN_DB = 0;
		}
		decibelSumCnt ++;
		/*
		if (decibelSumCnt >= AVR_DB_CHECK_TERM) {
			decibelSum = 0;
			decibelSumCnt = 0;
		}
		double avrDB = -AVR_DB_INIT_VALUE;
		decibelSum += decibel;
		decibelSumCnt ++;
		if (decibelSum != 0 && decibelSumCnt != 0) {
			avrDB = decibelSum / decibelSumCnt;
		}
		*/
		return MAX_DB*2 < MAX_DB_CRIT_VALUE? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
	}
	static int noiseCheck(double decibel) {
		//1�е��� �Ҹ��� �߻����� �ʾҴ��� üũ�Ѵ�.
		//0.01�� ����������, 6000�� �ؾ� 60����.
		//1���� �Ǿ�����, ���ú����� ���� �Ҹ��� �߻����� ���� ���
		if(noiseChkCnt>=6000) {
			int tmpN = noiseChkSum;
			noiseChkCnt = 0;
			noiseChkSum = 0;
			noiseNoneChkSum = 0;
			return tmpN;
		}else {
			//���� 1���� �ȵǾ����� ��� �Ҹ� üũ�� �Ѵ�.
			//�Ҹ� üũ�� 1�е��� ��� ���ú����� ���� �Ӱ� ���ú��� �Ҹ��� �߻��ߴ����� üũ�Ѵ�.
			//������ 0�̸� ���� �����ϰ� �Ǿ�����.X
			if(decibel >= getMinDB()) {
				//noiseChkCnt++;
				noiseChkSum++;
			}else {
				noiseNoneChkSum++;
			}
			noiseChkCnt++;
			return 6001;
			//return noiseChkCnt;
		}
		
	}

	static int noiseCheckForStart(double decibel) {
		//1�е��� �Ҹ��� �߻����� �ʾҴ��� üũ�Ѵ�.
		//0.01�� ����������, 6000�� �ؾ� 60����.
		//1���� �Ǿ�����, ���ú����� ���� �Ҹ��� �߻����� ���� ���
		if(noiseChkForStartCnt>=200) {
			int tmpN = noiseChkForStartSum;
			noiseChkForStartCnt = 0;
			noiseChkForStartSum = 0;
			noiseNoneChkForStartSum = 0;
			return tmpN;
		}else {
			//���� 1���� �ȵǾ����� ��� �Ҹ� üũ�� �Ѵ�.
			//�Ҹ� üũ�� 1�е��� ��� ���ú����� ���� ���ú��� �Ҹ��� �߻��ߴ����� üũ�Ѵ�.
			//������ 0�̸� ���� �����ϰ� �Ǿ�����.
			if(decibel >= getMinDB()) {
				//noiseChkCnt++;
				noiseChkForStartSum++;
			}else {
				noiseNoneChkForStartSum++;
			}
			noiseChkForStartCnt++;
			return -1;
			//return noiseChkCnt;
		}
		
	}
	static int snoringCheckNew(String times, double decibel, double frequency, double sefrequency, double[] allFHAndDB) {
		
		//allFhAndDB�� ��� ���� decibel ���� ���� �ٸ� ���̺귯���� ���� �־�, ������ ���� �ʴ´�.
		//allFhAndDB�� ������ ���� ���ϳ� �ִ� 81���� �ּҴ� -67 ������(, decibel ~31.5~0����
		//allFhAndDB�� ������ �� ũ�ϱ� allFhAndDB �����ؼ� ���̸� ���Ѵ�.
	    DecimalFormat df = new DecimalFormat("0.00");
	    double d1 = -(31.5-allFHAndDB[0]/255*31.5);
    	System.out.print(times+"\t");
    	System.out.print(decibel+"\t");
    	System.out.print(frequency+"\t");
    	//System.out.print(df.format(d1)+"\t");
    	//System.out.print(df.format(allFHAndDB[0])+"\t");
    	System.out.println(df.format(Math.abs(decibel-d1)));
    	if(Math.abs(decibel-d1)>Math.abs(decibel)/2){
    		return 1;
    	}else {
    		return 0;
    	}
	}

	static int snoringCheck(double decibel, double frequency, double sefrequency) {
		System.out.println(decibel+" "+frequency);
		return 0;
	}
	static int grindingCheck(double times, double decibel, double frequency, double sefrequency) {
		// �̰���, �̰��̴� ���� ���ļ��� ������ ª�� �������� ������ ��Ÿ����.
		// �Ʒ� 1,2,3 ����, �ٽ�-> �̰��̴� 0.02~0.07�� ������ ū ������ ���´�. ��, 0.01�� ������ �м��ؼ� �������� 2~7ȸ��
		// ������ �߻��ϴ� ���� ������ ��.
		// �� ��, ������ ��ġ�� �ǹ̰� ���� �� �����Ƿ� ���� �ݺ�Ƚ���θ� ��ƺ���.
		// 1. Ư�� ���ļ����� ���� ���ļ��� --> Ư�����ļ� a=?
		// 2. Ư�� �ð����� Ư�� Ƚ������ ���� �ݺ��Ǵ��� --> Ư���ð� b=?, Ư��Ƚ�� c=?
		// 3. üũ�Ǹ� �̰��̴�.

		// Ư�� ���ļ��뿪���� ���������� �߻��ؾ� �Ѵ�.
		// �м��ϴ� �Ҹ��� ���̴� 0.01��.
		// �Ʒ��� 0.01�� �����͸� 0.1�� ������ �м�X

		// �Ҹ��� ��� ũ�⺸�� Ŭ �� �м��Ѵ�.
		// -> �̰��̴� �Ҹ� ���� �ʹ� Ŀ�� ��� ���� �Ҹ� ������ ������ �ȵ� �� �� �ִ�.-> ��� �Ҹ����� 2�� �̻� ������ ũ�� ���ġ��
		// �ջ� ����.
		// if(amplitude > (maxAmp/sumCnt)) {
		// if(amplitude > 1000) {
		// ��� �߻��Ǵ� ���ļ��� 0.1�ʰ������� ���Ͽ� ����� ���ļ� �϶�(+-50)�� ���ӵǴ� ��쿡 ī���� �Ѵ�.X
		// �������� ���� �߻��Ǵ� ������ 0.01�� ������ 2~7ȸ ���ӵǴ��� üũ

		// System.out.println(times+" - "+termTime+" =
		// "+String.valueOf(times-termTime));
		//System.out.println(String.format("%.3f", times)+" "+checkTermSecond+" "+curTermSecond);
		// ���ú��� �� ���� ���ļ��뿪�� 100�� �ڸ����� �������� �� �����ϸ�, 0.02�� ���ȸ� �ݺ��Ǿ�� �Ѵ�.(1�� �ݺ�)X
		//System.out.println("grindingChkDb:" +decibel +"vs" + getMinDB()*1.1+" ");
		if (decibel > getMinDB()*0.55
		// curTermDb >= decibel && // �񱳱����� �Ǵ� ���ú��� �����̾�� �Ѵ�.X -> �������� ���� ���ú��� �������°��� �ٸ�
		// ����� ������ Ư¡�̴�. ����� �뿪�� �Ҹ��� ��� �߻��ϴ°��� ã�ƾ��Ѵ�.
				/*&& (
		Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // ���ú� ���� ������ 1db �̸� ���� �̾����� �Ҹ��� �Ǵ��Ѵ�.
		//���ļ��� ���ƾ���.
		&& (
				// 100���� ū ��� ���� �ڸ� ���� ������ ���Ѵ�.
				(frequency > 100 && curTermHz > 100 && (int)curTermHz / 100 != (int)frequency / 100 ) 
				||
				// 100���� ���� ��� ���� �ڸ� ���� ������ ���Ѵ�.
				(frequency < 10 && curTermHz < 10 && (int)curTermHz / 10 != (int)frequency / 10 )
				||
				// 100���� ū ��� ���� �ڸ� ���� ������ ���Ѵ�.
				(sefrequency > 100 && curTermSecondHz > 100 && (int)curTermSecondHz / 100 != (int)sefrequency / 100 ) 
				||
				// 100���� ���� ��� ���� �ڸ� ���� ������ ���Ѵ�.
				(sefrequency < 10 && curTermSecondHz < 10 && (int)curTermSecondHz / 10 != (int)sefrequency / 10 )
				
				)
				)
				*/
				){
			//0.01�� ������ �������� �ݺ��ϴ� ���� ī��Ʈ ����
			//�ſ� ª�� ���� ���� ���� ���ú��� ���� �ȴٰ� �ϰ�, �� ������ 0.02���̸�, 0.02�ʰ� ������, üũ���� �̰��� ��� ���ú��� ���� 2���ú��� ���̳���, �ݴ� ī��Ʈ�� �����Ѵ�. 
			if(grindingRepeatOnceAmpCnt==0) {
				GrindingCheckStartTermDecibel = decibel;
			}else {
				GrindingCheckStartTermDecibel = (GrindingCheckStartTermDecibel+decibel) / grindingRepeatOnceAmpCnt;
			}
			if(grindingRepeatOnceAmpCnt>=2) {
				if(decibel > grindingRepeatOnceAmpCnt) {
					grindingContinueAmpOppCnt++;	
				}else {
					grindingRepeatOnceAmpCnt++;
				}
			}else {
				grindingRepeatOnceAmpCnt++;
			}
			//System.out.println(String.format("%.2f", times) + "s " + frequency + " " + decibel + " " + amplitude + " " + sefrequency + " " +grindingContinueAmpCnt);
			
			/*
			System.out.println("==========GRIND CHECK STA==============");
			System.out.println(String.format("%.2f", times) + "s " + frequency + " " + decibel + " " + amplitude + " " + sefrequency + " " );
			System.out.println( curTermDb + " " + Math.abs(decibel));

			System.out.println( (int)curTermHz / 100+ " " + (int)frequency / 100);
			System.out.println( (int)curTermHz%100 / 10+ " " + (int)frequency%100 / 10);
			System.out.println((frequency > 100 && curTermHz > 100 && (int)curTermHz / 100 != (int)frequency / 100 ) 
					||
					// 100���� ���� ��� ���� �ڸ� ���� ������ ���Ѵ�.
					(frequency < 10 && curTermHz < 10 && (int)curTermHz / 10 != (int)frequency / 10 ));
			System.out.println( (int)curTermSecondHz / 100+ " " + (int)sefrequency / 100);
			System.out.println( (int)curTermSecondHz%100 / 10+ " " + (int)sefrequency%100 / 10);
			System.out.println((sefrequency > 100 && curTermSecondHz > 100 && (int)curTermSecondHz / 100 != (int)sefrequency / 100 ) 
					||
					// 100���� ���� ��� ���� �ڸ� ���� ������ ���Ѵ�.
					(sefrequency < 10 && curTermSecondHz < 10 && (int)curTermSecondHz / 10 != (int)sefrequency / 10 ));
			System.out.println( (int)curTermAmp / 100+ " " + (int)amplitude / 100);
			System.out.println( (int)curTermAmp%100 / 10+ " " + (int)amplitude%100 / 10);
			System.out.println("==========GRIND CHECK END==============");
			*/
		} else {
			//��ȣ�� �ٲ� �� ��ȣ�� �ݺ��� ī��Ʈ�� 1�� ��쿡�� ��ȿī��Ʈ�� �����Ѵ�.
			//System.out.println(String.format("%.2f", times) + "s " + grindingRepeatOnceAmpCnt);
			//System.out.println(String.format("%.2f", times) + "s " + frequency + " " + decibel + " " + amplitude + " " + sefrequency + " " +grindingContinueAmpCnt);
			if (grindingRepeatOnceAmpCnt <= 4 && grindingRepeatOnceAmpCnt>=2) {
				if(grindingContinueAmpCnt == 0) {
					GrindingCheckStartTermSecond = times;
				}
				grindingContinueAmpCnt++;
				//System.out.println(String.format("%.2f", times) + "s " + frequency + " " + decibel + " " + amplitude + " " + sefrequency + " " +grindingContinueAmpCnt);
			}
			grindingContinueAmpOppCnt++;	
			grindingRepeatOnceAmpCnt = 0;
		}

		// 1. ����, ���ú�, ���ļ��� �̿��ؼ� 0.01�� ������ 1���� �߻��ϰų� 2�� ���ӹ߻��� ����Ʈ�� �����ϰ�,
		// 2. �� ����Ʈ�� 1�ʵ��� ��ŭ �߻��ߴ����� ī��Ʈ�� üũ, ����ġ�� 3���� ��Ҵ�.
		// 3. 3�� �̻� ���ӹ߻��� ����Ʈ�� ī��Ʈ�� üũ, ����ġ�� 69�� ��Ҵ�.
		// -> �� 1���� ����Ʈ�� 1�ʵ��� 3�� �߻��ߴ��� üũ, ���ÿ� 3ȸ�̻󿬼��� ����Ʈ ī��Ʈ�� 69�� �Ѿ����. �� ������ ����(2��)
		// ���� �߻��ؾ� ��
		// ���� ī��Ʈ
		//System.out.println(String.format("%.3f", times)+" "+checkTermSecond+" "+curTermSecond+" "+grindingContinueAmpCnt+" "+grindingContinueAmpOppCnt);

		//if (checkTerm % 100 == 0) { //�м������� 0.11, 0.12�ʶ� 100�̶�� ��ġ�� ������ �߻��Ѵ�.
		//�ʸ� ����ؼ� ó���ϵ��� ��.
		//System.out.println(curTermSecond + " "+checkTermSecond+" "+grindingContinueAmpCnt+" "+grindingContinueAmpOppCnt);
		if (Math.floor((GrindingCheckTermSecond - GrindingCheckStartTermSecond)*100) == 101) {
			//System.out.println(curTermSecond + "~"+checkTermSecond+"s, grindingContinueAmpCnt:"+grindingContinueAmpCnt+", grindingContinueAmpOppCnt:"+grindingContinueAmpOppCnt+", grindingRepeatAmpCnt:"+grindingRepeatAmpCnt);
			if(grindingContinueAmpCnt >= 3
					&& grindingContinueAmpCnt <=15 
					&& grindingContinueAmpOppCnt >= 50
					) {
				grindingRepeatAmpCnt++;
				//System.out.println(curTermSecond + " "+checkTermSecond+" "+grindingContinueAmpCnt+" "+grindingContinueAmpOppCnt+" "+grindingRepeatAmpCnt);
			}else {
				grindingRepeatAmpCnt = 0;
				//System.out.println("����8");
			}
			grindingContinueAmpCnt = 0;
			grindingContinueAmpOppCnt = 0;
		}

		//������ ��ȯ�ϰ�, 2~3�� �̻� ���ӵǴ����� ���⼭ �Ǵ����� �ʴ´�.
		/*
		if(grindingRepeatAmpCnt>=2) {
			System.out.println(curTermSecond + " "+checkTermSecond+" "+grindingContinueAmpCnt+" "+grindingContinueAmpOppCnt+" "+grindingRepeatAmpCnt);
			grindingRepeatAmpCnt = 0;
			//grindingContinueAmpCnt=0;
			//grindingContinueAmpOppCnt = 0;
			return 1;
		}
		*/

		//1�� ������ ��ȿī��Ʈ�� ��ȸ���� üũ�Ѵ�. ��ȿī��Ʈ�� ������ �ش��ϸ�, ���ʵ��� ���ӵǴ��� üũ�Ѵ�.
		/*
		if (checkTerm % 100 == 0) {
			if (//checkTerm % 100 == 0 &&
					grindingRepeatAmpCnt >= 2 
					//&& grindingContinueAmpCnt >= 1 && grindingContinueAmpCnt <=5 && grindingContinueAmpOppCnt >= 60
					) {
				int tmpI = grindingRepeatAmpCnt;
				grindingRepeatAmpCnt=0;
				grindingRepeatOnceAmpCnt = 0;
				grindingContinueAmpCnt = 0;
				grindingContinueAmpOppCnt = 0;
				return tmpI;
			}
			grindingContinueAmpCnt = 0;
			grindingContinueAmpOppCnt = 0;
		}else {
			//System.out.println(String.format("%.2f", times) + "s "+checkTerm % 100+" "+grindingRepeatAmpCnt+" "+grindingContinueAmpCnt+" "+grindingContinueAmpOppCnt);
			if(grindingContinueAmpCnt >= 1 
					&& grindingContinueAmpCnt <=6 
					&& grindingContinueAmpOppCnt >= 70) {
				
				grindingRepeatAmpCnt++;
				grindingContinueAmpCnt = 0;
				grindingContinueAmpOppCnt = 0;
			}
		}
		*/
		return 0;
	}

	/*
	 * ��ȣ������ �׻� �ڰ��̸� �����ϸ�, �ڰ����� �����ϰ� ������ �ð��� 5~10���̳� ���� ����� �ϱ� ������ ȣ���� ���ĸ� ������ �ִ�,
	 * ��ȣ�� �ڰ��� �ð��� ������ �� ���� ���ߴ� �ð��� 30~50�� �̳��̴�. 1.db����� ��ȣ���ڰ��� �� ȣ������ �����Ǵ� �κ���
	 * Ư���Ѵ�. 2. ȣ���� 0.7�� ������ 0.2�� 3. 2���� �����Ǹ� ��ȣ���ڰ��� Ȥ�� ȣ���ϴ� �����̴�. 4. 3���� �������� �ʴ°��
	 * ���� db���Ϸ� 30~50�� ���� �����Ǵ����� üũ�����ν� ��ȣ�� �������� Ư���Ѵ�.
	 */
	static int OSACheck(double times, double decibel, int amplitude, double frequency, double sefrequency) {
		// 2. ���� ���ú����� ���� �Ҹ���� ȣ��(Ȥ�� �ڰ���) �������� üũ�Ѵ�.
		//System.out.println("OSACheckDb:" +decibel +"vs" + getMinDB());
		if (decibel > getMinDB()*0.45) {
			// 2-1. ���ú��� �̿��ؼ� ���ӵ� �Ҹ����� üũ�Ѵ�.
			// 2-1-1. ���ӵ� �Ҹ����� üũ�ϱ� ���ؼ��� ����� ���ú������� üũ�Ѵ�.
			// (���ļ��� ������ 0.01�� ������ �����ϱ� ������ ���ͷ� �̿��� �� ����.)
			// 2-1-2. ����� ���ú��� ���� ������ ���ú����� +-1db�� �ϸ�, üũ�� ��� ������ ���� ī��Ʈ�� 1�� �����Ѵ�.
			// üũ�� �ȵǸ� ������ �ƴ� ī��Ʈ�� 1�� �����Ѵ�. �����ⱸ���� true �ȴ�.
			/*
			 * if (Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // ���ú� ���� ������
			 * 1db������ üũ ) { isBreathTermCnt++; isBreathTerm = true; // 2-1-2-1. ������ ������
			 * true�� �� ��, ��ȣ�� ������ true�� ���, ��ȣ�� ������ false�� �ٲٸ�, // �̶� ���� ������ �ð��� ��ȣ�� ���۽ð�,
			 * ����ð��� ��ȣ�� ����ð��� �ȴ�. // ��ȣ�� ���� ī��Ʈ �α��� �ϰ� ��ȣ�� ���� ī��Ʈ�� �ʱ�ȭ �Ѵ�. if(isOSATerm ==
			 * true) { System.out.println("["+String.format("%.2f", curTermTime) +
			 * "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
			 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); curTermTime = times;
			 * isOSATerm = false; isOSATermCnt = 0; } } else { isBreathTermCntOpp++; }
			 */

			if (isOSATerm == true) {
				// ��ȣ���� ȣ������ �Ѿ���� ��� ���������� 5�ʴ� �Ѿ�� ��ȣ�������� ����.
				if (beforeTermWord.equals(BREATH) && isOSATermCnt > 1000) {
					isOSATermTimeOccur=false;
					/*
					 * if(beforeTermWord.equals(OSA)) { System.out.println("["+String.format("%.2f",
					 * curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
					 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); }else {
					 * System.out.println("["+String.format("%.2f", curTermTime) +
					 * "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
					 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); curTermTime = times;
					 * }
					 */
					System.out.println("![" + String.format("%.2f", OSAcurTermTime) + "~" + String.format("%.2f", times)
							+ "s, isOSATermCnt: " + isOSATermCnt + ", isOSATermCntOpp:" + isOSATermCntOpp + "]");
					//�м��� ����Ǵ� ������ �տ��� �м��� �ð����κ��� 1���̻� �ʰ��� ��쿡 �����ϰ�, �ƴ� ��쿡�� ���� �ð��� end.times�� �߰��Ѵ�.
					if(EventFireGui.osaTermList.size()>1) {
						int beforeEndTime = (int) EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-2).end; //0�̰ų� ���� �ְų�
						int currentTime = (int) times;
						System.out.println(beforeEndTime +" "+ currentTime+"="+(currentTime-beforeEndTime));
						if(currentTime - beforeEndTime > 60) { 
							//System.out.println("���vo����");
							isOSAAnsStart = false;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).end=times;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).first = EventFireGui.firstDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).second = EventFireGui.secondDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).chk = EventFireGui.snoringDbChkCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).positiveCnt = isOSATermCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).negitiveCnt = isOSATermCntOpp;
						}else {
							System.out.println("1���� �� �������Ƿ�, ���vo�������� �ʰ�, �������vo�� �����Է�, ���� ���vo ����");
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-2).AnalysisRawDataList.addAll(
									EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).AnalysisRawDataList);
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).AnalysisRawDataList=EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-2).AnalysisRawDataList;
							EventFireGui.osaTermList.remove(EventFireGui.osaTermList.size()-1);
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).end=times;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).first = EventFireGui.firstDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).second = EventFireGui.secondDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).chk = EventFireGui.snoringDbChkCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).positiveCnt = isOSATermCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).negitiveCnt = isOSATermCntOpp;
						}
					}else { 
						System.out.println("�ڰ��̱��vo ����");
						isOSAAnsStart = false;
						//if(EventFireGui.osaTermList.size()>0) {
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size() - 1).end = times;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).first = EventFireGui.firstDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).second = EventFireGui.secondDecibelAvg;
							EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).chk = EventFireGui.snoringDbChkCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).positiveCnt = isOSATermCnt;
		    				EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).negitiveCnt = isOSATermCntOpp;
                        //}
					}
					
					double tmpD = OSAcurTermTime;
					OSAcurTermTime = times;
					isOSATerm = false;
					isOSATermCnt = 0;
					isOSATermCntOpp = 0;
					// beforeTermWord=OSA;
					return (int) (times-tmpD);
				} else {
					/*
					System.out.println("[ignore term, "+String.format("%.2f", curTermTime) +
					 "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]");
					*/
					//curTermTime = times;
					isOSATerm = false;
					isOSATermCnt = 0;
					isOSATermCntOpp = 0;
					// beforeTermWord=OSA;
				}
			} else {
				//���� ���ú��� ���� ���ú����� ũ��, ��ȣ�� ������ �ƴ� ����̴�. ���⼭ �ʱ�ȭ�� �ؾ��Ѵ�.
				//������ ��ȣ�� ������ �ƴ� ��� 0.01�� ������ ��� �̰��� Ÿ�� ������, �ѹ��� �ʱ�ȭ�� �� �־�� �Ѵ�. �ѹ� �ʱ�ȭ ������ �ٽ� ���ϵ��� �Ѵ�.
				//������ ������ �ѹ� �ʱ�ȭ �� ��� ���� �ʹ� ������ٴ� ������ �ִ�. �տ��������� 15�� �����͸� �����ϵ��� �Ѵ�.
				//15���� ������ ��ȣ���� �߻��ϴ� �������� ȣ��ð��� 15�� ���� �߻��ϱ� �����̴�.
				//�м��� ����Ǵ� ������ �տ��� �м��� �ð����κ��� 1���̻� �ʰ��� ��쿡 �����ϰ�, �ƴ� ��쿡�� ���� �ð��� end.times�� �߰��Ѵ�.
				//�ʱ�ȭ�� ������ �ְų�, �ʱ�ȭ�ϰ� 15�ʰ� ������?
				
				//��ȣ�� �߻��߳�?
				if(!isOSATermTimeOccur) {
					isOSAAnsStart = true;
					OSAcurTermTime = times;
					System.out.println("���vo����");
					EventFireGui.osaTermList.add(new StartEnd());
					EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).start=times;
					EventFireGui.osaTermList.get(EventFireGui.osaTermList.size() - 1).AnalysisRawDataList = new ArrayList<AnalysisRawData>();
					//EventFireGui.osaTermList.get(EventFireGui.osaTermList.size() - 1).AnalysisRawDataList.add(new AnalysisRawData(times, amplitude, EventFireGui.tmpMaxDb, frequency, sefrequency, 0));
					isOSATermTimeOccur = true;
				}
				
				isBreathTermCnt++;
				isBreathTerm = true;
			}
			// 2-1-3. ������ �ƴ� ī��Ʈ�� 20�� ������(0.2�ʰ� �ʰ��Ǹ�), ������ ���� ī��Ʈ�� 70(0.7��) �̸��� ��,
			// ������ ���� ī��Ʈ, ������ �ƴ� ī��Ʈ�� 0���� �ʱ�ȭ �Ѵ�. �����ⱸ���� false�� �ȴ�.
		} else {
			// 3. ���� ���ú����� ���� �Ҹ��� ���� ��ȣ�� ������ üũ�Ѵ�.
			// 3-1. ������ ������ false�� ���, ��ȣ�� ���� ī��Ʈ�� �����ϸ�, ��ȣ�� ������ true�� �ȴ�.
			if (isBreathTerm == false) {
				isOSATermCnt++;
				isOSATerm = true;
			} else {
				// 3-1-2. ������ ������ true�� ��쿡�� ������ �ƴ� ī��Ʈ�� ������Ų��.
				isBreathTermCntOpp++;
				isOSATermCntOpp++;
			}
		}

		if (isBreathTermCntOpp > 20) {
			if (isBreathTermCnt < 70) {
				// ���� ���ú� �̻��̰�, ������ ī��Ʈ�� 0.2�� ������ �߻��� �����ͷ� ������.
				// System.out.println("[ignore term, "+String.format("%.2f", curTermTime) +
				// "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " +
				// isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
				// curTermTime = time;
				isBreathTermCnt = 0;
				isBreathTermCntOpp = 0;
				isBreathTerm = false;
				// beforeTermWord=BREATH;
			} else {
				// 2-1-3-1. ���� ������ ���� �ð��� ������ ���۽ð�, ���� �ð��� ������ ���� �ð�
				// (�� �ð��� �ѹ� ȣ������ ��ȣ�� ������ ������ �ǹ������� �ʴ´�.)
				/*
				 * if(beforeTermWord.equals(BREATH)) {
				 * System.out.println("["+String.format("%.2f", curTermTime) +
				 * "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " +
				 * isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]"); }else {
				 * System.out.println("["+String.format("%.2f", curTermTime) +
				 * "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " +
				 * isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]"); curTermTime
				 * = times; }
				 */
				// System.out.println("["+String.format("%.2f", curTermTime) +
				// "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " +
				// isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
				//System.out.println("!!");
				/*
				if(times-OSAcurTermTime >15) {
					OSAcurTermTime = times;
					System.out.println(OSAcurTermTime);
					EventFireGui.osaTermList.add(new StartEnd());
					EventFireGui.osaTermList.get(EventFireGui.osaTermList.size()-1).start=OSAcurTermTime;
					isOSATermTimeOccur = true;
				}
				*/
				isBreathTermCnt = 0;
				isBreathTermCntOpp = 0;
				isBreathTerm = false;
				beforeTermWord = BREATH;
			}
		}
		// 1. ���ӵ� �Ҹ��� �Ǵ� ���� ������ �ʱ�ȭ �Ѵ�.
		// 1-1. ���� ������ ���� �ð��� ���� �ð��� 0�̰ų�, ������ ���� ī��Ʈ�� 0�̸�, ������ �ƴ� ī��Ʈ�� 0�� ��� �ʱ�ȭ �Ѵ�.
		if (OSAcurTermTime == 0 || (isBreathTermCnt == 0 && isOSATermCnt == 0)) {
			//System.out.println("@@");
			//OSAcurTermTime = times;
		}
		return 0;
	}
}
