package proxy.client;


import java.io.Serializable;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

public class ServerConn implements Serializable {
    private static final long serialVersionUID = 1852506157407273889L;
    private Server server;
    private Config connPoolConf;
    private ObjectPool objectPool;
    private int _connectSuccess = 0;
    private int _connectFailed = 0;
    private long _invokeSuccessMills = 0L;
    private int _invokeSuccessNums = 0;
    private int _invokeTimeoutNums = 0;

    public ServerConn(Server server) {
        this.server = server;
    }

    public ServerConn() {
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Config getConnPoolConf() {
        return this.connPoolConf;
    }

    public void setConnPoolConf(Config connPoolConf) {
        this.connPoolConf = connPoolConf;
    }

    public ObjectPool getObjectPool() {
        return this.objectPool;
    }

    public void setObjectPool(ObjectPool objectPool) {
        this.objectPool = objectPool;
    }

    public void getConnectSuccess() {
        ++this._connectSuccess;
    }

    public void getConnnectFailed() {
        ++this._connectFailed;
    }

    public void invokeSuccess(long takeMills) {
        ++this._invokeSuccessNums;
        this._invokeSuccessMills += takeMills;
    }

    public void invokeTimeout(long takeMills) {
        ++this._invokeTimeoutNums;
    }

    public ServerFeedback resetFeedback() {
        ServerFeedback feedback = new ServerFeedback(this._connectSuccess, this._connectFailed, this._invokeTimeoutNums, this._invokeSuccessNums, this._invokeSuccessNums > 0 ? (int)(this._invokeSuccessMills / (long)this._invokeSuccessNums) : 0);
        this._connectSuccess = 0;
        this._connectFailed = 0;
        this._invokeSuccessMills = 0L;
        this._invokeSuccessNums = 0;
        this._invokeTimeoutNums = 0;
        return feedback;
    }

    public String toString() {
        return "ServerConn{server=" + this.server + '}';
    }
}
