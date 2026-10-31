
package com.exam.parking.controller.web;

import com.exam.parking.entity.Stationnement;
import com.exam.parking.enumeration.ModePaiement;
import com.exam.parking.enumeration.StatutStationnement;
import com.exam.parking.service.PaiementService;
import com.exam.parking.service.StationnementService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/paiements")
public class PaiementController {

    private final PaiementService paiementService;
    private final StationnementService stationnementService;

    public PaiementController(
            PaiementService paiementService,
            StationnementService stationnementService) {

        this.paiementService = paiementService;
        this.stationnementService = stationnementService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
            "paiements",
            paiementService.lister()
        );

        return "paiements/index";
    }

    @GetMapping("/ajouter")
    public String formulaire(Model model) {

        List<Stationnement> stationnementsTermines =
            stationnementService.lister()
                .stream()
                .filter(s ->
                    s.getStatut() == StatutStationnement.TERMINE
                )
                .filter(s -> paiementService
                    .listerParStationnement(s.getId())
                    .stream()
                    .noneMatch(p ->
                        p.getStatut().name().equals("CONFIRME")
                        || p.getStatut().name().equals("EN_ATTENTE")
                    )
                )
                .toList();

        model.addAttribute(
            "stationnements",
            stationnementsTermines
        );

        model.addAttribute(
            "modesPaiement",
            ModePaiement.values()
        );

        return "paiements/form";
    }

    @PostMapping("/ajouter")
    public String creerPaiement(
            @RequestParam Long stationnementId,
            @RequestParam ModePaiement modePaiement,
            RedirectAttributes redirectAttributes) {

        try {
            paiementService.creerPaiement(
                stationnementId,
                modePaiement
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Paiement créé avec succès."
            );

            return "redirect:/admin/paiements";

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );

            return "redirect:/admin/paiements/ajouter";
        }
    }

    @PostMapping("/{id}/confirmer")
    public String confirmer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            paiementService.confirmerPaiement(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Paiement confirmé avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/admin/paiements";
    }
}
