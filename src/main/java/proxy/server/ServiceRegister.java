package proxy.server;

import org.apache.thrift.TBaseProcessor;

import java.util.Iterator;
import java.util.Map;

public class ServiceRegister extends ServiceStartListener {
    
    private Map<String, Object> services;
    
    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, Object> getServices() {
        return services;
    }

    public void setServices(Map<String, Object> services) {
        this.services = services;
    }
    
    public void init(){
        Iterator<Map.Entry<String, Object>> entries = services.entrySet().iterator();

        while (entries.hasNext()) {

            Map.Entry<String, Object> entry = entries.next();

            ProviderConfig providerConfig = new ProviderConfig();
            providerConfig.setUrl(entry.getKey());
            providerConfig.setProcessor((TBaseProcessor) entry.getValue());
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setPort(port);
            providerConfig.setServerConfig(serverConfig);
            ServiceFactory.addService(providerConfig);

        }


    }
}
