package com.newmarket.area;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    @PostConstruct
    public void initAreaData() throws IOException {
        if (areaRepository.count() == 0) {
            Resource resource = new ClassPathResource("area_korea.csv");
            List<Area> areaList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Area.builder()
                                .cityOrProvince(split[0])
                                .cityOrCountryOrDistrict(split[1])
                                .townOrTownshipOrNeighborhood(split[2])
                                .build();
                    }).collect(Collectors.toList());
            areaRepository.saveAll(areaList);
        }
    }

}
