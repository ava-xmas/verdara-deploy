package com.verdantia.verdara.controller;

import com.verdantia.verdara.dto.PlantFeaturesDTO;
import com.verdantia.verdara.dto.PixelDTO;
import com.verdantia.verdara.service.GraphicGeneratorService;
import com.verdantia.verdara.service.DecoderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public void promptBasedGeneration(
            @RequestBody String prompt,
            HttpServletResponse response
    ) throws IOException {
        PlantFeaturesDTO features = decoderService.decode(prompt);
        features = decoderService.scaleFeatures(features, 1);
        features = decoderService.ageFeatures(features, features.getAge());
        List<PixelDTO> pixels = graphicGeneratorService.generate(features);

        BufferedImage image = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);

        for (PixelDTO pixel : pixels) {
            int x = pixel.getX();
            int y = pixel.getY();
            int imgX = x + 150;
            int imgY = 399 - y;
            if (imgX >= 0 && imgX < 300 && imgY >= 0 && imgY < 400) {
                Color color = Color.decode(pixel.getShade());
                int argb = (0xFF << 24) | (color.getRGB() & 0xFFFFFF);
                image.setRGB(imgX, imgY, argb);
            }
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "inline; filename=\"tree.png\"");
        ImageIO.write(image, "png", response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
}
