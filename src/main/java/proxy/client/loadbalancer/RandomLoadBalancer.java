package proxy.client.loadbalancer;

import org.aopalliance.intercept.MethodInvocation;
import proxy.client.ServerConn;

import java.util.List;
import java.util.Random;


public class RandomLoadBalancer extends AbstractLoadBalancer {
    private final Random random = new Random();

    public RandomLoadBalancer(int slowStartSeconds) {
    }

    public ServerConn doSelect(List<ServerConn> serverList, MethodInvocation methodInvocation) {
        if (serverList.size() == 0) {
            return null;
        } else {
            int length = serverList.size();
            double[] weightAccumulate = new double[length];
            double totalWeight = 0.0D;
            boolean sameWeight = true;
            double lastWeight = -1.0D;

            for(int i = 0; i < length; ++i) {
                double weight = this.getWeight((ServerConn)serverList.get(i));
                totalWeight += weight;
                weightAccumulate[i] = totalWeight;
                if (sameWeight && i > 0 && Double.compare(weight, lastWeight) != 0) {
                    sameWeight = false;
                }

                lastWeight = weight;
            }

            if (!sameWeight && Double.compare(totalWeight, 0.0D) > 0) {
                double offset = this.random.nextDouble() * totalWeight;

                for(int i = 0; i < length; ++i) {
                    double weightA = weightAccumulate[i];
                    if (Double.compare(offset, weightA) < 0) {
                        return (ServerConn)serverList.get(i);
                    }
                }
            }

            return (ServerConn)serverList.get(this.random.nextInt(length));
        }
    }
}
