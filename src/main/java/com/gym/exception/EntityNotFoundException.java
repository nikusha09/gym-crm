package com.gym.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " not found with identifier: " + identifier);
    }
}
