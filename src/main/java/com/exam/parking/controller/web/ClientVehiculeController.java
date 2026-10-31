
package com.exam.parking.controller.web;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.entity.Vehicule;
import com.exam.parking.service.UtilisateurService;
import com.exam.parking.service.VehiculeService;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/client/vehicules")
public class ClientVehiculeController {

    private final VehiculeService vehiculeService;
    private final UtilisateurService utilisateurService;

    public ClientVehiculeController(
            VehiculeService vehiculeService,
            UtilisateurService utilisateurService) {

        this.vehiculeService = vehiculeService;
        this.utilisateurService = utilisateurService;
    }

    private Utilisateur clientConnecte(Authentication authentication) {
        return utilisateurService.rechercherParEmail(
                authentication.getName()
        );
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {

        Utilisateur client = clientConnecte(authentication);

        model.addAttribute("pageTitle", "Mes véhicules");
        model.addAttribute("activePage", "vehicules");

        model.addAttribute(
                "vehicules",
                vehiculeService.listerParProprietaire(client.getId())
        );

        return "client/vehicules/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute("pageTitle", "Ajouter un véhicule");
        model.addAttribute("activePage", "vehicules");

        return "client/vehicules/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam String immatriculation,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) String couleur,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Utilisateur client = clientConnecte(authentication);

        try {
            vehiculeService.ajouter(
                    immatriculation,
                    marque,
                    modele,
                    couleur,
                    client.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Véhicule ajouté avec succès."
            );

            return "redirect:/client/vehicules";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/client/vehicules/form";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Utilisateur client = clientConnecte(authentication);
        Vehicule vehicule = vehiculeService.rechercherParId(id);

        verifierProprietaire(vehicule, client.getId());

        model.addAttribute("pageTitle", "Modifier un véhicule");
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("vehicule", vehicule);

        return "client/vehicules/form";
    }

    @PostMapping("/{id}/modifier")
    public String modifier(
            @PathVariable Long id,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) String couleur,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Utilisateur client = clientConnecte(authentication);

        vehiculeService.modifierPourClient(
                id,
                client.getId(),
                marque,
                modele,
                couleur
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Véhicule modifié avec succès."
        );

        return "redirect:/client/vehicules/index";
    }

    private void verifierProprietaire(
            Vehicule vehicule,
            Long clientId) {

        if (vehicule.getProprietaire() == null
                || !vehicule.getProprietaire()
                        .getId().equals(clientId)) {

            throw new AccessDeniedException(
                    "Ce véhicule ne vous appartient pas."
            );
        }
    }
}
