package com.modelhub.backend.dto.collection;

public class CollectionToggleResponse {
    private boolean collected;

    public CollectionToggleResponse() {
    }

    public CollectionToggleResponse(boolean collected) {
        this.collected = collected;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
