
package com.exam.parking.controller.web;

import com.exam.parking.entity.Place;
import com.exam.parking.entity.Utilisateur;
import com.exam.parking.service.ReservationService;
import com.exam.parking.service.UtilisateurService;
import com.exam.parking.service.VehiculeService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/client/reservations")
public class ClientReservationController {

    private final ReservationService reservationService;
    private final UtilisateurService utilisateurService;
    private final VehiculeService vehiculeService;

    public ClientReservationController(
            ReservationService reservationService,
            UtilisateurService utilisateurService,
            VehiculeService vehiculeService) {

        this.reservationService = reservationService;
        this.utilisateurService = utilisateurService;
        this.vehiculeService = vehiculeService;
    }

    private Utilisateur clientConnecte(
            Authentication authentication) {

        return utilisateurService.rechercherParEmail(
                authentication.getName()
        );
    }

    @GetMapping
    public String index(
            Authentication authentication,
            Model model) {

        Utilisateur client = clientConnecte(authentication);

        model.addAttribute("pageTitle", "Mes réservations");
        model.addAttribute("activePage", "reservations");

        model.addAttribute(
                "reservations",
                reservationService.listerParUtilisateur(client.getId())
        );

        return "client/reservations/index";
    }

    @GetMapping("/ajouter")
    public String formulaire(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime debut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin,

            Authentication authentication,
            Model model) {

        Utilisateur client = clientConnecte(authentication);

        model.addAttribute("pageTitle", "Nouvelle réservation");
        model.addAttribute("activePage", "reservations");

        model.addAttribute(
                "vehicules",
                vehiculeService.listerParProprietaire(client.getId())
        );

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);

        List<Place> places = List.of();

        if (debut != null && fin != null) {
            try {
                places = reservationService.placesDisponibles(debut, fin);

                System.out.println("Début : " + debut);
                System.out.println("Fin : " + fin);
                System.out.println("Nombre de places disponibles : " + places.size());

                places.forEach(place ->
                    System.out.println(
                        "Place : " + place.getNumero()
                        + " | Zone : " + place.getZone()
                        + " | Statut : " + place.getStatut()
                    )
                );

            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
                System.out.println("Erreur : " + e.getMessage());
            }
        }

        model.addAttribute("places", places);

        return "client/reservations/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam Long vehiculeId,
            @RequestParam Long placeId,
            @RequestParam LocalDateTime debut,
            @RequestParam LocalDateTime fin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Utilisateur client = clientConnecte(authentication);

        try {
            reservationService.reserver(
                    client.getId(),
                    vehiculeId,
                    placeId,
                    debut,
                    fin
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Réservation enregistrée. Elle est en attente de confirmation."
            );

            return "redirect:/client/reservations";

        } catch (IllegalArgumentException
                | IllegalStateException
                | NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/client/reservations/ajouter";
        }
    }

    @PostMapping("/{id}/annuler")
    public String annuler(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Utilisateur client = clientConnecte(authentication);

        try {
            reservationService.annulerPourClient(
                    id,
                    client.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Réservation annulée avec succès."
            );

        } catch (IllegalStateException
                | NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/client/reservations";
    }
}
