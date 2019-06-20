package snoring;

public class RecordFragment {
    private boolean mShouldContinue = true;
    public boolean getShouldContinue(){
        return this.mShouldContinue;
    }
    public void setMShoudContinue(boolean mShouldContinue) {
    	this.mShouldContinue=mShouldContinue;
    }
    private String filePath;
    public void setFilePath(String filePath){
    	this.filePath=filePath;
    }
    public String getFilePath() {
    	return this.filePath;
    }
    RecordingThread recordingThread;
    Long recordStartDtL;
    public Long getRecordStartDtl(){
        return this.recordStartDtL;
    }
    public void start() {
        recordStartDtL= System.currentTimeMillis();
        mShouldContinue = true;
        recordingThread = new RecordingThread(this);
        recordingThread.setPriority(Thread.MAX_PRIORITY);
        recordingThread.start();
    }

}
