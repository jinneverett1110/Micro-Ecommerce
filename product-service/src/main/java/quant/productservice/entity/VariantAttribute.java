package quant.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "variant_attributes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttribute {

    @EmbeddedId
    private VariantAttributeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("variantId")
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attributeValueId")
    @JoinColumn(name = "attribute_value_id")
    private AttributeValue attributeValue;

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VariantAttributeId implements Serializable {
        private String variantId;
        private String attributeValueId;
    }
}
