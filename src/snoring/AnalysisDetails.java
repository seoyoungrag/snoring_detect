package snoring;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisDetails {

	private Integer analysisDetailsId;
	private Analysis analysis;
	private Integer termTypeCd;
	private LocalDateTime termStartDt;
	private LocalDateTime termEndDt;
	
	public Integer getAnalysisDetailsId() {
		return analysisDetailsId;
	}
	public void setAnalysisDetailsId(Integer analysisDetailsId) {
		this.analysisDetailsId = analysisDetailsId;
	}
	public Analysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	public Integer getTermTypeCd() {
		return termTypeCd;
	}
	public void setTermTypeCd(Integer termTypeCd) {
		this.termTypeCd = termTypeCd;
	}
	public LocalDateTime getTermStartDt() {
		return termStartDt;
	}
	public void setTermStartDt(LocalDateTime termStartDt) {
		this.termStartDt = termStartDt;
	}
	public LocalDateTime getTermEndDt() {
		return termEndDt;
	}
	public void setTermEndDt(LocalDateTime termEndDt) {
		this.termEndDt = termEndDt;
	}

	

}
