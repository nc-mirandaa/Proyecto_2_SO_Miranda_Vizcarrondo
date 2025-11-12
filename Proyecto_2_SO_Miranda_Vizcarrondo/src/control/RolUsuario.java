/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author vizca
 */
public enum RolUsuario {
    ADMIN,   // puede todo
    USER,    // puede crear, listar; no puede eliminar/renombrar
    GUEST    // sólo operaciones de lectura (por si queremos usarlo)
}