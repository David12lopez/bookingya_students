import { expect, test } from '@playwright/test';
import { randomUUID } from 'crypto';

test.describe('ATDD Habitacion', () => {
  test('Crear habitacion con datos validos', async ({ request }) => {
    const habitacion = crearDatosHabitacion();

    const response = await request.post('room', {
      data: habitacion
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBeTruthy();
    expect(body.code).toBe(habitacion.code);
    expect(body.name).toBe(habitacion.name);
    expect(body.city).toBe(habitacion.city);
    expect(body.maxGuests).toBe(habitacion.maxGuests);
    expect(body.nightlyPrice).toBe(habitacion.nightlyPrice);
    expect(body.available).toBe(habitacion.available);
  });

  test('Consultar habitacion por ID', async ({ request }) => {
    const habitacion = crearDatosHabitacion();

    const createResponse = await request.post('room', {
      data: habitacion
    });

    expect(createResponse.status()).toBe(200);

    const habitacionCreada = await createResponse.json();
    console.log('[ATDD]', JSON.stringify(habitacionCreada));

    const getResponse = await request.get(`room/${habitacionCreada.id}`);

    expect(getResponse.status()).toBe(200);

    const body = await getResponse.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBe(habitacionCreada.id);
    expect(body.code).toBe(habitacion.code);
    expect(body.name).toBe(habitacion.name);
    expect(body.city).toBe(habitacion.city);
    expect(body.maxGuests).toBe(habitacion.maxGuests);
    expect(body.nightlyPrice).toBe(habitacion.nightlyPrice);
    expect(body.available).toBe(habitacion.available);
  });

  test('Crear habitacion con capacidad invalida', async ({ request }) => {
    const habitacionDatoInvalido = {
      ...crearDatosHabitacion(),
      maxGuests: 'abc'
    };

    const response = await request.post('room', {
      data: habitacionDatoInvalido
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));
  });

  test('Consultar habitacion inexistente', async ({ request }) => {
    const habitacionId = randomUUID();

    const response = await request.get(`room/${habitacionId}`);

    expect(response.status()).toBe(404);

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
