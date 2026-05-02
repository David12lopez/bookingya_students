package com.project.bookingya.bdd.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.models.Guest;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.models.Room;
import com.project.bookingya.services.GuestService;
import com.project.bookingya.services.ReservationService;
import com.project.bookingya.services.RoomService;
import com.project.bookingya.shared.Constants;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@SpringBootTest
public class ReservaSteps {

    @Autowired
    private RoomService roomService;

    @Autowired
    private GuestService guestService;

    @Autowired
    private ReservationService reservationService;

    private Room habitacion;
    private Guest huesped;
    private ReservationDto reserva;
    private Reservation reservaCreada;
    private Exception excepcion;

    @Given("que tengo una habitacion creada con los siguientes datos:")
    public void queTengoUnaHabitacionCreadaConLosSiguientesDatos(DataTable dataTable) {
        RoomDto habitacionDto = crearHabitacionDesdeTabla(dataTable);
        habitacion = roomService.create(habitacionDto);
        imprimirRespuesta("Habitacion creada", resumenHabitacion(habitacion));
    }

    @Given("un huesped creado con los siguientes datos:")
    public void unHuespedCreadoConLosSiguientesDatos(DataTable dataTable) {
        GuestDto huespedDto = crearHuespedDesdeTabla(dataTable);
        huesped = guestService.create(huespedDto);
        imprimirRespuesta("Huesped creado", resumenHuesped(huesped));
    }

    @Given("una reserva con los siguientes datos:")
    public void unaReservaConLosSiguientesDatos(DataTable dataTable) {
        reserva = crearReservaDesdeTabla(dataTable);
        imprimirRespuesta("Datos de reserva recibidos", resumenReservaDto(reserva));
    }

    @When("cuando la habitacion no esta disponible y quiero crear una reserva con los siguientes datos:")
    public void cuandoLaHabitacionNoEstaDisponibleYQuieroCrearUnaReservaConLosSiguientesDatos(DataTable dataTable) {
        reserva = crearReservaDesdeTabla(dataTable);
        intentarCrearReserva(reserva);
    }

    @When("quiero crear una reserva con la salida antes que la entrada")
    public void quieroCrearUnaReservaConLaSalidaAntesQueLaEntrada() {
        intentarCrearReserva(reserva);
    }

    @When("la capacidad de huespedes excede la maxima de la habitacion y quiero crear una reserva")
    public void laCapacidadDeHuespedesExcedeLaMaximaDeLaHabitacionYQuieroCrearUnaReserva() {
        intentarCrearReserva(reserva);
    }

    @When("quiero crear una reserva con cantidad de huespedes invalida")
    public void quieroCrearUnaReservaConCantidadDeHuespedesInvalida() {
        intentarCrearReserva(reserva);
    }

    @When("intento crear otra reserva en la misma habitacion y mismas fechas con los siguientes datos:")
    public void intentoCrearOtraReservaEnLaMismaHabitacionYMismasFechasConLosSiguientesDatos(DataTable dataTable) {
        reservaCreada = reservationService.create(reserva);
        imprimirRespuesta("Reserva base creada para validar fechas ocupadas", resumenReserva(reservaCreada));

        ReservationDto nuevaReserva = crearReservaDesdeTabla(dataTable);
        intentarCrearReserva(nuevaReserva);
    }

    @Then("la reserva no se puede crear porque la habitacion no esta disponible")
    public void laReservaNoSePuedeCrearPorqueLaHabitacionNoEstaDisponible() {
        validarMensajeDeError(Constants.ROOM_NOT_AVAILABLE);
    }

    @Then("la reserva no se puede crear porque la fecha de entrada debe ser anterior a la fecha de salida")
    public void laReservaNoSePuedeCrearPorqueLaFechaDeEntradaDebeSerAnteriorALaFechaDeSalida() {
        validarMensajeDeError(Constants.INVALID_RESERVATION_RANGE);
    }

    @Then("la reserva no se crea porque no hay cupo para la cantidad de huespedes")
    public void laReservaNoSeCreaPorqueNoHayCupoParaLaCantidadDeHuespedes() {
        validarMensajeDeError(Constants.ROOM_CAPACITY_EXCEEDED);
    }

