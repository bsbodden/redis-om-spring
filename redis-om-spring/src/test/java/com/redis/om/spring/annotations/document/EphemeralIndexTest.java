package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.annotations.hash.fixtures.Company;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

class EphemeralIndexTest extends AbstractBaseDocumentTest {
  @Autowired
  PatientRepository patientRepository;

  @Autowired
  DoctorVisitRepository doctorVisitRepository;

  @Autowired
  EntityStream entityStream;

  @Autowired
  RediSearchIndexer indexer;

  @BeforeEach
  void prepare() {
    patientRepository.deleteAll();
    doctorVisitRepository.deleteAll();

    var nicholas = Patient.of("nicholas_angel", "Angel, Nicholas");
    var danny = Patient.of("danny_butterman", "Butterman, Danny");
    var simon = Patient.of("simon_skinner", "Skinner, Simon");
    var doris = Patient.of("doris_thatcher", "Thatcher, Doris");
    var andyWainwright = Patient.of("andy_wainwright", "Wainwright, Andy");
    var andyCartwright = Patient.of("andy_cartwright", "Cartwright, Andy");
    var bob = Patient.of("bob_walker", "Walker, Bob");
    var joyce = Patient.of("joyce_cooper", "Cooper, Joyce");
    var frank = Patient.of("frank_butterman", "Butterman, Frank");

    patientRepository.saveAll(List.of(nicholas, danny, simon, doris, andyWainwright, andyCartwright, bob, joyce, frank));

    var visit1 = DoctorVisit.of(nicholas, LocalDate.of(2020, 1, 10), "Swan bite");
    var visit2 = DoctorVisit.of(danny, LocalDate.of(2020, 2, 15), "Model village injury");
    var visit3 = DoctorVisit.of(simon, LocalDate.of(2020, 3, 20), "Slipped on a banana peel");
    var visit4 = DoctorVisit.of(doris, LocalDate.of(2020, 4, 25), "Sprained ankle during pursuit");
    var visit5 = DoctorVisit.of(andyWainwright, LocalDate.of(2020, 5, 30), "Bee sting");
    var visit6 = DoctorVisit.of(andyCartwright, LocalDate.of(2020, 6, 10), "Sunburn");
    var visit7 = DoctorVisit.of(bob, LocalDate.of(2020, 7, 15), "Paper cut");
    var visit8 = DoctorVisit.of(joyce, LocalDate.of(2020, 8, 20), "Mystery ailment");
    var visit9 = DoctorVisit.of(frank, LocalDate.of(2020, 9, 25), "Tennis elbow");
    var visit10 = DoctorVisit.of(nicholas, LocalDate.of(2020, 10, 30), "Stubbed toe");
    var visit11 = DoctorVisit.of(danny, LocalDate.of(2020, 11, 10), "Caught a cold");
    var visit12 = DoctorVisit.of(simon, LocalDate.of(2020, 12, 15), "Allergic reaction to a plant");
    var visit13 = DoctorVisit.of(doris, LocalDate.of(2021, 1, 20), "Chased by a swan");
    var visit14 = DoctorVisit.of(andyWainwright, LocalDate.of(2021, 2, 25), "Accidental fall");
    var visit15 = DoctorVisit.of(andyCartwright, LocalDate.of(2021, 3, 10), "Food poisoning");
    var visit16 = DoctorVisit.of(bob, LocalDate.of(2021, 4, 15), "Dog bite");
    var visit17 = DoctorVisit.of(joyce, LocalDate.of(2021, 5, 20), "Bad back");
    var visit18 = DoctorVisit.of(frank, LocalDate.of(2021, 6, 25), "Golf injury");
    var visit19 = DoctorVisit.of(nicholas, LocalDate.of(2021, 7, 30), "Nosebleed");
    var visit20 = DoctorVisit.of(danny, LocalDate.of(2021, 8, 10), "Bumped head");

    doctorVisitRepository.saveAll(List.of(visit1, visit2, visit3, visit4, visit5, visit6, visit7, visit8, visit9, visit10,
        visit11, visit12, visit13, visit14, visit15, visit16, visit17, visit18, visit19, visit20));
  }

  @Test
  void testTemporaryIndexCreation() {
    String tempIndexName = "Nicolas Visits";
    String filter = "@patient=='com.redis.om.spring.annotations.document.fixtures.Patient:nicholas_angel'";
    indexer.createTemporaryIndexFor(DoctorVisit.class, tempIndexName, filter, Duration.ofSeconds(5));

    assertThat(indexer.getIndexInfo(tempIndexName)).isNotNull();

    // wait for the index to destroyed
//    with() //
//      .await("index removed")
//      .atMost(Duration.ofMinutes(1))
//      .until(() -> indexer.getIndexInfo(tempIndexName) == null);

    assertThat(indexer.getIndexInfo(tempIndexName)).isNull();
  }

  @Test
  void testTemporaryIndexWithEntityStream() {
    String tempIndexName = "Nicolas Visits 2";
    String filter = "@patient=='com.redis.om.spring.annotations.document.fixtures.Patient:nicholas_angel'";
    indexer.createTemporaryIndexFor(DoctorVisit.class, tempIndexName, filter, Duration.ofSeconds(15));

    List<DoctorVisit> visits;
    try (SearchStream<DoctorVisit> stream = entityStream.of(DoctorVisit.class, tempIndexName)) {
      visits = stream.collect(Collectors.toList());
    }

    assertAll( //
      () -> assertThat(visits).hasSize(3), //
      () -> assertThat(visits).extracting("patient.name").allSatisfy(v -> assertThat(v).isEqualTo("Angel, Nicholas")) //
    );
  }

}
