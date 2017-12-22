import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceStartTest{


    @Test
    public void startServiceTest(){
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:appContext.xml");
        while(true);
    }

}
