package com.exam.parking.controller.web;

import com.exam.parking.repository.CategorieClientRepository;
import com.exam.parking.repository.RoleRepository;
import com.exam.parking.service.UtilisateurService;

import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final RoleRepository roleRepository;
    private final CategorieClientRepository categorieRepository;

    public UtilisateurController(
            UtilisateurService utilisateurService,
            RoleRepository roleRepository,
            CategorieClientRepository categorieRepository) {

        this.utilisateurService = utilisateurService;
        this.roleRepository = roleRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute("pageTitle", "Gestion des utilisateurs");
        model.addAttribute("activePage", "utilisateurs");
        model.addAttribute("utilisateurs", utilisateurService.lister());

        return "utilisateurs/index";
    }

    @PostMapping("/{id}/activer")
    public String activer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            utilisateurService.reactiver(id);

            redirectAttributes.addFlashAttribute(
                "success", "Utilisateur activé avec succès."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/{id}/desactiver")
    public String desactiver(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            utilisateurService.desactiver(
                id,
                authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Utilisateur désactivé avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute("pageTitle", "Ajouter un utilisateur");
        model.addAttribute("activePage", "utilisateurs");
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute(
        	    "categories",
        	    categorieRepository.findAll()
        	);

        return "utilisateurs/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam(required = false) String telephone,
            @RequestParam String motDePasse,
            @RequestParam Long roleId,
            @RequestParam Long categorieId,
            RedirectAttributes redirectAttributes) {

        try {
            utilisateurService.ajouter(
                nom, prenom, email, motDePasse, telephone, roleId, categorieId
            );

            redirectAttributes.addFlashAttribute(
                "success", "Utilisateur ajouté avec succès."
            );

            return "redirect:/admin/utilisateurs";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );

            return "redirect:/admin/utilisateurs/ajouter";
        }
    }
}