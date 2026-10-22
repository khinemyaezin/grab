package com.saleschannel.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRegionId implements Serializable {
    @Column(name = "sales_channel_id", length = 255, insertable = false, updatable = false)
    private String salesChannelId;

    @Column(name = "region_id", length = 64)
    private String regionId;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChannelRegionId that)) {
            return false;
        }
        return Objects.equals(salesChannelId, that.salesChannelId)
                && Objects.equals(regionId, that.regionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salesChannelId, regionId);
    }
}
