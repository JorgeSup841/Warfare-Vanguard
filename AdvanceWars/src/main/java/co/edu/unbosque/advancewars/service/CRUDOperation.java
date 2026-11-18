package co.edu.unbosque.advancewars.service;

import java.util.List;

/**
 * Define el contrato común de operaciones CRUD sencillas del proyecto.
 */
public interface CRUDOperation<T> {
    /**
     * Registra un objeto nuevo.
     */
    int create(T data);
    /**
     * Consulta los elementos disponibles.
     */
    List<T> getAll();
    /**
     * Elimina un elemento identificado por su id.
     */
    int deleteById(Long id);
    /**
     * Modifica los datos de un elemento existente.
     */
    int updateById(Long id, T newData);
    /**
     * Cuenta los elementos almacenados.
     */
    long count();
    /**
     * Determina si un identificador corresponde a un elemento.
     */
    boolean exist(Long id);
}
