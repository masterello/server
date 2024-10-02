
package com.masterello.categoryservice.repository

import com.masterello.categoryservice.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {

    @Query(nativeQuery = true, value =
    """ 
        WITH RECURSIVE category_parents AS (
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            WHERE
                c.category_code = :categoryCode AND c.active = true
        UNION ALL
             SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            INNER JOIN
                category_parents ch ON c.category_code = ch.parent_code
        ) SELECT * FROM category_parents;
            """)
    fun findAllParentsByCategoryCode(@Param("categoryCode") categoryCode: Int): List<Category>

    @Query(nativeQuery = true, value =
    """ 
        WITH RECURSIVE category_children AS (
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            WHERE
                c.category_code = :categoryCode AND c.active = true
            UNION ALL
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            INNER JOIN
                category_children cc ON c.parent_code = cc.category_code
        )
        SELECT *
        FROM category_children
            """)
    fun findAllChildsByCategoryCode(@Param("categoryCode") categoryCode: Int): List<Category>

    @Query(nativeQuery = true, value =
    """ 
        WITH RECURSIVE category_parents AS (
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            WHERE
                c.category_code = :categoryCode AND c.active = true AND c.is_service = true
        UNION ALL
             SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            INNER JOIN
                category_parents ch ON c.category_code = ch.parent_code
        ) SELECT * FROM category_parents;
            """)
    fun findAllServiceParentsByCategoryCode(@Param("categoryCode") categoryCode: Int): List<Category>

    @Query(nativeQuery = true, value =
    """ 
        WITH RECURSIVE category_children AS (
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            WHERE
                c.category_code = :categoryCode AND c.active = true AND c.is_service = true
            UNION ALL
            SELECT
                c.uuid,
                c.name,
                c.original_name,
                c.description,
                c.category_code,
                c.parent_code,
                c.is_service,
                c.created_date,
                c.updated_date,
                c.active
            FROM
                categories.category c
            INNER JOIN
                category_children cc ON c.parent_code = cc.category_code
        )
        SELECT *
        FROM category_children
            """)
    fun findAllServiceChildsByCategoryCode(@Param("categoryCode") categoryCode: Int): List<Category>

    fun findByName(name: String): Category?

    fun findByCategoryCode(code: Int): Category?
}