package gov.cmr.minfi.db.gbe.app.common.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Page de résultats renvoyée par l'API. Format propre à l'application (indépendant de la
 * sérialisation interne de Spring Data, qui n'est pas garantie stable). {@code page} commence à 0.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
