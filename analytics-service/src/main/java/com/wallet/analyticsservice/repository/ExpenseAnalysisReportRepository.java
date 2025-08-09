package com.wallet.analyticsservice.repository;

import com.wallet.analyticsservice.entity.ExpenseAnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseAnalysisReportRepository extends JpaRepository<ExpenseAnalysisReport, UUID> {
    Optional<ExpenseAnalysisReport> findFirstByCardNumberAndPeriodFromAndPeriodTo(String cardNumber, LocalDate periodFrom, LocalDate periodTo);
}
