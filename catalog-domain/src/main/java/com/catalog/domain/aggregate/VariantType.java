package com.catalog.domain.aggregate;

import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.valueobject.VariantTypeStatus;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.*;

/**
 * Variant type defines as a set of attributes.
 * Color -> [ Yellow, brown, green ]
 */
@Getter
public class VariantType extends AggregateRoot<Id> {
    private String name;
    private VariantTypeStatus status;
    private final Set<VariantOption> options = new LinkedHashSet<>();

    private VariantType(Id id, String name) {
        super(id);
        this.name = name;
        this.status = VariantTypeStatus.ACTIVE;
    }

    public VariantType(
            Id id,
            String name,
            VariantTypeStatus status,
            List<VariantOption> options
    ) {
        super(id);
        this.name = name;
        this.status = status;
        this.options.addAll(options);
    }

    public static VariantType create(Id id, String name){
        return new VariantType(id, name);
    }

    public void activate() {
        this.status = VariantTypeStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = VariantTypeStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == VariantTypeStatus.ACTIVE;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void addOption(VariantOption option) {
        Objects.requireNonNull(option, "option");

        if(!options.add(option)) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.DuplicateVariantOption( option.getName(), getName()),
                    "Duplicate variant option in variant type."
            );
        }
    }

    public Optional<VariantOption> findOptionById(Id optionId) {
        if (optionId == null) {
            return Optional.empty();
        }
        return options.stream()
                .filter(option -> Objects.equals(option.getId(), optionId))
                .findFirst();
    }

    public Set<VariantOption> getOptions() {
        return Collections.unmodifiableSet(options);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantType that = (VariantType) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        String optionValues = options.stream()
                //.sorted(Comparator.comparing(VariantOption::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(option -> "\"" + option.getName() + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"options\":[%s]}",
                Objects.toString(getId(), ""),
                name,
                optionValues
        );
    }
}
