package com.masterello.worker.repository;

import com.masterello.user.value.City;
import com.masterello.worker.domain.Language;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WorkerInfoCustomRepositoryImpl implements WorkerInfoCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<UUID> findWorkerIdsByFilters(List<City> cities, List<Language> languages, List<Integer> serviceIds,
                                             boolean shouldShowTestWorkers, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class); // Use Object[] to select multiple columns
        Root<WorkerInfo> root = query.from(WorkerInfo.class);
        List<String> sortedFields = getSortFields(pageable.getSort());
        List<Selection<?>> selectFields = new ArrayList<>();
        sortedFields.forEach(field -> {
            selectFields.add(root.get(field)); // Add sorting fields
        });

        query.multiselect(selectFields);

        // Build predicates
        Predicate[] predicates = getPredicates(cities, languages, serviceIds, shouldShowTestWorkers, root, cb);

        // Apply predicates
        query.where(cb.and(predicates)).distinct(true);

        // Apply sorting
        applySorting(query, root, cb, pageable);

        // Create a typed query for pagination
        TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);

        // Apply pagination
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        typedQuery.setFirstResult(pageNumber * pageSize);
        typedQuery.setMaxResults(pageSize);

        // Fetch results - return only workerId as UUID
        List<UUID> workerIds = new ArrayList<>();
        for (Object[] row : typedQuery.getResultList()) {
            workerIds.add((UUID) row[sortedFields.indexOf("workerId")]); // Extract the workerId from the result array
        }

        // Count query for total elements
        CriteriaBuilder ccb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = ccb.createQuery(Long.class);
        Root<WorkerInfo> countRoot = countQuery.from(WorkerInfo.class);
        var countPredicates = getPredicates(cities, languages, serviceIds, shouldShowTestWorkers, countRoot, ccb);
        countQuery.select(ccb.countDistinct(countRoot)).where(ccb.and(countPredicates));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(workerIds, pageable, totalElements);
    }

    private List<String> getSortFields(Sort sort) {
        ArrayList<String> sortFields = sort.stream().map(Sort.Order::getProperty).collect(Collectors.toCollection(ArrayList::new));
        if (!sortFields.contains("workerId")) {
            sortFields.add("workerId");
        }
        return sortFields;
    }

    @NotNull
    private static Predicate[] getPredicates(List<City> cities, List<Language> languages, List<Integer> serviceIds,
                                             boolean shouldShowTestWorkers, Root<WorkerInfo> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("active"), true));
        predicates.add(cb.equal(root.get("verified"), true));
        if(!shouldShowTestWorkers){
            predicates.add(cb.equal(root.get("test"), false));
        }

        if (cities != null && !cities.isEmpty()) {
            predicates.add(root.get("city").in(cities));
        }

        if (languages != null && !languages.isEmpty()) {
            Join<WorkerInfo, Language> languageJoin = root.join("languages");
            predicates.add(languageJoin.in(languages));
        }

        if (serviceIds != null && !serviceIds.isEmpty()) {
            Join<WorkerInfo, WorkerServiceEntity> serviceJoin = root.join("services");
            predicates.add(serviceJoin.get("serviceId").in(serviceIds));
        }
        return predicates.toArray(new Predicate[0]);
    }

    private void applySorting(CriteriaQuery<?> query, Root<WorkerInfo> root, CriteriaBuilder cb, Pageable pageable) {
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }
    }
}
