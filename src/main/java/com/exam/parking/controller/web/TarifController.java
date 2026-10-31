
package com.exam.parking.controller.web;

import com.exam.parking.service.TarifService;
import com.exam.parking.repository.TypePlaceRepository;
import com.exam.parking.repository.CategorieClientRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/tarifs")
public class TarifController {

    private final TarifService tarifService;
    private final TypePlaceRepository typePlaceRepository;
    private final CategorieClientRepository categorieRepository;

    public TarifController(
            TarifService tarifService,
            TypePlaceRepository typePlaceRepository,
            CategorieClientRepository categorieRepository) {

        this.tarifService = tarifService;
        this.typePlaceRepository = typePlaceRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tarifs", tarifService.lister());
        return "tarifs/index";
    }

    @GetMapping("/ajouter")
    public String ajouterForm(Model model) {
        chargerListes(model);
        return "tarifs/form";
    }

    @GetMapping("/{id}/modifier")
    public String modifierForm(
            @PathVariable Long id,
            Model model) {

        chargerListes(model);
        model.addAttribute("tarif", tarifService.rechercherParId(id));

        return "tarifs/form";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam(required = false) Long id,
            @RequestParam Long typePlaceId,
            @RequestParam(required = false) Long categorieId,
            @RequestParam BigDecimal prixHoraire,
            @RequestParam(required = false) BigDecimal prixJournalier,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime heureDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime heureFin,
            RedirectAttributes redirectAttributes) {

        try {
            if (id == null) {
                tarifService.ajouter(
                    typePlaceId, categorieId,
                    prixHoraire, prixJournalier,
                    heureDebut, heureFin
                );
            } else {
                tarifService.modifier(
                    id, typePlaceId, categorieId,
                    prixHoraire, prixJournalier,
                    heureDebut, heureFin
                );
            }

            redirectAttributes.addFlashAttribute(
                "success", "Tarif enregistré avec succès."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/tarifs";
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            tarifService.changerStatut(id);

            redirectAttributes.addFlashAttribute(
                "success", "Statut du tarif modifié."
            );

        } catch (IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/tarifs";
    }

    private void chargerListes(Model model) {
        model.addAttribute(
            "typesPlace",
            typePlaceRepository.findAll()
        );

        model.addAttribute(
            "categories",
            categorieRepository.findAll()
        );
    }
}
