package proxy.server;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import proxy.server.ProviderConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceFactory {

    private static Map<String, ProviderConfig> stringProviderConfigMap = new HashMap<String, ProviderConfig>();


    public static void addService(ProviderConfig<?> providerConfig){
        stringProviderConfigMap.put(providerConfig.getUrl(),providerConfig);
    }

    public static void publishOne(final ProviderConfig providerConfig){
        new Thread(new Runnable() {
            public void run() {
                publishService(providerConfig);
            }
        }).start();

        //publishService(providerConfig);
    }

    public static void publishService(ProviderConfig providerConfig) {
        try {
            /*TNonblockingServerSocket tServerSocket = new TNonblockingServerSocket(providerConfig.getServerConfig().getPort());
            //Class c = providerConfig.getServiceInterface();
            //TProcessor tProcessor = new TBaseProcessor<c>(providerConfig.getService());
            TNonblockingServer.Args tArgs = new TNonblockingServer.Args(tServerSocket);

            tArgs.processor(providerConfig.getProcessor());
            // 启动Thrift服务
            TNonblockingServer server = new TNonblockingServer(tArgs);

            server.serve();
            */

            // 简单的单线程服务模型，一般用于测试
            TServerSocket serverTransport = new TServerSocket(providerConfig.getServerConfig().getPort());
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.processor(providerConfig.getProcessor());
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            // tArgs.protocolFactory(new TCompactProtocol.Factory());
            // tArgs.protocolFactory(new TJSONProtocol.Factory());
            TServer server = new TSimpleServer(tArgs);
            server.serve();
        } catch (Exception e){
            System.out.println(providerConfig.getUrl() + " fail. " + e.getMessage());
        }
    }

    public static void publishAll(){
        Iterator<Map.Entry<String, ProviderConfig>> entries = stringProviderConfigMap.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, ProviderConfig> entry = entries.next();
            publishOne(entry.getValue());
        }
    }
}
