package snoring;

import java.util.ArrayList;
import java.util.List;

public class SleepCheck {
    private static final String LOG_TAG3 = "SleepCheck";

    static double decibelSumCnt = 0;

    static int AVR_DB_CHECK_TERM = 2000;
    static double MAX_DB_CRIT_VALUE = -31.5;
    static double MIN_DB_CRIT_VALUE = -(31.5-(31.5*35/120)); //http://www.noiseinfo.or.kr/about/info.jsp?pageNo=942 조용한 공원(수면에 거의 영향 없음) 35, 40부터 낮아진다

    public static int noiseChkSum = 0;
    static int noiseNoneChkSum = 0;
    public static int noiseChkCnt = 0;
    static int noiseChkForStartSum = 0;
    static int noiseNoneChkForStartSum = 0;
    static int noiseChkForStartCnt = 0;

    static double MAX_DB = -31.5;
    static double MIN_DB = 0;

    public static double getMinDB() {
        return MIN_DB/2 > MIN_DB_CRIT_VALUE ? Math.floor(MIN_DB_CRIT_VALUE) : MIN_DB/2;
    }

    public static double setMinDB(double decibel) {
        //10분마다 평균 데시벨을 다시 계산한다.
        if(Math.abs(decibel) != 0 && decibel < MIN_DB) {
            MIN_DB = decibel;
        }
        return MIN_DB/2 > MIN_DB_CRIT_VALUE ? Math.floor(MIN_DB_CRIT_VALUE) : MIN_DB/2;
    }
    public static double getMaxDB() {
        return MAX_DB*2 < MAX_DB_CRIT_VALUE ? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
    }

