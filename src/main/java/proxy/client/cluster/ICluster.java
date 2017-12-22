package proxy.client.cluster;

import proxy.client.ServerConn;

import java.util.List;

public interface ICluster {
    List<ServerConn> getServerConnList();

    void destroy();

    boolean isAsync();

    void updateServerConn(ServerConn var1);
}
