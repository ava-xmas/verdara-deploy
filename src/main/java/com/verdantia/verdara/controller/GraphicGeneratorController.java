package com.verdantia.verdara.controller;

import com.verdantia.verdara.dto.PlantFeaturesDTO;
import com.verdantia.verdara.dto.PixelDTO;
import com.verdantia.verdara.service.GraphicGeneratorService;
import com.verdantia.verdara.service.DecoderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GraphicGeneratorController {

    private final GraphicGeneratorService graphicGeneratorService;
    private final DecoderService decoderService;

    @PostMapping("/generate")
    public ResponseEntity<List<PixelDTO>> generate(@RequestBody PlantFeaturesDTO features) {
        List<PixelDTO> pixels = graphicGeneratorService.generate(features);
        return ResponseEntity.ok(pixels);
    }

    @PostMapping(
        path = "/prompt",
        consumes = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<List<PixelDTO>> promptBasedGeneration(@RequestBody String prompt) {
        PlantFeaturesDTO features = decoderService.decode(prompt);
        features = decoderService.scaleFeatures(features, 1);
        features = decoderService.ageFeatures(features, features.getAge());
        List<PixelDTO> pixels = graphicGeneratorService.generate(features);
        return ResponseEntity.ok(pixels);
    }
}
