package proxy.server;

import org.springframework.context.ApplicationEvent;

public class ServiceStartEvent extends ApplicationEvent{

    public ServiceStartEvent(final Object o){
        super(o);
    }
}
