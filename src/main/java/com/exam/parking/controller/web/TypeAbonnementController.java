
package com.exam.parking.controller.web;

import com.exam.parking.service.TypeAbonnementService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/types-abonnement")
public class TypeAbonnementController {

    private final TypeAbonnementService typeAbonnementService;

    public TypeAbonnementController(
            TypeAbonnementService typeAbonnementService) {

        this.typeAbonnementService = typeAbonnementService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
            "typesAbonnement",
            typeAbonnementService.lister()
        );

        return "types-abonnement/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute(
            "unites",
            new ChronoUnit[] {
                ChronoUnit.WEEKS,
                ChronoUnit.MONTHS,
                ChronoUnit.YEARS
            }
        );

        return "types-abonnement/form";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
            "type",
            typeAbonnementService.rechercherParId(id)
        );

        model.addAttribute(
            "unites",
            new ChronoUnit[] {
                ChronoUnit.WEEKS,
                ChronoUnit.MONTHS,
                ChronoUnit.YEARS
            }
        );

        return "types-abonnement/form";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String description,
            @RequestParam int duree,
            @RequestParam ChronoUnit uniteDuree,
            @RequestParam BigDecimal prix,
            RedirectAttributes redirectAttributes) {

        try {
            if (id == null) {

                typeAbonnementService.ajouter(
                    nom,
                    description,
                    duree,
                    uniteDuree,
                    prix
                );

            } else {

                typeAbonnementService.modifier(
                    id,
                    description,
                    duree,
                    uniteDuree,
                    prix
                );
            }

            redirectAttributes.addFlashAttribute(
                "success",
                "Formule enregistrée avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/types-abonnement";
    }

    @PostMapping("/{id}/desactiver")
    public String desactiver(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            typeAbonnementService.desactiver(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Formule désactivée."
            );

        } catch (NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/types-abonnement";
    }

    @PostMapping("/{id}/reactiver")
    public String reactiver(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            typeAbonnementService.reactiver(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Formule réactivée."
            );

        } catch (NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/types-abonnement";
    }
}
