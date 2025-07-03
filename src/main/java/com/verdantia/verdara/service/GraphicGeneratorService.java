package com.verdantia.verdara.service;

import com.verdantia.verdara.dto.PlantFeaturesDTO;
import com.verdantia.verdara.service.Grower;
import com.verdantia.verdara.dto.PixelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphicGeneratorService {

    public List<PixelDTO> generate(PlantFeaturesDTO features) {
        Grower.screenWidth = features.getScreen_width();
        Grower.screenHeight = features.getScreen_height();
        List<PixelDTO> pixels = new ArrayList<>();

        pixels = growStem(features);

        return pixels;
    }

    private List<PixelDTO> growStem(PlantFeaturesDTO features) {

        int base = features.getStem().getTrunk_width();
        int length = features.getStem().getTrunk_height();

        int leanAngle = 0;
        int bendAngle = 0;
        int taper = features.getStem().getTaper();
        String color = features.getStem().getStem_color();

        Node origin = new Node(0,0,90,base);

        com.verdantia.verdara.service.Grower grower = new Grower(origin, length, leanAngle, bendAngle, taper, color);

        grower.setNodeFlags(features.getStem().getSplits());

        List<PixelDTO> pixels = grower.grow();

        List<Node> nodes = grower.getNodes();
        for(int i=0; i<nodes.size(); i++) {
            pixels.addAll(growSplit(features, nodes.get(i), i));
        }

        return pixels;
    }

    private List<PixelDTO> growSplit(PlantFeaturesDTO features, Node node, int i) {

        int leanAngle = features.getStem().getSplit_angles().get(i);
        int bendAngle = features.getStem().getSplit_bending();
        int length = features.getStem().getSplit_lengths().get(i);
        int taper = features.getStem().getTaper();
        String color = features.getStem().getStem_color();

        Grower grower = new Grower(node, length, leanAngle, bendAngle, taper, color);

        grower.setNodeFlags(features.getStem().getBranch_locations());

        List<PixelDTO> pixels = grower.grow();

        List<Node> nodes = grower.getNodes();
        for(int j=0; j<nodes.size(); j++) {
            pixels.addAll(growBranch(features, nodes.get(j), j));
        }

        return pixels;
    }

    private List<PixelDTO> growBranch(PlantFeaturesDTO features, Node node, int i) {

        int leanAngle = features.getStem().getBranch_angles().get(i);
        int bendAngle = 0;
        int length = features.getStem().getBranch_lengths().get(i);
        int taper = features.getStem().getTaper();
        String color = features.getStem().getStem_color();

        Grower grower = new Grower(node, length, leanAngle, bendAngle, taper, color);

        grower.makeBranchNodes(features.getLeaf().getLeaf_spacing());

        List<PixelDTO> pixels = grower.grow();

        List<Node> nodes = grower.getNodes();
        for(int j=0; j<nodes.size(); j++) {
            Leafer leaf = new Leafer(features.getLeaf(), j%2==0?1:-1);
            pixels.addAll(leaf.attach(nodes.get(j)));
        }

        return pixels;
    }
}
