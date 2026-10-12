package cl.biblioteca.modelo;

import java.sql.Timestamp;

public class Fecha {
    public static String hoy() {
        return new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
    }

    public static String sumarDias(String fecha, int cantidad) {
        int anio = Integer.parseInt(fecha.substring(0, 4));
        int mes = Integer.parseInt(fecha.substring(5, 7));
        int dia = Integer.parseInt(fecha.substring(8, 10));
        for (int i = 0; i < cantidad; i++) {
            dia++;
            if (dia > diasDelMes(mes, anio)) {
                dia = 1;
                mes++;
                if (mes > 12) {
                    mes = 1;
                    anio++;
                }
            }
        }
        String textoMes = mes < 10 ? "0" + mes : "" + mes;
        String textoDia = dia < 10 ? "0" + dia : "" + dia;
        return anio + "-" + textoMes + "-" + textoDia;
    }

    private static int diasDelMes(int mes, int anio) {
        if (mes == 2) {
            boolean bisiesto = anio % 400 == 0 || (anio % 4 == 0 && anio % 100 != 0);
            return bisiesto ? 29 : 28;
        }
        if (mes == 4 || mes == 6 || mes == 9 || mes == 11) {
            return 30;
        }
        return 31;
    }

    public static boolean posterior(String primera, String segunda) {
        int valorPrimero = Integer.parseInt(primera.substring(0, 4)) * 10000
                + Integer.parseInt(primera.substring(5, 7)) * 100
                + Integer.parseInt(primera.substring(8, 10));
        int valorSegundo = Integer.parseInt(segunda.substring(0, 4)) * 10000
                + Integer.parseInt(segunda.substring(5, 7)) * 100
                + Integer.parseInt(segunda.substring(8, 10));
        return valorPrimero > valorSegundo;
    }
}
