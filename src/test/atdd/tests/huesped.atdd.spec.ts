import { expect, test } from '@playwright/test';
import { randomUUID } from 'crypto';

test.describe('ATDD Huesped', () => {
  test('Crear huesped con datos validos', async ({ request }) => {
    const huesped = crearDatosHuesped();

    const response = await request.post('guest', {
      data: huesped
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBeTruthy();
    expect(body.identification).toBe(huesped.identification);
    expect(body.name).toBe(huesped.name);
    expect(body.email).toBe(huesped.email);
  });

  test('Consultar huesped por ID', async ({ request }) => {
    const huesped = crearDatosHuesped();

    const createResponse = await request.post('guest', {
      data: huesped
    });

    expect(createResponse.status()).toBe(200);

    const huespedCreado = await createResponse.json();
    console.log('[ATDD]', JSON.stringify(huespedCreado));

    const getResponse = await request.get(`guest/${huespedCreado.id}`);

    expect(getResponse.status()).toBe(200);

    const body = await getResponse.json();
    console.log('[ATDD]', JSON.stringify(body));

    expect(body.id).toBe(huespedCreado.id);
    expect(body.identification).toBe(huesped.identification);
    expect(body.name).toBe(huesped.name);
    expect(body.email).toBe(huesped.email);
  });

  test('Consultar huesped inexistente', async ({ request }) => {
    const huespedId = randomUUID();

    const response = await request.get(`guest/${huespedId}`);

    expect(response.status()).toBe(404);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));
  });

  test('Crear huesped con solicitud invalida', async ({ request }) => {
    const response = await request.post('guest', {
      headers: {
        'Content-Type': 'application/json'
      },
      data: '{'
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    console.log('[ATDD]', JSON.stringify(body));
  });
});

function crearDatosHuesped() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;

  return {
    identification: `ID${unique}`,
    name: `Huesped${unique}`,
    email: `huesped${unique}@example.com`
  };
}
