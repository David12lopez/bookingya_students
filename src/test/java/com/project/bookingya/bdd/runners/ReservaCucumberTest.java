package com.project.bookingya.bdd.runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/Reserva.feature",
        glue = {"com.project.bookingya.bdd.stepdefinitions"},
        plugin = {"pretty"}
)
public class ReservaCucumberTest {
}
