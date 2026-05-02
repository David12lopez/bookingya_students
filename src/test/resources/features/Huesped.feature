Feature: Gestion de huespedes
  Como administrador del sistema
  Quiero validar los datos de un huesped
  Para evitar registrar huespedes con informacion incorrecta o huespedes duplicados

  Scenario: Identificacion duplicada
    Given que existe un huesped registrado con los siguientes datos:
      | identification | name    | email               |
      | ID             | Huesped | huesped@example.com |
    When intento registrar otro huesped con la misma identificacion y los siguientes datos:
      | identification | name        | email                    |
      | ID             | OtroHuesped | otrohuesped@example.com  |
    Then el sistema debe rechazar el huesped por identificacion duplicada

  Scenario: Nombre de huesped vacio
    Given que tengo un huesped con los siguientes datos:
      | identification | name | email               |
      | ID             |      | huesped@example.com |
    When valido los datos del huesped
    Then el sistema debe rechazar el huesped por nombre vacio

  Scenario: Correo de huesped invalido
    Given que tengo un huesped con los siguientes datos:
      | identification | name    | email           |
      | ID             | Huesped | correo-invalido |
    When valido los datos del huesped
    Then el sistema debe rechazar el huesped por correo invalido
