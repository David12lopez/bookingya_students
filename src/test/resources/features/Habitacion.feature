Feature: Gestion de habitaciones
  Como administrador del sistema
  Quiero validar los datos de una habitacion
  Para evitar registrar habitaciones con informacion incorrecta

  Scenario: Precio de habitacion invalido
    Given que tengo una habitacion con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | ROOM | Habitacion | Bogota | 2         | -1000        | true      |
    When valido los datos de la habitacion
    Then el sistema debe rechazar la habitacion por precio invalido

  Scenario: Capacidad maxima invalida
    Given que tengo una habitacion con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | ROOM | Habitacion | Bogota | -2        | 180000       | true      |
    When valido los datos de la habitacion
    Then el sistema debe rechazar la habitacion por capacidad maxima invalida
