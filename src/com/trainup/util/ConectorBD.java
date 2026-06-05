package com.trainup.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConectorBD {

    // *** MODIFICAR ESTOS VALORES SEGÚN TU CONFIGURACIÓN ***
    private static final String URL      = "jdbc:mysql://localhost:3306/trainup_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Argentina/Buenos_Aires";
    private static final String USUARIO  = "TrainUpAdmin";
    private static final String PASSWORD = "TR@Admin";

    private static ConectorBD instancia;
    private Connection conexion;

    private ConectorBD() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado. Verificá que mysql-connector-j esté en el classpath.", e);
        }
    }

    public static ConectorBD getInstancia() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConectorBD();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        }
        return conexion;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
