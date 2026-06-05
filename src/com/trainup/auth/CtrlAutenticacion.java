package com.trainup.auth;

import com.trainup.dao.UsuarioDAO;
import com.trainup.model.UsuarioSistema;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class CtrlAutenticacion {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Autentica al usuario. Retorna el UsuarioSistema si las credenciales son válidas,
     * null si el usuario no existe, está inactivo o la contraseña es incorrecta.
     */
    public static UsuarioSistema autenticar(String username, String password) {
        try {
            UsuarioSistema usuario = usuarioDAO.buscarPorUsername(username);
            if (usuario == null) return null;

            String hashCalculado = hashearPassword(password, usuario.getPasswordSalt());
            if (!hashCalculado.equals(usuario.getPasswordHash())) return null;

            usuarioDAO.actualizarUltimoAcceso(usuario.getId());
            return usuario;

        } catch (SQLException e) {
            System.err.println("Error de base de datos al autenticar: " + e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error crítico: algoritmo SHA-256 no disponible.");
            return null;
        }
    }

    /**
     * Genera el hash SHA-256 de (password + salt).
     * Retorna el resultado en formato hexadecimal en minúsculas.
     */
    public static String hashearPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
