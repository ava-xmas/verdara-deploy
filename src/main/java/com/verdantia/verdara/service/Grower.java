package com.verdantia.verdara.service;

import com.verdantia.verdara.dto.PixelDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Grower {
    private final int baseWidth;
    private final int leanAngle;
    private final int bendAngle;
    private final int taper;
    private final int length;
    private final int orgX;
    private final int orgY;
    private final int baseAngle;
    private List<Integer> nodeFlags = new ArrayList<>();
    private final String color;
    public static int screenWidth;
    public static int screenHeight;

    private final List<Node> nodes = new ArrayList<>();
    private final List<PixelDTO> pixels = new ArrayList<>();

    /**
     * Constructs a Grower with the specified parameters.
     *
     * @param origin     Node whose x, y, and angle map to orgX, orgY, baseAngle
     * @param leanAngle  initial deviation angle
     * @param bendAngle  total additional bending angle
     * @param taper      taper percentage (0–100)
     * @param length     total growth length in pixels
     * @param baseWidth  starting width in pixels
     * @param color      primary color (hex code)
     */
    public Grower(Node origin,
                  int length,
                  int leanAngle,
                  int bendAngle,
                  int taper,
                  String color) {
        this.orgX = origin.getX();
        this.orgY = origin.getY();
        this.baseAngle = origin.getAngle();
        this.baseWidth = origin.getBase();
        this.leanAngle = leanAngle;
        this.bendAngle = bendAngle*Integer.signum(leanAngle);
        this.taper = taper;
        this.length = length;
        this.color = color;
    }

    public void setNodeFlags(List<Integer> nodeFlags) {
        this.nodeFlags = nodeFlags;
    }

    public void makeBranchNodes(int leaf_spacing) {
        if(leaf_spacing <= 0) return;
        for(int i=0; i<=length; i+=leaf_spacing) {
            nodeFlags.add(i);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    private List<PixelDTO> getPixels() {
        Set<PixelDTO> unique = new LinkedHashSet<>(pixels);
        pixels.clear();
        pixels.addAll(unique);
        return pixels;
    }

    public List<PixelDTO> grow() {

        double startAngle = Math.toRadians(baseAngle + leanAngle);
        drawCrossSection(orgX, orgY, baseWidth, startAngle, color);

        for (int step = 0; step <= length; step++) {

            double theta = Math.toRadians(baseAngle + leanAngle)
                         + Math.toRadians(bendAngle) * (step / (double) length);

            double t = taper / 100.0;
            double factor = Math.pow(1 - t, (double) step / length);
            int currWidth = (int) Math.max(1, Math.round(baseWidth * factor));
            
            double dx = Math.cos(theta);
            double dy = Math.sin(theta);
            int cx = (int) Math.round(orgX + dx * step);
            int cy = (int) Math.round(orgY + dy * step);

            if (nodeFlags.contains(step)) {
                int angleDeg = (int) Math.round(Math.toDegrees(theta));
                nodes.add(new Node(cx, cy, angleDeg, currWidth));
            }

            drawCrossSection(cx, cy, currWidth, theta, color);
        }

        return this.getPixels();
    }


    private void drawCrossSection(int cx, int cy, int width, double theta, String shade) {

        double px = -Math.sin(theta);
        double py = Math.cos(theta);
        int halfW = width / 2;
        int halfCol = (int) Math.floor(this.screenWidth / 2);

        for (int w = -halfW; w <= halfW; w++) {
            int x = (int) Math.round(cx + px * w);
            int y = (int) Math.round(cy + py * w);
            if (x >= -halfCol && x < halfCol && y >= 0 && y < this.screenHeight) {
                PixelDTO p = new PixelDTO();
                p.setX(x);
                p.setY(y);
                p.setShade(shade);
                pixels.add(p);
            }
        }
    }
}
