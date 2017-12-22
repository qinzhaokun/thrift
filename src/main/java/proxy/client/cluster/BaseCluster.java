package proxy.client.cluster;


import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.client.ServerConn;
import proxy.client.pool.ThriftPoolableObjectFactory;

public abstract class BaseCluster implements ICluster {
    private static final Logger logger = LoggerFactory.getLogger(BaseCluster.class);
    private boolean isImplFacebookService = true;
    private boolean async = false;
    protected String serverAppKey = "";
    protected String serviceName = "";
    protected int connTimeout = 500;

    public BaseCluster(boolean isImplFacebookService, boolean async) {
        this.isImplFacebookService = isImplFacebookService;
        this.async = async;
    }

    public boolean isAsync() {
        return this.async;
    }

    protected ObjectPool createPool(String host, int port, int timeOut, Config poolConfig, int connTimeout) {
        /*if (host.equals("127.0.0.1") || host.equals("localhost")) {
            host = getIpV4();
        }*/

        GenericObjectPool genericObjectPool = new GenericObjectPool(new ThriftPoolableObjectFactory(this.serverAppKey, host, port, timeOut, this.isImplFacebookService, this.async, connTimeout), poolConfig);
        if (0 == poolConfig.minIdle) {
            genericObjectPool.setMinEvictableIdleTimeMillis(poolConfig.timeBetweenEvictionRunsMillis);
        }

        return genericObjectPool;
    }

    protected void destoryPool(ObjectPool pool) {
        if (pool != null) {
            try {
                pool.close();
            } catch (Exception var3) {
                logger.error(var3.getMessage(), var3);
            }
        }

    }

    public abstract List<ServerConn> getServerConnList();

    public void destroy() {
        List<ServerConn> serverConnList = this.getServerConnList();
        if (serverConnList != null) {
            Iterator i$ = serverConnList.iterator();

            while (i$.hasNext()) {
                ServerConn serverConn = (ServerConn) i$.next();

                try {
                    serverConn.getObjectPool().close();
                } catch (Exception var5) {
                    logger.error(var5.getMessage(), var5);
                }
            }
        }

    }

    public abstract void updateServerConn(ServerConn var1);


    private static String getIpV4() {
        String ip = "";

        Enumeration networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException var9) {
            logger.error("fail to get network interface information.", var9);
            return ip;
        }

        HashSet ips = new HashSet();

        String str;
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) networkInterface.nextElement();
            str = null != ni ? ni.getName() : "";
            if (!isIgnoreNI(str)) {
                Enumeration inetAddress = null;

                try {
                    if (null != ni) {
                        inetAddress = ni.getInetAddresses();
                    }
                } catch (Exception var8) {
                    logger.debug("fail to get ip information.", var8);
                }

                while (null != inetAddress && inetAddress.hasMoreElements()) {
                    InetAddress ia = (InetAddress) inetAddress.nextElement();
                    if (!(ia instanceof Inet6Address)) {
                        String thisIp = ia.getHostAddress();
                        if (!ia.isLoopbackAddress() && !thisIp.contains(":") && !"127.0.0.1".equals(thisIp)) {
                            ips.add(thisIp);
                            if (StringUtils.isEmpty(ip)) {
                                ip = thisIp;
                            }
                        }
                    }
                }
            }
        }

        if (ips.size() >= 2) {
            Iterator i$ = ips.iterator();

            while (i$.hasNext()) {
                str = (String) i$.next();
                if (str.startsWith("10.")) {
                    ip = str;
                    break;
                }
            }
        }

        if (StringUtils.isBlank(ip)) {
            logger.error("cannot get local ip.");
            ip = "";
        }

        return ip;
    }

    private static boolean isIgnoreNI(String niName) {
        List<String> ignoreNiNames = new ArrayList();
        ignoreNiNames.add("vnic");
        ignoreNiNames.add("docker");
        ignoreNiNames.add("vmnet");
        ignoreNiNames.add("vmbox");
        ignoreNiNames.add("vbox");
        Iterator i$ = ignoreNiNames.iterator();

        String item;
        do {
            if (!i$.hasNext()) {
                return false;
            }

            item = (String) i$.next();
        } while (containsIgnoreCase(niName, item));

        return true;
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        return null != str && null != searchStr && str.toLowerCase().contains(searchStr);
    }
}
