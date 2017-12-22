package proxy.client.timeoutpolicy;

import org.aopalliance.intercept.MethodInvocation;

public interface ITimeoutPolicy {
    int getTimeoutByConfig(MethodInvocation var1, int var2);
}
