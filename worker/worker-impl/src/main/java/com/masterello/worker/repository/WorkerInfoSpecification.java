package com.masterello.worker.repository;

import com.masterello.user.value.City;
import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.service.WorkerService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class WorkerInfoSpecification {

    public static Specification<WorkerInfo> byFilters(List<City> cities, List<Language> languages, List<Integer> serviceIds) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Predicate predicate = criteriaBuilder.conjunction();

            if (cities != null && !cities.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("city").in(cities));
            }

            if (serviceIds != null && !serviceIds.isEmpty()) {
                Join<WorkerInfo, WorkerService> serviceJoin = root.join("services");
                predicate = criteriaBuilder.and(predicate, serviceJoin.get("serviceId").in(serviceIds));
            }

            if (languages != null && !languages.isEmpty()) {
                Expression<Language> languageExpression = root.join("languages");
                predicate = criteriaBuilder.and(predicate, languageExpression.in(languages));
            }
            return predicate;
        };
    }
}

