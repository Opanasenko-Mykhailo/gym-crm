package com.gcc.app.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true, schema = "PUBLIC")
public abstract class AbstractRepositoryTest {
}