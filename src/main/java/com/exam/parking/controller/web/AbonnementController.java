
package com.exam.parking.controller.web;

import com.exam.parking.service.AbonnementService;
import com.exam.parking.enumeration.StatutPlace;
import com.exam.parking.repository.PlaceRepository;
import com.exam.parking.repository.TypeAbonnementRepository;
import com.exam.parking.repository.UtilisateurRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/abonnements")
public class AbonnementController {

    private final AbonnementService abonnementService;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PlaceRepository placeRepository;

    public AbonnementController(
            AbonnementService abonnementService,
            TypeAbonnementRepository typeAbonnementRepository,
            UtilisateurRepository utilisateurRepository,
            PlaceRepository placeRepository) {

        this.abonnementService = abonnementService;
        this.typeAbonnementRepository = typeAbonnementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.placeRepository = placeRepository;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
            "abonnements",
            abonnementService.lister()
        );

        return "abonnements/index";
    }

    @GetMapping("/ajouter")
    public String formulaire(Model model) {

        model.addAttribute(
            "utilisateurs",
            utilisateurRepository.findAll()
        );

        model.addAttribute(
            "typesAbonnement",
            typeAbonnementRepository.findByActifTrue()
        );
        model.addAttribute(
        	    "places",
        	    placeRepository.findByStatut(StatutPlace.LIBRE)
        );

        return "abonnements/form";
    }

    @PostMapping("/ajouter")
    public String souscrire(
            @RequestParam Long utilisateurId,
            @RequestParam Long typeAbonnementId,
            @RequestParam Long placeId,
            RedirectAttributes redirectAttributes) {

        try {
            abonnementService.souscrire(
                utilisateurId,
                typeAbonnementId,
                placeId
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Abonnement créé avec succès."
            );

            return "redirect:/admin/abonnements";

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );

            return "redirect:/admin/abonnements/ajouter";
        }
    }

    @PostMapping("/{id}/renouveler")
    public String renouveler(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            abonnementService.renouveler(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Abonnement renouvelé avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/abonnements";
    }

    @PostMapping("/{id}/annuler")
    public String annuler(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            abonnementService.annuler(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Abonnement annulé avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/abonnements";
    }
}
