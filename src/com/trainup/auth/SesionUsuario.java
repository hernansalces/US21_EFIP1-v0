package com.trainup.auth;

import com.trainup.enums.RolUsuario;
import com.trainup.model.UsuarioSistema;

public class SesionUsuario {

    private static UsuarioSistema usuarioActual;

    public static void iniciar(UsuarioSistema usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static UsuarioSistema getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static RolUsuario getRolActual() {
        if (usuarioActual == null) return null;
        return usuarioActual.getRol();
    }

    public static Integer getIdEmpleadoActual() {
        if (usuarioActual == null) return null;
        return usuarioActual.getIdEmpleado();
    }

    public static boolean esAdministrador() {
        return getRolActual() == RolUsuario.ADMIN;
    }

    public static boolean esGerente() {
        return getRolActual() == RolUsuario.GERENTE;
    }

    public static boolean esEmpleado() {
        return getRolActual() == RolUsuario.EMPLEADO;
    }
}
