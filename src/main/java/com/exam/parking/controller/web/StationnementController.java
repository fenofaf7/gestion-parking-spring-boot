
package com.exam.parking.controller.web;

import com.exam.parking.entity.Place;
import com.exam.parking.entity.Reservation;
import com.exam.parking.enumeration.StatutPlace;
import com.exam.parking.enumeration.StatutReservation;
import com.exam.parking.service.PlaceService;
import com.exam.parking.service.ReservationService;
import com.exam.parking.service.StationnementService;
import com.exam.parking.service.VehiculeService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/stationnements")
public class StationnementController {

    private final StationnementService stationnementService;
    private final VehiculeService vehiculeService;
    private final PlaceService placeService;
    private final ReservationService reservationService;

    public StationnementController(
            StationnementService stationnementService,
            VehiculeService vehiculeService,
            PlaceService placeService,
            ReservationService reservationService) {

        this.stationnementService = stationnementService;
        this.vehiculeService = vehiculeService;
        this.placeService = placeService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
            "stationnements",
            stationnementService.lister()
        );

        model.addAttribute(
            "stationnementsEnCours",
            stationnementService.listerEnCours().size()
        );

        return "stationnements/index";
    }

    @GetMapping("/ajouter")
    public String formulaire(Model model) {

        List<Place> placesLibres = placeService.lister()
            .stream()
            .filter(place -> place.getStatut() == StatutPlace.LIBRE)
            .toList();

        List<Reservation> reservationsConfirmees =
            reservationService.lister()
                .stream()
                .filter(reservation ->
                    reservation.getStatut() == StatutReservation.CONFIRMEE
                )
                .toList();

        model.addAttribute("vehicules", vehiculeService.lister());
        model.addAttribute("places", placesLibres);
        model.addAttribute("reservations", reservationsConfirmees);

        return "stationnements/form";
    }

    @PostMapping("/ajouter")
    public String enregistrerEntree(
            @RequestParam Long vehiculeId,
            @RequestParam Long placeId,
            @RequestParam(required = false) Long reservationId,
            RedirectAttributes redirectAttributes) {

        try {
            stationnementService.enregistrerEntree(
                vehiculeId,
                placeId,
                reservationId
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Entrée du véhicule enregistrée avec succès."
            );

            return "redirect:/admin/stationnements";

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );

            return "redirect:/admin/stationnements/ajouter";
        }
    }

    @PostMapping("/{id}/sortie")
    public String enregistrerSortie(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            stationnementService.enregistrerSortie(id);

            redirectAttributes.addFlashAttribute(
                "success",
                "Sortie enregistrée et montant calculé."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/stationnements";
    }
}
