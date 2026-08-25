package dev.m2g2.simao.model;

import dev.m2g2.simao.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Quem usa o admin. A tabela é app_user, não user: USER é palavra reservada no
 * Postgres e uma tabela com esse nome teria de vir entre aspas em todo
 * statement que o Hibernate gera.
 */
@Entity
@Table(name = "app_user")
public class User extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** Hash BCrypt — nunca a senha em texto puro. */
    @Column(nullable = false)
    private String password;

    /**
     * Começa em OPERATOR de propósito: quem for criado sem papel explícito cai
     * no menor privilégio, nunca em ADMIN por acidente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.OPERATOR;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
