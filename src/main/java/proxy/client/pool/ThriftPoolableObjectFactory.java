package proxy.client.pool;

import java.net.SocketTimeoutException;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftPoolableObjectFactory implements PoolableObjectFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftPoolableObjectFactory.class);
    private String host;
    private int port;
    private int timeOut;
    private int connTimeOut;
    private boolean isImplFacebookService;
    private boolean async;
    private String serverAppKey;

    public ThriftPoolableObjectFactory(String host, int port, int timeOut, int connTimeOut) {
        this("", host, port, timeOut, true, false, connTimeOut);
    }

    public ThriftPoolableObjectFactory(String serverAppKey, String host, int port, int timeOut, boolean isImplFacebookService, boolean async, int connTimeOut) {
        this.connTimeOut = 500;
        this.isImplFacebookService = true;
        this.async = false;
        this.serverAppKey = serverAppKey;
        this.host = host;
        this.port = port;
        this.timeOut = timeOut;
        this.isImplFacebookService = isImplFacebookService;
        this.async = async;
        this.connTimeOut = connTimeOut;
    }

    public void destroyObject(Object tTransport) throws Exception {
        if (null != tTransport) {
            if (tTransport instanceof TSocket) {
                TSocket socket = (TSocket) tTransport;
                if (socket.isOpen()) {
                    LOG.debug("destroyObject() host:" + this.host + ",port:" + this.port + ",socket:" + socket.getSocket() + ",isOpen:" + socket.isOpen());
                    socket.close();
                }
            } else if (tTransport instanceof TNonblockingSocket) {
                TNonblockingSocket socket = (TNonblockingSocket) tTransport;
                if (socket.getSocketChannel().isOpen()) {
                    LOG.debug("destroyObject() host:" + this.host + ",port:" + this.port + ",isOpen:" + socket.isOpen());
                    socket.close();
                }
            }

        }
    }

    public Object makeObject() throws Exception {
        int count = 3;
        TTransportException exception = null;

        while (true) {
            if (count-- > 0) {
                long start = System.currentTimeMillis();
                TTransport transport = null;
                boolean connectSuccess = false;

                Object var7;
                try {
                    if (this.async) {
                        transport = new TNonblockingSocket(this.host, this.port, this.connTimeOut);
                        LOG.debug("makeObject() " + ((TNonblockingSocket) transport).getSocketChannel().socket());
                    } else {
                        transport = new TSocket(this.host, this.port, this.connTimeOut);
                        ((TTransport) transport).open();
                        ((TSocket) transport).setTimeout(this.timeOut);
                        LOG.debug("makeObject() " + ((TSocket) transport).getSocket());
                    }

                    connectSuccess = true;
                    var7 = transport;
                } catch (TTransportException var19) {
                    exception = var19;
                    LOG.warn("makeObject() " + var19.getLocalizedMessage() + ":" + var19.getType() + "/" + this.host + ":" + this.port + "/" + (System.currentTimeMillis() - start));
                    if (var19.getCause() instanceof SocketTimeoutException) {
                        continue;
                    }

                    throw new RuntimeException(exception);
                } catch (Exception var20) {
                    LOG.warn("makeObject()", var20);
                    throw new RuntimeException(var20);
                } finally {
                    if (transport != null && !connectSuccess) {
                        try {
                            ((TTransport) transport).close();
                        } catch (Exception var18) {
                            LOG.warn(var18.getMessage(), var18);
                        }
                    }

                }

                return var7;
            }

            throw new RuntimeException(exception);
        }
    }

    public boolean validateObject(Object arg0) {
        try {
            if (arg0 instanceof TSocket) {
                TSocket thriftSocket = (TSocket) arg0;
                if (thriftSocket.isOpen()) {
                    return true;
                } else {
                    LOG.warn("validateObject() failed " + thriftSocket.getSocket());
                    return false;
                }
            } else if (arg0 instanceof TNonblockingSocket) {
                TNonblockingSocket socket = (TNonblockingSocket) arg0;
                if (socket.getSocketChannel().isOpen()) {
                    return true;
                } else {
                    LOG.warn("validateObject() failed " + socket.getSocketChannel().socket());
                    return false;
                }
            } else {
                LOG.warn("validateObject() failed unkown Object:" + arg0);
                return false;
            }
        } catch (Exception var3) {
            LOG.warn("validateObject() failed " + var3.getLocalizedMessage());
            return false;
        }
    }

    public void passivateObject(Object arg0) throws Exception {
    }

    public void activateObject(Object arg0) throws Exception {
    }
}
