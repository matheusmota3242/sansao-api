package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CostParametersDTO;
import dev.m2g2.simao.model.catalog.CostParameters;
import dev.m2g2.simao.repository.CostParametersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CostParametersService {

    private final CostParametersRepository repository;

    public CostParametersService(CostParametersRepository repository) {
        this.repository = repository;
    }

    public CostParameters getEntity() {
        return repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Parâmetros de custo não inicializados."));
    }

    public CostParametersDTO get() {
        return toDto(getEntity());
    }

    public CostParametersDTO update(CostParametersDTO dto) {
        CostParameters p = getEntity();
        p.setFilamentPricePerKg(dto.filamentPricePerKg());
        p.setPowerKw(dto.powerKw());
        p.setEnergyRate(dto.energyRate());
        p.setDepreciationPerHour(dto.depreciationPerHour());
        p.setLaborPerHour(dto.laborPerHour());
        p.setSurchargePct(dto.surchargePct());
        p.setMarkup(dto.markup());
        p.setMarketplaceCommissionPct(dto.marketplaceCommissionPct());
        p.setFixedFee(dto.fixedFee());
        p.setUpdatedAt(LocalDateTime.now());
        return toDto(repository.save(p));
    }

    private CostParametersDTO toDto(CostParameters p) {
        return new CostParametersDTO(
                p.getFilamentPricePerKg(), p.getPowerKw(), p.getEnergyRate(), p.getDepreciationPerHour(),
                p.getLaborPerHour(), p.getSurchargePct(), p.getMarkup(), p.getMarketplaceCommissionPct(), p.getFixedFee());
    }
}
