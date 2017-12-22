package proxy.client.loadbalancer;

import org.aopalliance.intercept.MethodInvocation;
import proxy.client.ServerConn;

import java.util.List;

public interface ILoadBalancer {

    ServerConn select(List<ServerConn> var1, MethodInvocation var2);

}

