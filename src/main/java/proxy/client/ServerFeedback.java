package proxy.client;

import java.io.Serializable;

/** @deprecated */
@Deprecated
public class ServerFeedback implements Serializable {
    private static final long serialVersionUID = 6121511348472568555L;
    private int getConnectSuccessNums;
    private int getConnectFailedNums;
    private int invokeTimeoutNums;
    private int invokeSuccessNums;
    private int invokeTime;
    private int score;

    public ServerFeedback(int getConnectSuccessNums, int getConnectFailedNums, int invokeTimeoutNums, int invokeSuccessNums, int invokeTime) {
        this.getConnectSuccessNums = getConnectSuccessNums;
        this.getConnectFailedNums = getConnectFailedNums;
        this.invokeTimeoutNums = invokeTimeoutNums;
        this.invokeSuccessNums = invokeSuccessNums;
        this.invokeTime = invokeTime;
    }

    public int getGetConnectSuccessNums() {
        return this.getConnectSuccessNums;
    }

    public int getGetConnectFailedNums() {
        return this.getConnectFailedNums;
    }

    public int getInvokeTimeoutNums() {
        return this.invokeTimeoutNums;
    }

    public int getInvokeSuccessNums() {
        return this.invokeSuccessNums;
    }

    public int getInvokeTime() {
        return this.invokeTime;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGetConnectFailedRate() {
        return this.getConnectSuccessNums + this.getConnectFailedNums > 0 ? 100 * this.getConnectFailedNums / (this.getConnectSuccessNums + this.getConnectFailedNums) : 0;
    }

    public int getInvokeTimeoutRate() {
        return this.invokeSuccessNums + this.invokeTimeoutNums > 0 ? 100 * this.invokeTimeoutNums / (this.invokeSuccessNums + this.invokeTimeoutNums) : 0;
    }

    public String toString() {
        return "ServerFeedback{getConnectSuccessNums=" + this.getConnectSuccessNums + ", getConnectFailedNums=" + this.getConnectFailedNums + ", invokeTimeoutNums=" + this.invokeTimeoutNums + ", invokeSuccessNums=" + this.invokeSuccessNums + ", invokeTime=" + this.invokeTime + ", score=" + this.score + '}';
    }
}
