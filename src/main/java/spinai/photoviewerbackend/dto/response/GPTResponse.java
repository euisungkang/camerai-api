package spinai.photoviewerbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GPTResponse {
    private String id;
    private String object;
    private Integer created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String system_fingerprint;

    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        private String logprobs;
        private String finish_reason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}

/* {
    "id": "chatcmpl-9RsqyY4c27YMi81mnLJHN2LIshawr",
    "object": "chat.completion",
    "created": 1716432740,
    "model": "gpt-4-turbo-2024-04-09",
    "choices": [
        {
            "index": 0,
            "message": {
                "role": "assistant",
                "content": "This image captures a beautiful natural landscape featuring a long wooden boardwalk extending through a lush green field with tall grasses. The path leads towards the horizon under a vibrant blue sky adorned with soft, white clouds. Trees and bushes are scattered throughout the field, adding variety to the greenery. The scene conveys a sense of tranquility and invites exploration, making it typical of a peaceful natural reserve or park setting. The lighting suggests it is either late afternoon or early evening, with the sun casting a warm glow that enhances the vivid colors of the environment."
            },
            "logprobs": null,
            "finish_reason": "stop"
        }
    ],
    "usage": {
        "prompt_tokens": 1118,
        "completion_tokens": 111,
        "total_tokens": 1229
    },
    "system_fingerprint": "fp_0737e0dfd9"
}
 */