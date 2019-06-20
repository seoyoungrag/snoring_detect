package snoring;

import java.util.ArrayList;
import java.util.List;

public class SleepCheck {
    private static final String LOG_TAG3 = "SleepCheck";

    static double decibelSumCnt = 0;

    static int AVR_DB_CHECK_TERM = 2000;
    static double MAX_DB_CRIT_VALUE = -31.5;
    static double MIN_DB_CRIT_VALUE = -(31.5-(31.5*35/120)); //http://www.noiseinfo.or.kr/about/info.jsp?pageNo=942 ������ ����(���鿡 ���� ���� ����) 35, 40���� ��������

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
        //10�и��� ��� ���ú��� �ٽ� ����Ѵ�.
        if(Math.abs(decibel) != 0 && decibel < MIN_DB) {
            MIN_DB = decibel;
        }
        return MIN_DB/2 > MIN_DB_CRIT_VALUE ? Math.floor(MIN_DB_CRIT_VALUE) : MIN_DB/2;
    }
    public static double getMaxDB() {
        return MAX_DB*2 < MAX_DB_CRIT_VALUE ? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
    }

    public static double setMaxDB(double decibel) {
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
        return MAX_DB*2 < MAX_DB_CRIT_VALUE? Math.floor(MAX_DB_CRIT_VALUE) : MAX_DB*2;
    }
    public static int noiseCheck(double decibel) {
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

    public static int noiseCheckForStart(double decibel) {
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
        //�̰��� ���İ� �ſ� ª�� ������, �ڰ����� ������ �и��ؾ��Ѵ�. �ڰ��̴� 0.16�� ������ �м�, �̰��̴� 0.01�ʷ� �м��ؾ���
        //�ڰ����� ���� ���� �� ���İ� �ƴ� ����� 1�� �������� ��� �ϰ� ��������, �ڰ��̰� �ƴ� ��쿡 �̰������� üũ�ϵ��� �Ѵ�.
        //�̰��̴� 1�� �̳��� ������ �߻��ϸ�, �߻��ÿ� 0.02~0.03���� ���ӵ� ª�� ���� ������ �߻��Ѵ�.�� ī��Ʈ�� 1�ʿ� 5ȸ �̸��� �͸� �̾Ƴ���. //
        //�׷��ٸ� �ð� ��� �ڰ��� Ƚ���� ����ؼ� ����ϸ� �ȴ�.
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
        //���İ� �߻��ϴ� ����,
        //���İ� �߻��ϴ� �����̶� �ڰ��̰� �߻��ؼ� �ڰ��� 1ȸ�� ���۰� ���� �ǹ��Ѵ�.
        //���İ� �߻��ϴ� �������� �ڰ��̰� �߻��ߴ����� üũ �ؾ� �Ѵ�.
        //�÷��׷� ���İ� �߻��ϰ� �ִ����� �����Ѵ�.
        //�Ҹ��� �߻��ϰ�, 0.5�� �������� ���ӵǰ� �ִ��� üũ�Ѵ�.
        //���ӵ��� �ʴ� ���� ���İ� ���� ������ �����Ѵ�.
        //���İ� ������ ���� �ڰ��̰� �߻��ߴ��� üũ�ϰ�, �ڰ��̰� �߻����� ���� ���� �̰��̷� �����Ѵ�.
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
            //�ڰ��̴� �Ӱ�ġ�� �����ؼ� �ڰ����� ���� ���θ� �Ǵ��Ѵ�.
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
                //�ڰ��� ���İ� �߻�����.
                if(soundStartInRecording==false) {
                    //�ڰ��� �м� �� �̰��� ���� �ϱ����� ī��Ʈ �ʱ�ȭ, �̰��̶�� �� ī��Ʈ�� �ſ� ���Ƽ� �ȵȴ�.
                    continueCntInChkTermForGrinding = 0;
                    continueCntInChkTermForGrindingChange = 0;
                    //TODO ���� �������� ���� ��� ���ú��� ������, ���ķ� ������ �Ҹ��� �ѹ��� ���� �Ѵ�.
                    chkDBAgainInRecording = decibel;
                    //���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ����, ���� ���� ���·� ��ȯ
                    soundStartInRecording = true;
                    //�ڰ��� ī��Ʈ�� �ʱ�ȭ(���� ���� �߿� ī��Ʈ ����)
                    soundStartAndSnroingCnt = 0;
                    //���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
                    //���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
                    soundStartAndSnroingOppCnt = 0;
                    //���Ľ��۽ð��� �����ϱ� ���� ���vo�� ����
                    StartEnd st = new StartEnd();
                    st.start = times;
                    st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
                    //st.AnalysisRawDataList.add(maxARD);
                    snoringTermList.add(st);
                    //���İ� ����Ǵ� ���� �ִ� ���ú��� �����ļ��� ���ú��� ����� ����ϱ� ���� ���� �ʱ�ȭ �Ѵ�.
                    //�ִ� ���ú� ���� �����ļ� ���ú� ���� �����Ѵ�.(�ʱ�ȭ)
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
                            //������θ� ���ϱ� �Ұǵ�, ��ճ������� �󸶳� ���̰� �־����� ���غ�.. ���� �� ���� �ִ�.
                            snoringDbChkCnt++;
                        }
                        firstDecibelAvg = (firstDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 2, 40))/2;
                        secondDecibelAvg = (secondDecibelAvg+calcforChkSnoringDbNotNomarlize(allFHAndDB, 10, 18))/2;
                    }
                }
            }else {
                //�Ҹ��� �߻����� �ʾ�����, ���� �ڰ��� ���� �߻������� üũ �Ѵ�.
                if(soundStartInRecording==true) {
                    if(snoringTermList == null || snoringTermList.size()==0){
                        soundStartInRecording = false;
                        CHECKED_STATUS = CHECKED_ERROR;
                        return CHECKED_ERROR;
                    }
                    //���� ���� ���̶��, ���� üũ���� üũ ���۽ð��� 1�ʸ� �Ѿ����� üũ�Ѵ�.
                    if(times-snoringTermList.get(snoringTermList.size()-1).start>0.16*7){
                        //���Ľ��۽ð����� 1�ʰ� �������ٸ� , �м��� �ߴ��ϰ�, ���� �ڰ��� �߻� ī��Ʈ�� üũ�Ͽ� ����Ѵ�.
                        soundStartInRecording = false;
                        //�ι�° ���ú��� �� ũ�� ��Ÿ����.
                        double  diffMaxToLow = Math.abs(secondDecibelAvg) - Math.abs(firstDecibelAvg);
                        //���̰� �ƽø� ���ú��� ���� �̻��ΰ�
                        if(diffMaxToLow > 0 ) {
                            //1�ʰ� �������ٸ�, ���� ����� ������ �ִ� ���ú� ��հ� ��� ���ú��� ���̸� ���Ѵ�.
                            //���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
                            //���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
                            soundStartAndSnroingCnt++;
                        }else {
                            //���� ī��Ʈ ���� ���ϰ� ���
                            //-> ����� ī��Ʈ ��� �ݴ� ī��Ʈ ����
                            soundStartAndSnroingOppCnt++;
                        }
                        //1. 5~200 ���ļ��� ��� ���ú����� 43~80 ���ļ��� ��� ���ú��� �� Ŀ����
                        //2. �ڰ��� ���� ī��Ʈ 1 ��, ����ī��Ʈ�� 3���� ũ�� �ȵȴ�.(
                        if(soundStartAndSnroingCnt > 0 && soundStartAndSnroingOppCnt<soundStartAndSnroingCnt*3) {
                            //�ڰ��� ī��Ʈ�� �����߾���, �ڰ��� ���vo�� ���� �ð��� ���
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
                            //�ڰ��� ī��Ʈ�� ������ ���� ������.
                            //�ڰ��� ��� vo ��� �̰��� ��� vo�� �ִ´�.
                            //�̰��̴� ���� ������� �Ѵ�.
                            if(continueCntInChkTermForGrindingChange > 0 && continueCntInChkTermForGrinding> 0 &&
                                    firstDecibelAvg > tmpMaxDb/2 &&
                                    Math.abs(firstDecibelAvg - secondDecibelAvg)<5 &&
                                    //grindingChange�� 3�̻��� ����, / �� 10���� ũ�� 12���� �۾ƾ���
                                    ((continueCntInChkTermForGrindingChange >= 3 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange >= 10 && continueCntInChkTermForGrinding/continueCntInChkTermForGrindingChange <= 12)
                                            ||
                                            //2������ ����, / �� 9���� �۾ƾ���
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
                        //���� ���� ���̰�, �Ҹ��� �߻����� �ʾ����� ���� 1�ʰ� ������ �ʾҴ�.
                        //���� ī��Ʈ ���� ���ϰ� ���
                        //-> ����� ī��Ʈ ��� �ݴ� ī��Ʈ ����
                        //���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
                        soundStartAndSnroingOppCnt++;
                        //snoringTermList.remove(snoringTermList.size()-1);
                        //soundStartInRecording = false;
                    }
                }
                //�Ҹ��� �߻����� �ʾҰ�, ���İ� ���� ���� ���°� �ƴϴ�.

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
            //�Ҹ��� �߻��߰�, �м� ���� ���� ���� true �� ��� �����Ѵ�.
            if(isOSATermTimeOccur) {
                //0.1�� ���� �Ҹ��� 70% �̻� �߻��� ��� �Ҹ��� �߻��� ������ ����.

                if(isOSATermCnt+isBreathTermCnt>90 && isOSATermCnt > 20 && isBreathTermCnt > 70) {
                    //���������� �д�. 0.5�� ���� �������� �Ҹ��� �߻��ؾ� �Ѵ�.
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
            //��ȣ���� �����ϱ� ���� �м� ���� ���� �ʱ�ȭ
            //�ڰ��̰� �߻��ϰ� 5�ʰ� ��������� ��.
            if(snoringTermList.size() > 0
                    && snoringTermList.get(snoringTermList.size()-1).end != 0
                    && times - snoringTermList.get(snoringTermList.size()-1).end > 0
                    && times - snoringTermList.get(snoringTermList.size()-1).end < 5
                    && !isOSATermTimeOccur) {
                //0.1�� ���� ������ 70% �̻� �߻��� ��� �Ҹ��� �߻��� ������ ����.
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
        //��ȣ�� �߻��� 3�е��� ������� �ʴ´ٸ� ���
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end==0 && times-osaTermList.get(osaTermList.size()-1).start > 180) {
            isOSATermTimeOccur = false;
            isOSATermCnt = 0;
            isBreathTerm = false;
            isBreathTermCnt = 0;
            OSAcurTermTime = 0.0;
            osaTermList.remove(osaTermList.size()-1);
        }

        //��ȣ���� ������� �ʾҰ�, ������ �߻��ߴٸ� ���
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
        
        //��ȣ�� ���� �� ������ �ð��� �ʹ� ª���� �����Ѵ�.
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
            if(osaTermList.get(osaTermList.size()-1).end - osaTermList.get(osaTermList.size()-1).start < 5 ){
                osaTermList.remove(osaTermList.size()-1);
            }
        }

        //��ȣ�� ���� �� 5�� �̳��� �ڰ��̰� �߻����� ������ ���
        //��ȣ�� ���� �� 5�� ���� �ڰ��� �߻����θ� üũ�Ѵ�.
        if(osaTermList.size()>0 && osaTermList.get(osaTermList.size()-1).end!=0 && times - osaTermList.get(osaTermList.size()-1).end < 5) {
            if(snoringTermList.size()>0 && snoringTermList.get(snoringTermList.size()-1).start - osaTermList.get(osaTermList.size()-1).end > 0 && snoringTermList.get(snoringTermList.size()-1).start - osaTermList.get(osaTermList.size()-1).end < 5){
                //�ڰ��̰� ���� ���̰� �Ǿ��� ��, üũ �÷��׸� ������Ʈ
                if(snoringTermList.get(snoringTermList.size() - 1).end==0){
                    osaTermList.get(osaTermList.size()-1).chk = 1;
                }
            }
        }
        //��ȣ�� ���� �� 5�ʰ� ���� ��� �÷��׸� üũ�ؼ� �ڰ��̸� �����Ѵ�.
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
            //���İ� �߻�����.
            if(someNoiseStartInRecording==false) {
                //TODO ���� �������� ���� ��� ���ú��� ������, ���ķ� ������ �Ҹ��� �ѹ��� ���� �Ѵ�.
                someNoiseChkDBAgainInRecording = amplitude;
                //���� �߿� �Ҹ��� �߻��߰� ���� ������ �ƴ� ����, ���� ���� ���·� ��ȯ
                someNoiseStartInRecording = true;
                //�ڰ��� ī��Ʈ�� �ʱ�ȭ(���� ���� �߿� ī��Ʈ ����)
                someNoiseStartCnt = 0;
                //���� ���ļ� ����� ���ú��� ���ݺ��� ���ٸ� �ڰ��� ī��Ʈ ����
                //���� ���� �ð� ���� ��ŭ üũ�� �ȵǾ����� ī��Ʈ�� �ؼ� ���� �� �ִ�.
                someNoiseStartOppCnt = 0;
                //���Ľ��۽ð��� �����ϱ� ���� ���vo�� ����
                StartEnd st = new StartEnd();
                st.start = times;
                st.AnalysisRawDataList = new ArrayList<AnalysisRawData>();
                noiseTermListForOsaList.add(st);
                //���İ� ����Ǵ� ���� �ִ� ���ú��� �����ļ��� ���ú��� ����� ����ϱ� ���� ���� �ʱ�ȭ �Ѵ�.
                //�ִ� ���ú� ���� �����ļ� ���ú� ���� �����Ѵ�.(�ʱ�ȭ)
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
	                //�ڰ��� ī��Ʈ�� �����߾���, �ڰ��� ���vo�� ���� �ð��� ���
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