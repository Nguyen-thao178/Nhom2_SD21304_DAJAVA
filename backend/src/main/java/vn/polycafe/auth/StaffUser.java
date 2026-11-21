package vn.polycafe.auth;

import java.util.Set;
import java.util.UUID;

public record StaffUser(UUID id, String username, String fullName, int refreshVersion, Set<String> roles, boolean canManageProducts) {}
