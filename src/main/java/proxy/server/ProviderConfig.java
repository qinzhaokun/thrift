package proxy.server;

import org.apache.thrift.TBaseProcessor;

public class ProviderConfig<T> {

    private Class<?> serviceInterface;

    private String url;

    private String version;

    private TBaseProcessor processor;

    private ServerConfig serverConfig;

    private boolean isPublished;

    public boolean isPublished() {
        return isPublished;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public TBaseProcessor getProcessor() {
        return processor;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void setProcessor(TBaseProcessor processor) {
        this.processor = processor;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}


