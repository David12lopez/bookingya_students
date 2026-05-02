package com.project.bookingya.tdd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.shared.Constants;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservaTddTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearReservaConExito() throws Exception {
        UUID roomId = crearHabitacionParaConsultarReservaPorId();
        UUID guestId = crearHuespedParaConsultarReservaPorId();

        // Crea una reserva
        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 5, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 5, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva creada desde metodo crear reserva con Exito");

        // Valida los datos creados de la reserva
        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.guestId").value(guestId.toString()))
                .andExpect(jsonPath("$.guestsCount").value(2))
                .andExpect(jsonPath("$.notes").value("Reserva creada desde metodo crear reserva con Exito"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
    }


    @Test
    void obtenerReservaPorId() throws Exception {
        UUID roomId = crearHabitacionParaConsultarReservaPorId();
        UUID guestId = crearHuespedParaConsultarReservaPorId();
        UUID reservationId = crearReservaParaConsultarReservaPorId(roomId, guestId);

        // Consulta una reserva por ID
        String reservationResponse = mockMvc.perform(get("/reservation/{id}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.guestId").value(guestId.toString()))
                .andExpect(jsonPath("$.guestsCount").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
    }

    private UUID crearHabitacionParaConsultarReservaPorId() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());


        RoomDto room = new RoomDto();
        room.setCode("ROOM" + unique);
        room.setName("Habitacion" + unique);
        room.setCity("Bogota");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("180000"));
        room.setAvailable(true);

        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
        return extractId(roomResponse);
    }

    private UUID crearHuespedParaConsultarReservaPorId() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
        return extractId(guestResponse);
    }

    private UUID crearReservaParaConsultarReservaPorId(UUID roomId, UUID guestId) throws Exception {
        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 6, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 6, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva para obtener por ID");

        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
        return extractId(reservationResponse);
    }
    private UUID extractId(String json) throws Exception {
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    @Test
    void actualizarReserva() throws Exception {
        UUID roomId = crearHabitacionParaActualizar();
        UUID guestId = crearHuespedParaActualizar();
        UUID reservationId = crearReservaParaActualizar(roomId, guestId);

        // Actualiza los datos de una reserva existente en este caso, fechas de ingreso y salida y cantidad de huespedes
        ReservationDto reservationUpdate = new ReservationDto();
        reservationUpdate.setRoomId(roomId);
        reservationUpdate.setGuestId(guestId);
        reservationUpdate.setCheckIn(LocalDateTime.of(2026, 6, 25, 7, 0));
        reservationUpdate.setCheckOut(LocalDateTime.of(2026, 6, 30, 19, 0));
        reservationUpdate.setGuestsCount(4);
        reservationUpdate.setNotes("Reserva creada para actualizar");
        String reservationResponse = mockMvc.perform(put("/reservation/{id}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.guestId").value(guestId.toString()))
                .andExpect(jsonPath("$.guestsCount").value(4))
                .andExpect(jsonPath("$.notes").value("Reserva creada para actualizar"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
    }

    private UUID crearHabitacionParaActualizar() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        RoomDto room = new RoomDto();
        room.setCode("ROOM" + unique);
        room.setName("Habitacion" + unique);
        room.setCity("Bogota");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("180000"));
        room.setAvailable(true);

        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
        return extractId(roomResponse);
    }

    private UUID crearHuespedParaActualizar() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
        return extractId(guestResponse);
    }

    private UUID crearReservaParaActualizar(UUID roomId, UUID guestId) throws Exception {
        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 6, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 6, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva para Actualizar");

        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
        return extractId(reservationResponse);
    }

    @Test
    void eliminarReserva() throws Exception {
        UUID roomId = crearHabitacionParaEliminar();
        UUID guestId = crearHuespedParaEliminar();
        UUID reservationId = crearReservaParaEliminar(roomId, guestId);

        //Elimina una reserva existente o cancela
        int deleteStatus = mockMvc.perform(delete("/reservation/{id}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getStatus();

        imprimirRespuesta("status " + deleteStatus);
    }

    @Test
    void reservaNoEncontrada() throws Exception {
        UUID reservationId = UUID.randomUUID();

        String reservationResponse = mockMvc.perform(get("/reservation/{id}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(Constants.RESERVATION_NOT_FOUND))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
    }

    @Test
    void huespedConReservaEnElMismoRangoDeFechas() throws Exception {
        UUID firstRoomId = crearHabitacionParaReservaConHuespedOcupado();
        UUID secondRoomId = crearHabitacionParaReservaConHuespedOcupado();
        UUID guestId = crearHuespedParaReservaConHuespedOcupado();

        crearReservaBaseParaHuespedOcupado(firstRoomId, guestId);

        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(secondRoomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 7, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 7, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva cruzada para el mismo huesped");

        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Constants.RESERVATION_OVERLAP_GUEST))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
    }

    private UUID crearHabitacionParaEliminar() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        RoomDto room = new RoomDto();
        room.setCode("ROOM" + unique);
        room.setName("Habitacion" + unique);
        room.setCity("Bogota");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("180000"));
        room.setAvailable(true);

        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
        return extractId(roomResponse);
    }

    private UUID crearHuespedParaEliminar() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
        return extractId(guestResponse);
    }

    private UUID crearReservaParaEliminar(UUID roomId, UUID guestId) throws Exception {
        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 6, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 6, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva para Eliminar");

        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
        return extractId(reservationResponse);
    }

    private UUID crearHabitacionParaReservaConHuespedOcupado() throws Exception {
        String unique = String.valueOf(System.nanoTime());

        RoomDto room = new RoomDto();
        room.setCode("ROOM" + unique);
        room.setName("Habitacion" + unique);
        room.setCity("Bogota");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("180000"));
        room.setAvailable(true);

        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
        return extractId(roomResponse);
    }

    private UUID crearHuespedParaReservaConHuespedOcupado() throws Exception {
        String unique = String.valueOf(System.nanoTime());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
        return extractId(guestResponse);
    }

    private UUID crearReservaBaseParaHuespedOcupado(UUID roomId, UUID guestId) throws Exception {
        ReservationDto reservation = new ReservationDto();
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.of(2026, 7, 10, 14, 0));
        reservation.setCheckOut(LocalDateTime.of(2026, 7, 12, 11, 0));
        reservation.setGuestsCount(2);
        reservation.setNotes("Reserva base para validar huesped ocupado");

        String reservationResponse = mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(reservationResponse);
        return extractId(reservationResponse);
    }

    private void imprimirRespuesta(String respuesta) {
        System.out.println("[TDD] " + respuesta);
    }

}
