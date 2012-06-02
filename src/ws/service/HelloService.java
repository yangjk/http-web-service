package ws.service;


import ws.annotation.HttpWebService;
import ws.annotation.Path;
import ws.service.impl.Hello;

@HttpWebService
public interface HelloService {


    @Path(value = "/heloWorld", paramNames = "name")
    Hello helloWorld2(String name);

    @Path(value = "/test")
    void test();

    @Path(value = "/exception")
    void exception();

}
