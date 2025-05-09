package com.redis.om.spring.vectorize;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.redis.om.spring.RedisOMAiProperties;
import com.redis.om.spring.annotations.Vectorize;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EmbeddingModelFactory {
    private final RedisOMAiProperties properties;
    private final SpringAiProperties springAiProperties;

    private final Map<String, Object> modelCache = new ConcurrentHashMap<>();

    public EmbeddingModelFactory(RedisOMAiProperties properties, SpringAiProperties springAiProperties) {
        this.properties = properties;
        this.springAiProperties = springAiProperties;
    }

    /**
     * Generates a cache key for a model based on its type and parameters
     * @param modelType The type of the model
     * @param params Parameters that uniquely identify the model configuration
     * @return A string key for caching
     */
    private String generateCacheKey(String modelType, String... params) {
        StringBuilder keyBuilder = new StringBuilder(modelType);
        for (String param : params) {
            keyBuilder.append(":").append(param);
        }
        return keyBuilder.toString();
    }

    /**
     * Clears the model cache, forcing new models to be created on next request.
     * This can be useful when configuration changes or to free up resources.
     */
    public void clearCache() {
        modelCache.clear();
    }

    /**
     * Removes a specific model from the cache.
     * 
     * @param modelType The type of the model (e.g., "openai", "transformers")
     * @param params Parameters that were used to create the model
     * @return true if a model was removed, false otherwise
     */
    public boolean removeFromCache(String modelType, String... params) {
        String cacheKey = generateCacheKey(modelType, params);
        return modelCache.remove(cacheKey) != null;
    }

    /**
     * Returns the current number of models in the cache.
     * 
     * @return The number of cached models
     */
    public int getCacheSize() {
        return modelCache.size();
    }

    public TransformersEmbeddingModel createTransformersEmbeddingModel(Vectorize vectorize) {
        String cacheKey = generateCacheKey("transformers",
            vectorize.transformersModel(), 
            vectorize.transformersTokenizer(), 
            vectorize.transformersResourceCacheConfiguration(),
            String.join(",", vectorize.transformersTokenizerOptions()));

        TransformersEmbeddingModel cachedModel = (TransformersEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();

        if (!vectorize.transformersModel().isEmpty()) {
            embeddingModel.setModelResource(vectorize.transformersModel());
        }

        if (!vectorize.transformersTokenizer().isEmpty()) {
            embeddingModel.setTokenizerResource(vectorize.transformersTokenizer());
        }

        if (!vectorize.transformersResourceCacheConfiguration().isEmpty()) {
            embeddingModel.setResourceCacheDirectory(vectorize.transformersResourceCacheConfiguration());
        }

        if (vectorize.transformersTokenizerOptions().length > 0) {
            Map<String, String> options = Arrays.stream(vectorize.transformersTokenizerOptions())
                    .map(entry -> entry.split("=", 2))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
            embeddingModel.setTokenizerOptions(options);
        }

        try {
            embeddingModel.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TransformersEmbeddingModel", e);
        }

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }

    public OpenAiEmbeddingModel createOpenAiEmbeddingModel(EmbeddingModel model) {
        return createOpenAiEmbeddingModel(model.value);
    }

    public OpenAiEmbeddingModel createOpenAiEmbeddingModel(String model) {
        String cacheKey = generateCacheKey("openai", model, properties.getOpenAi().getApiKey());
        OpenAiEmbeddingModel cachedModel = (OpenAiEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        String apiKey = properties.getOpenAi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            apiKey = springAiProperties.getOpenai().getApiKey();
            properties.getOpenAi().setApiKey(apiKey);
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(properties.getOpenAi().getResponseTimeOut()));

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(properties.getOpenAi().getApiKey())
                .restClientBuilder(RestClient.builder().requestFactory(factory))
                .build();

        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(model)
                        .build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE
        );

        modelCache.put(cacheKey, embeddingModel);
        return embeddingModel;
    }

    private OpenAIClient getOpenAIClient() {
        OpenAIClientBuilder builder = new OpenAIClientBuilder();
        if (properties.getAzure().getEntraId().isEnabled()) {
            builder.credential(new DefaultAzureCredentialBuilder().tenantId(properties.getAzure().getEntraId().getTenantId()).build())
                    .endpoint(properties.getAzure().getEntraId().getEndpoint());
        } else {
            builder.credential(new AzureKeyCredential(properties.getAzure().getOpenAi().getApiKey()))
                    .endpoint(properties.getAzure().getOpenAi().getEndpoint());
        }
        return builder.buildClient();
    }

    public AzureOpenAiEmbeddingModel createAzureOpenAiEmbeddingModel(String deploymentName) {
        String cacheKey = generateCacheKey("azure-openai",
            deploymentName, 
            properties.getAzure().getOpenAi().getApiKey(),
            properties.getAzure().getOpenAi().getEndpoint(),
            String.valueOf(properties.getAzure().getEntraId().isEnabled()));

        AzureOpenAiEmbeddingModel cachedModel = (AzureOpenAiEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        String apiKey = properties.getAzure().getOpenAi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            apiKey = springAiProperties.getAzure().getApiKey(); // Fallback to Spring AI property
            properties.getAzure().getOpenAi().setApiKey(apiKey);
        }

        String endpoint = properties.getAzure().getOpenAi().getEndpoint();
        if (!StringUtils.hasText(endpoint)) {
            endpoint = springAiProperties.getAzure().getEndpoint(); // Fallback to Spring AI property
            properties.getAzure().getOpenAi().setEndpoint(endpoint);
        }

        OpenAIClient openAIClient = getOpenAIClient();

        AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
                .deploymentName(deploymentName)
                .build();

        AzureOpenAiEmbeddingModel embeddingModel = new AzureOpenAiEmbeddingModel(openAIClient, MetadataMode.EMBED, options);

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }

    public VertexAiTextEmbeddingModel createVertexAiTextEmbeddingModel(String model) {
        String cacheKey = generateCacheKey("vertex-ai",
            model, 
            properties.getVertexAi().getApiKey(),
            properties.getVertexAi().getEndpoint(),
            properties.getVertexAi().getProjectId(),
            properties.getVertexAi().getLocation());

        VertexAiTextEmbeddingModel cachedModel = (VertexAiTextEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        String apiKey = properties.getVertexAi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            apiKey = springAiProperties.getVertexAi().getApiKey(); // Fallback to Spring AI property
            if (!StringUtils.hasText(apiKey)) {
                apiKey = System.getenv("VERTEX_AI_API_KEY"); // Fallback to environment variable

                if (!StringUtils.hasText(apiKey)) {
                    apiKey = System.getProperty("SPRING_AI_VERTEX_AI_API_KEY"); // Fallback to system property
                }
            }
            properties.getVertexAi().setApiKey(apiKey);
        }

        String baseUrl = properties.getVertexAi().getEndpoint();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = springAiProperties.getVertexAi().getEndpoint();
            properties.getVertexAi().setEndpoint(baseUrl);
        }

        String projectId = properties.getVertexAi().getProjectId();
        if (!StringUtils.hasText(projectId)) {
            projectId = springAiProperties.getVertexAi().getProjectId(); // Fallback to Spring AI property
            properties.getVertexAi().setProjectId(projectId);
        }

        String location = properties.getVertexAi().getLocation();
        if (!StringUtils.hasText(location)) {
            location = springAiProperties.getVertexAi().getLocation(); // Fallback to Spring AI property
            properties.getVertexAi().setLocation(location);
        }

        VertexAiEmbeddingConnectionDetails connectionDetails = VertexAiEmbeddingConnectionDetails.builder()
                .projectId(properties.getVertexAi().getProjectId())
                .location(properties.getVertexAi().getLocation())
                .apiEndpoint(properties.getVertexAi().getEndpoint())
                .build();

        VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
                .model(model)
                .build();

        VertexAiTextEmbeddingModel embeddingModel = new VertexAiTextEmbeddingModel(connectionDetails, options);

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }

    public OllamaEmbeddingModel createOllamaEmbeddingModel(String model) {
        String cacheKey = generateCacheKey("ollama", 
            model, 
            properties.getOllama().getBaseUrl());

        OllamaEmbeddingModel cachedModel = (OllamaEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        OllamaApi api = new OllamaApi(properties.getOllama().getBaseUrl());

        OllamaOptions options = OllamaOptions.builder()
                .model(model)
                .truncate(false)
                .build();

        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .ollamaApi(api)
                .defaultOptions(options)
                .build();

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }

    private AwsCredentials getAwsCredentials() {
        String accessKey = properties.getAws().getAccessKey();
        if (!StringUtils.hasText(accessKey)) {
            accessKey = springAiProperties.getBedrock().getAws().getAccessKey(); // Fallback to Spring AI property
            properties.getAws().setAccessKey(accessKey);
        }

        String secretKet = properties.getAws().getSecretKey();
        if (!StringUtils.hasText(secretKet)) {
            secretKet = springAiProperties.getBedrock().getAws().getSecretKey(); // Fallback to Spring AI property
            properties.getAws().setSecretKey(secretKet);
        }

        String region = properties.getAws().getRegion();
        if (!StringUtils.hasText(region)) {
            region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
            properties.getAws().setRegion(region);
        }

        return AwsBasicCredentials.create(
                properties.getAws().getAccessKey(),
                properties.getAws().getSecretKey()
        );
    }

    public BedrockCohereEmbeddingModel createCohereEmbeddingModel(String model) {
        String cacheKey = generateCacheKey("bedrock-cohere",
            model, 
            properties.getAws().getAccessKey(),
            properties.getAws().getSecretKey(),
            properties.getAws().getRegion(),
            String.valueOf(properties.getAws().getBedrockCohere().getResponseTimeOut()));

        BedrockCohereEmbeddingModel cachedModel = (BedrockCohereEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        String region = properties.getAws().getRegion();
        if (!StringUtils.hasText(region)) {
            region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
            properties.getAws().setRegion(region);
        }

        var cohereEmbeddingApi = new CohereEmbeddingBedrockApi(
                model,
                StaticCredentialsProvider.create(getAwsCredentials()),
                properties.getAws().getRegion(),
                ModelOptionsUtils.OBJECT_MAPPER,
                Duration.ofMinutes(properties.getAws().getBedrockCohere().getResponseTimeOut())
        );

        BedrockCohereEmbeddingModel embeddingModel = new BedrockCohereEmbeddingModel(cohereEmbeddingApi);

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }

    public BedrockTitanEmbeddingModel createTitanEmbeddingModel(String model) {
        // Generate a cache key based on the model parameters
        String cacheKey = generateCacheKey("bedrock-titan", 
            model, 
            properties.getAws().getAccessKey(),
            properties.getAws().getSecretKey(),
            properties.getAws().getRegion(),
            String.valueOf(properties.getAws().getBedrockTitan().getResponseTimeOut()));

        BedrockTitanEmbeddingModel cachedModel = (BedrockTitanEmbeddingModel) modelCache.get(cacheKey);

        if (cachedModel != null) {
            return cachedModel;
        }

        String region = properties.getAws().getRegion();
        if (!StringUtils.hasText(region)) {
            region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
            properties.getAws().setRegion(region);
        }

        var titanEmbeddingApi = new TitanEmbeddingBedrockApi(
                model,
                StaticCredentialsProvider.create(getAwsCredentials()),
                properties.getAws().getRegion(),
                ModelOptionsUtils.OBJECT_MAPPER,
                Duration.ofMinutes(properties.getAws().getBedrockTitan().getResponseTimeOut())
        );

        BedrockTitanEmbeddingModel embeddingModel = new BedrockTitanEmbeddingModel(titanEmbeddingApi);

        modelCache.put(cacheKey, embeddingModel);

        return embeddingModel;
    }
}
