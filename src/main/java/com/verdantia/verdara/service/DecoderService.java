package com.verdantia.verdara.service;

import com.verdantia.verdara.dto.PlantFeaturesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class DecoderService {

    private static final Pattern SPLIT_PATTERN  = Pattern.compile("^stem-(\\d+)-(-?\\d+)-(\\d+)$");
    private static final Pattern BRANCH_PATTERN = Pattern.compile("^branch-(\\d+)-(-?\\d+)-(\\d+)$");

    private String addPresets(String prompt) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("com/verdantia/verdara/data/presets.json");
            if (is == null) throw new RuntimeException("presets.json not found");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> presets = mapper.readValue(is, Map.class);
            return Arrays.stream(prompt.split("\\s+"))
                .map(word -> presets.getOrDefault(word, word))
                .collect(Collectors.joining(" "));
        } catch (Exception e) {
            throw new RuntimeException("Failed to process presets", e);
        }
    }

    public PlantFeaturesDTO decode(String prompt) {
        PlantFeaturesDTO cfg = new PlantFeaturesDTO();

        cfg.setScreen_width(300);
        cfg.setScreen_height(400);
        cfg.setAge(100);

        PlantFeaturesDTO.StemDTO stem = new PlantFeaturesDTO.StemDTO();
        stem.setTrunk_height(0);
        stem.setTrunk_width(0);
        stem.setSplits(new ArrayList<>());
        stem.setSplit_angles(new ArrayList<>());
        stem.setSplit_lengths(new ArrayList<>());
        stem.setSplit_bending(0);
        stem.setBranch_locations(new ArrayList<>());
        stem.setBranch_angles(new ArrayList<>());
        stem.setBranch_lengths(new ArrayList<>());
        stem.setTaper(0);
        stem.setStem_color("#000000");
        stem.setBranch_color("#000000");
        cfg.setStem(stem);

        PlantFeaturesDTO.LeafDTO leaf = new PlantFeaturesDTO.LeafDTO();
        leaf.setLeaf_alternate(true);
        leaf.setLeaf_width(0);
        leaf.setLeaf_height(0);
        leaf.setMargin(0);
        leaf.setVein(0);
        leaf.setLeaf_angle(0);
        leaf.setLeaf_spacing(0);
        leaf.setMargin_color("#000000");
        leaf.setLeaf_color("#000000");
        leaf.setVein_color("#000000");
        cfg.setLeaf(leaf);

        String injectedPrompt = setDisease(addPresets(prompt));
        System.out.println(injectedPrompt);

        for (String token : injectedPrompt.trim().split("\\s+")) {
            if (token.startsWith("screen-")) {
                String[] p = token.split("-");
                if (p.length >= 3) {
                    cfg.setScreen_width(Integer.parseInt(p[1]));
                    cfg.setScreen_height(Integer.parseInt(p[2]));
                }
            } else if (token.startsWith("age-")) {
                cfg.setAge(Integer.parseInt(token.split("-")[1]));
            } else if (token.startsWith("tall-")) {
                stem.setTrunk_height(Integer.parseInt(token.split("-")[1]));
            } else if (token.startsWith("base-")) {
                stem.setTrunk_width(Integer.parseInt(token.split("-")[1]));
            } else if (token.startsWith("stem-")) {
                Matcher m = SPLIT_PATTERN.matcher(token);
                if (m.matches()) {
                    stem.getSplits().add(Integer.parseInt(m.group(1)));
                    stem.getSplit_angles().add(Integer.parseInt(m.group(2)));
                    stem.getSplit_lengths().add(Integer.parseInt(m.group(3)));
                }
            } else if (token.startsWith("stems-bendup-")) {
                stem.setSplit_bending(-Integer.parseInt(token.split("-")[2]));
            } else if (token.startsWith("stems-benddown-")) {
                stem.setSplit_bending(Integer.parseInt(token.split("-")[2]));
            } else if (token.startsWith("branch-")) {
                Matcher m = BRANCH_PATTERN.matcher(token);
                if (m.matches()) {
                    stem.getBranch_locations().add(Integer.parseInt(m.group(1)));
                    stem.getBranch_angles().add(Integer.parseInt(m.group(2)));
                    stem.getBranch_lengths().add(Integer.parseInt(m.group(3)));
                }
            } else if (token.startsWith("taper-")) {
                stem.setTaper(Integer.parseInt(token.split("-")[1]));
            } else if (token.startsWith("leaf-")) {
                String[] p = token.split("-");
                if (p.length >= 8) {
                    leaf.setLeaf_width(Integer.parseInt(p[1]));
                    leaf.setLeaf_height(Integer.parseInt(p[2]));
                    leaf.setMargin(Integer.parseInt(p[3]));
                    leaf.setVein(Integer.parseInt(p[4]));
                    leaf.setLeaf_angle(Integer.parseInt(p[5]));
                    leaf.setLeaf_spacing(Integer.parseInt(p[6]));
                    leaf.setLeaf_alternate("alt".equals(p[7]));
                }
            } else if (token.startsWith("stem#")) {
                stem.setStem_color("#" + token.substring(5));
            } else if (token.startsWith("branch#")) {
                stem.setBranch_color("#" + token.substring(7));
            } else if (token.startsWith("leaf#")) {
                leaf.setLeaf_color("#" + token.substring(5));
            } else if (token.startsWith("margin#")) {
                leaf.setMargin_color("#" + token.substring(7));
            } else if (token.startsWith("vein#")) {
                leaf.setVein_color("#" + token.substring(5));
            }
        }

        if (!stem.getSplits().isEmpty()) {
            List<SplitTriple> triples = new ArrayList<>();
            for (int i = 0; i < stem.getSplits().size(); i++) {
                triples.add(new SplitTriple(
                    stem.getSplits().get(i),
                    stem.getSplit_angles().get(i),
                    stem.getSplit_lengths().get(i)
                ));
            }
            Collections.sort(triples, Comparator.comparingInt(t -> t.loc));
            stem.getSplits().clear();
            stem.getSplit_angles().clear();
            stem.getSplit_lengths().clear();
            for (SplitTriple t : triples) {
                stem.getSplits().add(t.loc);
                stem.getSplit_angles().add(t.angle);
                stem.getSplit_lengths().add(t.len);
            }
        }

        if (!stem.getBranch_locations().isEmpty()) {
            List<SplitTriple> triples = new ArrayList<>();
            for (int i = 0; i < stem.getBranch_locations().size(); i++) {
                triples.add(new SplitTriple(
                    stem.getBranch_locations().get(i),
                    stem.getBranch_angles().get(i),
                    stem.getBranch_lengths().get(i)
                ));
            }
            Collections.sort(triples, Comparator.comparingInt(t -> t.loc));
            stem.getBranch_locations().clear();
            stem.getBranch_angles().clear();
            stem.getBranch_lengths().clear();
            for (SplitTriple t : triples) {
                stem.getBranch_locations().add(t.loc);
                stem.getBranch_angles().add(t.angle);
                stem.getBranch_lengths().add(t.len);
            }
        }

        return cfg;
    }

    private static class SplitTriple {
        int loc, angle, len;
        SplitTriple(int loc, int angle, int len) {
            this.loc = loc;
            this.angle = angle;
            this.len = len;
        }
    }

    public PlantFeaturesDTO scaleFeatures(PlantFeaturesDTO orig, int scaleFactor) {
        PlantFeaturesDTO scaled = new PlantFeaturesDTO();

        scaled.setScreen_width(orig.getScreen_width() * scaleFactor);
        scaled.setScreen_height(orig.getScreen_height() * scaleFactor);
        scaled.setAge(orig.getAge());

        PlantFeaturesDTO.StemDTO oStem = orig.getStem();
        PlantFeaturesDTO.StemDTO sStem = new PlantFeaturesDTO.StemDTO();
        sStem.setTrunk_height(oStem.getTrunk_height() * scaleFactor);
        sStem.setTrunk_width (oStem.getTrunk_width()  * scaleFactor);
        sStem.setSplits(scaleList(oStem.getSplits(), scaleFactor));
        sStem.setSplit_angles(new ArrayList<>(oStem.getSplit_angles()));
        sStem.setSplit_lengths(scaleList(oStem.getSplit_lengths(), scaleFactor));
        sStem.setSplit_bending(oStem.getSplit_bending());
        sStem.setBranch_locations(scaleList(oStem.getBranch_locations(), scaleFactor));
        sStem.setBranch_angles(new ArrayList<>(oStem.getBranch_angles()));
        sStem.setBranch_lengths(scaleList(oStem.getBranch_lengths(), scaleFactor));
        sStem.setTaper(oStem.getTaper());
        sStem.setStem_color  (oStem.getStem_color());
        sStem.setBranch_color(oStem.getBranch_color());
        scaled.setStem(sStem);

        PlantFeaturesDTO.LeafDTO oLeaf = orig.getLeaf();
        PlantFeaturesDTO.LeafDTO sLeaf = new PlantFeaturesDTO.LeafDTO();
        sLeaf.setLeaf_angle     (oLeaf.getLeaf_angle());
        sLeaf.setLeaf_width     (oLeaf.getLeaf_width()   * scaleFactor);
        sLeaf.setLeaf_height    (oLeaf.getLeaf_height()  * scaleFactor);
        sLeaf.setLeaf_spacing   (oLeaf.getLeaf_spacing() * scaleFactor);
        sLeaf.setMargin         (oLeaf.getMargin()       * scaleFactor);
        sLeaf.setVein           (oLeaf.getVein()         * scaleFactor);
        sLeaf.setLeaf_alternate (oLeaf.getLeaf_alternate());
        sLeaf.setMargin_color   (oLeaf.getMargin_color());
        sLeaf.setLeaf_color     (oLeaf.getLeaf_color());
        sLeaf.setVein_color     (oLeaf.getVein_color());
        scaled.setLeaf(sLeaf);

        return scaled;
    }

    private List<Integer> scaleList(List<Integer> input, int scale) {
        if (input == null) return Collections.emptyList();
        List<Integer> out = new ArrayList<>(input.size());
        for (Integer v : input) {
            out.add(v * scale);
        }
        return out;
    }

    public PlantFeaturesDTO ageFeatures(PlantFeaturesDTO orig, int agePercent) {
        // Clamp age to [0,100]
        agePercent = Math.max(0, Math.min(100, agePercent));
        double ageFrac = Math.log1p(agePercent) / Math.log1p(100);

        int origH = orig.getStem().getTrunk_height();
        int newH  = (int) Math.round(origH * ageFrac);

        PlantFeaturesDTO aged = new PlantFeaturesDTO();
        aged.setScreen_width(orig.getScreen_width());
        aged.setScreen_height(orig.getScreen_height());
        aged.setAge(agePercent);

        PlantFeaturesDTO.StemDTO oS = orig.getStem();
        PlantFeaturesDTO.StemDTO sS = new PlantFeaturesDTO.StemDTO();
        sS.setTrunk_height(newH);
        sS.setTrunk_width((int)Math.round(oS.getTrunk_width() * ageFrac));
        sS.setSplit_bending(oS.getSplit_bending());
        sS.setTaper(oS.getTaper());
        sS.setStem_color(oS.getStem_color());
        sS.setBranch_color(oS.getBranch_color());

        // 1) Adjust split locations and lengths
        List<SplitTriple> splitTriples = new ArrayList<>();
        int topThreshold = (int)Math.floor(origH * 0.9);

        for (int i = 0; i < oS.getSplits().size(); i++) {
            int loc0 = oS.getSplits().get(i);
            int angle = oS.getSplit_angles().get(i);
            int len0  = oS.getSplit_lengths().get(i);

            // new split location
            int locAged = loc0 >= topThreshold
                ? Math.max(0, newH - (origH - loc0))
                : loc0;

            // compute fraction for growth from 10%→100% of original
            double f;
            if (newH <= locAged) {
                // trunk shorter than split → bottom of range
                f = 0.10;
            } else {
                // interpolate between 10% at locAged and 100% at origH
                int denom = origH - locAged;
                double frac = (newH - locAged) / (double) denom;
                f = 0.10 + 0.90 * Math.min(1.0, Math.max(0.0, frac));
            }
            int lenAged = (int) Math.round(len0 * f);

            splitTriples.add(new SplitTriple(locAged, angle, lenAged));
        }
        // sort by location
        splitTriples.sort(Comparator.comparingInt(t->t.loc));
        sS.setSplits(new ArrayList<>());
        sS.setSplit_angles(new ArrayList<>());
        sS.setSplit_lengths(new ArrayList<>());
        for (SplitTriple t : splitTriples) {
            sS.getSplits()       .add(t.loc);
            sS.getSplit_angles().add(t.angle);
            sS.getSplit_lengths().add(t.len);
        }

        // 2) Adjust branch lengths (always keep ≥10% of original)
        sS.setBranch_locations(new ArrayList<>(oS.getBranch_locations()));
        sS.setBranch_angles   (new ArrayList<>(oS.getBranch_angles()));
        List<Integer> branched = new ArrayList<>();
        for (int len0 : oS.getBranch_lengths()) {
            // f = 0.10 + 0.90×ageFrac
            double f = 0.10 + 0.90 * ageFrac;
            branched.add((int)Math.round(len0 * f));
        }
        sS.setBranch_lengths(branched);

        aged.setStem(sS);
        aged.setLeaf(orig.getLeaf());  // leaves unaffected by age

        return aged;
    }

    public static String mixHexColors(String hex1, String hex2, int mix) {
        mix = Math.max(1, Math.min(mix, 100));
        int mix1 = 100 - mix;
        int mix2 = mix;
        hex1 = hex1.replace("#", "");
        hex2 = hex2.replace("#", "");
        int r1 = Integer.parseInt(hex1.substring(0, 2), 16);
        int g1 = Integer.parseInt(hex1.substring(2, 4), 16);
        int b1 = Integer.parseInt(hex1.substring(4, 6), 16);
        int r2 = Integer.parseInt(hex2.substring(0, 2), 16);
        int g2 = Integer.parseInt(hex2.substring(2, 4), 16);
        int b2 = Integer.parseInt(hex2.substring(4, 6), 16);
        int rMix = (r1 * mix1 + r2 * mix2) / 100;
        int gMix = (g1 * mix1 + g2 * mix2) / 100;
        int bMix = (b1 * mix1 + b2 * mix2) / 100;
        return String.format("#%02X%02X%02X", rMix, gMix, bMix);
    }

    public String setDisease(String prompt) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("com/verdantia/verdara/data/diseases.json");
            if (is == null) throw new RuntimeException("diseases.json not found");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> diseasePresets = mapper.readValue(is, Map.class);

            String keyPattern = diseasePresets.keySet().stream()
                .map(Pattern::quote) // Escape any special regex chars in keys
                .collect(Collectors.joining("|"));
            Pattern diseasePattern = Pattern.compile("(" + keyPattern + ")-(\\d{1,3})");

            List<String[]> diseasesFound = new ArrayList<>();
            Matcher diseaseMatcher = diseasePattern.matcher(prompt);
            while (diseaseMatcher.find()) {
                diseasesFound.add(new String[]{diseaseMatcher.group(1), diseaseMatcher.group(2)});
            }

            if (diseasesFound.isEmpty()) {
                return prompt;
            }

            String newPrompt = prompt;

            for (String[] diseaseInfo : diseasesFound) {
                String disease = diseaseInfo[0];
                int mixValue = Integer.parseInt(diseaseInfo[1]);

                String diseaseColors = diseasePresets.get(disease);
                Map<String, String> diseaseColorMap = new HashMap<>();
                for (String part : diseaseColors.split(" ")) {
                    String[] kv = part.split("#");
                    if (kv.length == 2) {
                        diseaseColorMap.put(kv[0], kv[1]);
                    }
                }

                for (String part : Arrays.asList("leaf", "margin", "vein", "stem", "branch")) {
                    Pattern colorPattern = Pattern.compile(part + "#([0-9A-Fa-f]{6})");
                    Matcher colorMatcher = colorPattern.matcher(newPrompt);
                    if (colorMatcher.find()) {
                        String currentColor = colorMatcher.group(1);
                        String diseaseColor = diseaseColorMap.get(part);
                        if (diseaseColor != null) {
                            String mixedColor = mixHexColors(currentColor, diseaseColor, mixValue);
                            newPrompt = newPrompt.replaceFirst(part + "#[0-9A-Fa-f]{6}", part + mixedColor);
                        }
                    }
                }
            }

            newPrompt = diseasePattern.matcher(newPrompt).replaceAll("");

            Pattern leafPattern = Pattern.compile("leaf-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(alt|noalt)");
            Matcher leafMatcher = leafPattern.matcher(newPrompt);

            if (leafMatcher.find() && !diseasesFound.isEmpty()) {
                String[] groups = new String[8];
                for (int i = 1; i <= 7; i++) groups[i] = leafMatcher.group(i);

                int S = Integer.parseInt(groups[6]);
                int diseaseSuffix = Integer.parseInt(diseasesFound.get(0)[1]);
                int newS = S + (int)Math.ceil(diseaseSuffix / 10.0);

                String newLeafCmd = String.format("leaf-%s-%s-%s-%s-%s-%d-%s",
                        groups[1], groups[2], groups[3], groups[4], groups[5], newS, groups[7]);
                newPrompt = leafPattern.matcher(newPrompt).replaceFirst(newLeafCmd);
            }
            newPrompt = newPrompt.replaceAll("\\s+", " ").trim();
            return newPrompt;

        } catch (Exception e) {
            throw new RuntimeException("Failed to process disease prompt", e);
        }
    }
}
