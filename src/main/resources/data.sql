-- =============================================================================
-- Script de Inicialización de Datos - DeviozRecluta Express
-- Carga inicial de Vacantes y Postulantes de ejemplo
-- =============================================================================

-- 3 Vacantes de ejemplo
INSERT IGNORE INTO vacantes (id, titulo, cliente, seniority, salario_minimo, salario_maximo, modalidad, estado)
VALUES
(1, 'Desarrollador Java Senior', 'Banco Continental', 'SENIOR', 6500.00, 9500.00, 'REMOTO', 'ABIERTA'),
(2, 'Ingeniero de Software Fullstack', 'Fintech Innova', 'SEMI_SENIOR', 4500.00, 6000.00, 'HIBRIDO', 'ABIERTA'),
(3, 'Desarrollador Frontend Junior', 'Agencia Digital Devioz', 'JUNIOR', 2500.00, 3500.00, 'PRESENCIAL', 'ABIERTA');

-- 8 Postulantes de ejemplo
INSERT IGNORE INTO postulantes (id, nombres, apellidos, dni, email, telefono, anios_experiencia, habilidades, fecha_registro)
VALUES
(1, 'Carlos', 'Mendoza Quispe', '45871234', 'carlos.mendoza@gmail.com', '987654321', 6, 'Java 21, Spring Boot, Microservicios, Docker, AWS', '2026-10-01'),
(2, 'Ana', 'García Romero', '72345678', 'ana.garcia@outlook.com', '981234567', 4, 'Java, Spring Boot, React, PostgreSQL', '2026-10-02'),
(3, 'Luis', 'Torres Benitez', '46781290', 'luis.torres@gmail.com', '976543210', 2, 'JavaScript, TypeScript, Vue.js, CSS3', '2026-10-03'),
(4, 'María', 'López Salazar', '73456789', 'maria.lopez@yahoo.com', '998877665', 5, 'Java 17, Spring Cloud, Kafka, Kubernetes', '2026-10-04'),
(5, 'Jorge', 'Flores Castillo', '47892345', 'jorge.flores@gmail.com', '984561230', 7, 'Java, Python, Arquitectura de Software, Cloud', '2026-10-05'),
(6, 'Lucía', 'Ramos Herrera', '74567890', 'lucia.ramos@hotmail.com', '971239845', 3, 'Node.js, Express, React, MongoDB', '2026-10-06'),
(7, 'Pedro', 'Vargas Huamán', '48901234', 'pedro.vargas@gmail.com', '965432178', 1, 'HTML, CSS, JavaScript, Git', '2026-10-07'),
(8, 'Elena', 'Castro Paredes', '75678901', 'elena.castro@gmail.com', '953412876', 4, 'Spring Boot, Angular, MySQL, Docker', '2026-10-08');
