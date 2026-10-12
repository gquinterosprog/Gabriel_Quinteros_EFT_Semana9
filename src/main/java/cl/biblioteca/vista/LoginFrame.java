package cl.biblioteca.vista;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    public final JTextField email = new JTextField(25);
    public final JPasswordField password = new JPasswordField(25);
    public final JButton ingresar = new JButton("Ingresar");
    public final JButton conexion = new JButton("Conexión MySQL");
    public final JLabel estado = new JLabel("Ingresa con tu cuenta de la biblioteca");

    public LoginFrame() {
        super("Biblioteca Escolar · Ingreso");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel formulario = new JPanel(new GridLayout(2, 2, 10, 12));
        formulario.add(new JLabel("Correo"));
        formulario.add(email);
        formulario.add(new JLabel("Contraseña"));
        formulario.add(password);
        JPanel botones = new JPanel(new FlowLayout());
        botones.add(ingresar);
        botones.add(conexion);
        JPanel centro = new JPanel(new BorderLayout(10, 16));
        centro.add(formulario, BorderLayout.CENTER);
        centro.add(botones, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new BorderLayout(20, 24));
        panel.add(new JLabel("Biblioteca Escolar", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        panel.add(estado, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));
        setContentPane(panel);
        getRootPane().setDefaultButton(ingresar);
        setSize(520, 300);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public void busy(boolean ocupado) {
        ingresar.setEnabled(!ocupado);
        conexion.setEnabled(!ocupado);
        email.setEnabled(!ocupado);
        password.setEnabled(!ocupado);
        estado.setText(ocupado ? "Comprobando los datos..." : "Ingresa con tu cuenta de la biblioteca");
    }
}
