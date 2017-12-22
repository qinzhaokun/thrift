import com.qinzhaokun.thrift.IUserService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceInvokeTest {



    @Test
    public void invokeServiceTest(){

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:appContext-client.xml");

        IUserService.Iface iface = (IUserService.Iface)context.getBean("useService");

        try {
            System.out.println(iface.findById(1).getEmail());
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
/*

        TTransport transport;
        try {
            transport = new TSocket("localhost", 4000);
            TProtocol protocol = new TBinaryProtocol(transport);
            //IUserService.Iface iface;
            IUserService.Client client = new IUserService.Client(protocol);
            transport.open();
            System.out.println(client.findById(1).getEmail());
            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        */
    }
}
