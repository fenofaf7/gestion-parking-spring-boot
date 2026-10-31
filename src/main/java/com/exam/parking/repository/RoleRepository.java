package com.exam.parking.repository;

import com.exam.parking.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(String nom);

    boolean existsByNom(String nom);
}