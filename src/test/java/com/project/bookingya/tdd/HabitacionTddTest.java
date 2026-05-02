package com.project.bookingya.tdd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.shared.Constants;

@SpringBootTest
@AutoConfigureMockMvc
public class HabitacionTddTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearHabitacionConExito() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());
        // Crear Habitacion
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
                .andExpect(jsonPath("$.code").value("ROOM" + unique))
                .andExpect(jsonPath("$.name").value("Habitacion" + unique))
                .andExpect(jsonPath("$.city").value("Bogota"))
                .andExpect(jsonPath("$.maxGuests").value(4))
                .andExpect(jsonPath("$.available").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
    }

    @Test
    void habitacionNoEncontrada() throws Exception {
        UUID roomId = UUID.randomUUID();

        String roomResponse = mockMvc.perform(get("/room/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(Constants.ROOM_NOT_FOUND))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
    }

    @Test
    void habitacionConCodigoDuplicado() throws Exception {
        String unique = String.valueOf(System.nanoTime());

        RoomDto room = new RoomDto();
        room.setCode("ROOM" + unique);
        room.setName("Habitacion" + unique);
        room.setCity("Bogota");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("180000"));
        room.setAvailable(true);

        crearHabitacion(room);

        RoomDto duplicatedRoom = new RoomDto();
        duplicatedRoom.setCode(room.getCode());
        duplicatedRoom.setName("HabitacionDuplicada" + unique);
        duplicatedRoom.setCity("Medellin");
        duplicatedRoom.setMaxGuests(2);
        duplicatedRoom.setNightlyPrice(new BigDecimal("150000"));
        duplicatedRoom.setAvailable(true);

        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatedRoom)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(Constants.ROOM_EXISTS))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
    }

    private void crearHabitacion(RoomDto room) throws Exception {
        String roomResponse = mockMvc.perform(post("/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(roomResponse);
    }

    private void imprimirRespuesta(String respuesta) {
        System.out.println("[TDD] " + respuesta);
    }
}
