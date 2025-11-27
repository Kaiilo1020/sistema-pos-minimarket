package com.minimarket.patterns.behavioral.observer;

/**
 * Interfaz Subject para el patrón Observer
 * Define los métodos para gestionar observers y notificar eventos
 */
public interface Subject {
    
    /**
     * Agrega un observer al sujeto
     */
    void agregarObserver(Observer observer);
    
    /**
     * Remueve un observer del sujeto
     */
    void removerObserver(Observer observer);
    
    /**
     * Notifica a todos los observers sobre un evento
     */
    void notificarObservers(EventoSistema evento);
    
    /**
     * Obtiene la cantidad de observers registrados
     */
    int getCantidadObservers();
    
    /**
     * Verifica si un observer específico está registrado
     */
    boolean tieneObserver(String observerId);
}
