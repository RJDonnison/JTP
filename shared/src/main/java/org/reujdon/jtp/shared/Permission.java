package org.reujdon.jtp.shared;

/**
 * Represents permission levels for accessing or modifying a resource.
 * <p>
 * Permission hierarchy:
 * <ul>
 *     <li>{@code FULL} > {@code WRITE} > {@code READ} > {@code NONE}</li>
 * </ul>
 * Used to check access levels against required permissions.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public enum Permission {
    /**
     * Read-only permission.
     * <p>Allows querying data but no modifications.</p>
     */
    READ,

    /**
     * Write permission.
     * <p>Includes read rights plus data modification capabilities.</p>
     * @see #READ
     */
    WRITE,

    /**
     * Full administrative permission.
     * <p>Includes all read/write rights plus system management.</p>
     * @see #WRITE
     */
    FULL,

    /**
     * No permissions granted.
     * <p>Allows only authentication and help commands.</p>
     */
    NONE;

    /**
     * Checks if this permission has sufficient rights for the required permission.
     * <p>
     * Hierarchy:
     * <ul>
     *     <li>{@code FULL} > {@code WRITE} > {@code READ} > {@code NONE}</li>
     * </ul>
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

