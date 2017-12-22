package proxy.client;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import proxy.client.cluster.DirectlyCluster;
import proxy.client.cluster.ICluster;
import proxy.client.loadbalancer.ILoadBalancer;
import proxy.client.pool.ThriftPoolConfig;
import proxy.client.timeoutpolicy.ITimeoutPolicy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ThriftClientProxy implements FactoryBean<Object> , InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ThriftClientProxy.class);

    private Class<?> serviceInterface;

    private String remoteAppKey;

    private Integer remotePort;

    private String serverIpPorts;

    private String appKey;

    private boolean async = false;

    private Object serviceProxy;

    private boolean annotatedThrift;

    private int timeout;

    private Constructor synConstructor;

    private Constructor asynConstructor;

    private TProtocolFactory asyncProtocol;

    private boolean retryRequest;

    private int retryTimes;

    private ILoadBalancer userDefinedBalancer;

    private ITimeoutPolicy timeoutPolicy;

    private ICluster cluster;

    private ThriftPoolConfig thriftPoolConfig;

    private String serviceName;

    private Integer connTimeout;

    private static int cores = Runtime.getRuntime().availableProcessors();


    public Class<?> getServiceInterface() {
        return serviceInterface;
    }


    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean getAsync(){
        return this.async;
    }

    public int getTimeout()
    {
        return timeout > 0 ? timeout : 5000;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceProxy() {
        return serviceProxy;
    }

    public void setServiceProxy(Object serviceProxy) {
        this.serviceProxy = serviceProxy;
    }


    public void setAnnotatedThrift(boolean annotatedThrift) {
        this.annotatedThrift = annotatedThrift;
    }

    public boolean getAnnotatedThrift(){
        return this.annotatedThrift;
    }

    public void setRetryRequest(boolean retryRequest) {
        this.retryRequest = retryRequest;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public ILoadBalancer getUserDefinedBalancer() {
        return userDefinedBalancer;
    }

    public void setUserDefinedBalancer(ILoadBalancer userDefinedBalancer) {
        this.userDefinedBalancer = userDefinedBalancer;
    }

    public ITimeoutPolicy getTimeoutPolicy() {
        return timeoutPolicy;
    }

    public void setTimeoutPolicy(ITimeoutPolicy timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
    }

    public void setThriftPoolConfig(ThriftPoolConfig thriftPoolConfig) {
        this.thriftPoolConfig = thriftPoolConfig;
    }

    public Integer getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(Integer connTimeout) {
        this.connTimeout = connTimeout;
    }

    public String getServiceName() {
        if (StringUtils.isBlank(this.serviceName)) {
            this.serviceName = this.serviceInterface.getName();
        }

        return this.serviceName;
    }

    public String getServerIpPorts() {
        return serverIpPorts;
    }

    public void setServerIpPorts(String serverIpPorts) {
        this.serverIpPorts = serverIpPorts;
    }

    public ThriftClientProxy() {
        //this.asyncSelectorThreadCount = cores * 2;
        this.connTimeout = 500;
        //this.localServerPort = -1;
        //this.isImplFacebookService = false;
        //this.isHabse = false;
        //this.maxResponseMessageBytes = 16384000;
        //his.serverDynamicWeight = false;
        //this.enableRemoteDCServer = true;
        //this.clusterManager = "OCTO";
        this.retryRequest = true;
        this.retryTimes = 3;
        //this.slowStartSeconds = 180;
        //this.bUpdateLocalConfig = false;
        this.userDefinedBalancer = null;
        this.timeoutPolicy = null;
        //this.responseCollector = null;
        this.annotatedThrift = false;
        //this.gzip = false;
        //this.snappy = false;
        //this.chenkSum = false;
        //this.protocol = Consts.protocol;
        //this.disableTimeoutStackTrace = false;
    }

    public ThriftPoolConfig getThriftPoolConfig() {
        if (this.thriftPoolConfig == null) {
            this.thriftPoolConfig = new ThriftPoolConfig();
            this.thriftPoolConfig.setMaxActive(cores * 50);
            this.thriftPoolConfig.setMaxIdle(cores * 5);
            this.thriftPoolConfig.setMinIdle(5);
            this.thriftPoolConfig.setMaxWait(500L);
            this.thriftPoolConfig.setTestOnBorrow(false);
        }

        return this.thriftPoolConfig;
    }


    private Class<?> getIfaceInterface() {
        return this.async ? this.getAsyncIfaceInterface() : this.getSynIfaceInterface();
    }

    public Class<?> getObjectType() {
        return this.serviceInterface == null ? null : this.getIfaceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

    private Class<?> getAsyncIfaceInterface() {
        Class<?>[] classes = this.serviceInterface.getClasses();
        Class[] arr$ = classes;
        int len$ = classes.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Class c = arr$[i$];
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("AsyncIface")) {
                return c;
            }
        }

        throw new IllegalArgumentException("serviceInterface must contain Sub Interface of AsyncIface");
    }


    private Class<?> getSynIfaceInterface() {
        if (this.annotatedThrift) {
            return this.serviceInterface;
        } else {
            Class<?>[] classes = this.serviceInterface.getClasses();
            Class[] arr$ = classes;
            int len$ = classes.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                Class c = arr$[i$];
                if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("Iface")) {
                    return c;
                }
            }

            throw new IllegalArgumentException("serviceInterface must contain Sub Interface of Iface");
        }
    }

    public Object getObject() throws Exception {
        return this.serviceProxy;
    }

    public void afterPropertiesSet() throws Exception {
        this.serverIpPorts = this.getRemoteAppKey() + ":" + this.getRemotePort();
        if (null == this.appKey || StringUtils.isBlank(this.appKey)) {
            logger.warn("appKey is empty, may cause problem, please use the right appKey !!!");
            this.appKey = "";
        }

        this.asyncProtocol = new TProtocolFactory() {
            public TProtocol getProtocol(TTransport tTransport) {
                TBinaryProtocol proto = new TBinaryProtocol(tTransport);
                return proto;
            }
        };

        HashSet servers = new HashSet();
        Class _interface;
        ProxyFactory pf;

        String[] ipPortArr = this.serverIpPorts.trim().split("[^0-9a-zA-Z_\\-\\.:]+");
        servers = new HashSet();
        String[] arr$ = ipPortArr;
        int len$ = ipPortArr.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String ipPort = arr$[i$];
            String[] items = ipPort.split(":");
            if (items.length == 2) {
                servers.add(new Server(items[0], Integer.parseInt(items[1])));
            } else if (items.length == 3) {
                servers.add(new Server(items[0], Integer.parseInt(items[1]), "", Integer.parseInt(items[2])));
            } else {
                logger.error("ignore thrift server " + ipPort);
            }
        }

        this.cluster = new DirectlyCluster(servers, this.getThriftPoolConfig(), this.getTimeout(), this.async, this.getServiceName(), this.connTimeout);
        List<ServerConn> serverList = new ArrayList<ServerConn>();
        serverList.add(new ServerConn(new Server(this.remoteAppKey, this.remotePort)));

        ThriftMethodInterceptor clientInterceptor = new ThriftMethodInterceptor(this, this.cluster, this.userDefinedBalancer,  this.timeoutPolicy);

        clientInterceptor.setRetryTimes(this.retryTimes);
        clientInterceptor.setRetryRequest(this.retryRequest);
        //clientInterceptor.setClusterManager(this.clusterManager);
        if (this.annotatedThrift) {
            _interface = this.serviceInterface;
           // ThriftClientMetadata clientMetadata = (ThriftClientMetadata)this.clientMetadataCache.getUnchecked(new TypeAndName(this.serviceInterface, this.serviceInterface.getName()));
            //clientInterceptor.setClientMetadata(clientMetadata);
        } else {
            _interface = this.getIfaceInterface();
        }

        pf = new ProxyFactory(_interface, clientInterceptor);
        this.serviceProxy = pf.getProxy();
    }

    public TProtocol getProtocol(TTransport socket) {
        TFramedTransport transport = (TFramedTransport)socket;


        TBinaryProtocol protocol = new TBinaryProtocol(transport);

        return protocol;
    }

    public Object getClientInstance(TTransport socket) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        this.getClientConstructorWithTProtocol();
        if (this.async) {
            Object o = this.asynConstructor.newInstance(this.asyncProtocol/*, asyncClientManagerList.get(socket.hashCode() % this.asyncSelectorThreadCount), socket*/);
            ((TAsyncClient) o).setTimeout((long) this.timeout);
            return o;
        }
        //} else if (this.isHabse) {
        else{
            TProtocol protocol = new TBinaryProtocol(socket);
            Object o = this.synConstructor.newInstance(protocol);
            return o;
        }
        /*} else {
            CustomizedTFramedTransport transport = (CustomizedTFramedTransport)socket;
            transport.setUnifiedProto(mtThrfitInvokeInfo.isUniProto());
            transport.setServiceName(this.getServiceName());
            transport.setProtocol(this.protocol);
            if (mtThrfitInvokeInfo.isUniProto()) {
                TraceInfo traceInfo = TraceInfoUtil.clientSend(mtThrfitInvokeInfo.getSpanName(), this.localEndpoint, mtThrfitInvokeInfo.getServerAppKey(), mtThrfitInvokeInfo.getServerIp(), mtThrfitInvokeInfo.getServerPort());
                transport.setTraceInfo(traceInfo);
            }

            MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
            protocol.setLocalEndpoint(this.localEndpoint);
            protocol.setClusterManager(this.clusterManager);
            return this.synConstructor.newInstance(protocol);
        }*/
    }

    private Class<?> getSynClientClass() {
        Class<?>[] classes = this.serviceInterface.getClasses();
        Class[] arr$ = classes;
        int len$ = classes.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Class c = arr$[i$];
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Client")) {
                return c;
            }
        }

        throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client");
    }

    private Class<?> getAsyncClientClass() {
        Class<?>[] classes = this.serviceInterface.getClasses();
        Class[] arr$ = classes;
        int len$ = classes.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Class c = arr$[i$];
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("AsyncClient")) {
                return c;
            }
        }

        throw new IllegalArgumentException("serviceInterface must contain Sub Class of AsyncClient");
    }


    private Constructor<?> getClientConstructorWithTProtocol() {
        if (this.async) {
            if (this.asynConstructor == null) {
                try {
                    this.asynConstructor = this.getAsyncClientClass().getConstructor(TProtocolFactory.class, TAsyncClientManager.class, TNonblockingTransport.class);
                } catch (Exception var2) {
                    throw new IllegalArgumentException("serviceInterface must contain Sub Class of AsyncClient with Constructor(TProtocol.class)");
                }
            }

            return this.asynConstructor;
        } else {
            if (this.synConstructor == null) {
                try {
                    this.synConstructor = this.getSynClientClass().getConstructor(TProtocol.class);
                } catch (Exception var3) {
                    throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client with Constructor(TProtocol.class)");
                }
            }

            return this.synConstructor;
        }
    }



}
