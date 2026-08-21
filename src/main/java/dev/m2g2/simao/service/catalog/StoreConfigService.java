package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.StoreConfigDTO;
import dev.m2g2.simao.model.catalog.StoreConfig;
import dev.m2g2.simao.repository.StoreConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StoreConfigService {

    private final StoreConfigRepository repository;

    public StoreConfigService(StoreConfigRepository repository) {
        this.repository = repository;
    }

    public StoreConfig getEntity() {
        return repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configuração da loja não inicializada."));
    }

    public StoreConfigDTO get() {
        return toDto(getEntity());
    }

    public StoreConfigDTO update(StoreConfigDTO dto) {
        StoreConfig s = getEntity();
        s.setInstagram(dto.instagram());
        s.setWhatsapp(dto.whatsapp());
        s.setFreteGratis(dto.freteGratis());
        s.setHeroTitulo(dto.heroTitulo());
        s.setHeroTexto(dto.heroTexto());
        s.setConfianca(orEmpty(dto.confianca()));
        s.setProcesso(orEmpty(dto.processo()));
        s.setFaq(orEmpty(dto.faq()));
        s.setRodape(dto.rodape());
        s.setObsPedido(dto.obsPedido());
        s.setUpdatedAt(LocalDateTime.now());
        return toDto(repository.save(s));
    }

    StoreConfigDTO toDto(StoreConfig s) {
        return new StoreConfigDTO(
                s.getInstagram(), s.getWhatsapp(), s.getFreteGratis(),
                s.getHeroTitulo(), s.getHeroTexto(),
                orEmpty(s.getConfianca()), orEmpty(s.getProcesso()), orEmpty(s.getFaq()),
                s.getRodape(), s.getObsPedido());
    }

    private static List<Map<String, String>> orEmpty(List<Map<String, String>> v) {
        return v == null ? new ArrayList<>() : v;
    }
}
