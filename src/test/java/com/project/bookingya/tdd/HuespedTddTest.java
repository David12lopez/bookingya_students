package com.project.bookingya.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Guest;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.services.GuestService;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
public class HuespedTddTest {

    @Mock
    private IGuestRepository guestRepository;

    private GuestService guestService;

    @BeforeEach
    void configurarPrueba() {
        guestService = new GuestService(guestRepository, crearModelMapper());
    }

    @Test
    void crearHuespedConExito() {
        GuestDto huesped = crearHuespedDto();
        GuestEntity huespedGuardado = crearHuespedEntity(UUID.randomUUID(), huesped);

        when(guestRepository.existsByIdentification(huesped.getIdentification())).thenReturn(false);
        when(guestRepository.existsByEmail(huesped.getEmail())).thenReturn(false);
        when(guestRepository.save(any(GuestEntity.class))).thenReturn(huespedGuardado);

        Guest respuesta = guestService.create(huesped);

        assertNotNull(respuesta.getId());
        assertEquals(huesped.getIdentification(), respuesta.getIdentification());
        assertEquals(huesped.getName(), respuesta.getName());
        assertEquals(huesped.getEmail(), respuesta.getEmail());

        verify(guestRepository).existsByIdentification(huesped.getIdentification());
        verify(guestRepository).existsByEmail(huesped.getEmail());
        verify(guestRepository).save(any(GuestEntity.class));
    }

    @Test
    void huespedConIdentificacionDuplicada() {
        GuestDto huesped = crearHuespedDto();
        when(guestRepository.existsByIdentification(huesped.getIdentification())).thenReturn(true);

        EntityExistsException excepcion = assertThrows(
                EntityExistsException.class,
                () -> guestService.create(huesped)
        );

        assertEquals(Constants.GUEST_EXISTS, excepcion.getMessage());
        verify(guestRepository).existsByIdentification(huesped.getIdentification());
        verify(guestRepository, never()).save(any(GuestEntity.class));
    }

    @Test
    void huespedConCorreoDuplicado() {
        GuestDto huesped = crearHuespedDto();

        when(guestRepository.existsByIdentification(huesped.getIdentification())).thenReturn(false);
        when(guestRepository.existsByEmail(huesped.getEmail())).thenReturn(true);

        EntityExistsException excepcion = assertThrows(
                EntityExistsException.class,
                () -> guestService.create(huesped)
        );

        assertEquals(Constants.GUEST_EMAIL_EXISTS, excepcion.getMessage());
        verify(guestRepository).existsByIdentification(huesped.getIdentification());
        verify(guestRepository).existsByEmail(huesped.getEmail());
        verify(guestRepository, never()).save(any(GuestEntity.class));
    }

    @Test
    void huespedNoEncontrado() {
        UUID huespedId = UUID.randomUUID();
        when(guestRepository.findById(huespedId)).thenReturn(Optional.empty());

        EntityNotExistsException excepcion = assertThrows(
                EntityNotExistsException.class,
                () -> guestService.getById(huespedId)
        );

        assertEquals(Constants.GUEST_NOT_FOUND, excepcion.getMessage());
        verify(guestRepository).findById(huespedId);
    }

    private GuestDto crearHuespedDto() {
        GuestDto huesped = new GuestDto();
        huesped.setIdentification("ID-TDD");
        huesped.setName("Huesped TDD");
        huesped.setEmail("huesped.tdd@example.com");
        return huesped;
    }

    private GuestEntity crearHuespedEntity(UUID id, GuestDto huesped) {
        GuestEntity entity = new GuestEntity();
        entity.setId(id);
        entity.setIdentification(huesped.getIdentification());
        entity.setName(huesped.getName());
        entity.setEmail(huesped.getEmail());
        return entity;
    }

    private ModelMapper crearModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
