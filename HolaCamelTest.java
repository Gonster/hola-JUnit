package cn.future.csoc.dataexchange;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.apache.camel.test.junit4.CamelTestSupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;


/**
 * <p>created on 2015/6/12</p>
 *
 * @author Gonster
 */
public class sendRequestToOtherSystemRouteTest extends CamelTestSupport {


    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @EndpointInject(uri = "mock:mid")
    protected MockEndpoint midEndpoint;

    @EndpointInject(uri = "mock:error")
    protected MockEndpoint errorEndpoint;

    @EndpointInject(uri = "mock:exceptionHandler")
    protected MockEndpoint exceptionHandlerEndpoint;

    @Produce(uri = "direct:start")
    private ProducerTemplate template;

    @Produce(uri = "direct:start2")
    private ProducerTemplate template2;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:start").setHeader("service_id", constant(1))
                        .errorHandler(deadLetterChannel("mock:error").maximumRedeliveries(0))
                        .onException(ConnectException.class)
//                        .maximumRedeliveries(0).redeliveryDelay(300)
                        .handled(true).to(ExchangePattern.InOnly, "mock:exceptionHandler").end()
//                        .transacted()
//                        .throwException(new ConnectException("123"))
                        .to(ExchangePattern.InOnly, "mock:mid")
                        .loadBalance().failover(Exception.class)
                        .to(ExchangePattern.InOnly, "mock:result");

                from("direct:start2")
//                        .id(id)
                        .errorHandler(deadLetterChannel("mock:error").maximumRedeliveries(0))
                        .onException(ConnectException.class)
                        .maximumRedeliveries(-1).redeliveryDelay(300)
                        .handled(true)
//                        .to("mock:exceptionHandler")
                        .end()
//                        .transacted()
                        .process(new Processor() {
                            private final int max_mock_exception_count = (int) Math.ceil((Math.random() + 1.0) * 10.0);
                            private int max_mock_exception_count_current = 0;

                            @Override
                            public void process(Exchange exchange) throws Exception {
                                System.out.println("enter processor"
                                        + "(" + max_mock_exception_count + "/" + max_mock_exception_count_current + ")"
                                        + ": " + exchange.getIn().getMandatoryBody());
                                if(max_mock_exception_count_current++ < max_mock_exception_count)
                                    throw new ConnectException("123");
                            }
                        })
                        .loadBalance().failover(Exception.class)
                        .to(ExchangePattern.InOnly, "mock:result");
            }
        };
    }

    @Test
    public void testRoute() throws Exception {
        // set mock expectations
        getMockEndpoint("mock:error").expectedMessageCount(0);
        getMockEndpoint("mock:mid").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:exceptionHandler").expectedMessageCount(0);

        // send a message
        template.sendBody("direct:start", "Hello World");

        // assert mocks
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testRouteOrigin() throws Exception {
        // set mock expectations
        getMockEndpoint("mock:error").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(1);
//        getMockEndpoint("mock:exceptionHandler").expectedMessageCount(1);


        // send a message
        template2.sendBody("direct:start2", "Hello World");
//        template.sendBody("direct:start", "Hello World2");

        // assert mocks
        assertMockEndpointsSatisfied();
    }
}