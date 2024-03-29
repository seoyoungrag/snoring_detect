package snoring;

public class GrinderClass {

	String findedTime;
	int findedTimeCnt;
	double findedHz;
	double findedSecondHz;
	double findedDecibel;
	int findedAmplitude;
	public String getFindedTime() {
		return findedTime;
	}
	public void setFindedTime(String findedTime) {
		this.findedTime = findedTime;
	}
	public int getFindedTimeCnt() {
		return findedTimeCnt;
	}
	public void setFindedTimeCnt(int findedTimeCnt) {
		this.findedTimeCnt = findedTimeCnt;
	}
	public double getFindedHz() {
		return findedHz;
	}
	public void setFindedHz(double findedHz) {
		this.findedHz = findedHz;
	}
	
	public double getFindedSecondHz() {
		return findedSecondHz;
	}
	public void setFindedSecondHz(double findedSecondHz) {
		this.findedSecondHz = findedSecondHz;
	}
	
	public double getFindedDecibel() {
		return findedDecibel;
	}
	public void setFindedDecibel(double findedDecibel) {
		this.findedDecibel = findedDecibel;
	}
	public int getFindedAmplitude() {
		return findedAmplitude;
	}
	public void setFindedAmplitude(int findedAmplitude) {
		this.findedAmplitude = findedAmplitude;
	}
	public GrinderClass() {
		super();
	}
	public GrinderClass(String findedTime, int findedTimeCnt, double findedHz, double findedSecondHz,
			double findedDecibel, int findedAmplitude) {
		super();
		this.findedTime = findedTime;
		this.findedTimeCnt = findedTimeCnt;
		this.findedHz = findedHz;
		this.findedSecondHz = findedSecondHz;
		this.findedDecibel = findedDecibel;
		this.findedAmplitude = findedAmplitude;
	}
	@Override
	public String toString() {
		return "GrinderClass [findedTime=" + findedTime + ", findedTimeCnt=" + findedTimeCnt + ", findedHz=" + findedHz
				+ ", findedSecondHz=" + findedSecondHz + ", findedDecibel=" + findedDecibel + ", findedAmplitude="
				+ findedAmplitude + "]";
	}
	
	
}
