package com.project.bookingya.bdd.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.models.Guest;
import com.project.bookingya.shared.Constants;
import com.project.bookingya.services.GuestService;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@SpringBootTest
public class HuespedSteps {

    @Autowired
    private GuestService guestService;

    private Guest huespedRegistrado;
    private GuestDto huesped;
    private Exception excepcion;
    private Set<ConstraintViolation<GuestDto>> errores;

    @Given("que existe un huesped registrado con los siguientes datos:")
    public void queExisteUnHuespedRegistradoConLosSiguientesDatos(DataTable dataTable) {
        GuestDto huespedRegistrado = crearHuespedDesdeTabla(dataTable);
        imprimirRespuesta("Datos del huesped base para duplicado", resumenHuespedDto(huespedRegistrado));

        try {
            this.huespedRegistrado = guestService.getByIdentification(huespedRegistrado.getIdentification());
            imprimirRespuesta("Huesped ya registrado", resumenHuesped(this.huespedRegistrado));
        } catch (Exception e) {
            this.huespedRegistrado = guestService.create(huespedRegistrado);
            imprimirRespuesta("Huesped creado para prueba de duplicado", resumenHuesped(this.huespedRegistrado));
        }
    }

    @When("intento registrar otro huesped con la misma identificacion y los siguientes datos:")
    public void intentoRegistrarOtroHuespedConLaMismaIdentificacionYLosSiguientesDatos(DataTable dataTable) {
        GuestDto huespedDuplicado = crearHuespedDesdeTabla(dataTable);
        imprimirRespuesta("Comparacion de identificaciones",
                "base: " + huespedRegistrado.getIdentification()
                        + ", duplicado: " + huespedDuplicado.getIdentification());
        imprimirRespuesta("Datos del huesped duplicado", resumenHuespedDto(huespedDuplicado));

        try {
            Guest guest = guestService.create(huespedDuplicado);
            imprimirRespuesta("Huesped creado inesperadamente", resumenHuesped(guest));
        } catch (Exception e) {
            excepcion = e;
            imprimirRespuesta("Excepcion al crear huesped duplicado", e.getMessage());
        }
    }

    @Given("que tengo un huesped con los siguientes datos:")
    public void queTengoUnHuespedConLosSiguientesDatos(DataTable dataTable) {
        huesped = crearHuespedDesdeTabla(dataTable);
        imprimirRespuesta("Datos de huesped recibidos",
                "identification: " + huesped.getIdentification()
                        + ", name: " + huesped.getName()
                        + ", email: " + huesped.getEmail());
    }

    @When("valido los datos del huesped")
    public void validoLosDatosDelHuesped() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        errores = validator.validate(huesped);
    }

    @Then("el sistema debe rechazar el huesped por identificacion duplicada")
    public void elSistemaDebeRechazarElHuespedPorIdentificacionDuplicada() {
        assertNotNull(excepcion);
        imprimirRespuesta("Excepcion validada", excepcion.getMessage());
        assertEquals(Constants.GUEST_EXISTS, excepcion.getMessage());
    }

    @Then("el sistema debe rechazar el huesped por nombre vacio")
    public void elSistemaDebeRechazarElHuespedPorNombreVacio() {
        validarErrorEnCampo("name");
    }

    @Then("el sistema debe rechazar el huesped por correo invalido")
    public void elSistemaDebeRechazarElHuespedPorCorreoInvalido() {
        validarErrorEnCampo("email");
    }

    private GuestDto crearHuespedDesdeTabla(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);

        GuestDto guest = new GuestDto();
        guest.setIdentification(row.get("identification"));
        guest.setName(row.get("name"));
        guest.setEmail(row.get("email"));
        return guest;
    }

    private void validarErrorEnCampo(String campo) {
        String mensaje = obtenerMensajeDeError(campo);

        imprimirRespuesta("Validar error en campo " + campo, campo + " -> " + mensaje);
        assertNotNull(mensaje);
    }

    private String obtenerMensajeDeError(String campo) {
        return errores.stream()
                .filter(error -> error.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(null);
    }

    private String resumenHuesped(Guest guest) {
        return "id: " + guest.getId()
                + ", identification: " + guest.getIdentification()
                + ", name: " + guest.getName()
                + ", email: " + guest.getEmail();
    }

    private String resumenHuespedDto(GuestDto guest) {
        return "identification: " + guest.getIdentification()
                + ", name: " + guest.getName()
                + ", email: " + guest.getEmail();
    }

    private void imprimirRespuesta(String accion, String respuesta) {
        System.out.println("[BDD] " + accion + " -> " + respuesta);
    }
}
