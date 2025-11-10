package com.eflo.user.domain.repository;

import com.eflo.user.BaseRepositoryTest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.enums.BusinessUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessUnitRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    private BusinessUnit testBusinessUnit;

    @BeforeEach
    void setUp() {
        businessUnitRepository.deleteAll();

        testBusinessUnit = new BusinessUnit();
        testBusinessUnit.setCode("HQ001");
        testBusinessUnit.setName("Headquarters");
        testBusinessUnit.setType(BusinessUnitType.DEPARTMENT);
        testBusinessUnit.setRegionCode("US-EAST");
        testBusinessUnit.setCity("New York");
        testBusinessUnit.setActive(true);

        testBusinessUnit = businessUnitRepository.save(testBusinessUnit);
    }

    @Test
    void findByCode_ShouldReturnBusinessUnit_WhenExists() {
        // When
        Optional<BusinessUnit> found = businessUnitRepository.findByCode("HQ001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Headquarters");
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<BusinessUnit> found = businessUnitRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByRegionCode_ShouldReturnBusinessUnitsInRegion() {
        // Given
        BusinessUnit bu2 = new BusinessUnit();
        bu2.setCode("HQ002");
        bu2.setName("East Coast Office");
        bu2.setType(BusinessUnitType.BRANCH);
        bu2.setRegionCode("US-EAST");
        bu2.setActive(true);
        businessUnitRepository.save(bu2);

        BusinessUnit bu3 = new BusinessUnit();
        bu3.setCode("HQ003");
        bu3.setName("West Coast Office");
        bu3.setType(BusinessUnitType.BRANCH);
        bu3.setRegionCode("US-WEST");
        bu3.setActive(true);
        businessUnitRepository.save(bu3);

        // When
        List<BusinessUnit> eastCoast = businessUnitRepository.findByRegionCode("US-EAST");

        // Then
        assertThat(eastCoast).hasSize(2);
        assertThat(eastCoast).allMatch(bu -> "US-EAST".equals(bu.getRegionCode()));
    }

    @Test
    void findByType_ShouldReturnBusinessUnitsOfType() {
        // Given
        BusinessUnit branch = new BusinessUnit();
        branch.setCode("BR001");
        branch.setName("Branch Office");
        branch.setType(BusinessUnitType.BRANCH);
        branch.setActive(true);
        businessUnitRepository.save(branch);

        // When
        List<BusinessUnit> departments = businessUnitRepository.findByType(BusinessUnitType.DEPARTMENT);
        List<BusinessUnit> branches = businessUnitRepository.findByType(BusinessUnitType.BRANCH);

        // Then
        assertThat(departments).hasSize(1);
        assertThat(branches).hasSize(1);
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveBusinessUnits() {
        // Given
        BusinessUnit inactive = new BusinessUnit();
        inactive.setCode("INACTIVE");
        inactive.setName("Inactive Unit");
        inactive.setType(BusinessUnitType.DEPARTMENT);
        inactive.setActive(false);
        businessUnitRepository.save(inactive);

        // When
        List<BusinessUnit> active = businessUnitRepository.findByIsActiveTrue();

        // Then
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getCode()).isEqualTo("HQ001");
    }

    @Test
    void findActiveByType_ShouldReturnOnlyActiveBusinessUnitsOfType() {
        // Given
        BusinessUnit inactiveDept = new BusinessUnit();
        inactiveDept.setCode("DEPT002");
        inactiveDept.setName("Inactive Department");
        inactiveDept.setType(BusinessUnitType.DEPARTMENT);
        inactiveDept.setActive(false);
        businessUnitRepository.save(inactiveDept);

        // When
        List<BusinessUnit> activeDepts = businessUnitRepository.findActiveByType(BusinessUnitType.DEPARTMENT);

        // Then
        assertThat(activeDepts).hasSize(1);
        assertThat(activeDepts.get(0).getCode()).isEqualTo("HQ001");
    }

    @Test
    void findActiveByRegionCode_ShouldReturnOnlyActiveBusinessUnitsInRegion() {
        // Given
        BusinessUnit inactive = new BusinessUnit();
        inactive.setCode("INACTIVE");
        inactive.setName("Inactive East");
        inactive.setType(BusinessUnitType.DEPARTMENT);
        inactive.setRegionCode("US-EAST");
        inactive.setActive(false);
        businessUnitRepository.save(inactive);

        // When
        List<BusinessUnit> activeEast = businessUnitRepository.findActiveByRegionCode("US-EAST");

        // Then
        assertThat(activeEast).hasSize(1);
        assertThat(activeEast.get(0).getCode()).isEqualTo("HQ001");
    }

    @Test
    void searchBusinessUnits_ShouldFindByVariousCriteria() {
        // When - Search by name
        List<BusinessUnit> byName = businessUnitRepository.searchBusinessUnits("head");

        // Then
        assertThat(byName).hasSize(1);
        assertThat(byName.get(0).getName()).isEqualTo("Headquarters");

        // When - Search by code
        List<BusinessUnit> byCode = businessUnitRepository.searchBusinessUnits("HQ001");

        // Then
        assertThat(byCode).hasSize(1);

        // When - Search by city
        List<BusinessUnit> byCity = businessUnitRepository.searchBusinessUnits("york");

        // Then
        assertThat(byCity).hasSize(1);
    }
}
