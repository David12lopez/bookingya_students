package com.project.bookingya.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
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

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
public class ReservaTddTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    private ReservationService reservationService;

    @BeforeEach
    void configurarPrueba() {
        reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                guestRepository,
                crearModelMapper()
        );
    }

    @Test
    void crearReservaConExito() {
        UUID habitacionId = UUID.randomUUID();
        UUID huespedId = UUID.randomUUID();
        ReservationDto reserva = crearReservaDto(habitacionId, huespedId);
        ReservationEntity reservaGuardada = crearReservaEntity(UUID.randomUUID(), reserva);

        prepararHabitacionYHuespedExistentes(habitacionId, huespedId, true, 4);
        prepararSinCrucesDeReserva(reserva, null);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(reservaGuardada);

        Reservation respuesta = reservationService.create(reserva);

        assertNotNull(respuesta.getId());
        assertEquals(habitacionId, respuesta.getRoomId());
        assertEquals(huespedId, respuesta.getGuestId());
        assertEquals(reserva.getGuestsCount(), respuesta.getGuestsCount());
        assertEquals(reserva.getNotes(), respuesta.getNotes());

        verify(reservationRepository).saveAndFlush(any(ReservationEntity.class));
    }

    @Test
    void obtenerReservaPorId() {
        UUID reservaId = UUID.randomUUID();
        ReservationDto reserva = crearReservaDto(UUID.randomUUID(), UUID.randomUUID());
        ReservationEntity reservaEncontrada = crearReservaEntity(reservaId, reserva);

        when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reservaEncontrada));

        Reservation respuesta = reservationService.getById(reservaId);

        assertEquals(reservaId, respuesta.getId());
        assertEquals(reserva.getRoomId(), respuesta.getRoomId());
        assertEquals(reserva.getGuestId(), respuesta.getGuestId());
        verify(reservationRepository).findById(reservaId);
    }

    @Test
    void consultarReservas() {
        UUID reservaId = UUID.randomUUID();
        ReservationDto reserva = crearReservaDto(UUID.randomUUID(), UUID.randomUUID());
        ReservationEntity reservaEncontrada = crearReservaEntity(reservaId, reserva);

        when(reservationRepository.findAll()).thenReturn(List.of(reservaEncontrada));

        List<Reservation> respuesta = reservationService.getAll();

        assertEquals(1, respuesta.size());
        assertEquals(reservaId, respuesta.get(0).getId());
        assertEquals(reserva.getRoomId(), respuesta.get(0).getRoomId());
        assertEquals(reserva.getGuestId(), respuesta.get(0).getGuestId());

        verify(reservationRepository).findAll();
    }

    @Test
    void actualizarReserva() {
        UUID reservaId = UUID.randomUUID();
        UUID habitacionId = UUID.randomUUID();
        UUID huespedId = UUID.randomUUID();
        ReservationDto datosActualizados = crearReservaDto(habitacionId, huespedId);
        datosActualizados.setGuestsCount(3);
        datosActualizados.setNotes("Reserva actualizada desde TDD puro");

        ReservationEntity reservaExistente = crearReservaEntity(reservaId, crearReservaDto(habitacionId, huespedId));
        ReservationEntity reservaActualizada = crearReservaEntity(reservaId, datosActualizados);

        when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reservaExistente));
        prepararHabitacionYHuespedExistentes(habitacionId, huespedId, true, 4);
        prepararSinCrucesDeReserva(datosActualizados, reservaId);
        when(reservationRepository.saveAndFlush(reservaExistente)).thenReturn(reservaActualizada);

        Reservation respuesta = reservationService.update(datosActualizados, reservaId);

        assertEquals(reservaId, respuesta.getId());
        assertEquals(3, respuesta.getGuestsCount());
        assertEquals("Reserva actualizada desde TDD puro", respuesta.getNotes());

        verify(reservationRepository).findById(reservaId);
        verify(reservationRepository).saveAndFlush(reservaExistente);
    }

    @Test
    void eliminarReserva() {
        UUID reservaId = UUID.randomUUID();
        ReservationEntity reserva = crearReservaEntity(
                reservaId,
                crearReservaDto(UUID.randomUUID(), UUID.randomUUID())
        );

        when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

        reservationService.delete(reservaId);

        verify(reservationRepository).findById(reservaId);
        verify(reservationRepository).delete(reserva);
        verify(reservationRepository).flush();
    }

    @Test
    void reservaNoEncontrada() {
        UUID reservaId = UUID.randomUUID();
        when(reservationRepository.findById(reservaId)).thenReturn(Optional.empty());

        EntityNotExistsException excepcion = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.getById(reservaId)
        );

        assertEquals(Constants.RESERVATION_NOT_FOUND, excepcion.getMessage());
        verify(reservationRepository).findById(reservaId);
    }

    @Test
    void huespedConReservaEnElMismoRangoDeFechas() {
        UUID habitacionId = UUID.randomUUID();
        UUID huespedId = UUID.randomUUID();
        ReservationDto reserva = crearReservaDto(habitacionId, huespedId);

        prepararHabitacionYHuespedExistentes(habitacionId, huespedId, true, 4);
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(habitacionId),
                eq(reserva.getCheckIn()),
                eq(reserva.getCheckOut()),
                eq(null)
        )).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(huespedId),
                eq(reserva.getCheckIn()),
                eq(reserva.getCheckOut()),
                eq(null)
        )).thenReturn(true);

        BusinessRuleException excepcion = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reserva)
        );

        assertEquals(Constants.RESERVATION_OVERLAP_GUEST, excepcion.getMessage());
        verify(reservationRepository, never()).saveAndFlush(any(ReservationEntity.class));
    }

    private void prepararHabitacionYHuespedExistentes(UUID habitacionId, UUID huespedId, boolean disponible, int capacidad) {
        RoomEntity habitacion = new RoomEntity();
        habitacion.setId(habitacionId);
        habitacion.setAvailable(disponible);
        habitacion.setMaxGuests(capacidad);

        GuestEntity huesped = new GuestEntity();
        huesped.setId(huespedId);

        when(roomRepository.findById(habitacionId)).thenReturn(Optional.of(habitacion));
        when(guestRepository.findById(huespedId)).thenReturn(Optional.of(huesped));
    }

    private void prepararSinCrucesDeReserva(ReservationDto reserva, UUID excluirReservaId) {
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(reserva.getRoomId()),
                eq(reserva.getCheckIn()),
                eq(reserva.getCheckOut()),
                eq(excluirReservaId)
        )).thenReturn(false);

        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(reserva.getGuestId()),
                eq(reserva.getCheckIn()),
                eq(reserva.getCheckOut()),
                eq(excluirReservaId)
        )).thenReturn(false);
    }

    private ReservationDto crearReservaDto(UUID habitacionId, UUID huespedId) {
        ReservationDto reserva = new ReservationDto();
        reserva.setRoomId(habitacionId);
        reserva.setGuestId(huespedId);
        reserva.setCheckIn(LocalDateTime.of(2026, 5, 10, 14, 0));
        reserva.setCheckOut(LocalDateTime.of(2026, 5, 12, 11, 0));
        reserva.setGuestsCount(2);
        reserva.setNotes("Reserva creada desde TDD puro");
        return reserva;
    }

    private ReservationEntity crearReservaEntity(UUID id, ReservationDto reserva) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(id);
        entity.setRoomId(reserva.getRoomId());
        entity.setGuestId(reserva.getGuestId());
        entity.setCheckIn(reserva.getCheckIn());
        entity.setCheckOut(reserva.getCheckOut());
        entity.setGuestsCount(reserva.getGuestsCount());
        entity.setNotes(reserva.getNotes());
        return entity;
    }

    private ModelMapper crearModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
