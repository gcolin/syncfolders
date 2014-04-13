package net.gcolin.sync.model;

public class BackupState implements Cloneable{

    public enum STATE{SYNC,COPY,DELETE, END}
    
    private STATE state;
    private int percent;
    private long timeInitial;
    private int percentCurrentFile;
    public STATE getState() {
        return state;
    }
    public void setState(STATE state) {
        this.state = state;
    }
    public int getPercent() {
        return percent;
    }
    public void setPercent(int percent) {
        this.percent = percent;
    }
    public long getTimeInitial() {
        return timeInitial;
    }
    public void setTimeInitial(long timeInitial) {
        this.timeInitial = timeInitial;
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    public int getPercentCurrentFile() {
        return percentCurrentFile;
    }
    public void setPercentCurrentFile(int percentCurrentFile) {
        this.percentCurrentFile = percentCurrentFile;
    }
}
