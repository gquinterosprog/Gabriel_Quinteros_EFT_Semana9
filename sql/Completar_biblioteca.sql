USE biblioteca;

-- Ejecutar una vez, después de los dos scripts originales.
-- Luis aparece en estudiantes, pero le falta la cuenta de acceso.
INSERT INTO usuarios(nombre,rut,correo,contraseña,rol)
VALUES ('Luis Gómez','19283746-5','luis@correo.cl','clave123','estudiante');

-- Se conserva el vencimiento cuando el préstamo pasa a devuelto.
CREATE TABLE detalle_prestamos (
    id_prestamo INT PRIMARY KEY,
    fecha_vencimiento DATE NOT NULL,
    atraso BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_prestamo) REFERENCES prestamos(id)
);

INSERT INTO detalle_prestamos(id_prestamo,fecha_vencimiento,atraso) VALUES
(1,'2025-06-22',FALSE),
(2,'2025-06-24',FALSE),
(3,'2025-06-27',FALSE),
(4,'2025-06-29',FALSE),
(5,'2025-07-02',FALSE),
(6,'2025-06-19',FALSE),
(7,'2025-06-19',FALSE),
(8,'2025-06-28',FALSE),
(9,'2025-06-30',FALSE),
(10,'2025-06-26',FALSE);
