package com.dailyon.promotionservice.domain.event.api;


import com.dailyon.promotionservice.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
public class EventApiController {
    private final EventService eventService;

}
