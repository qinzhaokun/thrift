package proxy.client;

import java.io.Serializable;
import java.util.Date;
public class Server implements Serializable {

    public static final int default_weight = 10;
    private static final long serialVersionUID = -2097689067316891538L;
    private String ip;
    private int port;
    private double weight;
    private String serverAppKey;
    private Date startTime;
    private float floating;
    private int status;
    private boolean unifiedProto;
    private int socketNullNum;
    private ServerFeedback lastFeedback;

    public Server(String ip, int port) {
        this(ip, port, "");
    }

    public Server(String ip, int port, String serverAppKey) {
        this(ip, port, serverAppKey, 10);
    }

    /** @deprecated */
    @Deprecated
    public Server(String ip, int port, String serverAppKey, int weight) {
        this.weight = 10.0D;
        this.startTime = new Date();
        this.floating = 1.0F;
        this.unifiedProto = false;
        this.socketNullNum = 0;
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = (double)weight;
    }

    public Server(String ip, int port, String serverAppKey, double weight) {
        this.weight = 10.0D;
        this.startTime = new Date();
        this.floating = 1.0F;
        this.unifiedProto = false;
        this.socketNullNum = 0;
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = weight;
    }

    public Server(String ip, int port, String serverAppKey, double weight, boolean unifiedProto) {
        this.weight = 10.0D;
        this.startTime = new Date();
        this.floating = 1.0F;
        this.unifiedProto = false;
        this.socketNullNum = 0;
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = weight;
        this.unifiedProto = unifiedProto;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getDoubleServerWeight() {
        return this.weight;
    }

    public int getServerWeight() {
        return (int)this.weight;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    /** @deprecated */
    @Deprecated
    public Date getStartTime() {
        return this.startTime;
    }

    /** @deprecated */
    @Deprecated
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getServerAppKey() {
        return this.serverAppKey;
    }

    public void setServerAppKey(String serverAppKey) {
        this.serverAppKey = serverAppKey;
    }

    /** @deprecated */
    @Deprecated
    public float getFloating() {
        return this.floating;
    }

    /** @deprecated */
    @Deprecated
    public void setFloating(float floating) {
        this.floating = floating;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Server)) {
            return false;
        } else {
            Server server = (Server)o;
            if (this.port != server.port) {
                return false;
            } else {
                return this.ip.equals(server.ip);
            }
        }
    }

    /** @deprecated */
    @Deprecated
    public ServerFeedback getLastFeedback() {
        return this.lastFeedback;
    }

    /** @deprecated */
    @Deprecated
    public void setLastFeedback(ServerFeedback lastFeedback) {
        this.lastFeedback = lastFeedback;
    }

    public int hashCode() {
        int result = this.ip.hashCode();
        result = 31 * result + this.port;
        return result;
    }

    public void reduceFloating() {
        this.floating /= 3.0F;
    }

    public String toString() {
        return "Server{ip='" + this.ip + '\'' + ", port=" + this.port + ", weight=" + this.weight + ", serverAppKey='" + this.serverAppKey + '\'' + ", status=" + this.status + ", socketNullNum=" + this.socketNullNum + '}';
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void degrade() {
        if (Double.compare(this.weight, 1.0D) >= 0) {
            this.weight = 0.1D;
        } else if (Double.compare(this.weight, 0.001D) >= 0) {
            this.weight = 1.0E-4D;
        } else if (Double.compare(this.weight, 1.0E-6D) >= 0) {
            this.weight = 1.0E-6D;
        }

    }

    public int getSocketNullNum() {
        return this.socketNullNum;
    }

    public void addSocketNullNum() {
        ++this.socketNullNum;
    }

    public void resetSocketNullNum() {
        this.socketNullNum = 0;
    }

    public boolean isUnifiedProto() {
        return this.unifiedProto;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }
}
