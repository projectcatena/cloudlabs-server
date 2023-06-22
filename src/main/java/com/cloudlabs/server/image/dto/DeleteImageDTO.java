package com.cloudlabs.server.image.dto;

import com.cloudlabs.server.image.enums.DeleteImageStatus;

public class DeleteImageDTO extends ImageDTO {
    private DeleteImageStatus deleteStatus;
    private long deleteOperationId;

    public DeleteImageDTO() {}

    public DeleteImageStatus getDeleteStatus() {
        return this.deleteStatus;
    }

    public void setDeleteStatus(DeleteImageStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public long getDeleteOperationId() {
        return this.deleteOperationId;
    }

    public void setDeleteOperationId(long deleteOperationId) {
        this.deleteOperationId = deleteOperationId;
    }
}
