
package com.exam.parking.controller.web;

import com.exam.parking.repository.PlaceRepository;
import com.exam.parking.repository.VehiculeRepository;
import com.exam.parking.repository.ReservationRepository;
import com.exam.parking.repository.StationnementRepository;

import com.exam.parking.enumeration.StatutPlace;
import com.exam.parking.enumeration.StatutStationnement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final PlaceRepository placeRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final StationnementRepository stationnementRepository;

    public DashboardController(
            PlaceRepository placeRepository,
            VehiculeRepository vehiculeRepository,
            ReservationRepository reservationRepository,
            StationnementRepository stationnementRepository) {

        this.placeRepository = placeRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.reservationRepository = reservationRepository;
        this.stationnementRepository = stationnementRepository;
    }

    @GetMapping({"/", "/dashboard", "/admin/dashboard"})
    public String afficherDashboard(Model model) {

        long totalPlaces = placeRepository.count();

        long placesLibres = placeRepository
                .findByStatut(StatutPlace.LIBRE)
                .size();

        long placesOccupees = placeRepository
                .findByStatut(StatutPlace.OCCUPEE)
                .size();

        long totalVehicules = vehiculeRepository.count();

        long totalReservations = reservationRepository.count();

        long stationnementsEnCours = stationnementRepository
                .findByStatut(StatutStationnement.EN_COURS)
                .size();

        double tauxOccupation = totalPlaces == 0
                ? 0
                : (placesOccupees * 100.0) / totalPlaces;

        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("activePage", "dashboard");

        model.addAttribute("totalPlaces", totalPlaces);
        model.addAttribute("placesLibres", placesLibres);
        model.addAttribute("placesOccupees", placesOccupees);
        model.addAttribute("totalVehicules", totalVehicules);
        model.addAttribute("totalReservations", totalReservations);
        model.addAttribute("stationnementsEnCours", stationnementsEnCours);

        model.addAttribute("tauxOccupation",
                Math.round(tauxOccupation * 10.0) / 10.0);

        return "dashboard/index";
    }
}
