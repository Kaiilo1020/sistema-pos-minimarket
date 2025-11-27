package com.minimarket.patterns.behavioral.observer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Gestor central de eventos del sistema
 * Implementa el patrón Observer para notificar cambios a múltiples observers
 */
public class GestorEventos implements Subject {
    
    private List<Observer> observers;
    private ExecutorService executorService;
    private static final Logger logger = Logger.getLogger(GestorEventos.class.getName());
    private boolean notificacionesAsincronas;
    
    // Singleton instance
    private static GestorEventos instance;
    
    private GestorEventos() {
        this.observers = new CopyOnWriteArrayList<>(); // Thread-safe
        this.executorService = Executors.newFixedThreadPool(5);
        this.notificacionesAsincronas = true;
    }
    
    /**
     * Obtiene la instancia única del gestor de eventos
     */
    public static synchronized GestorEventos getInstance() {
        if (instance == null) {
            instance = new GestorEventos();
        }
        return instance;
    }
    
    @Override
    public void agregarObserver(Observer observer) {
        if (observer != null && !tieneObserver(observer.getId())) {
            observers.add(observer);
            logger.info("Observer agregado: " + observer.getDescripcion());
        }
    }
    
    @Override
    public void removerObserver(Observer observer) {
        if (observer != null) {
            observers.removeIf(obs -> obs.getId().equals(observer.getId()));
            logger.info("Observer removido: " + observer.getDescripcion());
        }
    }
    
    /**
     * Remueve un observer por su ID
     */
    public void removerObserver(String observerId) {
        observers.removeIf(obs -> obs.getId().equals(observerId));
        logger.info("Observer removido por ID: " + observerId);
    }
    
    @Override
    public void notificarObservers(EventoSistema evento) {
        if (evento == null) {
            return;
        }
        
        logger.info("Notificando evento: " + evento.getTipo() + " - " + evento.getDescripcion());
        
        List<Observer> observersActivos = observers.stream()
                .filter(Observer::isActivo)
                .toList();
        
        if (notificacionesAsincronas) {
            notificarAsincrono(evento, observersActivos);
        } else {
            notificarSincrono(evento, observersActivos);
        }
    }
    
    /**
     * Notifica a los observers de forma asíncrona
     */
    private void notificarAsincrono(EventoSistema evento, List<Observer> observersActivos) {
        for (Observer observer : observersActivos) {
            executorService.submit(() -> {
                try {
                    observer.actualizar(evento);
                } catch (Exception e) {
                    logger.severe("Error al notificar observer " + observer.getId() + ": " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Notifica a los observers de forma síncrona
     */
    private void notificarSincrono(EventoSistema evento, List<Observer> observersActivos) {
        for (Observer observer : observersActivos) {
            try {
                observer.actualizar(evento);
            } catch (Exception e) {
                logger.severe("Error al notificar observer " + observer.getId() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public int getCantidadObservers() {
        return observers.size();
    }
    
    @Override
    public boolean tieneObserver(String observerId) {
        return observers.stream().anyMatch(obs -> obs.getId().equals(observerId));
    }
    
    /**
     * Obtiene todos los observers registrados
     */
    public List<Observer> getObservers() {
        return new ArrayList<>(observers);
    }
    
    /**
     * Obtiene observers por tipo específico
     */
    @SuppressWarnings("unchecked")
    public <T extends Observer> List<T> getObserversPorTipo(Class<T> tipo) {
        return observers.stream()
                .filter(tipo::isInstance)
                .map(obs -> (T) obs)
                .toList();
    }
    
    /**
     * Publica un evento en el sistema
     */
    public void publicarEvento(EventoSistema evento) {
        notificarObservers(evento);
    }
    
    /**
     * Publica múltiples eventos
     */
    public void publicarEventos(List<EventoSistema> eventos) {
        for (EventoSistema evento : eventos) {
            publicarEvento(evento);
        }
    }
    
    /**
     * Configura si las notificaciones deben ser asíncronas
     */
    public void setNotificacionesAsincronas(boolean asincronas) {
        this.notificacionesAsincronas = asincronas;
        logger.info("Notificaciones asíncronas: " + asincronas);
    }
    
    /**
     * Filtra observers por tipo de evento que les interesa
     */
    public void notificarObserversEspecificos(EventoSistema evento, 
                                            EventoSistema.TipoEvento... tiposInteres) {
        Set<EventoSistema.TipoEvento> tiposSet = Set.of(tiposInteres);
        
        List<Observer> observersFiltrados = observers.stream()
                .filter(Observer::isActivo)
                .filter(obs -> {
                    // Aquí podrías implementar lógica para verificar qué tipos de eventos
                    // le interesan a cada observer específico
                    return true; // Por ahora, notificar a todos
                })
                .toList();
        
        if (notificacionesAsincronas) {
            notificarAsincrono(evento, observersFiltrados);
        } else {
            notificarSincrono(evento, observersFiltrados);
        }
    }
    
    /**
     * Muestra estadísticas del gestor de eventos
     */
    public void mostrarEstadisticas() {
        System.out.println("=== ESTADÍSTICAS DEL GESTOR DE EVENTOS ===");
        System.out.println("Total de observers: " + observers.size());
        
        long observersActivos = observers.stream().filter(Observer::isActivo).count();
        System.out.println("Observers activos: " + observersActivos);
        System.out.println("Observers inactivos: " + (observers.size() - observersActivos));
        
        System.out.println("Notificaciones asíncronas: " + notificacionesAsincronas);
        
        // Agrupar observers por tipo
        Map<String, Long> tiposObserver = observers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    obs -> obs.getClass().getSimpleName(),
                    java.util.stream.Collectors.counting()
                ));
        
        System.out.println("\nTipos de observers:");
        tiposObserver.forEach((tipo, cantidad) -> 
            System.out.println("- " + tipo + ": " + cantidad));
    }
    
    /**
     * Limpia todos los observers
     */
    public void limpiarObservers() {
        observers.clear();
        logger.info("Todos los observers han sido removidos");
    }
    
    /**
     * Cierra el gestor de eventos y libera recursos
     */
    public void shutdown() {
        executorService.shutdown();
        observers.clear();
        logger.info("Gestor de eventos cerrado");
    }
    
    /**
     * Métodos de conveniencia para eventos comunes
     */
    
    public void notificarStockBajo(String nombreProducto, int stockActual, String usuario) {
        EventoSistema evento = EventoSistema.stockBajo(nombreProducto, stockActual, usuario);
        publicarEvento(evento);
    }
    
    public void notificarProductoProximoVencer(String nombreProducto, 
                                             java.time.LocalDate fechaVencimiento, String usuario) {
        EventoSistema evento = EventoSistema.productoProximoVencer(nombreProducto, fechaVencimiento, usuario);
        publicarEvento(evento);
    }
    
    public void notificarVentaRegistrada(String numeroBoleta, java.math.BigDecimal total, String cajera) {
        EventoSistema evento = EventoSistema.ventaRegistrada(numeroBoleta, total, cajera);
        publicarEvento(evento);
    }
    
    public void notificarAccesoDesautorizado(String usuario, String accionIntentada) {
        EventoSistema evento = EventoSistema.accesoDesautorizado(usuario, accionIntentada);
        publicarEvento(evento);
    }
}
