package ws.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ws.service.HelloService;

public class HelloServiceImpl implements HelloService {

    private static Log log = LogFactory.getLog(HelloServiceImpl.class);

    public HelloServiceImpl() {
        log.debug("Hello service init!!!");
    }


    @Override
    public Hello helloWorld2(String name) {
        return new Hello(name);
    }

    @Override
    public void test() {
       log.debug("test");
    }

    @Override
    public void exception() {
        if(1==1) {
            throw new RuntimeException("error");
        }
    }

}
