package com.anbu.aipos.adapters.out.persistence.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByTenantIdOrderByNameAsc(String tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(String tenantId, String name);

    List<ProductEntity> findByTenantId(String tenantId);

//    List<ProductEntity> findById(List<Long> ids);

    Optional<ProductEntity> findByIdAndTenantId(Long id, String tenantId);

    @Query("""
            select p
            from ProductEntity p
            where p.tenantId = :tenantId
              and (
                    lower(p.name) like lower(concat('%', :query, '%'))
                    or lower(p.category) like lower(concat('%', :query, '%'))
                  )
              and (:activeOnly = false or p.active = true)
            order by
                case
                    when lower(p.name) = lower(:query) then 0
                    when lower(p.name) like lower(concat(:query, '%')) then 1
                    else 2
                end,
                p.name asc
            """)
    List<ProductEntity> searchByTenantId(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );

    List<ProductEntity> findByTenantIdAndActiveTrueAndStockQuantityLessThanEqualOrderByStockQuantityAscNameAsc(
            String tenantId,
            Integer stockQuantity
    );
}
