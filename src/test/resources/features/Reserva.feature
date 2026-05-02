Feature: Gestion de reservas

  Como administrador del sistema
  Quiero validar disponibilidad de habitaciones, fechas y capacidad
  Para realizar reservaciones en el sistema

  Scenario: Habitacion no disponible
    Given que tengo una habitacion creada con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | Room | Habitacion | Bogota | 5         | 240000       | false     |
    And un huesped creado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    When cuando la habitacion no esta disponible y quiero crear una reserva con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes                                 |
      | 2026-05-10T07:00:00 | 2026-05-16T14:00:00 | 3           | Habitacion marcada como no disponible |
    Then la reserva no se puede crear porque la habitacion no esta disponible

  Scenario: Fechas invalidas
    Given que tengo una habitacion creada con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | Room | Habitacion | Bogota | 5         | 240000       | true      |
    And un huesped creado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    And una reserva con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes            |
      | 2026-05-10T07:00:00 | 2026-05-08T14:00:00 | 3           | Fechas invalidas |
    When quiero crear una reserva con la salida antes que la entrada
    Then la reserva no se puede crear porque la fecha de entrada debe ser anterior a la fecha de salida

  Scenario: Capacidad excedida
    Given que tengo una habitacion creada con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | Room | Habitacion | Bogota | 5         | 240000       | true      |
    And un huesped creado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    And una reserva con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes                           |
      | 2026-05-10T07:00:00 | 2026-05-15T14:00:00 | 6           | Capacidad de ocupacion excedida |
    When la capacidad de huespedes excede la maxima de la habitacion y quiero crear una reserva
    Then la reserva no se crea porque no hay cupo para la cantidad de huespedes

  Scenario: Cantidad de huespedes invalida
    Given que tengo una habitacion creada con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | Room | Habitacion | Bogota | 5         | 240000       | true      |
    And un huesped creado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    And una reserva con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes                          |
      | 2026-05-10T07:00:00 | 2026-05-15T14:00:00 | 0           | Cantidad de huespedes invalida |
    When quiero crear una reserva con cantidad de huespedes invalida
    Then la reserva no se puede crear porque la cantidad de huespedes es invalida

  Scenario: Fechas no disponibles
    Given que tengo una habitacion creada con los siguientes datos:
      | code | name       | city   | maxGuests | nightlyPrice | available |
      | Room | Habitacion | Bogota | 5         | 240000       | true      |
    And un huesped creado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    And una reserva con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes    |
      | 2026-05-10T07:00:00 | 2026-05-15T14:00:00 | 3           | Fechas ocupadas |
    When intento crear otra reserva en la misma habitacion y mismas fechas con los siguientes datos:
      | checkIn             | checkOut            | guestsCount | notes           |
      | 2026-05-10T07:00:00 | 2026-05-15T14:00:00 | 3           | Fechas ocupadas |
    Then la reserva no se puede crear porque ya existe una reservacion en esas mismas fechas
