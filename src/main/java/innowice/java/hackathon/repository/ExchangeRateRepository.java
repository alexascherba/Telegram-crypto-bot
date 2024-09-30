package innowice.java.hackathon.repository;

import innowice.java.hackathon.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    ExchangeRate findByChartId(Long chartId);
}
