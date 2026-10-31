package com.exam.parking.controller.web;

import com.exam.parking.service.VehiculeService;
import com.exam.parking.service.UtilisateurService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;
    private final UtilisateurService utilisateurService;

    public VehiculeController(
            VehiculeService vehiculeService,
            UtilisateurService utilisateurService) {

        this.vehiculeService = vehiculeService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute("pageTitle", "Gestion des véhicules");
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("vehicules", vehiculeService.lister());

        return "vehicules/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute("pageTitle", "Ajouter un véhicule");
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("utilisateurs", utilisateurService.lister());

        return "vehicules/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam String immatriculation,
            @RequestParam String marque,
            @RequestParam String modele,
            @RequestParam(required = false) String couleur,
            @RequestParam(required = false) Long proprietaireId,
            RedirectAttributes redirectAttributes) {

        try {
            vehiculeService.ajouter(
                immatriculation,
                marque,
                modele,
                couleur,
                proprietaireId
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Véhicule ajouté avec succès."
            );

            return "redirect:/admin/vehicules";

        } catch (IllegalArgumentException | NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );

            return "redirect:/admin/vehicules/ajouter";
        }
    }
}