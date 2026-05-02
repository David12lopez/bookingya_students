import { expect, test } from '@playwright/test';

test.describe('ATDD Reserva', () => {
  test('Crear reserva para habitacion disponible', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const huesped = await crearHuesped(request);
    const reserva = crearDatosReserva(habitacion.id, huesped.id);

    const response = await request.post('reservation', {
      data: reserva
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBeTruthy();
    expect(body.roomId).toBe(reserva.roomId);
    expect(body.guestId).toBe(reserva.guestId);
    expect(body.guestsCount).toBe(reserva.guestsCount);
    expect(body.notes).toBe(reserva.notes);
  });

  test('Consultar reserva por ID', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const huesped = await crearHuesped(request);
    const reservaCreada = await crearReserva(request, habitacion.id, huesped.id);

    const response = await request.get(`reservation/${reservaCreada.id}`);

    expect(response.status()).toBe(200);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBe(reservaCreada.id);
    expect(body.roomId).toBe(habitacion.id);
    expect(body.guestId).toBe(huesped.id);
  });

  test('Actualizar datos de reserva existente', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const huesped = await crearHuesped(request);
    const reservaCreada = await crearReserva(request, habitacion.id, huesped.id);

    const reservaActualizada = {
      roomId: habitacion.id,
      guestId: huesped.id,
      checkIn: '2026-08-15T14:00:00',
      checkOut: '2026-08-18T11:00:00',
      guestsCount: 3,
      notes: 'Reserva actualizada desde ATDD'
    };

    const response = await request.put(`reservation/${reservaCreada.id}`, {
      data: reservaActualizada
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBe(reservaCreada.id);
    expect(body.guestsCount).toBe(reservaActualizada.guestsCount);
    expect(body.notes).toBe(reservaActualizada.notes);
  });

  test('Eliminar reserva por ID', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const huesped = await crearHuesped(request);
    const reservaCreada = await crearReserva(request, habitacion.id, huesped.id);

    const response = await request.delete(`reservation/${reservaCreada.id}`);

    expect(response.status()).toBe(200);

    console.log('[ATDD]', `status ${response.status()}`);
  });

  test('Crear reserva con cantidad invalida', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const huesped = await crearHuesped(request);
    const reservaDatoInvalido = {
      ...crearDatosReserva(habitacion.id, huesped.id),
      guestsCount: 'abc'
    };

    const response = await request.post('reservation', {
      data: reservaDatoInvalido
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));
  });

  test('Crear reserva sin huesped asociado', async ({ request }) => {
    const habitacion = await crearHabitacion(request);
    const reservaSinHuesped = {
      roomId: habitacion.id,
      checkIn: '2026-08-10T14:00:00',
      checkOut: '2026-08-12T11:00:00',
      guestsCount: 2,
      notes: 'Reserva sin huesped desde ATDD'
    };

    const response = await request.post('reservation', {
      data: reservaSinHuesped
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));
  });
});

function crearDatosHabitacion() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;

  return {
    code: `ROOM${unique}`,
    name: `Habitacion${unique}`,
    city: 'Bogota',
    maxGuests: 4,
    nightlyPrice: 180000,
    available: true
  };
}

function crearDatosHuesped() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;

  return {
    identification: `ID${unique}`,
    name: `Huesped${unique}`,
    email: `huesped${unique}@example.com`
  };
}

function crearDatosReserva(roomId: string, guestId: string) {
  return {
    roomId,
    guestId,
    checkIn: '2026-08-10T14:00:00',
    checkOut: '2026-08-12T11:00:00',
    guestsCount: 2,
    notes: 'Reserva creada desde ATDD'
  };
}

async function crearHabitacion(request) {
  const response = await request.post('room', {
    data: crearDatosHabitacion()
  });

  expect(response.status()).toBe(200);

  const body = await response.json();
  console.log('[ATDD]', JSON.stringify(body));
  return body;
}

async function crearHuesped(request) {
  const response = await request.post('guest', {
    data: crearDatosHuesped()
  });

  expect(response.status()).toBe(200);

  const body = await response.json();
  console.log('[ATDD]', JSON.stringify(body));
  return body;
}

async function crearReserva(request, roomId: string, guestId: string) {
  const response = await request.post('reservation', {
    data: crearDatosReserva(roomId, guestId)
  });

  expect(response.status()).toBe(200);

  const body = await response.json();
  console.log('[ATDD]', JSON.stringify(body));
  return body;
}
