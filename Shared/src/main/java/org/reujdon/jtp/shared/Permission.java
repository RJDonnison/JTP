package org.reujdon.jtp.shared;

public enum Permission {
    READ,
    WRITE,
    FULL,
    NONE;

    /**
     * Checks if this permission has sufficient rights for the required permission.
     * <p>
     * Hierarchy:
     * FULL > WRITE > READ > NONE
     *
     * @param permission The permission level to compare
     * @return true if this permission meets or exceeds the required permission, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        return switch (this) {
            case NONE -> permission == NONE;
            case READ -> permission == READ || permission == NONE;
            case WRITE -> permission == WRITE || permission == NONE;
            case FULL -> true;
        };
    }
}

