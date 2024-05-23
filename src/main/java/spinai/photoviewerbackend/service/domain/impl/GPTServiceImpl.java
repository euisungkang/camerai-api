package spinai.photoviewerbackend.service.domain.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spinai.photoviewerbackend.dto.response.GPTResponse;
import spinai.photoviewerbackend.exception.RESTResponseException;
import spinai.photoviewerbackend.exception.ResponseProcessingException;
import spinai.photoviewerbackend.service.domain.GPTService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GPTServiceImpl implements GPTService {

    @Value("${openai.api.key}")
    private String openAIApiKey;

    @Value("${openai.gpt.4.url}")
    private String openAIUrl;

    @Value("${openai.organization.id}")
    private String organizationId;

    @Value("${openai.user.prompt}")
    private String userPrompt;

    public String analyzeImageDescription(String image_url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("OpenAI-Organization", organizationId);
        headers.setBearerAuth(openAIApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4-turbo");
        body.put("messages", new Object[]{
                Map.of("role", "user",
                        "content", List.of(
                                Map.of(
                                        "type", "text",
                                        "text", String.format(userPrompt)
                                ),
                                Map.of(
                                        "type", "image_url",
                                        "image_url", Map.of("url", image_url)
                                )
                        )
                )
        });

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<GPTResponse> response = restTemplate.postForEntity(openAIUrl, requestEntity, GPTResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return mapResponseToString(response.getBody())
                    .orElseThrow(() -> new ResponseProcessingException("GPT-4: Error processing JSON response during deserialization"));
        } else {
            throw new RESTResponseException("Error connecting to GPT-4: " + response.getStatusCode());
        }
    }

    private Optional<String> mapResponseToString(GPTResponse response) {
        if (response != null && response.getChoices() != null) {
            GPTResponse.Choice choice = response.getChoices().getFirst();

            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                String text = choice.getMessage().getContent();

                return Optional.of(text);
            }
        }

        return Optional.empty();
    }
}
