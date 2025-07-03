package com.verdantia.verdara.service;

import com.verdantia.verdara.dto.PixelDTO;
import com.verdantia.verdara.dto.PlantFeaturesDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Leafer {
    
    private PlantFeaturesDTO.LeafDTO leaf;
    private int side;
    private final List<PixelDTO> pixels = new ArrayList<>();

    public Leafer(PlantFeaturesDTO.LeafDTO leafFeatures, int side) {
        this.leaf = leafFeatures;
        this.side = side;
    }

    public List<PixelDTO> attach(Node node) {
        List<PixelDTO> designedLeaf = design();
        List<PixelDTO> rotatedLeaf = rotate(designedLeaf, node, this.side);
        List<PixelDTO> rotatedLeaf2 = rotate(designedLeaf, node, -this.side);
        List<PixelDTO> relocate = relocate(rotatedLeaf, node);
        List<PixelDTO> relocate2 = relocate(rotatedLeaf2, node);
        if(!this.leaf.getLeaf_alternate()) relocate.addAll(relocate2);
        return relocate;
    }

    public List<PixelDTO> design() {
        List<PixelDTO> leafPixels = new ArrayList<>();
        
        int width = leaf.getLeaf_width();
        int height = leaf.getLeaf_height();
        int marginWidth = leaf.getMargin();
        int veinWidth = leaf.getVein();
        String leafColor = leaf.getLeaf_color();
        String marginColor = leaf.getMargin_color();
        String veinColor = leaf.getVein_color();
        
        int halfWidth = width / 2;
        
        // Generate leaf pixels row by row from bottom (y=0) to top (y=height)
        for (int y = 0; y <= height; y++) {
            // Calculate leaf boundary at this y level using leaf shape function
            double t = (double) y / height;
            double widthFactor;
            
            if (t <= 0.5) {
                // Bottom half - narrowing to point
                widthFactor = Math.sin(t * Math.PI) * 0.8 + 0.2;
            } else {
                // Top half - narrowing to point
                widthFactor = Math.sin((1 - t) * Math.PI) * 0.8 + 0.2;
            }
            
            int currentHalfWidth = (int) Math.round(halfWidth * widthFactor);
            
            // Handle pointed ends
            if (currentHalfWidth <= 0) {
                PixelDTO pixel = new PixelDTO();
                pixel.setX(0);
                pixel.setY(y);
                pixel.setShade(leafColor);
                leafPixels.add(pixel);
                continue;
            }
            
            // Fill the row with appropriate colors
            for (int x = -currentHalfWidth; x <= currentHalfWidth; x++) {
                String color = leafColor; // Default color
                
                // Check if pixel is in margin area (near edge)
                int distanceFromEdge = Math.min(
                    Math.abs(x + currentHalfWidth), 
                    Math.abs(currentHalfWidth - x)
                );
                if (distanceFromEdge < marginWidth) {
                    color = marginColor;
                }
                
                // Check if pixel is in central vein (overrides margin)
                if (Math.abs(x) <= veinWidth / 2) {
                    color = veinColor;
                }
                
                PixelDTO pixel = new PixelDTO();
                pixel.setX(x);
                pixel.setY(y);
                pixel.setShade(color);
                leafPixels.add(pixel);
            }
        }
        
        return leafPixels;
    }

    /**
     * Rotate the leaf about the origin by the angle of node.angle counterclockwise
     */
    public List<PixelDTO> rotate(List<PixelDTO> leafPixels, Node node, int side) {
        node = node.rotate(-90);
        int leafAngle = leaf.getLeaf_angle()*side;
        List<PixelDTO> rotatedPixels = new ArrayList<>();
        double angleRad = Math.toRadians(node.getAngle() + leafAngle);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        
        for (PixelDTO pixel : leafPixels) {
            int x = pixel.getX();
            int y = pixel.getY();
            
            // Rotate counterclockwise
            int newX = (int) Math.round(x * cosA - y * sinA);
            int newY = (int) Math.round(x * sinA + y * cosA);
            
            PixelDTO rotatedPixel = new PixelDTO();
            rotatedPixel.setX(newX);
            rotatedPixel.setY(newY);
            rotatedPixel.setShade(pixel.getShade());
            rotatedPixels.add(rotatedPixel);
        }
        
        return rotatedPixels;
    }

    /**
     * Shift the bottom tip of the leaf from origin to node.x and node.y
     */
    public List<PixelDTO> relocate(List<PixelDTO> leafPixels, Node node) {
        List<PixelDTO> relocatedPixels = new ArrayList<>();
        
        for (PixelDTO pixel : leafPixels) {
            PixelDTO relocatedPixel = new PixelDTO();
            relocatedPixel.setX(pixel.getX() + node.getX());
            relocatedPixel.setY(pixel.getY() + node.getY());
            relocatedPixel.setShade(pixel.getShade());
            relocatedPixels.add(relocatedPixel);
        }
        
        return relocatedPixels;
    }

    /**
     * Alternative rotate method that operates on the internal pixels list
     */
    public List<PixelDTO> rotate(Node node, int side) {
        return rotate(new ArrayList<>(pixels), node, side);
    }

    /**
     * Alternative relocate method that operates on the internal pixels list
     */
    public List<PixelDTO> relocate(Node node) {
        return relocate(new ArrayList<>(pixels), node);
    }

    public List<PixelDTO> getPixels() {
        Set<String> seen = new LinkedHashSet<>();
        List<PixelDTO> unique = new ArrayList<>();
        
        for (PixelDTO pixel : pixels) {
            String key = pixel.getX() + "," + pixel.getY();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(pixel);
            }
        }
        
        return unique;
    }
}
