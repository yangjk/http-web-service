package ws.service.impl;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Hello {

    @JsonCreator
    public Hello(@JsonProperty(value = "name") String name) {
        this.name = name;
    }

    private String name ;

    private String hello = "hello";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
