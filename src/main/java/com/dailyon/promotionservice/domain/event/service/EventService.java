package com.dailyon.promotionservice.domain.event.service;

import com.dailyon.promotionservice.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService {
    private EventRepository eventRepository;
}
