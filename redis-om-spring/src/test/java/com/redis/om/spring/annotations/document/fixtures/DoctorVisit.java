package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document
public class DoctorVisit {
  @Id
  private String id;

  @Reference
  @Indexed
  @NonNull
  private Patient patient;

  @Indexed
  @NonNull
  private LocalDate date;

  @Searchable
  @NonNull
  private String description;
}
