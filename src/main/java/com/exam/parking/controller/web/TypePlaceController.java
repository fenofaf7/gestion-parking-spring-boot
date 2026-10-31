
package com.exam.parking.controller.web;

import com.exam.parking.entity.TypePlace;
import com.exam.parking.service.TypePlaceService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/types-places")
public class TypePlaceController {

    private final TypePlaceService typePlaceService;

    public TypePlaceController(TypePlaceService typePlaceService) {
        this.typePlaceService = typePlaceService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("pageTitle", "Types de places");
        model.addAttribute("activePage", "types-places");
        model.addAttribute("types", typePlaceService.lister());

        return "types-places/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {
        model.addAttribute("pageTitle", "Ajouter un type");
        model.addAttribute("activePage", "types-places");

        return "types-places/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal tarifHoraireBase,
            @RequestParam(defaultValue = "false") boolean reserveeVIP,
            RedirectAttributes redirectAttributes) {

        try {
            typePlaceService.ajouter(
                nom, description, tarifHoraireBase, reserveeVIP
            );

            redirectAttributes.addFlashAttribute(
                "success", "Type de place ajouté avec succès."
            );

            return "redirect:/admin/types-places";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/types-places/ajouter";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(
            @PathVariable Long id,
            Model model) {

        TypePlace type = typePlaceService.rechercherParId(id);

        model.addAttribute("pageTitle", "Modifier un type");
        model.addAttribute("activePage", "types-places");
        model.addAttribute("type", type);

        return "types-places/form";
    }

    @PostMapping("/{id}/modifier")
    public String modifier(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal tarifHoraireBase,
            @RequestParam(defaultValue = "false") boolean reserveeVIP,
            RedirectAttributes redirectAttributes) {

        try {
            typePlaceService.modifier(
                id, nom, description, tarifHoraireBase, reserveeVIP
            );

            redirectAttributes.addFlashAttribute(
                "success", "Type de place modifié avec succès."
            );

            return "redirect:/admin/types-places";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/types-places/" + id + "/modifier";
        }
    }
}
