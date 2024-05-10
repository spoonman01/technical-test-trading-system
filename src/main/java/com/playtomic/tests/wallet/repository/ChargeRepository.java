package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> { }
