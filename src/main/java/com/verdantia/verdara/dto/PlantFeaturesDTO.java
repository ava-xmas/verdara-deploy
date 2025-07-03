package com.verdantia.verdara.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlantFeaturesDTO {
    private StemDTO stem;
    private LeafDTO leaf;
    private int screen_width;
    private int screen_height;
    private int age;

    @Data
    public static class StemDTO {
        private Integer trunk_height; // 0 - 400
        private Integer trunk_width;
        private List<Integer> splits; // 0 - 400
        private List<Integer> split_angles; // -90 - 90
        private List<Integer> split_lengths; // 0 - 300
        private Integer split_bending; // 0 - 90
        private List<Integer> branch_locations; // 0 - 300
        private List<Integer> branch_angles; // -90 - 90
        private List<Integer> branch_lengths; // 0 - 300
        private Integer taper; // 0 - 100 (%)
        private String stem_color; // hex color code
        private String branch_color; // hex color code
    }

    @Data
    public static class LeafDTO {
        private Integer leaf_angle; // -90 - 90
        private Integer leaf_width; // 0 - 100
        private Integer leaf_height; // 0 - 100
        private Integer leaf_spacing;
        private Integer margin; // 0 - 10
        private Integer vein; // 0 - 10
        private Boolean leaf_alternate;
        private String margin_color; // hex color code
        private String leaf_color; // hex color code
        private String vein_color; // hex color code
    }
}
