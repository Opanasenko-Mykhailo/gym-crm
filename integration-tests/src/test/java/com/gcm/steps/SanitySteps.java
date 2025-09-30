package com.gcm.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class SanitySteps {

    private boolean value;

    @Given("a test scenario")
    public void a_test_scenario() {
    }

    @When("I check true")
    public void i_check_true() {
        value = true;
    }

    @Then("it should be true")
    public void it_should_be_true() {
        assertThat(value).isTrue();
    }
}