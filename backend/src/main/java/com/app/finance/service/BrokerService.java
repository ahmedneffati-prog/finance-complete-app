package com.app.finance.service;

import com.app.finance.dto.request.BrokerRequestDTO;
import com.app.finance.dto.response.BrokerResponseDTO;
import com.app.finance.entity.Broker;
import com.app.finance.exception.ResourceNotFoundException;
import com.app.finance.repository.BrokerRepository;
import com.app.finance.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrokerService {

    private final BrokerRepository brokerRepository;

    @Cacheable(Constants.CACHE_BROKERS)
    public List<BrokerResponseDTO> getAllBrokers() {
        return brokerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BrokerResponseDTO> getActiveBrokers() {
        return brokerRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BrokerResponseDTO getBrokerById(Long id) {
        Broker broker = brokerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Broker", id));
        return toDTO(broker);
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_BROKERS, allEntries = true)
    public BrokerResponseDTO createBroker(BrokerRequestDTO dto) {
        if (brokerRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Broker with name '" + dto.getName() + "' already exists");
        }
        Broker broker = Broker.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .website(dto.getWebsite())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        return toDTO(brokerRepository.save(broker));
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_BROKERS, allEntries = true)
    public BrokerResponseDTO updateBroker(Long id, BrokerRequestDTO dto) {
        Broker broker = brokerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Broker", id));
        broker.setName(dto.getName());
        broker.setCountry(dto.getCountry());
        broker.setWebsite(dto.getWebsite());
        if (dto.getIsActive() != null) broker.setIsActive(dto.getIsActive());
        return toDTO(brokerRepository.save(broker));
    }

    @Transactional
    @CacheEvict(value = Constants.CACHE_BROKERS, allEntries = true)
    public void deleteBroker(Long id) {
        if (!brokerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Broker", id);
        }
        brokerRepository.deleteById(id);
    }

    private BrokerResponseDTO toDTO(Broker broker) {
        return BrokerResponseDTO.builder()
                .id(broker.getId())
                .name(broker.getName())
                .country(broker.getCountry())
                .website(broker.getWebsite())
                .isActive(broker.getIsActive())
                .createdAt(broker.getCreatedAt())
                .build();
    }
}
