package com.linhvecac.booking;

import com.linhvecac.catalog.concession.Concession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_concessions")
@Getter
@Setter
@NoArgsConstructor
public class BookingConcession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concession_id")
    private Concession concession;

    @Column(nullable = false)
    private int quantity;

    /** Giá tại thời điểm đặt — không đổi khi admin sửa giá món. */
    @Column(name = "unit_price", nullable = false)
    private long unitPrice;
}
