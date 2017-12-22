package proxy.server;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import proxy.server.ServiceFactory;

public class ServiceStartListener implements ApplicationListener<ContextRefreshedEvent>{


    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent){

        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            System.out.println("service init finish");

            ServiceFactory.publishAll();
        }
    }
}
