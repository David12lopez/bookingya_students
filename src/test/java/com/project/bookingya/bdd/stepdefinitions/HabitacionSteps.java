package com.project.bookingya.bdd.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.project.bookingya.dtos.RoomDto;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class HabitacionSteps {

    private RoomDto habitacion;
    private Set<ConstraintViolation<RoomDto>> errores;

    @Given("que tengo una habitacion con los siguientes datos:")
    public void queTengoUnaHabitacionConLosSiguientesDatos(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        String unique = String.valueOf(System.currentTimeMillis());

        habitacion = new RoomDto();
        habitacion.setCode(row.get("code") + unique);
        habitacion.setName(row.get("name") + unique);
        habitacion.setCity(row.get("city"));
        habitacion.setMaxGuests(Integer.parseInt(row.get("maxGuests")));
        habitacion.setNightlyPrice(new BigDecimal(row.get("nightlyPrice")));
        habitacion.setAvailable(Boolean.parseBoolean(row.get("available")));

        imprimirRespuesta("Datos de habitacion recibidos",
                "code: " + habitacion.getCode()
                        + ", name: " + habitacion.getName()
                        + ", maxGuests: " + habitacion.getMaxGuests()
                        + ", nightlyPrice: " + habitacion.getNightlyPrice()
                        + ", available: " + habitacion.getAvailable());
    }

    @When("valido los datos de la habitacion")
    public void validoLosDatosDeLaHabitacion() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        errores = validator.validate(habitacion);
    }

    @Then("el sistema debe rechazar la habitacion por precio invalido")
    public void elSistemaDebeRechazarLaHabitacionPorPrecioInvalido() {
        String mensaje = obtenerMensajeDeError("nightlyPrice");

        imprimirRespuesta("Validar precio invalido", "nightlyPrice -> " + mensaje);
        assertNotNull(mensaje);
    }

    @Then("el sistema debe rechazar la habitacion por capacidad maxima invalida")
    public void elSistemaDebeRechazarLaHabitacionPorCapacidadMaximaInvalida() {
        String mensaje = obtenerMensajeDeError("maxGuests");

        imprimirRespuesta("Validar capacidad maxima invalida", "maxGuests -> " + mensaje);
        assertNotNull(mensaje);
    }

    private String obtenerMensajeDeError(String campo) {
        return errores.stream()
                .filter(error -> error.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(null);
    }

    private void imprimirRespuesta(String accion, String respuesta) {
        System.out.println("[BDD] " + accion + " -> " + respuesta);
    }
}
