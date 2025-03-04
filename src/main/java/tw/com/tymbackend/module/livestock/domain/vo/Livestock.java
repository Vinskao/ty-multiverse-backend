package tw.com.tymbackend.module.livestock.domain.vo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "livestock")
public class Livestock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "livestock", nullable = false)
    private String livestock;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "melee", nullable = false)
    private Integer melee;

    @Column(name = "magicka", nullable = false)
    private Integer magicka;

    @Column(name = "ranged", nullable = false)
    private Integer ranged;

    @Column(name = "selling_price", precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    @Column(name = "buying_price", precision = 19, scale = 4)
    private BigDecimal buyingPrice;

    @Column(name = "deal_price", precision = 19, scale = 4)
    private BigDecimal dealPrice;

    @Column(name = "buyer")
    private String buyer;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Version
    private Long version;
} 