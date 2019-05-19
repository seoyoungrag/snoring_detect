package snoring;

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
	static double chkDb = -10;

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

	static int snoringCheck(double decibel, double frequency, double sefrequency) {

		if (decibel > chkDb && frequency >= 150 && frequency <= 250 && sefrequency >= 950 && sefrequency < 1050
		// && amplitude < sefamplitude
		) {
			snoringContinue++;
		} else {
			snoringContinueOpp++;
		}
		//System.out.println(checkTerm+" "+snoringContinue+" "+ snoringContinueOpp);
		if (checkTerm % 300 == 0 && snoringContinue >= 60 && snoringContinueOpp <= 240) {
			int tmpI = snoringContinue;
			snoringContinue = 0;
			snoringContinueOpp = 0;
			return tmpI;
		}
		return 0;
	}

	static int grindingCheck(double times, double decibel, int amplitude, double frequency, double sefrequency) {
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

		// System.out.println(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+"
		// "+sehz+" "+seamp);
		// �Ҹ��� ��� ũ�⺸�� Ŭ �� �м��Ѵ�.
		// -> �̰��̴� �Ҹ� ���� �ʹ� Ŀ�� ��� ���� �Ҹ� ������ ������ �ȵ� �� �� �ִ�.-> ��� �Ҹ����� 2�� �̻� ������ ũ�� ���ġ��
		// �ջ� ����.
		// if(amplitude > (maxAmp/sumCnt)) {
		// if(amplitude > 1000) {
		// ��� �߻��Ǵ� ���ļ��� 0.1�ʰ������� ���Ͽ� ����� ���ļ� �϶�(+-50)�� ���ӵǴ� ��쿡 ī���� �Ѵ�.X
		// �������� ���� �߻��Ǵ� ������ 0.01�� ������ 2~7ȸ ���ӵǴ��� üũ

		// System.out.println(times+" - "+termTime+" =
		// "+String.valueOf(times-termTime));

		// ���ú��� �� ���� ���ļ��뿪�� 100�� �ڸ����� �������� �� �����ϸ�, 0.02�� ���ȸ� �ݺ��Ǿ�� �Ѵ�.(1�� �ݺ�)X
		if (decibel > chkDb && (
		// curTermDb >= decibel && // �񱳱����� �Ǵ� ���ú��� �����̾�� �Ѵ�.X -> �������� ���� ���ú��� �������°��� �ٸ�
		// ����� ������ Ư¡�̴�. ����� �뿪�� �Ҹ��� ��� �߻��ϴ°��� ã�ƾ��Ѵ�.
		Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // ���ú� ���� ������ 1db �̸� ���� �̾����� �Ҹ��� �Ǵ��Ѵ�.

				|| Math.floor(curTermHz / 10) * 10 == Math.floor(frequency / 10) * 10
				|| Math.floor(curTermHz / 100) * 100 == Math.floor(frequency / 100) * 100

				|| Math.floor(curTermAmp / 10) * 10 == Math.floor(amplitude / 10) * 10
				|| Math.floor(curTermAmp / 100) * 100 == Math.floor(amplitude / 100) * 100

						&& (Math.floor(curTermSecondHz / 10) * 10 == Math.floor(sefrequency / 10) * 10
								|| Math.floor(curTermSecondHz / 100) * 100 == Math.floor(sefrequency / 100) * 100))) {
			//0.01�� ������ �������� �ݺ��ϴ� ���� ī��Ʈ ���� 
			grindingRepeatOnceAmpCnt++;

		} else {
			//��ȣ�� �ٲ� �� ���� ������ ī��Ʈ�� 2�� ��쿡�� ��ȿī��Ʈ�� �����Ѵ�.
			if (grindingRepeatOnceAmpCnt == 2) {
				grindingContinueAmpCnt++;
			}else {
				grindingContinueAmpOppCnt++;	
			}
			grindingRepeatOnceAmpCnt = 0;
		}

		// 1. ����, ���ú�, ���ļ��� �̿��ؼ� 0.01�� ������ 1���� �߻��ϰų� 2�� ���ӹ߻��� ����Ʈ�� �����ϰ�,
		// 2. �� ����Ʈ�� 1�ʵ��� ��ŭ �߻��ߴ����� ī��Ʈ�� üũ, ����ġ�� 3���� ��Ҵ�.
		// 3. 3�� �̻� ���ӹ߻��� ����Ʈ�� ī��Ʈ�� üũ, ����ġ�� 69�� ��Ҵ�.
		// -> �� 1���� ����Ʈ�� 1�ʵ��� 3�� �߻��ߴ��� üũ, ���ÿ� 3ȸ�̻󿬼��� ����Ʈ ī��Ʈ�� 69�� �Ѿ����. �� ������ ����(2��)
		// ���� �߻��ؾ� ��
		// ���� ī��Ʈ
		
		//1�� ������ ��ȿī��Ʈ�� ��ȸ���� üũ�Ѵ�. ��ȿī��Ʈ�� ������ �ش��ϸ�, ���ʵ��� ���ӵǴ��� üũ�Ѵ�.
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
		if (decibel > chkOSADb) {
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
				// ��ȣ���� ȣ������ �Ѿ���� ��� ���������� 3�ʴ� �Ѿ�� ��ȣ�������� ����.
				if (beforeTermWord.equals(BREATH) && isOSATermCnt > 500) {
					/*
					 * if(beforeTermWord.equals(OSA)) { System.out.println("["+String.format("%.2f",
					 * curTermTime) + "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
					 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); }else {
					 * System.out.println("["+String.format("%.2f", curTermTime) +
					 * "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
					 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); curTermTime = times;
					 * }
					 */
					/*
					System.out.println("[" + String.format("%.2f", OSAcurTermTime) + "~" + String.format("%.2f", times)
							+ "s, isOSATermCnt: " + isOSATermCnt + ", isOSATermCntOpp:" + isOSATermCntOpp + "]");
					*/
					OSAcurTermTime = times;
					isOSATerm = false;
					isOSATermCnt = 0;
					isOSATermCntOpp = 0;
					// beforeTermWord=OSA;
					return (int) (times-OSAcurTermTime);
				} else {
					// System.out.println("[ignore term, "+String.format("%.2f", curTermTime) +
					// "~"+String.format("%.2f", times) + "s, isOSATermCnt: " + isOSATermCnt+",
					// isOSATermCntOpp:"+isOSATermCntOpp+"]");
					// curTermTime = time;
					isOSATerm = false;
					isOSATermCnt = 0;
					isOSATermCntOpp = 0;
					// beforeTermWord=OSA;
				}
			} else {
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
				OSAcurTermTime = times;
				isBreathTermCnt = 0;
				isBreathTermCntOpp = 0;
				isBreathTerm = false;
				beforeTermWord = BREATH;
			}
		}
		// 1. ���ӵ� �Ҹ��� �Ǵ� ���� ������ �ʱ�ȭ �Ѵ�.
		// 1-1. ���� ������ ���� �ð��� ���� �ð��� 0�̰ų�, ������ ���� ī��Ʈ�� 0�̸�, ������ �ƴ� ī��Ʈ�� 0�� ��� �ʱ�ȭ �Ѵ�.
		if (OSAcurTermTime == 0 || (isBreathTermCnt == 0 && isOSATermCnt == 0)) {
			OSAcurTermTime = times;
		}
		// 1-1. ���� ������ ���ú��� ��� �ʱ�ȭ �Ѵ�. (���ļ��� ������ 0.01�� ������ �����ϱ� ������ ���ͷ� �̿��� �� ����, �α׿�����
		// �ʱ�ȭ)
		curTermDb = decibel;
		curTermAmp = amplitude;
		curTermHz = frequency;
		curTermSecondHz = sefrequency;
		return 0;
	}
}
