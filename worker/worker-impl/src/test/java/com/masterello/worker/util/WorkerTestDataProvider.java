package com.masterello.worker.util;


import com.masterello.category.dto.CategoryDto;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;
import io.restassured.http.Cookie;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;

public final class WorkerTestDataProvider {

    public static final String USER_S = "bb2c6e16-2228-4ac1-8482-1f3548672b43";
    public static final UUID USER = UUID.fromString(USER_S);
    public static final String ADMIN_S = "455aee9c-9629-466f-bfc5-8d956da74769";
    public static final UUID ADMIN = UUID.fromString(ADMIN_S);
    public static final String WORKER_1_S = "e5fcf8dd-b6be-4a36-a85a-e2d952cc6254";
    public static final UUID WORKER_1 = UUID.fromString(WORKER_1_S);
    public static final String WORKER_2_S = "e4de38bf-168e-41fc-b7b1-b9d74a47529e";

    public static final UUID WORKER_2 = UUID.fromString(WORKER_2_S);
    public static final UUID WORKER_3 = UUID.fromString("d1c822c9-0ee4-462a-a88e-7c45e3bb0e54");
    public static final UUID WORKER_4 = UUID.fromString("57bc029c-d8e3-458f-b25a-7f73283cec98");
    public static final UUID WORKER_5 = UUID.fromString("b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52");
    public static final String WORKER_6_S = "8824a15c-98f5-49d9-bd97-43d1cba3f62c";
    public static final UUID WORKER_6 = UUID.fromString(WORKER_6_S);
    private static final String WORKER_7_S = "f2e91db9-4ceb-4231-bd4e-c898b441247d";
    public static final UUID WORKER_7 = UUID.fromString(WORKER_7_S);
    private static final String WORKER_8_S = "dda832b4-b8e3-43df-a457-d77043b01751";
    public static final UUID WORKER_8 = UUID.fromString(WORKER_8_S);

    public static final String DESCRIPTION = "Best nogotochki in Berlin";
    public static final String WHATSAPP = "whatsap4ik";
    public static final String TELEGRAM = "telezhka";
    public static final String PHONE = "555-35-35";
    public static final String ACCESS_TOKEN = "bg6yX_eErXRKdklESRPHpyA5SDxzIi4EuYacVX29MKCMDcm_GniWXltRhjjh6FBbpfePaDGmVE5p72cA9agNd5WveHEK4gbm9u9tA9UqntlPLMYtFFaB";

    public static CategoryDto randomCategory(int id, int parentId) {
        return new CategoryDto(
                UUID.randomUUID(),
                "random service " + id,
                "super service",
                id,
                parentId,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                true
        );
    }

    public static Cookie tokenCookie() {
        return new Cookie.Builder(M_TOKEN_COOKIE, ACCESS_TOKEN).build();
    }


    public static Map<UUID, MasterelloUser> getMasterelloTestUsers() {
        Map<UUID, MasterelloUser> workers = new HashMap<>();

        workers.put(WORKER_1, MasterelloTestUser.builder()
                .uuid(WORKER_1)
                .title("Mr.")
                .name("worker1")
                .lastname("workerson")
                .build());

        workers.put(WORKER_2, MasterelloTestUser.builder()
                .uuid(WORKER_2)
                .title("Herr")
                .name("worker2")
                .lastname("workerson")
                .build());

        workers.put(WORKER_3, MasterelloTestUser.builder()
                .uuid(WORKER_3)
                .title("Ms.")
                .name("worker3")
                .lastname("workerson")
                .build());

        workers.put(WORKER_4, MasterelloTestUser.builder()
                .uuid(WORKER_4)
                .title("Frau")
                .name("worker4")
                .lastname("workerson")
                .build());

        workers.put(WORKER_5, MasterelloTestUser.builder()
                .uuid(WORKER_5)
                .title(null)
                .name("worker5")
                .lastname("workerson")
                .build());

        workers.put(WORKER_6, MasterelloTestUser.builder()
                .uuid(WORKER_6)
                .title(null)
                .name("worker6")
                .lastname("workerson")
                .build());

        workers.put(WORKER_7, MasterelloTestUser.builder()
                .uuid(WORKER_7)
                .title("Mr.")
                .name("worker7")
                .lastname("workerson")
                .build());

        workers.put(WORKER_8, MasterelloTestUser.builder()
                .uuid(WORKER_8)
                .title("Mr.")
                .name("worker8")
                .lastname("workerson")
                .build());

        return workers;
    }
}
