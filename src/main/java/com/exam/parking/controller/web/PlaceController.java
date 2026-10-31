
package com.exam.parking.controller.web;

import com.exam.parking.service.PlaceService;
import com.exam.parking.service.TypePlaceService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/places")
public class PlaceController {

    private final PlaceService placeService;
    private final TypePlaceService typePlaceService;

    public PlaceController(
            PlaceService placeService,
            TypePlaceService typePlaceService) {

        this.placeService = placeService;
        this.typePlaceService = typePlaceService;
    }

    @GetMapping
    public String liste(Model model) {

        model.addAttribute("pageTitle", "Gestion des places");
        model.addAttribute("activePage", "places");
        model.addAttribute("places", placeService.lister());

        return "places/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute("pageTitle", "Ajouter une place");
        model.addAttribute("activePage", "places");
        model.addAttribute("typesPlace", typePlaceService.lister());

        return "places/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam String numero,
            @RequestParam String zone,
            @RequestParam Long typePlaceId,
            RedirectAttributes redirectAttributes) {

        try {
            placeService.ajouter(numero, zone, typePlaceId);

            redirectAttributes.addFlashAttribute(
                "success", "Place ajoutée avec succès."
            );

            return "redirect:/admin/places";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );

            return "redirect:/places/ajouter";
        }
    }

    @PostMapping("/{id}/maintenance")
    public String mettreEnMaintenance(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            placeService.mettreEnMaintenance(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Place mise en maintenance avec succès."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/places";
    }

    @PostMapping("/{id}/service")
    public String remettreEnService(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            placeService.remettreEnService(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Place remise en service avec succès."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/places";
    }
}
