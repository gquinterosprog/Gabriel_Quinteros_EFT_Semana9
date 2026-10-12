# Biblioteca Escolar

Gabriel Quinteros — Desarrollo Orientado a Objetos II — EFT Semana 9

Aplicación Java con JFrame, JDBC y MySQL para administrar libros y estudiantes, registrar préstamos y devoluciones y consultar reportes.

## Preparación de la base en otro computador

Se necesita Java 17 o superior y MySQL Server. MySQL Workbench permite importar los scripts y comprobar los datos de la conexión.

Iniciar MySQL y abrir MySQL Workbench. Preparar una base con la estructura original de la actividad, ejecutando una vez estos archivos, en orden:

1. `sql/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql`.
2. `sql/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql`.
3. `sql/Completar_biblioteca.sql`.

Los dos primeros archivos son los originales. El tercero agrega la cuenta que falta para Luis Gómez y crea `detalle_prestamos`, que conserva el vencimiento y el atraso. Las cuentas de estudiantes y sus perfiles se relacionan por RUT; sus identificadores son independientes.

En los préstamos pendientes del poblado, `fecha_devolucion` contiene la fecha prevista. Al devolver un libro, ese campo guarda la fecha real y el vencimiento queda conservado en `detalle_prestamos`.

## Conexión MySQL

En la pantalla de ingreso, pulsar **Conexión MySQL** e indicar los datos del servidor donde se importaron los scripts:

| Campo | Valor para una instalación local habitual |
|---|---|
| Servidor | `127.0.0.1` o `localhost` |
| Puerto | `3306`, o el puerto configurado en MySQL |
| Base de datos | `biblioteca` |
| Usuario MySQL | El usuario utilizado en Workbench, por ejemplo `root` |
| Contraseña MySQL | La contraseña de ese usuario de MySQL |

Pulsar **Aceptar** y esperar el mensaje de conexión comprobada. Después ingresar con una cuenta de la biblioteca, por ejemplo `antonia@correo.cl` y `clave123`.

La configuración se aplica durante la ejecución del programa. Al volver a abrirlo en otro servidor, indicar los datos nuevamente. No es necesario editar el código ni recompilar el JAR para cambiar la conexión. El usuario y la contraseña de MySQL son distintos de las cuentas de ingreso de la aplicación.

Los valores iniciales corresponden a la base local utilizada para las pruebas:

- Servidor: `127.0.0.1`, puerto `3319`.
- Base: `biblioteca`.
- Usuario MySQL: `biblioteca_app`.
- Contraseña MySQL: `BibliotecaDemo2026!`.

En otro computador, utilizar los datos de su propio MySQL desde el botón **Conexión MySQL**. El iniciador local de la base de pruebas no forma parte del proyecto ni se necesita para preparar otro servidor.

## Ejecución

Abrir la carpeta o `pom.xml` en IntelliJ IDEA con JDK 17 o superior. El proyecto declara Connector/J en Maven y también incluye el conector en `lib`.

Ejecutar `cl.biblioteca.Main` desde IntelliJ. El ejecutable Java preparado está en `dist/biblioteca.jar` e incluye Connector/J. Con Java instalado, también se puede ejecutar desde la carpeta del proyecto:

```text
java -jar dist/biblioteca.jar
```

MySQL debe estar iniciado y tener importados los tres scripts. El JAR incluye el conector JDBC; MySQL Server se instala y se inicia por separado. Desde Maven se puede usar `package`; el JAR generado queda en `target/biblioteca-escolar-1.0.0.jar` y utiliza el conector de la carpeta `lib`.

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Bibliotecario | antonia@correo.cl | clave123 |
| Estudiante | carlos@correo.cl | clave123 |
| Estudiante | maria@correo.cl | clave123 |
| Estudiante | luis@correo.cl | clave123 |

## Funciones y clases

El bibliotecario administra libros, categorías, estudiantes y cuentas de bibliotecarios. El estudiante consulta el catálogo y gestiona sus propios préstamos. Los registros con préstamos asociados se conservan.

`Usuario` es una clase abstracta. `Estudiante` y `Bibliotecario` sobrescriben sus comportamientos. Las interfaces `Identificable` y `Permisos` definen las capacidades comunes. La sobrecarga aparece en `resumen` y `prestar`.

Los paquetes separan modelo, vista, controlador, servicio y DAO. Los DAOs utilizan `PreparedStatement`, `ResultSet` y operaciones INSERT, SELECT, UPDATE y DELETE. `DatabaseConnection` aplica el Singleton exigido en la pauta final.

La interfaz usa botones, tablas y paneles con `FlowLayout`, `GridLayout` y `BorderLayout`. El formulario de conexión usa `JTextField` y `JPasswordField`, valida el puerto y comprueba la conexión con JDBC. El controlador ejecuta las operaciones con `Thread` y `Runnable`, y muestra sus resultados mediante `SwingUtilities.invokeLater`. El acceso compartido a la conexión y al stock se protege con `synchronized`.

`Fecha` obtiene el día actual a partir de `Timestamp` y suma los 14 días mediante números, condicionales y ciclos. Contempla el cambio de mes, año y años bisiestos. Las fechas se guardan en columnas DATE usando texto con formato `AAAA-MM-DD`.

El stock disponible se almacena en la tabla original. El total se calcula sumando los ejemplares disponibles y los préstamos activos. Los reportes permiten consultar los libros más prestados, el historial de un estudiante y los préstamos activos.

## Pruebas

La clase `cl.biblioteca.IntegrationTest` se probó con la conexión local de pruebas y una base preparada con los datos iniciales. Crea registros temporales y los elimina al finalizar. Revisa CRUD, validaciones, permisos, stock, devoluciones, fechas y doce solicitudes simultáneas para un ejemplar. Esta prueba de consola utiliza los valores iniciales de `DatabaseConnection`; la aplicación gráfica permite elegir otro servidor desde la pantalla de ingreso.

Los resultados están en `docs/resultado_IntegrationTest.txt`, `docs/resultado_interfaz.txt` y `docs/resultado_conexion.txt`. También se comprobó el cambio de servidor y usuario desde la pantalla de ingreso del JAR. Las capturas de los módulos se encuentran en `docs/capturas`.
