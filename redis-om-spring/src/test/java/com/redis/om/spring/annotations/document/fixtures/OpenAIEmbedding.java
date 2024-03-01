package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.DistanceMetric;
import com.redis.om.spring.VectorType;
import com.redis.om.spring.annotations.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@Document
public class OpenAIEmbedding {
  @Id
  private String id;

  @Indexed(//
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgorithm.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 1536, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private float[] textEmbedding;

  @Vectorize(destination = "textEmbedding", embeddingType = EmbeddingType.SENTENCE, provider = EmbeddingProvider.OPENAI)
  @NonNull
  private String text;
}
