package pe.com.mcco.security.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodigoRol {

    ADMIN("Administrador", "Acceso total al sistema"),
    SUPERVISOR("Supervisor", "Supervision de sucursal"),
    CAJERO("Cajero", "Operaciones de caja y ventas"),
    FARMACEUTICO("Farmaceutico", "Despacho de medicamentos controlados");

    private final String nombre;
    private final String descripcion;
}