    @Then("la reserva no se puede crear porque ya existe una reservacion en esas mismas fechas")
    public void laReservaNoSePuedeCrearPorqueYaExisteUnaReservacionEnEsasMismasFechas() {
        assertNotNull(reservaCreada);
        validarMensajeDeError(Constants.RESERVATION_OVERLAP_ROOM);
    }

    @Then("la reserva no se puede crear porque la cantidad de huespedes es invalida")
    public void laReservaNoSePuedeCrearPorqueLaCantidadDeHuespedesEsInvalida() {
        validarMensajeDeError(Constants.INVALID_GUESTS_COUNT);
    }

    private void intentarCrearReserva(ReservationDto reservationDto) {
        excepcion = null;

        try {
            Reservation reservation = reservationService.create(reservationDto);
            imprimirRespuesta("Reserva creada por el servicio", resumenReserva(reservation));
        } catch (Exception e) {
            excepcion = e;
            imprimirRespuesta("Excepcion al crear reserva", e.getMessage());
        }
    }

    private void validarMensajeDeError(String mensajeEsperado) {
        assertNotNull(excepcion);
        imprimirRespuesta("Excepcion validada", excepcion.getMessage());
        assertEquals(mensajeEsperado, excepcion.getMessage());
    }

    private RoomDto crearHabitacionDesdeTabla(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        String unique = String.valueOf(System.nanoTime());

        RoomDto room = new RoomDto();
        room.setCode(row.get("code") + unique);
        room.setName(row.get("name") + unique);
        room.setCity(row.get("city"));
        room.setMaxGuests(Integer.parseInt(row.get("maxGuests")));
        room.setNightlyPrice(new BigDecimal(row.get("nightlyPrice")));
        room.setAvailable(Boolean.parseBoolean(row.get("available")));
        return room;
    }

    private GuestDto crearHuespedDesdeTabla(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        String unique = String.valueOf(System.nanoTime());

        GuestDto guest = new GuestDto();
        guest.setIdentification(row.get("identification") + unique);
        guest.setName(row.get("name") + unique);
        guest.setEmail(agregarUniqueAlCorreo(row.get("email"), unique));
        return guest;
    }

    private ReservationDto crearReservaDesdeTabla(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);

        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(habitacion.getId());
        reservation.setGuestId(huesped.getId());
        reservation.setCheckIn(LocalDateTime.parse(row.get("checkIn")));
        reservation.setCheckOut(LocalDateTime.parse(row.get("checkOut")));
        reservation.setGuestsCount(Integer.parseInt(row.get("guestsCount")));
        reservation.setNotes(row.get("notes"));
        return reservation;
    }

    private String agregarUniqueAlCorreo(String email, String unique) {
        int atIndex = email.indexOf("@");
        return email.substring(0, atIndex) + unique + email.substring(atIndex);
    }

    private String resumenHabitacion(Room room) {
        return "id: " + room.getId()
                + ", code: " + room.getCode()
                + ", maxGuests: " + room.getMaxGuests()
                + ", available: " + room.getAvailable();
    }

    private String resumenHuesped(Guest guest) {
        return "id: " + guest.getId()
                + ", identification: " + guest.getIdentification()
                + ", email: " + guest.getEmail();
    }

    private String resumenReserva(Reservation reservation) {
        return "id: " + reservation.getId()
                + ", roomId: " + reservation.getRoomId()
                + ", guestId: " + reservation.getGuestId()
                + ", checkIn: " + reservation.getCheckIn()
                + ", checkOut: " + reservation.getCheckOut()
                + ", guestsCount: " + reservation.getGuestsCount()
                + ", notes: " + reservation.getNotes();
    }

    private String resumenReservaDto(ReservationDto reservation) {
        return "roomId: " + reservation.getRoomId()
                + ", guestId: " + reservation.getGuestId()
                + ", checkIn: " + reservation.getCheckIn()
                + ", checkOut: " + reservation.getCheckOut()
                + ", guestsCount: " + reservation.getGuestsCount()
                + ", notes: " + reservation.getNotes();
    }

    private void imprimirRespuesta(String accion, String respuesta) {
        System.out.println("[BDD] " + accion + " -> " + respuesta);
    }
}