    public static double setMaxDB(double decibel) {
        //10분마다 평균 데시벨을 다시 계산한다.
        if(Math.abs(decibel) != 0 && decibel > MAX_DB) {
            MAX_DB = decibel-1;
        }
        if (decibelSumCnt >= AVR_DB_CHECK_TERM) {
            decibelSumCnt = 0;
            MAX_DB = -31.5;
            MIN_DB = 0;
        }
        decibelSumCnt ++;
        return MAX_DB*2 < MAX_DB_CRIT_VALUE? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
    }
    public static int noiseCheck(double decibel) {
        //1분동안 소리가 발생하지 않았는지 체크한다.
        //0.01초 단위임으로, 6000번 해야 60초임.
        //1분이 되었으면, 데시벨보다 높은 소리가 발생하지 않은 경우
        if(noiseChkCnt>=6000) {
            int tmpN = noiseChkSum;
            noiseChkCnt = 0;
            noiseChkSum = 0;
            noiseNoneChkSum = 0;
            return tmpN;
        }else {
            //아직 1분이 안되었으면 계속 소리 체크를 한다.
            //소리 체크는 1분동안 평균 데시벨보다 최저 임계 데시벨의 소리가 발생했는지를 체크한다.
            //리턴이 0이면 녹음 종료하게 되어있음.X
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

    public static int noiseCheckForStart(double decibel) {
        //1분동안 소리가 발생하지 않았는지 체크한다.
        //0.01초 단위임으로, 6000번 해야 60초임.
        //1분이 되었으면, 데시벨보다 높은 소리가 발생하지 않은 경우
        if(noiseChkForStartCnt>=200) {
            int tmpN = noiseChkForStartSum;
            noiseChkForStartCnt = 0;
            noiseChkForStartSum = 0;
            noiseNoneChkForStartSum = 0;
            return tmpN;
        }else {
            //아직 1분이 안되었으면 계속 소리 체크를 한다.
            //소리 체크는 1분동안 평균 데시벨보다 높은 데시벨의 소리가 발생했는지를 체크한다.
            //리턴이 0이면 녹음 종료하게 되어있음.
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

    public static double calcforChkSnoringDbNotNomarlize(double[] allFHAndDB, int startN, int endN) {
        double forChkSnroingDb = 0;
        for (int i = 0; i <= endN - startN; i++) {
            forChkSnroingDb += allFHAndDB[startN+i];
        }
        forChkSnroingDb = Math.abs((forChkSnroingDb) / (endN - startN + 1));
        return forChkSnroingDb;
    }

    static int grindingRepeatOnceAmpCnt;
    static int continueCntInChkTermForGrinding;
    static int continueCntInChkTermForGrindingChange;
    public static double tmpMaxDb = 0;
    public static double tmpMinDb = 99999;
    static boolean soundStartInRecording = false;
    static double chkDBAgainInRecording = 0.0;
    static int soundStartAndSnroingCnt = 0;
    static int soundStartAndSnroingOppCnt = 0;
    static double firstDecibelAvg = 0.0;
    static double secondDecibelAvg = 0.0;
    static double snoringDbChkCnt = 0;
    static boolean isOSATermTimeOccur = false;
    static int isOSATermCnt = 0;
    static int isBreathTermCnt = 0;
    static int osaContinueCnt = 0;
    static boolean isBreathTerm = false;
    static double OSAcurTermTime = 0.0;

    public static int allFHAndDb_NEED_INITIALIZE = 2;
    public static int CHECKED_COMPLETE = 1;
    public static int CHECKED_ERROR = 0;
    public static int CHECKED_STATUS = 0;
    public static int snoringCheck(double[] allFHAndDB, double decibel, double times, List<StartEnd> snoringTermList, List<StartEnd> grindingTermList, AnalysisRawData maxARD){
        //이갈이 음파가 매우 짧기 때문에, 코골이의 로직과 분리해야한다. 코골이는 0.16초 단위로 분석, 이갈이는 0.01초로 분석해야함
        //코골이의 음파 길이 및 음파가 아닌 경우의 1초 범위까지 기록 하고 있음으로, 코골이가 아닌 경우에 이갈이인지 체크하도록 한다.
        //이갈이는 1초 이내에 여러번 발생하며, 발생시에 0.02~0.03초의 연속된 짧고 높은 진폭이 발생한다.이 카운트가 1초에 5회 미만인 것만 뽑아낸다. //
        //그렇다면 시간 대비 코골이 횟수를 비례해서 계산하면 된다.
        double chkGrindingDb = getMinDB();
        if(chkGrindingDb<=-30) {
            chkGrindingDb = getMinDB()/1.5;
        }else if(chkGrindingDb<=-20) {
            chkGrindingDb = getMinDB()/1.25;
        }else if(chkGrindingDb<=-10) {
            chkGrindingDb = getMinDB()/1.1;
        }
        if(decibel > chkGrindingDb) {
            grindingRepeatOnceAmpCnt++;
        }else {
            if( grindingRepeatOnceAmpCnt >= continueCntInChkTermForGrinding) {
                continueCntInChkTermForGrinding += grindingRepeatOnceAmpCnt;
                continueCntInChkTermForGrindingChange++;
            }
            grindingRepeatOnceAmpCnt = 0;
        }
        //음파가 발생하는 구간,
        //음파가 발생하는 구간이란 코골이가 발생해서 코골이 1회의 시작과 끝을 의미한다.
        //음파가 발생하는 구간동안 코골이가 발생했는지를 체크 해야 한다.
        //플래그로 음파가 발생하고 있는지를 관리한다.
        //소리가 발생하고, 0.5초 간격으로 연속되고 있는지 체크한다.
        //연속되지 않는 순간 음파가 끝난 것으로 간주한다.
        //음파가 끝나는 순간 코골이가 발생했는지 체크하고, 코골이가 발생하지 않은 것은 이갈이로 구분한다.
        double chkSnoringDb = getMinDB();
        if(chkSnoringDb<=-30) {
            chkSnoringDb = getMinDB()/2;
        }else if(chkSnoringDb<=-20) {
            chkSnoringDb = getMinDB()/1.75;
        }else if(chkSnoringDb<=-10) {
            chkSnoringDb = getMinDB()/1.5;
        }
        if(allFHAndDB!=null) {
            tmpMinDb = 99999;
            tmpMaxDb = 0;
            //코골이는 임계치를 보정해서 코골이의 음파 여부를 판단한다.
            int maxDBL = allFHAndDB.length;
            maxDBL = maxDBL > 41 ? 41 : maxDBL;
            for(int m = 0 ; m < maxDBL ; m++){
                if(allFHAndDB[m] > tmpMaxDb){
                    tmpMaxDb = allFHAndDB[m];
                    if(tmpMaxDb<0){
                        tmpMaxDb = Math.abs(tmpMaxDb);
                    }
                }
                if(allFHAndDB[m] < tmpMinDb){
                    tmpMinDb = allFHAndDB[m];
                }
            }
            if(decibel > chkSnoringDb && tmpMaxDb>40) {
                //코골이 음파가 발생했음.
                if(soundStartInRecording==false) {
                    //코골이 분석 중 이갈이 구별 하기위한 카운트 초기화, 이갈이라면 이 카운트가 매우 높아선 안된다.
                    continueCntInChkTermForGrinding = 0;
                    continueCntInChkTermForGrindingChange = 0;
                    //TODO 음파 진행중일 떄의 평균 데시벨을 가지고, 음파로 인정할 소리를 한번더 구별 한다.
                    chkDBAgainInRecording = decibel;
                    //녹음 중에 소리가 발생했고 음파 시작은 아닌 상태, 음파 시작 상태로 변환
                    soundStartInRecording = true;
                    //코골이 카운트를 초기화(음파 진행 중에 카운트 증가)
                    soundStartAndSnroingCnt = 0;
                    //낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
                    //음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
                    soundStartAndSnroingOppCnt = 0;
                    //음파시작시간을 보관하기 위해 기록vo를 생성
                    StartEnd st = new StartEnd();
                    st.start = times;
                    st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
                    //st.AnalysisRawDataList.add(maxARD);
                    snoringTermList.add(st);
                    //음파가 진행되는 동안 최대 데시벨과 저주파수의 데시벨의 평균을 계산하기 위해 값을 초기화 한다.
                    //최대 데시벨 값과 저주파수 데시벨 값을 저장한다.(초기화)
                    firstDecibelAvg = 0;
                    secondDecibelAvg = 0;
                    snoringDbChkCnt = 0;
                }else {
                    chkDBAgainInRecording = (chkDBAgainInRecording + decibel) /2;
                    if(firstDecibelAvg == 0 || secondDecibelAvg == 0) {
                        firstDecibelAvg = calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40);
                        secondDecibelAvg = calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18);
                        snoringDbChkCnt = 0;
                    }else {
                        if(Math.floor(decibel) >= Math.floor(chkDBAgainInRecording) &&
                                calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18)>calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40)) {
                            //평균으로만 비교하긴 할건데, 평균낼때까지 얼마나 차이가 있었나도 비교해봄.. 값을 쓸 수도 있다.
                            snoringDbChkCnt++;
                        }
                        firstDecibelAvg = (firstDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40))/2;
                        secondDecibelAvg = (secondDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18))/2;
                    }
                }
            }else {
                //소리가 발생하지 않았으면, 현재 코골이 음파 발생중인지 체크 한다.
                if(soundStartInRecording==true) {
                    if(snoringTermList == null || snoringTermList.size()==0){
                        soundStartInRecording = false;
                        CHECKED_STATUS = CHECKED_ERROR;
                        return CHECKED_ERROR;
                    }
                    //음파 진행 중이라면, 지금 체크중인 체크 시작시간이 1초를 넘었는지 체크한다.
                    if(times-snoringTermList.get(snoringTermList.size()-1).start>0.16*7){
                        //음파시작시간과는 1초가 벌어졌다면 , 분석을 중단하고, 이후 코골이 발생 카운트를 체크하여 기록한다.
                        soundStartInRecording = false;
                        //두번째 데시벨이 더 크게 나타난다.
                        double  diffMaxToLow = Math.abs(secondDecibelAvg) - Math.abs(firstDecibelAvg);
                        //차이가 맥시멈 데시벨의 절반 이상인가
                        if(diffMaxToLow > 0 ) {
                            //1초가 벌어졌다면, 음파 진행된 동안의 최대 데시벨 평균과 평균 데시벨의 차이를 비교한다.
                            //낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
                            //음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
                            soundStartAndSnroingCnt++;
                        }else {
                            //진행 카운트 증가 안하고 통과
                            //-> 진행된 카운트 대신 반대 카운트 증가
                            soundStartAndSnroingOppCnt++;
                        }
                        //1. 5~200 주파수의 평균 데시벨보다 43~80 주파수의 평균 데시벨이 더 커야함
                        //2. 코골이 긍장 카운트 1 당, 부정카운트가 3보다 크면 안된다.(
                        if(soundStartAndSnroingCnt > 0 && soundStartAndSnroingOppCnt<soundStartAndSnroingCnt*3) {
                            //코골이 카운트가 증가했었고, 코골이 기록vo에 종료 시간을 기록
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
                            //코골이 카운트가 증가한 적이 없었다.
                            //코골이 기록 vo 대신 이갈이 기록 vo로 넣는다.
                            //이갈이는 원본 로직대로 한다.
                            if(continueCntInChkTermForGrindingChange > 0 && continueCntInChkTermForGrinding> 0 &&
                                    firstDecibelAvg > tmpMaxDb/2 &&
                                    Math.abs(firstDecibelAvg - secondDecibelAvg)<5 &&
                                    //grindingChange가 3이상일 때는, / 가 10보다 크고 12보다 작아야함
                                    ((continueCntInChkTermForGrindingChange >= 3 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange >= 10 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange <= 12)
                                            ||
                                            //2이하일 때는, / 가 9보다 작아야함
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
                        //음파 진행 중이고, 소리가 발생하지 않았으나 아직 1초가 지나지 않았다.
                        //진행 카운트 증가 안하고 통과
                        //-> 진행된 카운트 대신 반대 카운트 증가
                        //음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
                        soundStartAndSnroingOppCnt++;
                        //snoringTermList.remove(snoringTermList.size()-1);
                        //soundStartInRecording = false;
                    }
                }
                //소리가 발생하지 않았고, 음파가 진행 중인 상태가 아니다.

                // baos.write(frameBytes);

            }
            //allFHAndDB = null;
            CHECKED_STATUS = allFHAndDb_NEED_INITIALIZE;
            return allFHAndDb_NEED_INITIALIZE;
        }else {
        }
        CHECKED_STATUS = CHECKED_COMPLETE;
        return CHECKED_COMPLETE;
    }
    public static int osaCheck(double decibel, double times, List<StartEnd> osaTermList, List<StartEnd> snoringTermList, List<StartEnd> noiseTermListForOsaList){
        double chkGrindingDb = getMinDB();
        if(chkGrindingDb<=-30) {
            chkGrindingDb = getMinDB()/1.5;
        }else if(chkGrindingDb<=-20) {
            chkGrindingDb = getMinDB()/1.25;
        }else if(chkGrindingDb<=-10) {
            chkGrindingDb = getMinDB()/1.1;
        }
        if(decibel > chkGrindingDb) {
            //소리가 발생했고, 분석 시작 변수 값이 true 인 경우 종료한다.
            if(isOSATermTimeOccur) {
                //0.1초 동안 소리가 70% 이상 발생한 경우 소리가 발생한 것으로 본다.

                if(isOSATermCnt+isBreathTermCnt>90 && isOSATermCnt > 20 && isBreathTermCnt > 70) {
                    //오차범위를 둔다. 0.5초 동안 연속으로 소리가 발생해야 한다.
                    if(osaContinueCnt > 4) {
                        isOSATermTimeOccur = false;
                        isBreathTermCnt = 0;
                        isBreathTerm = true;
                        if(osaTermList!=null&&osaTermList.size()>0) {
                            osaTermList.get(osaTermList.size() - 1).end = times;
                            osaTermList.get(osaTermList.size() - 1).chk = 0;
                        }else{
                        	System.out.println("osaTermList!=null && osaTermList.size()>0, line 252");
                        }
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
            //무호흡을 측정하기 위한 분석 시작 변수 초기화
            //코골이가 발생하고 5초가 안지났어야 함.
            if(snoringTermList.size() > 0
                    && snoringTermList.get(snoringTermList.size()-1).end != 0
                    && times - snoringTermList.get(snoringTermList.size()-1).end > 0
                    && times - snoringTermList.get(snoringTermList.size()-1).end < 5
                    && !isOSATermTimeOccur) {
                //0.1초 동안 묵음이 70% 이상 발생한 경우 소리가 발생한 것으로 본다.
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
        //무호흡 발생후 3분동안 종료되지 않는다면 취소
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end==0 && times-osaTermList.get(osaTermList.size()-1).start > 180) {
            isOSATermTimeOccur = false;
            isOSATermCnt = 0;
            isBreathTerm = false;
            isBreathTermCnt = 0;
            OSAcurTermTime = 0.0;
            osaTermList.remove(osaTermList.size()-1);
        }

        //무호흡이 종료되지 않았고, 소음이 발생했다면 취소
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end==0) {
            if(noiseTermListForOsaList.size()>0){
            	if(noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).start - osaTermList.get(osaTermList.size()-1).start > 0){
                    osaTermList.remove(osaTermList.size()-1);
            	}else {
            		noiseTermListForOsaList.remove(noiseTermListForOsaList.size()-1);
            	}
            }else {
            }
            isOSATermTimeOccur = false;
            isOSATermCnt = 0;
            isBreathTerm = false;
            isBreathTermCnt = 0;
            OSAcurTermTime = 0.0;
        }
        
        //무호흡 종료 후 녹음된 시간이 너무 짧으면 삭제한다.
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
            if(osaTermList.get(osaTermList.size()-1).end - osaTermList.get(osaTermList.size()-1).start < 5 ){
                osaTermList.remove(osaTermList.size()-1);
            }
        }

        //무호흡 종료 후 5초 이내에 코골이가 발생하지 않으면 취소
        //무호흡 종료 후 5초 동안 코골이 발생여부를 체크한다.
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
            if(snoringTermList.size()>0 && snoringTermList.get(snoringTermList.size()-1).start - osaTermList.get(osaTermList.size()-1).end > 0 && snoringTermList.get(snoringTermList.size()-1).start - osaTermList.get(osaTermList.size()-1).end < 5){
                //코골이가 녹음 중이게 되었을 때, 체크 플래그를 업데이트
                if(snoringTermList.get(snoringTermList.size() - 1).end==0){
                    osaTermList.get(osaTermList.size()-1).chk = 1;
                }
            }
        }
        //무호흡 종료 후 5초가 넘은 경우 플래그를 체크해서 코골이를 삭제한다.
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end > 5) {
            if(osaTermList.get(osaTermList.size()-1).chk==0) {
                osaTermList.remove(osaTermList.size()-1);
            }
        }
        CHECKED_STATUS = CHECKED_COMPLETE;
        return CHECKED_COMPLETE;
    }
    
