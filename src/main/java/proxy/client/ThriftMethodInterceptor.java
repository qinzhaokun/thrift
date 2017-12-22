package proxy.client;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.pool.ObjectPool;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.client.cluster.ICluster;
import proxy.client.loadbalancer.ILoadBalancer;
import proxy.client.loadbalancer.RandomLoadBalancer;
import proxy.client.timeoutpolicy.ITimeoutPolicy;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThriftMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ThriftMethodInterceptor.class);
    private ThriftClientProxy clientProxy;
    private ICluster cluster;
    private ILoadBalancer loadBalancer;
    private ITimeoutPolicy timeoutPolicy;
    private boolean retryRequest = true;
    private int retryTimes = 3;




    public void setRetryTimes(int retryTimes){
        if(retryTimes != 0){
            this.retryTimes = retryTimes;
        }
    }

    public void setRetryRequest(boolean retryRequest) {
        this.retryRequest = retryRequest;
    }

    public boolean isRetryRequest() {
        return retryRequest;
    }

    public ThriftMethodInterceptor(ThriftClientProxy clientProxy, ICluster cluster, ILoadBalancer loadBalancer, ITimeoutPolicy timeoutPolicy) {
        this.clientProxy = clientProxy;
        this.loadBalancer = loadBalancer;
        this.timeoutPolicy = timeoutPolicy;
        if (null == this.loadBalancer) {
            this.loadBalancer = new RandomLoadBalancer(1);
        }
        this.cluster = cluster;

        //this.responseCollector = responseCollector;
        //this.invokeInfo.append("|clientIP:").append(LocalPointConf.getAppIp()).append("|appkey: ").append(clientProxy.getAppKey()).append("|remoteAppkey:").append(clientProxy.getRemoteAppkey()).append("|remoteServerPort:").append(clientProxy.getRemoteServerPort()).append("|clusterManager:").append(clientProxy.getClusterManager()).append("|version:").append("|mtthrift-v" + MtThriftManifest.getVersion());
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String methodName = methodInvocation.getMethod().getName();
        long begin = System.currentTimeMillis();
        ServerConn serverConn = null;
        TTransport socket = null;
        Throwable toThrow = null;
        List<ServerConn> connList = this.cluster.getServerConnList();
        if (null != connList && !connList.isEmpty()) {
            int timeout = this.clientProxy.getTimeout();
            if (null != this.timeoutPolicy) {
                int timeoutInPolicy = this.timeoutPolicy.getTimeoutByConfig(methodInvocation, this.clientProxy.getTimeout());
                if (timeoutInPolicy > 0) {
                    timeout = timeoutInPolicy;
                }
            }

            boolean retryFirst = true;

            for(int i = 0; connList.size() > 0 && i < this.retryTimes; ++i) {
                serverConn = this.loadBalancer.select(connList, methodInvocation);
                socket = this.getConnection(serverConn, methodName, timeout);
                if (socket == null) {
                    connList = this.removeConn(connList, serverConn, retryFirst);
                    retryFirst = false;
                    serverConn.getServer().addSocketNullNum();
                    if (serverConn.getServer().getSocketNullNum() >= 3) {
                        serverConn.getServer().resetSocketNullNum();
                        serverConn.getServer().degrade();
                        //this.cluster.updateServerConn(serverConn);
                    }
                } else {
                    serverConn.getServer().resetSocketNullNum();
                    //this.clientProxy.noticeGetConnect(System.currentTimeMillis() - begin);
                    Entry<Object, Throwable> rpcResult = this.clientProxy.getAsync() ? this.asyncRpcInvoke(methodInvocation, serverConn, (TNonblockingSocket)socket, methodName, begin, timeout) : this.syncRpcInvoke(methodInvocation, serverConn, (TSocket)socket, methodName, begin, timeout);
                    if (rpcResult != null) {
                        toThrow = (Throwable)rpcResult.getT2();
                        if (rpcResult.getT1() == null && toThrow == null) {
                            LOG.info("status is success");
                            //MtraceUtils.clientMark(STATUS.SUCCESS);
                            return null;
                        }

                        if (rpcResult.getT1() != null || toThrow == null) {
                            /*if (this.responseCollector != null) {
                                this.responseCollector.collect(methodInvocation, rpcResult.getT1());
                            }*/

                            //MtraceUtils.clientMark(STATUS.SUCCESS);
                            LOG.info("status is success");
                            return rpcResult.getT1();
                        }

                        if (toThrow.getCause() == null || !(toThrow.getCause() instanceof TTransportException) || ((TTransportException)toThrow.getCause()).getType() != 4 && (toThrow.getCause().getCause() == null || !(toThrow.getCause().getCause() instanceof SocketException))) {
                            throw toThrow;
                        }

                        LOG.info(methodName + " invoke " + toThrow.getCause() + ", retry it");
                        if (!this.retryRequest) {
                            break;
                        }

                        connList = this.removeConn(connList, serverConn, retryFirst);
                        retryFirst = false;
                    }
                }
            }

            if (socket == null) {
                throw new TException("can't get valid connection to invoke " + methodName/* + this.invokeInfo*/);
            } else if (toThrow != null) {
                throw toThrow;
            } else {
                throw new TException("thrift rpc unknown Exception");
            }
        } else {
            throw new TException("connection list is empty !" /*+ this.invokeInfo.toString()*/);
        }
    }

    private TTransport getConnection(ServerConn serverConn, String methodName, int timeout) {
        TTransport socket = null;
        ObjectPool socketPool = null;
        String serverIpPort = null;
        long start = System.currentTimeMillis();
        long takes = 0L;
        String message = null;
        Object var12 = null;

        TTransport var13;
        try {
            serverIpPort = serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort();
            socketPool = serverConn.getObjectPool();
            if (socketPool != null) {
                socket = (TTransport)socketPool.borrowObject();
                takes = System.currentTimeMillis() - start;
                if (takes > (long)(timeout / 3)) {
                    message = "Get Connection from " + serverIpPort + " Timeout! Time:" + takes + ",actives:" + socketPool.getNumActive() + ",idle:" + socketPool.getNumIdle();
                    new RuntimeException(message);
                }

                var13 = socket;
                return var13;
            }

            var13 = null;
        } catch (RuntimeException var18) {
            if (var18.getCause() instanceof TTransportException && socket != null) {
                this.returnBrokenConnection(serverConn, socket);
                socket = null;
            }

            takes = System.currentTimeMillis() - start;
            (new StringBuilder()).append("Get Connection from ").append(serverIpPort).append(" Exception! Time:").append(takes).append(",actives:").append(socketPool.getNumActive()).append(",idle:").append(socketPool.getNumIdle()).toString();
            return socket;
        } catch (Exception var19) {
            takes = System.currentTimeMillis() - start;
            (new StringBuilder()).append("Get Connection from ").append(serverIpPort).append(" Exception! Time:").append(takes).append(",actives:").append(socketPool.getNumActive()).append(",idle:").append(socketPool.getNumIdle()).toString();
            return socket;
        } finally {
            if (socket != null) {
                serverConn.getConnectSuccess();
            } else {
                serverConn.getConnnectFailed();
            }

        }

        return var13;
    }

    void returnBrokenConnection(ServerConn serverConn, TTransport socket) {
        try {
            if (socket != null && serverConn != null && serverConn.getObjectPool() != null) {
                serverConn.getObjectPool().invalidateObject(socket);
            }
        } catch (Exception var4) {
            throw new RuntimeException("error returnBrokenConnection()", var4);
        }
    }

    private List<ServerConn> removeConn(List<ServerConn> connAll, ServerConn delete, boolean retryFirst) {
        if (retryFirst) {
            List<ServerConn> remainConns = new ArrayList();
            Iterator i$ = connAll.iterator();

            while(i$.hasNext()) {
                ServerConn conn = (ServerConn)i$.next();
                if (conn != delete) {
                    remainConns.add(conn);
                }
            }

            return remainConns;
        } else {
            connAll.remove(delete);
            return connAll;
        }
    }

    private Entry<Object, Throwable> asyncRpcInvoke(MethodInvocation methodInvocation, ServerConn serverConn, TNonblockingSocket socket, String methodName, long begin, int timeout) throws Throwable {
        Throwable toThrow = null;
        Object o = null;

        try {
            /*MtThrfitInvokeInfo mtThrfitInvokeInfo = new MtThrfitInvokeInfo(serverConn.getServer().getServerAppKey(), this.clientProxy.getServiceSimpleName() + "." + methodName, LocalPointConf.getAppIp(), 0, serverConn.getServer().getIp(), serverConn.getServer().getPort());
            MtThrfitInvokeInfo.setMtThrfitInvokeInfo(mtThrfitInvokeInfo);
            mtThrfitInvokeInfo.setUniProto(serverConn.getServer().isUnifiedProto());
            */Object service = this.clientProxy.getClientInstance(socket);
            ((TAsyncClient)service).setTimeout((long)timeout);
            Object[] args = methodInvocation.getArguments();
            AsyncMethodCallback callback = null;
            /*if (args != null && args.length > 0 && args[args.length - 1] instanceof AsyncMethodCallback) {
                callback = new AsyncMethodCallback((AsyncMethodCallback)args[args.length - 1], this, serverConn, socket, methodName);
                if (this.loadBalancer instanceof RandomLoadBalancer) {
                    callback.setRouteType("default");
                } else {
                    callback.setRouteType("user-defined");
                }

                if (serverConn.getServer().isUnifiedProto()) {
                    callback.setProtocolType("unified");
                } else {
                    callback.setProtocolType("old");
                }

                args[args.length - 1] = callback;
            }*/

            o = methodInvocation.getMethod().invoke(service, args);
            /*if (callback != null) {
                callback.setClientSpan(Tracer.getClientTracer().getSpan());
                Tracer.getClientTracer().clearCurrentSpan();
            }*/

            return new Entry(o, (Object)null);
        } catch (Exception var14) {
            LOG.debug("asyncRpcInvoke failed...", var14);
            toThrow = var14.getCause();
            if (socket != null) {
                this.returnBrokenConnection(serverConn, socket);
            }

            return new Entry(o, toThrow);
        }
    }

    private Entry<Object, Throwable> syncRpcInvoke(MethodInvocation methodInvocation, ServerConn serverConn, TSocket socket, String methodName, long begin, int timeout) throws Throwable {
        String serverIpPort = serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort();
        Throwable toThrow = null;
        Object o = null;

        TFramedTransport transport = null;

        try {
            //Span span;
            Object cause;
            boolean returnNull;
            boolean userDefinedException;
            boolean timeoutException;
            try {

                if (this.loadBalancer instanceof RandomLoadBalancer) {
                    LOG.info("Call.routeType", "default");
                } else {
                    LOG.info("Call.routeType", "user-defined");
                }

                if (serverConn.getServer().isUnifiedProto()) {
                    LOG.info("Call.protocolType", "unified");
                } else {
                    LOG.info("Call.protocolType", "old");
                }

                transport = new TFramedTransport(socket);
                socket.setTimeout(timeout);
                if (this.clientProxy.getAnnotatedThrift()) {
                    TProtocol protocol = this.clientProxy.getProtocol(transport);
                    LOG.info("Call.thriftType", "annotation");
                    //o = ((ThriftMethodHandler)this.methods.get(methodInvocation.getMethod())).invoke(protocol, protocol, this.sequenceId.getAndIncrement(), methodInvocation.getArguments());
                } else {
                    //Object service = this.clientProxy.getClientInstance(transport);
                    Object service = this.clientProxy.getClientInstance(socket);
                    LOG.info("Call.thriftType", "idl");
                    o = methodInvocation.getMethod().invoke(service, methodInvocation.getArguments());
                }

                //Cat.logEvent("OctoCall.requestSize", SizeUtil.getLogSize(transport.getRequestSize()));
                //Cat.logEvent("OctoCall.responseSize", SizeUtil.getLogSize(transport.getResponseSize()));
                //Tracer.getClientSpan().setPackageSize(transport.getResponseSize());
                //transaction.setStatus("0");
                serverConn.invokeSuccess(System.currentTimeMillis() - begin);
                Entry var27 = new Entry(o, (Object)null);
                return var27;
            } catch (Exception var23) {
                if (transport != null) {
                    //Cat.logEvent("OctoCall.requestSize", SizeUtil.getLogSize(transport.getRequestSize()));
                    //Cat.logEvent("OctoCall.responseSize", SizeUtil.getLogSize(transport.getResponseSize()));
                    //Tracer.getClientSpan().setPackageSize(transport.getResponseSize());
                }

                //span = Tracer.getClientTracer().getSpan();
                cause = var23.getCause() == null ? var23 : var23.getCause();
                returnNull = false;
                userDefinedException = false;
                timeoutException = false;
                if (socket != null) {
                    if (cause != null && cause instanceof TApplicationException) {
                        int type = ((TApplicationException)cause).getType();
                        if (type == 5 || type == 6 || type == 7 || type == 1 || type == 10001) {
                            this.returnConnection(serverConn, socket);
                            socket = null;
                        }
                    }

                    if (cause instanceof TBase) {
                        userDefinedException = true;
                        this.returnConnection(serverConn, socket);
                        socket = null;
                    }

                    if (socket != null) {
                        this.returnBrokenConnection(serverConn, socket);
                        socket = null;
                    }
                }

                if (cause != null && cause instanceof TApplicationException && ((TApplicationException)cause).getType() == 5) {
                    returnNull = true;
                } else if (cause != null && cause instanceof TTransportException && ((Throwable)cause).getCause() != null && ((Throwable)cause).getCause() instanceof SocketTimeoutException || cause != null && cause instanceof SocketTimeoutException) {
                    serverConn.invokeTimeout(System.currentTimeMillis() - begin);
                    /*if (this.clientProxy.isDisableTimeoutStackTrace()) {
                        toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke(" + methodName + ") timeout, traceId:" + ClientInfoUtil.getClientTracerTraceId(), (Throwable)null);
                        ((Throwable)toThrow).setStackTrace(new StackTraceElement[0]);
                    } else {
                        toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke(" + methodName + ") timeout, traceId:" + ClientInfoUtil.getClientTracerTraceId(), (Throwable)cause);
                    }*/

                    timeoutException = true;
                } else if (cause == null || !(cause instanceof TBase) && !(cause instanceof TProtocolException)) {
                    if (cause != null && cause instanceof TApplicationException) {
                        toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke(" + methodName + "), traceId:" /*+ ClientInfoUtil.getClientTracerTraceId()*/ + " Exception:" + ((Throwable)cause).getMessage(), (Throwable)cause);
                    } else {
                        toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke(" + methodName + ") Exception", (Throwable)cause);
                    }
                } else {
                    toThrow = (Throwable)cause;
                }
            }


        } finally {
            if (socket != null) {
                this.returnConnection(serverConn, socket);
            }

            /*this.clientProxy.noticeInvoke(methodName, serverIpPort, System.currentTimeMillis() - begin);
            if (transaction != null) {
                transaction.complete();
            }*/

        }

        //Tracer.clientRecv();
        return new Entry(o, toThrow);
    }

    void returnConnection(ServerConn serverConn, TTransport socket) {
        try {
            if (socket != null && serverConn != null && serverConn.getObjectPool() != null) {
                serverConn.getObjectPool().returnObject(socket);
            }
        } catch (Exception var4) {
            throw new RuntimeException("error returnBrokenConnection()", var4);
        }
    }
}
