package com.exam.parking.service;

import com.exam.parking.entity.Role;
import com.exam.parking.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    // Injection par constructeur
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Ajouter un rôle
    public Role ajouter(Role role) {

        if (role == null || role.getNom() == null
                || role.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du rôle est obligatoire."
            );
        }

        String nom = role.getNom().trim().toUpperCase();

        if (roleRepository.existsByNom(nom)) {
            throw new IllegalArgumentException(
                "Ce rôle existe déjà."
            );
        }

        Role nouveauRole = new Role(nom);

        return roleRepository.save(nouveauRole);
    }

    // Lister tous les rôles
    @Transactional(readOnly = true)
    public List<Role> lister() {
        return roleRepository.findAll();
    }

    // Rechercher un rôle par ID
    @Transactional(readOnly = true)
    public Role rechercherParId(Long id) {

        return roleRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Rôle introuvable : " + id
                    )
                );
    }

    // Modifier un rôle
    public Role modifier(Long id, String nouveauNom) {

        Role role = rechercherParId(id);

        if (nouveauNom == null || nouveauNom.isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du rôle est obligatoire."
            );
        }

        String nom = nouveauNom.trim().toUpperCase();

        if (!role.getNom().equals(nom)
                && roleRepository.existsByNom(nom)) {
            throw new IllegalArgumentException(
                "Ce nom de rôle est déjà utilisé."
            );
        }

        role.setNom(nom);

        return roleRepository.save(role);
    }

    // Supprimer un rôle
    public void supprimer(Long id) {

        Role role = rechercherParId(id);

        roleRepository.delete(role);
    }
}