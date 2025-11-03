package finalprojectprogramming.project.services.reservation.fixtures;

import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.ReservationAttendee;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.models.enums.UserRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReservationTestDataFactory {

    private ReservationTestDataFactory() {
    }

    public static UserBuilder userBuilder() {
        return new UserBuilder();
    }

    public static SpaceBuilder spaceBuilder() {
        return new SpaceBuilder();
    }

    public static ReservationBuilder reservationBuilder() {
        return new ReservationBuilder();
    }

    public static ReservationDtoBuilder reservationDtoBuilder() {
        return new ReservationDtoBuilder();
    }

    public static ReservationCheckInRequestBuilder checkInRequestBuilder() {
        return new ReservationCheckInRequestBuilder();
    }

    public static NotificationBuilder notificationBuilder() {
        return new NotificationBuilder();
    }

    public static ReservationAttendeeBuilder attendeeBuilder() {
        return new ReservationAttendeeBuilder();
    }

    public static final class UserBuilder {
        private Long id = 100L;
        private UserRole role = UserRole.USER;
        private String name = "Jane Doe";
        private String email = "jane.doe@example.com";
        private Boolean active = true;
        private LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        private LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        public UserBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder withRole(UserRole role) {
            this.role = role;
            return this;
        }

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setRole(role);
            user.setName(name);
            user.setEmail(email);
            user.setActive(active);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            user.setReservations(new ArrayList<>());
            user.setApprovedReservations(new ArrayList<>());
            user.setAuditLogs(new ArrayList<>());
            return user;
        }
    }

    public static final class SpaceBuilder {
        private Long id = 200L;
        private String name = "Conference Room";
        private SpaceType type = SpaceType.SALA;
        private Integer capacity = 10;
        private Boolean active = true;
        private Boolean requiresApproval = false;
        private Integer maxReservationDuration = 180;

        public SpaceBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public SpaceBuilder withRequiresApproval(boolean requiresApproval) {
            this.requiresApproval = requiresApproval;
            return this;
        }

        public SpaceBuilder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Space build() {
            Space space = new Space();
            space.setId(id);
            space.setName(name);
            space.setType(type);
            space.setCapacity(capacity);
            space.setActive(active);
            space.setRequiresApproval(requiresApproval);
            space.setMaxReservationDuration(maxReservationDuration);
            space.setReservations(new ArrayList<>());
            space.setSchedules(new ArrayList<>());
            space.setImages(new ArrayList<>());
            return space;
        }
    }

    public static final class ReservationBuilder {
        private Long id = 300L;
        private User user = userBuilder().build();
        private Space space = spaceBuilder().build();
        private ReservationStatus status = ReservationStatus.CONFIRMED;
        private LocalDateTime start = LocalDateTime.now().plusHours(2);
        private LocalDateTime end = LocalDateTime.now().plusHours(3);
        private String qrCode = "QR-123";
        private Integer attendees = 1;
        private User approvedBy;
        private LocalDateTime deletedAt;
        private LocalDateTime checkInAt;
        private List<Notification> notifications = new ArrayList<>();
        private List<ReservationAttendee> attendeeRecords = new ArrayList<>();

        public ReservationBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ReservationBuilder withUser(User user) {
            this.user = user;
            return this;
        }

        public ReservationBuilder withSpace(Space space) {
            this.space = space;
            return this;
        }

        public ReservationBuilder withStatus(ReservationStatus status) {
            this.status = status;
            return this;
        }

        public ReservationBuilder withStart(LocalDateTime start) {
            this.start = start;
            return this;
        }

        public ReservationBuilder withEnd(LocalDateTime end) {
            this.end = end;
            return this;
        }

        public ReservationBuilder withQrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public ReservationBuilder withAttendees(Integer attendees) {
            this.attendees = attendees;
            return this;
        }

        public ReservationBuilder withApprovedBy(User approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public ReservationBuilder withDeletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public ReservationBuilder withCheckInAt(LocalDateTime checkInAt) {
            this.checkInAt = checkInAt;
            return this;
        }

        public ReservationBuilder withNotifications(List<Notification> notifications) {
            this.notifications = notifications;
            return this;
        }

        public ReservationBuilder withAttendeeRecords(List<ReservationAttendee> attendeeRecords) {
            this.attendeeRecords = attendeeRecords;
            return this;
        }

        public Reservation build() {
            Reservation reservation = new Reservation();
            reservation.setId(id);
            reservation.setUser(user);
            reservation.setSpace(space);
            reservation.setStatus(status);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setQrCode(qrCode);
            reservation.setAttendees(attendees);
            reservation.setApprovedBy(approvedBy);
            reservation.setDeletedAt(deletedAt);
            reservation.setCheckinAt(checkInAt);
            reservation.setNotifications(notifications);
            reservation.setAttendeeRecords(attendeeRecords);
            return reservation;
        }
    }

    public static final class ReservationDtoBuilder {
        private Long id = 400L;
        private Long userId = 100L;
        private Long spaceId = 200L;
        private ReservationStatus status = ReservationStatus.CONFIRMED;
        private LocalDateTime start = LocalDateTime.now().plusHours(2);
        private LocalDateTime end = LocalDateTime.now().plusHours(3);
        private String qrCode = "QR-123";
        private Integer attendees = 1;
        private Long approvedByUserId;
        private List<Long> notificationIds = new ArrayList<>();

        public ReservationDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ReservationDtoBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ReservationDtoBuilder withSpaceId(Long spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public ReservationDtoBuilder withStatus(ReservationStatus status) {
            this.status = status;
            return this;
        }

        public ReservationDtoBuilder withStart(LocalDateTime start) {
            this.start = start;
            return this;
        }

        public ReservationDtoBuilder withEnd(LocalDateTime end) {
            this.end = end;
            return this;
        }

        public ReservationDtoBuilder withQrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public ReservationDtoBuilder withAttendees(Integer attendees) {
            this.attendees = attendees;
            return this;
        }

        public ReservationDtoBuilder withApprovedByUserId(Long approvedByUserId) {
            this.approvedByUserId = approvedByUserId;
            return this;
        }

        public ReservationDTO build() {
            return ReservationDTO.builder()
                    .id(id)
                    .userId(userId)
                    .spaceId(spaceId)
                    .status(status)
                    .startTime(start)
                    .endTime(end)
                    .qrCode(qrCode)
                    .attendees(attendees)
                    .approvedByUserId(approvedByUserId)
                    .build();
        }
    }

    public static final class ReservationCheckInRequestBuilder {
        private String qrCode = "QR-123";
        private String attendeeFirstName = "John";
        private String attendeeLastName = "Smith";
        private String attendeeIdNumber = "ID-999";

        public ReservationCheckInRequestBuilder withQrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public ReservationCheckInRequestBuilder withAttendeeId(String attendeeId) {
            this.attendeeIdNumber = attendeeId;
            return this;
        }

        public ReservationCheckInRequestBuilder withFirstName(String firstName) {
            this.attendeeFirstName = firstName;
            return this;
        }

        public ReservationCheckInRequestBuilder withLastName(String lastName) {
            this.attendeeLastName = lastName;
            return this;
        }

        public ReservationCheckInRequest build() {
            ReservationCheckInRequest request = new ReservationCheckInRequest();
            request.setQrCode(qrCode);
            request.setAttendeeFirstName(attendeeFirstName);
            request.setAttendeeLastName(attendeeLastName);
            request.setAttendeeIdNumber(attendeeIdNumber);
            return request;
        }
    }

    public static final class NotificationBuilder {
        private Long id = 900L;
        private NotificationType type = NotificationType.CONFIRMATION;
        private NotificationStatus status = NotificationStatus.SENT;

        public NotificationBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification();
            notification.setId(id);
            notification.setType(type);
            notification.setStatus(status);
            return notification;
        }
    }

    public static final class ReservationAttendeeBuilder {
        private Long id = 800L;
        private String idNumber = "ID-999";
        private String firstName = "John";
        private String lastName = "Smith";
        private LocalDateTime checkInAt = LocalDateTime.now().minusMinutes(10);

        public ReservationAttendeeBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ReservationAttendeeBuilder withIdNumber(String idNumber) {
            this.idNumber = idNumber;
            return this;
        }

        public ReservationAttendeeBuilder withCheckInAt(LocalDateTime checkInAt) {
            this.checkInAt = checkInAt;
            return this;
        }

        public ReservationAttendee build() {
            return ReservationAttendee.builder()
                    .id(id)
                    .idNumber(idNumber)
                    .firstName(firstName)
                    .lastName(lastName)
                    .checkInAt(checkInAt)
                    .build();
        }
    }
}
