package com.weekend.frmljslt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class FeatureConfig {

    private FeatureConfig() {
    }

    public static FeatureConfig Initialize() {
        if(featureConfig == null) {
            try {
                featureConfig = loadFeatureConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return featureConfig;
    }

    public static FeatureConfig loadFeatureConfig() throws IOException {

        Path file = ResourceUtils.getFile("classpath:featureConfig.json").toPath();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file.toFile(), FeatureConfig.class);
    }


    private static FeatureConfig featureConfig;
    private Integer id;
    private String name;
    private List<Transform> transforms = new ArrayList<>();

    @Data
    static class Transform {
        private String name;
        private String jsltExpression;
    }
}
