package proxy.client.loadbalancer;


import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import proxy.client.ServerConn;

public abstract class AbstractLoadBalancer implements ILoadBalancer {
    public AbstractLoadBalancer() {
    }

    public ServerConn select(List<ServerConn> serverList, MethodInvocation methodInvocation) {
        if (serverList != null && serverList.size() != 0) {
            return serverList.size() == 1 ? (ServerConn)serverList.get(0) : this.doSelect(serverList, methodInvocation);
        } else {
            return null;
        }
    }

    public abstract ServerConn doSelect(List<ServerConn> var1, MethodInvocation var2);

    public double getWeight(ServerConn serverConn) {
        return serverConn.getServer().getWeight();
    }
}
