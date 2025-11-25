package com.tricoq.application.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author trico qiang
 * @date 11/24/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrawGraphDTO {

    private List<NodeDTO> nodes;

    private List<EdgeDTO> edges;

    public record NodeDTO(String id, String type, String title, Map<String, String> inputsValues) {
    }

    public record EdgeDTO(String from,
                          String to,
                          String fromPort) {
    }
}
