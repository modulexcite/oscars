package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.config.SpringContext;
import org.springframework.context.ApplicationContext;

public class EnvironmentSteps {
    @Given("^I have set up Spring")
    public void I_have_set_up_Spring() throws Throwable {
        System.out.print("Initializing Spring... ");
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");
    }
    @When("^I wait (\\d+) milliseconds$")
    public void I_wait_milliseconds(int arg1) throws Throwable {
        System.out.println("sleeping for .."+arg1+" ms");
        Thread.sleep(arg1);
    }


}