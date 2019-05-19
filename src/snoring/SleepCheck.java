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

	static int checkTerm = 0; // 1당 0.01초
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
		// 이갈이, 이갈이는 높은 주파수가 굉장히 짧은 간격으로 여러번 나타난다.
		// 아래 1,2,3 무시, 다시-> 이갈이는 0.02~0.07초 사이의 큰 진폭을 갖는다. 즉, 0.01초 단위로 분석해서 연속으로 2~7회의
		// 진폭이 발생하는 것을 잡으면 됨.
		// 이 때, 진폭의 수치는 의미가 없을 수 있으므로 먼저 반복횟수로만 잡아본다.
		// 1. 특정 주파수보다 높은 주파수가 --> 특정주파수 a=?
		// 2. 특정 시간동안 특정 횟수보다 많이 반복되는지 --> 특정시간 b=?, 특정횟수 c=?
		// 3. 체크되면 이갈이다.

		// 특정 주파수대역에서 연속적으로 발생해야 한다.
		// 분석하는 소리의 길이는 0.01초.
		// 아래는 0.01초 데이터를 0.1초 단위로 분석X

		// System.out.println(String.format("%.2f", times)+"s "+hz +" "+db+" "+amp+"
		// "+sehz+" "+seamp);
		// 소리가 평균 크기보다 클 때 분석한다.
		// -> 이갈이는 소리 폭이 너무 커서 평균 측정 소리 때문에 측정이 안될 수 도 있다.-> 평균 소리보다 2배 이상 진폭이 크면 평균치에
		// 합산 안함.
		// if(amplitude > (maxAmp/sumCnt)) {
		// if(amplitude > 1000) {
		// 계속 발생되는 주파수의 0.1초간격으로 비교하여 비슷한 주파수 일때(+-50)가 연속되는 경우에 카운팅 한다.X
		// 연속으로 높게 발생되는 진폭이 0.01초 단위로 2~7회 연속되는지 체크

		// System.out.println(times+" - "+termTime+" =
		// "+String.valueOf(times-termTime));

		// 데시벨이 더 높고 주파수대역이 100의 자리에서 내림했을 때 동일하며, 0.02초 동안만 반복되어야 한다.(1번 반복)X
		if (decibel > chkDb && (
		// curTermDb >= decibel && // 비교기준이 되는 데시벨은 고점이어야 한다.X -> 고점에서 점차 데시벨이 내려오는것은 다른
		// 사운드와 동일한 특징이다. 비슷한 대역의 소리가 계속 발생하는것을 찾아야한다.
		Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // 데시벨 오차 범위가 1db 이면 같은 이어지는 소리로 판단한다.

				|| Math.floor(curTermHz / 10) * 10 == Math.floor(frequency / 10) * 10
				|| Math.floor(curTermHz / 100) * 100 == Math.floor(frequency / 100) * 100

				|| Math.floor(curTermAmp / 10) * 10 == Math.floor(amplitude / 10) * 10
				|| Math.floor(curTermAmp / 100) * 100 == Math.floor(amplitude / 100) * 100

						&& (Math.floor(curTermSecondHz / 10) * 10 == Math.floor(sefrequency / 10) * 10
								|| Math.floor(curTermSecondHz / 100) * 100 == Math.floor(sefrequency / 100) * 100))) {
			//0.01초 단위로 연속으로 반복하는 여부 카운트 증가 
			grindingRepeatOnceAmpCnt++;

		} else {
			//신호가 바뀔 때 연속 증가한 카운트가 2인 경우에만 유효카운트를 증가한다.
			if (grindingRepeatOnceAmpCnt == 2) {
				grindingContinueAmpCnt++;
			}else {
				grindingContinueAmpOppCnt++;	
			}
			grindingRepeatOnceAmpCnt = 0;
		}

		// 1. 진폭, 데시벨, 주파수를 이용해서 0.01초 단위로 1번만 발생하거나 2번 연속발생한 포먼트를 측정하고,
		// 2. 위 포먼트가 1초동안 얼만큼 발생했는지를 카운트를 체크, 기준치는 3으로 잡았다.
		// 3. 3번 이상 연속발생한 포먼트의 카운트를 체크, 기준치는 69로 잡았다.
		// -> 즉 1번의 포먼트가 1초동안 3번 발생했는지 체크, 동시에 3회이상연속한 포먼트 카운트가 69를 넘어야함. 이 현상이 수초(2초)
		// 동안 발생해야 함
		// 연속 카운트
		
		//1초 단위로 유효카운트가 몇회인지 체크한다. 유효카운트가 범위에 해당하면, 몇초동안 지속되는지 체크한다.
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
	 * 무호흡증은 항상 코골이를 동반하며, 코골이의 시작하고 종료한 시간은 5~10초이내 숨을 쉬어야 하기 때문에 호흡이 가파른 느낌이 있다,
	 * 무호흡 코골이 시간이 종료한 후 숨을 멈추는 시간이 30~50초 이내이다. 1.db세기로 무호흡코골이 및 호흡으로 측정되는 부분을
	 * 특정한다. 2. 호흡은 0.7초 간격은 0.2초 3. 2번이 유지되면 무호흡코골이 혹은 호흡하는 구간이다. 4. 3번을 유지하지 않는경우
	 * 일정 db이하로 30~50초 동안 유지되는지를 체크함으로써 무호흡 구간인지 특정한다.
	 */
	static int OSACheck(double times, double decibel, int amplitude, double frequency, double sefrequency) {
		// 2. 기준 데시벨보다 높은 소리라면 호흡(혹은 코골이) 구간인지 체크한다.
		if (decibel > chkOSADb) {
			// 2-1. 데시벨을 이용해서 연속된 소리인지 체크한다.
			// 2-1-1. 연속된 소리인지 체크하기 위해서는 비슷한 데시벨인지만 체크한다.
			// (주파수나 진폭은 0.01초 단위로 상이하기 때문에 팩터로 이용할 수 없음.)
			// 2-1-2. 비슷한 데시벨은 기준 정보의 데시벨에서 +-1db로 하며, 체크될 경우 숨쉬는 구간 카운트를 1씩 증가한다.
			// 체크가 안되면 숨쉬기 아님 카운트를 1씩 증가한다. 숨쉬기구간은 true 된다.
			/*
			 * if (Math.abs(Math.abs(curTermDb) - Math.abs(decibel)) < 1 // 데시벨 오차 범위가
			 * 1db인지만 체크 ) { isBreathTermCnt++; isBreathTerm = true; // 2-1-2-1. 숨쉬기 구간이
			 * true가 될 때, 무호흡 구간이 true인 경우, 무호흡 구간을 false로 바꾸며, // 이때 기준 정보의 시간은 무호흡 시작시간,
			 * 종료시간은 무호흡 종료시간이 된다. // 무호흡 구간 카운트 로깅을 하고 무호흡 구간 카운트를 초기화 한다. if(isOSATerm ==
			 * true) { System.out.println("["+String.format("%.2f", curTermTime) +
			 * "~"+String.format("%.2f", times) + "s, isOSATermCnt: " +
			 * isOSATermCnt+", isOSATermCntOpp:"+isOSATermCntOpp+"]"); curTermTime = times;
			 * isOSATerm = false; isOSATermCnt = 0; } } else { isBreathTermCntOpp++; }
			 */

			if (isOSATerm == true) {
				// 무호흡에서 호흡으로 넘어오는 경우 오차범위가 3초는 넘어야 무호흡구간으로 본다.
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
			// 2-1-3. 숨쉬기 아님 카운트가 20을 넘으면(0.2초가 초과되면), 숨쉬는 구간 카운트가 70(0.7초) 미만일 시,
			// 숨쉬는 구간 카운트, 숨쉬기 아님 카운트를 0으로 초기화 한다. 숨쉬기구간은 false가 된다.
		} else {
			// 3. 기준 데시벨보다 낮은 소리인 경우는 무호흡 중인지 체크한다.
			// 3-1. 숨쉬기 구간이 false인 경우, 무호흡 구간 카운트를 증가하며, 무호흡 구간은 true가 된다.
			if (isBreathTerm == false) {
				isOSATermCnt++;
				isOSATerm = true;
			} else {
				// 3-1-2. 숨쉬기 구간이 true인 경우에는 숨쉬기 아님 카운트를 증가시킨다.
				isBreathTermCntOpp++;
				isOSATermCntOpp++;
			}
		}

		if (isBreathTermCntOpp > 20) {
			if (isBreathTermCnt < 70) {
				// 일정 데시벨 이상이고, 숨쉬기 카운트의 0.2초 오차가 발생한 데이터로 무시함.
				// System.out.println("[ignore term, "+String.format("%.2f", curTermTime) +
				// "~"+String.format("%.2f", times) + "s, isBreathTermCnt: " +
				// isBreathTermCnt+", isBreathTermCntOpp: "+isBreathTermCntOpp+"]");
				// curTermTime = time;
				isBreathTermCnt = 0;
				isBreathTermCntOpp = 0;
				isBreathTerm = false;
				// beforeTermWord=BREATH;
			} else {
				// 2-1-3-1. 기준 정보의 기준 시간이 숨쉬기 시작시간, 현재 시간은 숨쉬기 종료 시간
				// (이 시간은 한번 호흡이지 무호흡 사이의 구간을 의미하지는 않는다.)
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
		// 1. 연속된 소리가 되는 기준 정보를 초기화 한다.
		// 1-1. 기준 정보의 기준 시간은 기준 시간이 0이거나, 숨쉬는 구간 카운트가 0이며, 숨쉬기 아님 카운트가 0일 경우 초기화 한다.
		if (OSAcurTermTime == 0 || (isBreathTermCnt == 0 && isOSATermCnt == 0)) {
			OSAcurTermTime = times;
		}
		// 1-1. 기준 정보의 데시벨만 계속 초기화 한다. (주파수나 진폭은 0.01초 단위로 상이하기 때문에 팩터로 이용할 수 없음, 로그용으로
		// 초기화)
		curTermDb = decibel;
		curTermAmp = amplitude;
		curTermHz = frequency;
		curTermSecondHz = sefrequency;
		return 0;
	}
}
