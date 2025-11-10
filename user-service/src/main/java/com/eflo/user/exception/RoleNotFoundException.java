package com.eflo.user.exception;

/**
 * Exception thrown when a role is not found in the system.
 */
public class RoleNotFoundException extends RuntimeException {

    private final String roleId;
    private final String roleName;

    public RoleNotFoundException(String roleId) {
        super(String.format("Role not found with ID: %s", roleId));
        this.roleId = roleId;
        this.roleName = null;
    }

    public RoleNotFoundException(String roleId, String roleName) {
        super(String.format("Role not found with ID: %s or name: %s", roleId, roleName));
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }
}
