package com.project.bookingya.tdd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.shared.Constants;

@SpringBootTest
@AutoConfigureMockMvc
public class HuespedTddTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearHuespedConExito() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        //  Crear huesped
        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.identification").value("ID" + unique))
                .andExpect(jsonPath("$.name").value("Huesped" + unique))
                .andExpect(jsonPath("$.email").value("huesped" + unique + "@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
    }

    @Test
    void huespedNoEncontrado() throws Exception {
        UUID guestId = UUID.randomUUID();

        String guestResponse = mockMvc.perform(get("/guest/{id}", guestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(Constants.GUEST_NOT_FOUND))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
    }

    @Test
    void huespedConIdentificacionDuplicada() throws Exception {
        String unique = String.valueOf(System.nanoTime());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        crearHuesped(guest);

        GuestDto duplicatedGuest = new GuestDto();
        duplicatedGuest.setIdentification(guest.getIdentification());
        duplicatedGuest.setName("OtroHuesped" + unique);
        duplicatedGuest.setEmail("otrohuesped" + unique + "@example.com");

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatedGuest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(Constants.GUEST_EXISTS))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
    }

    @Test
    void huespedConCorreoDuplicado() throws Exception {
        String unique = String.valueOf(System.nanoTime());

        GuestDto guest = new GuestDto();
        guest.setIdentification("ID" + unique);
        guest.setName("Huesped" + unique);
        guest.setEmail("huesped" + unique + "@example.com");

        crearHuesped(guest);

        GuestDto duplicatedGuest = new GuestDto();
        duplicatedGuest.setIdentification("OTRO" + unique);
        duplicatedGuest.setName("OtroHuesped" + unique);
        duplicatedGuest.setEmail(guest.getEmail());

        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatedGuest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(Constants.GUEST_EMAIL_EXISTS))
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
    }

    private void crearHuesped(GuestDto guest) throws Exception {
        String guestResponse = mockMvc.perform(post("/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        imprimirRespuesta(guestResponse);
    }

    private void imprimirRespuesta(String respuesta) {
        System.out.println("[TDD] " + respuesta);
    }
}
