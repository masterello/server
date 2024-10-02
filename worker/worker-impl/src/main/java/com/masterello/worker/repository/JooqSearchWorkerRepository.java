package com.masterello.worker.repository;

import com.masterello.user.value.Language;
import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.domain.WorkerServiceEntity;
import com.masterello.worker.dto.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;

@RequiredArgsConstructor
@Primary
@Service
public class JooqSearchWorkerRepository implements SearchWorkerRepository {

    private final DSLContext dsl;

    @Override
    public long getTotalCount(List<Language> languages, List<Integer> serviceIds) {
        val count = dsl.with("distinct_workers")
                .as(dsl.selectDistinct(field("uuid"))
                        .from(DSL.table("users").as("u"))
                        .join(DSL.table("user_roles").as("ur")).on(field("u.uuid").eq(field("ur.user_id")))
                        .leftJoin(DSL.table("user_languages").as("ul")).on(field("u.uuid").eq(field("ul.user_id")))
                        .leftJoin(DSL.table("worker_services").as("ws")).on(field("u.uuid").eq(field("ws.worker_id")))
                        .where(field("ur.role").eq("WORKER")
                                .and(hasLanguageFilter(languages) ? field("ul.language").in(languages.stream().map(Language::name).toArray()) : DSL.noCondition())
                                .and(hasServiceFilter(serviceIds) ? field("ws.service_id").in(serviceIds) : DSL.noCondition())
                        )
                )
                .selectCount()
                .from(DSL.table("distinct_workers"))
                .fetchOne(0, Long.class);
        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public Set<UUID> findWorkersIds(List<Language> languages, List<Integer> serviceIds, int page, int size, PageRequest.Sort sort) {
        // Collect the fields to be included in the distinct select clause
        List<String> sortFields = sort.getFields();

        List<Field<?>> distinctFields = sortFields.stream()
                .map(sortField -> DSL.field(sortField).as(sortField.replace(".", "_")))
                .collect(Collectors.toList());

        // Always include the UUID field
        distinctFields.add(0, field("u.uuid"));

        var query = dsl.with("distinct_workers")
                .as(dsl.selectDistinct(distinctFields)
                        .from(DSL.table("users").as("u"))
                        .join(DSL.table("user_roles").as("ur")).on(field("u.uuid").eq(field("ur.user_id")))
                        .leftJoin(DSL.table("user_languages").as("ul")).on(field("u.uuid").eq(field("ul.user_id")))
                        .leftJoin(DSL.table("worker_info").as("wi")).on(field("u.uuid").eq(field("wi.worker_id")))
                        .leftJoin(DSL.table("worker_services").as("ws")).on(field("u.uuid").eq(field("ws.worker_id")))
                        .where(field("ur.role").eq("WORKER")
                                .and(hasLanguageFilter(languages) ? field("ul.language").in(languages.stream().map(Language::name).toArray()) : DSL.noCondition())
                                .and(hasServiceFilter(serviceIds) ? field("ws.service_id").in(serviceIds) : DSL.noCondition())
                        )
                )
                .select(field("uuid"))
                .from(DSL.table("distinct_workers"))
                .orderBy(sortFields(sort, f -> f.replace(".", "_")))
                .limit(size)
                .offset(page * size);

        // Execute the query and get results
        List<UUID> ids = query.fetch(field("uuid"), UUID.class);
        return new HashSet<>(ids); // Use HashSet to ensure uniqueness
    }

    private List<org.jooq.SortField<?>> sortFields(PageRequest.Sort sort) {
        return sortFields(sort, s -> s);
    }

    private List<org.jooq.SortField<?>> sortFields(PageRequest.Sort sort, Function<String, String> columnNameMapper) {
        return sort.getFields().stream()
                .map(sortField -> sort.getOrder() == PageRequest.SortOrder.ASC
                        ? DSL.field(columnNameMapper.apply(sortField)).asc()
                        : DSL.field(columnNameMapper.apply(sortField)).desc())
                .collect(Collectors.toList());
    }

    @Override
    public List<FullWorkerProjection> findWorkers(Set<UUID> ids, PageRequest.Sort sort) {
        // Building the query
        var query = dsl.select(
                        DSL.field("u.uuid"),
                        DSL.field("u.title"),
                        DSL.field("u.name"),
                        DSL.field("u.lastname"),
                        DSL.field("u.city"),
                        DSL.field("wi.worker_id"),
                        DSL.field("wi.description"),
                        DSL.field("wi.phone"),
                        DSL.field("wi.telegram"),
                        DSL.field("wi.whatsapp"),
                        DSL.field("wi.viber"),
                        DSL.groupConcat(DSL.field("ul.language")).as("languages"),
                        DSL.groupConcat(DSL.concat(DSL.field("ws.service_id"), DSL.inline("#"), DSL.field("ws.amount"))).as("services")
                )
                .from(DSL.table("users").as("u"))
                .leftJoin(DSL.table("worker_info").as("wi")).on(DSL.field("u.uuid").eq(DSL.field("wi.worker_id")))
                .leftJoin(DSL.table("user_languages").as("ul")).on(DSL.field("u.uuid").eq(DSL.field("ul.user_id")))
                .leftJoin(DSL.table("worker_services").as("ws")).on(DSL.field("u.uuid").eq(DSL.field("ws.worker_id")))
                .where(DSL.field("u.uuid").in(ids))
                .groupBy(
                        DSL.field("u.uuid"), DSL.field("u.title"), DSL.field("u.name"),
                        DSL.field("u.lastname"), DSL.field("u.city"),
                        DSL.field("wi.worker_id"), DSL.field("wi.description"), DSL.field("wi.phone"),
                        DSL.field("wi.telegram"), DSL.field("wi.whatsapp"), DSL.field("wi.viber")
                )
                .orderBy(sortFields(sort));

        // Execute the query and get results
        return query.fetch(this::mapToFullWorkerProjection);
    }

    // Helper method to map a record to FullWorkerProjection
    private FullWorkerProjection mapToFullWorkerProjection(Record record) {
        return FullWorkerProjection.builder()
                .uuid(record.get("u.uuid", UUID.class))
                .title(record.get("u.title", String.class))
                .name(record.get("u.name", String.class))
                .lastname(record.get("u.lastname", String.class))
                .city(record.get("u.city", String.class))
                .workerInfo(mapWorkerInfo(record))
                .languages(mapLanguages(record))
                .build();
    }

    // Mapping WorkerInfo
    private WorkerInfo mapWorkerInfo(Record record) {
        if(record.get("wi.worker_id", UUID.class) == null) {
            return null;
        }
        return WorkerInfo.builder()
                .description(record.get("wi.description", String.class))
                .services(mapServices(record))
                .phone(record.get("wi.phone", String.class))
                .telegram(record.get("wi.telegram", String.class))
                .whatsapp(record.get("wi.whatsapp", String.class))
                .viber(record.get("wi.viber", String.class))
                .build();
    }

    // Mapping Services
    private List<WorkerServiceEntity> mapServices(Record record) {
        String servicesRaw = record.get("services", String.class);
        return Optional.ofNullable(servicesRaw)
                .map(services -> Arrays.stream(services.split(","))
                        .distinct()
                        .map(this::parseService)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(WorkerServiceEntity::getServiceId))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Parsing Service String
    private WorkerServiceEntity parseService(String service) {
        String[] parsed = service.split("#");
        return parsed.length > 1 ? new WorkerServiceEntity(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1])) : null;
    }

    // Mapping Languages
    private List<Language> mapLanguages(Record record) {
        String languagesRaw = record.get("languages", String.class);
        return Optional.ofNullable(languagesRaw)
                .map(languages -> Arrays.stream(languages.split(","))
                        .map(Language::valueOf)
                        .distinct()
                        .sorted(Comparator.comparing(Language::name))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    private boolean hasLanguageFilter(List<Language> languages) {
        return languages != null && !languages.isEmpty();
    }

    private boolean hasServiceFilter(List<Integer> serviceIds) {
        return serviceIds != null && !serviceIds.isEmpty();
    }

}
