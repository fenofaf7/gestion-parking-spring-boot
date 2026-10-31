package com.exam.parking.service;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.entity.Role;
import com.exam.parking.entity.CategorieClient;

import com.exam.parking.repository.UtilisateurRepository;
import com.exam.parking.repository.RoleRepository;
import com.exam.parking.repository.CategorieClientRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final CategorieClientRepository categorieRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            CategorieClientRepository categorieRepository,
            PasswordEncoder passwordEncoder) {

        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.categorieRepository = categorieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Ajouter un utilisateur
    public Utilisateur ajouter(
            String nom,
            String prenom,
            String email,
            String motDePasse,
            String telephone,
            Long roleId,
            Long categorieId) {

        if (nom == null || nom.isBlank()
                || prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException(
                "Le nom et le prénom sont obligatoires."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "L'email est obligatoire."
            );
        }

        if (motDePasse == null || motDePasse.isBlank()) {
            throw new IllegalArgumentException(
                "Le mot de passe est obligatoire."
            );
        }

        String emailNormalise =
                email.trim().toLowerCase(Locale.ROOT);

        if (utilisateurRepository.existsByEmail(emailNormalise)) {
            throw new IllegalArgumentException(
                "Cette adresse email existe déjà."
            );
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Rôle introuvable."
                    )
                );

        CategorieClient categorie = null;

        if (categorieId != null) {
            categorie = categorieRepository.findById(categorieId)
                    .orElseThrow(() ->
                        new NoSuchElementException(
                            "Catégorie introuvable."
                        )
                    );
        }

        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setNom(nom.trim());
        utilisateur.setPrenom(prenom.trim());
        utilisateur.setEmail(emailNormalise);
        utilisateur.setMotDePasse(
                passwordEncoder.encode(motDePasse)
        );
        utilisateur.setTelephone(telephone);
        utilisateur.setRole(role);
        utilisateur.setCategorie(categorie);
        utilisateur.setActif(true);

        return utilisateurRepository.save(utilisateur);
    }

    // Lister les utilisateurs
    @Transactional(readOnly = true)
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    // Rechercher par ID
    @Transactional(readOnly = true)
    public Utilisateur rechercherParId(Long id) {

        return utilisateurRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Utilisateur introuvable : " + id
                    )
                );
    }

    // Rechercher par email
    @Transactional(readOnly = true)
    public Utilisateur rechercherParEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "L'email est obligatoire."
            );
        }

        return utilisateurRepository
                .findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Utilisateur introuvable."
                    )
                );
    }

    public Utilisateur desactiver(Long id, String emailAdministrateur) {

        Utilisateur utilisateur = rechercherParId(id);

        if (utilisateur.getEmail().equalsIgnoreCase(emailAdministrateur)) {
            throw new IllegalStateException(
                "Vous ne pouvez pas désactiver votre propre compte."
            );
        }

        if (!utilisateur.isActif()) {
            throw new IllegalStateException(
                "Cet utilisateur est déjà désactivé."
            );
        }

        utilisateur.setActif(false);

        return utilisateurRepository.save(utilisateur);
    }

    // Réactiver un utilisateur
    public Utilisateur reactiver(Long id) {

        Utilisateur utilisateur = rechercherParId(id);

        utilisateur.setActif(true);

        return utilisateurRepository.save(utilisateur);
    }
}