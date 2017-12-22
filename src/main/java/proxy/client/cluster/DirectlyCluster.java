package proxy.client.cluster;

import proxy.client.Server;
import proxy.client.ServerConn;
import proxy.client.pool.ThriftPoolConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DirectlyCluster extends BaseCluster {
    private List<ServerConn> serverConns = new ArrayList();

    public DirectlyCluster(Set<Server> servers, ThriftPoolConfig poolConfig, int timeOut, boolean async, String serviceName, int connTimeout) {
        super(false,async);
        this.serviceName = serviceName;
        Iterator i$ = servers.iterator();

        while(i$.hasNext()) {
            Server server = (Server)i$.next();
            ServerConn serverConn = new ServerConn();
            serverConn.setServer(server);
            serverConn.setObjectPool(this.createPool(server.getIp(), server.getPort(), timeOut, poolConfig, connTimeout));
            this.serverConns.add(serverConn);
        }

    }

    public List<ServerConn> getServerConnList() {
        return this.serverConns;
    }


    public void destroy() {
        Iterator i$ = this.serverConns.iterator();

        while(i$.hasNext()) {
            ServerConn serverConn = (ServerConn)i$.next();
            this.destoryPool(serverConn.getObjectPool());
        }

        this.serverConns.clear();
    }

    public void updateServerConn(ServerConn serverConn) {
        List<ServerConn> _serverConns = new ArrayList();
        Iterator i$ = this.serverConns.iterator();

        while(true) {
            while(i$.hasNext()) {
                ServerConn conn = (ServerConn)i$.next();
                if (conn.getServer().getIp().equalsIgnoreCase(serverConn.getServer().getIp()) && conn.getServer().getPort() == serverConn.getServer().getPort()) {
                    _serverConns.add(serverConn);
                } else {
                    _serverConns.add(conn);
                }
            }

            this.serverConns = _serverConns;
            return;
        }
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
