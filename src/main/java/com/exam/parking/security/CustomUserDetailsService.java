
package com.exam.parking.security;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.repository.UtilisateurRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(
            UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Utilisateur introuvable"
                    )
                );

        if (utilisateur.getRole() == null) {
            throw new UsernameNotFoundException(
                "Aucun rôle attribué à cet utilisateur"
            );
        }

        String role = utilisateur.getRole()
                .getNom()
                .trim()
                .toUpperCase();

        if (!java.util.Set.of(
                "ADMIN", "MANAGER", "OPERATEUR", "CLIENT"
            ).contains(role)) {
            throw new UsernameNotFoundException(
                "Rôle utilisateur non reconnu"
            );
        }

        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities("ROLE_" + role)
                .disabled(!utilisateur.isActif())
                .build();
    }
}
