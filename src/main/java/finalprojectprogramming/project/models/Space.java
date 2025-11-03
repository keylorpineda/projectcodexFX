package finalprojectprogramming.project.models;

import finalprojectprogramming.project.models.enums.SpaceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SpaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 255)
    private String location;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "max_reservation_duration")
    private Integer maxReservationDuration;

    @Builder.Default
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceSchedule> schedules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "space")
    private List<Reservation> reservations = new ArrayList<>();
}