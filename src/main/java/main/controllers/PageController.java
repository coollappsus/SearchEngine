package main.controllers;

import main.dto.ResultDto;
import main.dto.SearchDto;
import main.service.FoundPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PageController {

    private final FoundPageService foundPageService;

    @Autowired
    public PageController(FoundPageService foundPageService) {
        this.foundPageService = foundPageService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query,
                                 @RequestParam(required = false) String site,
                                 @RequestParam(required = false, defaultValue = "0") int offset,
                                 @RequestParam(required = false, defaultValue = "20") int limit) {
        if (query.isEmpty()) {
            return ResponseEntity.ok().body(new ResultDto("Задан пустой поисковый запрос"));
        }
        try {
            SearchDto searchDto = foundPageService.getSearchResult(query, site, offset, limit);
            return ResponseEntity.ok().body(searchDto);
        } catch (InterruptedException | IOException e) {
            return ResponseEntity.ok().body(new ResultDto("В процессе поиска произошла неизвестная ошибка"));
        }
    }
}
