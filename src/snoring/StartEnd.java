package snoring;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StartEnd {
    public int negitiveCnt;
    public int positiveCnt;
    public double start;
    public double end;
    public List<AnalysisRawData> AnalysisRawDataList;
    public double second;
    public double first;
    public double chk;

    public String getTerm() {
        return
                String.format("%.2f", start)
                        + "~" + String.format("%.2f", end)
                        + " second: " + String.format("%.2f", second)
                        + " first: " + String.format("%.2f", first)
                        + " chk: " + String.format("%.2f", chk)
                        + " positiveCnt: " + positiveCnt
                        + " negitiveCnt: " + negitiveCnt;
    }

    public String getTermForRequest(int termCd, long recordStartDtL) {
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return "termTypeCd: " + termCd + ", termStartDt: "
                + dayTime.format(new Date((long) (recordStartDtL + this.start * 1000))) + ",termEndDt: "
                + dayTime.format(new Date((long) (recordStartDtL + this.end * 1000)));
    }


}


