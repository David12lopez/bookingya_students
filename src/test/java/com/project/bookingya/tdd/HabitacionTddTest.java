package com.project.bookingya.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Room;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.RoomService;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
public class HabitacionTddTest {

    @Mock
    private IRoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void configurarPrueba() {
        roomService = new RoomService(roomRepository, crearModelMapper());
    }

    @Test
    void crearHabitacionConExito() {
        RoomDto habitacion = crearHabitacionDto();
        RoomEntity habitacionGuardada = crearHabitacionEntity(UUID.randomUUID(), habitacion);

        when(roomRepository.existsByCode(habitacion.getCode())).thenReturn(false);
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(habitacionGuardada);

        Room respuesta = roomService.create(habitacion);

        assertNotNull(respuesta.getId());
        assertEquals(habitacion.getCode(), respuesta.getCode());
        assertEquals(habitacion.getName(), respuesta.getName());
        assertEquals(habitacion.getCity(), respuesta.getCity());
        assertEquals(habitacion.getMaxGuests(), respuesta.getMaxGuests());
        assertEquals(habitacion.getNightlyPrice(), respuesta.getNightlyPrice());
        assertEquals(habitacion.getAvailable(), respuesta.getAvailable());

        verify(roomRepository).existsByCode(habitacion.getCode());
        verify(roomRepository).save(any(RoomEntity.class));
    }

    @Test
    void habitacionNoEncontrada() {
        UUID habitacionId = UUID.randomUUID();
        when(roomRepository.findById(habitacionId)).thenReturn(Optional.empty());

        EntityNotExistsException excepcion = assertThrows(
                EntityNotExistsException.class,
                () -> roomService.getById(habitacionId)
        );

        assertEquals(Constants.ROOM_NOT_FOUND, excepcion.getMessage());
        verify(roomRepository).findById(habitacionId);
    }

    @Test
    void habitacionConCodigoDuplicado() {
        RoomDto habitacion = crearHabitacionDto();
        when(roomRepository.existsByCode(habitacion.getCode())).thenReturn(true);

        EntityExistsException excepcion = assertThrows(
                EntityExistsException.class,
                () -> roomService.create(habitacion)
        );

        assertEquals(Constants.ROOM_EXISTS, excepcion.getMessage());
        verify(roomRepository).existsByCode(habitacion.getCode());
        verify(roomRepository, never()).save(any(RoomEntity.class));
    }

    private RoomDto crearHabitacionDto() {
        RoomDto habitacion = new RoomDto();
        habitacion.setCode("ROOM-TDD");
        habitacion.setName("Habitacion TDD");
        habitacion.setCity("Bogota");
        habitacion.setMaxGuests(4);
        habitacion.setNightlyPrice(new BigDecimal("180000"));
        habitacion.setAvailable(true);
        return habitacion;
    }

    private RoomEntity crearHabitacionEntity(UUID id, RoomDto habitacion) {
        RoomEntity entity = new RoomEntity();
        entity.setId(id);
        entity.setCode(habitacion.getCode());
        entity.setName(habitacion.getName());
        entity.setCity(habitacion.getCity());
        entity.setMaxGuests(habitacion.getMaxGuests());
        entity.setNightlyPrice(habitacion.getNightlyPrice());
        entity.setAvailable(habitacion.getAvailable());
        return entity;
    }

    private ModelMapper crearModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