    static boolean someNoiseStartInRecording = false;
    static double someNoiseChkDBAgainInRecording = 0.0;
    static int someNoiseStartCnt = 0;
    static int someNoiseStartOppCnt = 0;
    static int someNoiseDbChkCnt = 0;
    
    public static int someNoiseCheck(double times, double amplitude, List<StartEnd> noiseTermListForOsaList){
            //음파가 발생했음.
            if(someNoiseStartInRecording==false) {
                //TODO 음파 진행중일 떄의 평균 데시벨을 가지고, 음파로 인정할 소리를 한번더 구별 한다.
                someNoiseChkDBAgainInRecording = amplitude;
                //녹음 중에 소리가 발생했고 음파 시작은 아닌 상태, 음파 시작 상태로 변환
                someNoiseStartInRecording = true;
                //코골이 카운트를 초기화(음파 진행 중에 카운트 증가)
                someNoiseStartCnt = 0;
                //낮은 주파수 평균이 데시벨의 절반보다 낮다면 코골이 카운트 증가
                //음파 진행 시간 동안 얼만큼 체크가 안되었는지 카운트를 해서 비교할 수 있다.
                someNoiseStartOppCnt = 0;
                //음파시작시간을 보관하기 위해 기록vo를 생성
                StartEnd st = new StartEnd();
                st.start = times;
                st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
                noiseTermListForOsaList.add(st);
                //음파가 진행되는 동안 최대 데시벨과 저주파수의 데시벨의 평균을 계산하기 위해 값을 초기화 한다.
                //최대 데시벨 값과 저주파수 데시벨 값을 저장한다.(초기화)
                someNoiseDbChkCnt = 0;
            }else {
            	someNoiseDbChkCnt++;
                if(amplitude > someNoiseChkDBAgainInRecording*2) {
                	someNoiseStartCnt++;
                }else {
                	someNoiseStartOppCnt++;
                }
                someNoiseChkDBAgainInRecording = amplitude;
            }
            if(noiseTermListForOsaList == null || noiseTermListForOsaList.size()==0){
            	someNoiseStartInRecording = false;
                CHECKED_STATUS = CHECKED_ERROR;
                return CHECKED_ERROR;
            }
        	if(times-noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).start>0.16*7){
            	someNoiseStartInRecording = false;
            	System.out.println(times+" "+amplitude+someNoiseDbChkCnt+" "+someNoiseStartCnt+" "+someNoiseStartOppCnt);
	            if(someNoiseStartCnt>0){
	                //코골이 카운트가 증가했었고, 코골이 기록vo에 종료 시간을 기록
	            	noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).end = times;
	            	noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).first = amplitude;
	            	noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).chk = someNoiseDbChkCnt;
	            	noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).positiveCnt = someNoiseStartCnt;
	            	noiseTermListForOsaList.get(noiseTermListForOsaList.size()-1).negitiveCnt = someNoiseStartOppCnt;
	            }else {
	            	noiseTermListForOsaList.remove(noiseTermListForOsaList.size()-1);
	            }
        	}
        CHECKED_STATUS = CHECKED_COMPLETE;
        return CHECKED_COMPLETE;
    }
}