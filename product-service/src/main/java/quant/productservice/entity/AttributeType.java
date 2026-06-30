package quant.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attribute_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name; // "Size", "Màu sắc"

    @OneToMany(mappedBy = "attributeType", cascade = CascadeType.ALL)
    private List<AttributeValue> values = new ArrayList<>();
}
