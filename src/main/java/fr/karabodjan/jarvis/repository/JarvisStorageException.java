package fr.karabodjan.jarvis.repository;

/**
 * Lançada quando uma operação de persistência falha.
 * Unchecked para não vazar detalhes de infraestrutura (JDBC/SQLException)
 * para as camadas superiores.
 */
public class JarvisStorageException extends RuntimeException {

    public JarvisStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}