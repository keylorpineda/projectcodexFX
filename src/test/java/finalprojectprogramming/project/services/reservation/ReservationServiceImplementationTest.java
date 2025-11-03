package finalprojectprogramming.project.services.reservation;

import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.attendeeBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.checkInRequestBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.notificationBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.reservationBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.reservationDtoBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.spaceBuilder;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.userBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.ReservationAttendee;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import finalprojectprogramming.project.services.notification.ReservationNotificationService;
import finalprojectprogramming.project.services.space.SpaceAvailabilityValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ReservationServiceImplementationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceAvailabilityValidator availabilityValidator;

    @Mock
    private ReservationNotificationService notificationService;

    @Mock
    private ReservationCancellationPolicy cancellationPolicy;

    @Mock
    private AuditLogService auditLogService;

    private ReservationServiceImplementation service;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new ReservationServiceImplementation(
                reservationRepository,
                userRepository,
                spaceRepository,
                new ModelMapper(),
                availabilityValidator,
                notificationService,
                cancellationPolicy,
                auditLogService,
                objectMapper);
    }

    @Test
    void markCheckIn_rejectsAfterWindowClosed() {
        var reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(2)) // window closed (start-30min .. start+30min)
                .withQrCode("QR-LATE")
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
            ReservationCheckInRequest req = checkInRequestBuilder().withQrCode("QR-LATE").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Check-in window has closed");
        }
    }

        @Test
        void markNoShow_rejects_when_status_not_confirmed() {
                var reservation = reservationBuilder()
                                .withStatus(ReservationStatus.PENDING)
                                .withStart(LocalDateTime.now().minusHours(1))
                                .build();
                when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
                        assertThatThrownBy(() -> service.markNoShow(reservation.getId()))
                                        .isInstanceOf(BusinessRuleException.class)
                                        .hasMessageContaining("Only confirmed reservations can be marked as no-show");
                }
        }

    @Test
    void markCheckIn_requires_attendee_fields() {
        var reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withQrCode("QR-F")
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);

            // Missing id number
            ReservationCheckInRequest r1 = checkInRequestBuilder().withQrCode("QR-F").withAttendeeId("  ").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), r1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("identification number is required");

            // Missing first name
            ReservationCheckInRequest r2 = checkInRequestBuilder().withQrCode("QR-F").withAttendeeId("ID1").withFirstName(" ").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), r2))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("first name is required");

            // Missing last name
            ReservationCheckInRequest r3 = checkInRequestBuilder().withQrCode("QR-F").withAttendeeId("ID1").withFirstName("A").withLastName(" ").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), r3))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("last name is required");
        }
    }

    @Test
    void markNoShow_rejects_when_checkin_already_registered() {
        var reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(2))
                .build();
        reservation.setCheckinAt(LocalDateTime.now().minusHours(1));
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
            assertThatThrownBy(() -> service.markNoShow(reservation.getId()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already has a check-in registered");
        }
    }

    @Test
    void create_rejects_initial_status_canceled_or_no_show() {
        var user = userBuilder().withId(55L).build();
        var space = spaceBuilder().withId(77L).withRequiresApproval(false).build();
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(77L)).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(55L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            var canceled = reservationDtoBuilder().withUserId(55L).withSpaceId(77L)
                    .withStatus(ReservationStatus.CANCELED).withQrCode("QR-1").build();
            assertThatThrownBy(() -> service.create(canceled))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be created with status");

            var noShow = reservationDtoBuilder().withUserId(55L).withSpaceId(77L)
                    .withStatus(ReservationStatus.NO_SHOW).withQrCode("QR-2").build();
            assertThatThrownBy(() -> service.create(noShow))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be created with status");
        }
    }

    @Test
    void createReservationPersistsEntityAndReturnsDto() {
        ReservationDTO request = reservationDtoBuilder()
                .withUserId(10L)
                .withSpaceId(20L)
                .withQrCode("unique-qr")
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .withEnd(LocalDateTime.now().plusHours(3))
                .withAttendees(2)
                .build();
        User user = userBuilder().withId(10L).build();
        Space space = spaceBuilder().withId(20L).build();
        Notification notification = notificationBuilder().withId(901L).build();
        Reservation saved = reservationBuilder()
                .withId(55L)
                .withUser(user)
                .withSpace(space)
                .withStatus(ReservationStatus.PENDING)
                .withQrCode("unique-qr")
                .withNotifications(List.of(notification))
                .build();
        saved.setAttendeeRecords(new ArrayList<>());

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(20L)).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(10L, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.create(request);

            assertThat(response.getId()).isEqualTo(55L);
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getSpaceId()).isEqualTo(20L);
            assertThat(response.getQrCode()).isEqualTo("unique-qr");
            assertThat(response.getNotificationIds()).containsExactly(901L);
                        // Con la política actual, todas las reservas nuevas inician en PENDING
                        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);

            ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(reservationCaptor.capture());
            Reservation persisted = reservationCaptor.getValue();
            assertThat(persisted.getUser()).isEqualTo(user);
            assertThat(persisted.getSpace()).isEqualTo(space);
                        assertThat(persisted.getStatus()).isEqualTo(ReservationStatus.PENDING);
            assertThat(persisted.getCreatedAt()).isNotNull();
            assertThat(persisted.getUpdatedAt()).isNotNull();
            assertThat(persisted.getNotifications()).isNotNull();
            assertThat(persisted.getAttendeeRecords()).isNotNull();

            mocked.verify(() -> SecurityUtils.requireSelfOrAny(10L, UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(availabilityValidator).assertAvailability(space, request.getStartTime(), request.getEndTime(), null);
            verify(notificationService).notifyReservationCreated(saved);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_CREATED"), eq("55"), any());
        }
    }

    @Test
    void createReservationRejectsDuplicatedQrCode() {
        ReservationDTO request = reservationDtoBuilder().withQrCode("duplicate").build();
        Reservation existing = reservationBuilder().withQrCode("duplicate").withDeletedAt(null).build();
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(userBuilder().build()));
        when(spaceRepository.findById(request.getSpaceId())).thenReturn(Optional.of(spaceBuilder().build()));
        when(reservationRepository.findAll()).thenReturn(List.of(existing));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(request.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("QR code is already associated");
        }

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservationChangesFieldsAndValidatesSecurity() {
        User owner = userBuilder().withId(101L).build();
        Space originalSpace = spaceBuilder().withId(201L).build();
        Space newSpace = spaceBuilder().withId(202L).build();
        Reservation existing = reservationBuilder()
                .withId(900L)
                .withUser(owner)
                .withSpace(originalSpace)
                .withQrCode("initial-qr")
                .withStart(LocalDateTime.now().plusHours(2))
                .withEnd(LocalDateTime.now().plusHours(3))
                .build();
        existing.setAttendees(2);
        existing.setAttendeeRecords(new ArrayList<>());

        ReservationDTO updateRequest = reservationDtoBuilder()
                .withUserId(owner.getId())
                .withSpaceId(newSpace.getId())
                .withStart(LocalDateTime.now().plusHours(4))
                .withEnd(LocalDateTime.now().plusHours(5))
                .withQrCode("updated-qr")
                .withAttendees(3)
                .build();

        when(reservationRepository.findById(900L)).thenReturn(Optional.of(existing));
        when(spaceRepository.findById(newSpace.getId())).thenReturn(Optional.of(newSpace));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(existing)).thenReturn(existing);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.update(900L, updateRequest);

            assertThat(response.getSpaceId()).isEqualTo(newSpace.getId());
            assertThat(response.getStartTime()).isEqualTo(updateRequest.getStartTime());
            assertThat(response.getEndTime()).isEqualTo(updateRequest.getEndTime());
            assertThat(existing.getQrCode()).isEqualTo("updated-qr");

            verify(availabilityValidator).assertAvailability(newSpace, updateRequest.getStartTime(),
                    updateRequest.getEndTime(), existing.getId());
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(owner.getId(), UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(auditLogService).logEvent(any(), eq("RESERVATION_UPDATED"), eq("900"), any());
        }
    }

        @Test
        void update_setsApprovedBy_when_provided() {
                User owner = userBuilder().withId(1001L).build();
                User approver = userBuilder().withId(2002L).build();
                Space space = spaceBuilder().withId(3003L).build();
                Reservation existing = reservationBuilder()
                                .withId(4004L)
                                .withUser(owner)
                                .withSpace(space)
                                .withQrCode("QR-X")
                                .build();
                when(reservationRepository.findById(4004L)).thenReturn(Optional.of(existing));
                when(userRepository.findById(2002L)).thenReturn(Optional.of(approver));
                when(reservationRepository.findAll()).thenReturn(List.of());
                when(reservationRepository.save(existing)).thenReturn(existing);

                ReservationDTO patch = reservationDtoBuilder()
                                .withUserId(owner.getId())
                                .withSpaceId(space.getId())
                                .withApprovedByUserId(2002L)
                                .withQrCode("QR-X")
                                .build();

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(inv -> null);

                        ReservationDTO out = service.update(4004L, patch);
                        assertThat(out).isNotNull();
                        assertThat(existing.getApprovedBy()).isEqualTo(approver);
                        verify(reservationRepository).save(existing);
                }
        }

    @Test
    void findByIdRequiresOwnershipAndReturnsDto() {
        User owner = userBuilder().withId(400L).build();
        Reservation reservation = reservationBuilder().withUser(owner).withId(501L).build();
        when(reservationRepository.findById(501L)).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO response = service.findById(501L);
            assertThat(response.getId()).isEqualTo(501L);
            assertThat(response.getUserId()).isEqualTo(owner.getId());
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(owner.getId(), UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findAllRequiresSupervisorRoleAndFiltersDeleted() {
        Reservation active = reservationBuilder().withId(10L).withDeletedAt(null).build();
        Reservation deleted = reservationBuilder().withId(11L).withDeletedAt(LocalDateTime.now()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(active, deleted));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findAll();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(10L);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findByUserFiltersByUserId() {
        User owner = userBuilder().withId(12L).build();
        Reservation matching = reservationBuilder().withUser(owner).build();
        Reservation other = reservationBuilder().withUser(userBuilder().withId(999L).build()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(matching, other));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findByUser(owner.getId());
            assertThat(results).extracting(ReservationDTO::getUserId).containsExactly(owner.getId());
        }
    }

        @Test
        void findByUser_skips_when_reservation_has_null_user() {
                Reservation withNullUser = reservationBuilder().withUser(null).build();
                User owner = userBuilder().withId(42L).build();
                Reservation matching = reservationBuilder().withUser(owner).build();
                when(reservationRepository.findAll()).thenReturn(List.of(withNullUser, matching));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(invocation -> null);

                        List<ReservationDTO> results = service.findByUser(owner.getId());
                        assertThat(results).extracting(ReservationDTO::getUserId).containsExactly(owner.getId());
                }
        }

    @Test
    void findBySpaceRequiresSupervisorAndFiltersBySpace() {
        Space target = spaceBuilder().withId(321L).build();
        Reservation matching = reservationBuilder().withSpace(target).build();
        Reservation other = reservationBuilder().withSpace(spaceBuilder().withId(999L).build()).build();
        when(reservationRepository.findAll()).thenReturn(List.of(matching, other));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            List<ReservationDTO> results = service.findBySpace(target.getId());
            assertThat(results).extracting(ReservationDTO::getSpaceId).containsExactly(target.getId());
        }
    }

        @Test
        void findBySpace_skips_when_reservation_has_null_space() {
                Reservation nullSpace = reservationBuilder().withSpace(null).build();
                Space target = spaceBuilder().withId(987L).build();
                Reservation matching = reservationBuilder().withSpace(target).build();
                when(reservationRepository.findAll()).thenReturn(List.of(nullSpace, matching));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(invocation -> null);

                        List<ReservationDTO> results = service.findBySpace(target.getId());
                        assertThat(results).extracting(ReservationDTO::getSpaceId).containsExactly(target.getId());
                }
        }

    @Test
    void cancelReservationSetsStatusAndNotifies() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .build();
        reservation.setAttendeeRecords(new ArrayList<>());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.cancel(reservation.getId(), "reason");
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getCancellationReason()).isEqualTo("reason");
            verify(notificationService).notifyReservationCanceled(reservation);
            verify(cancellationPolicy).assertCancellationAllowed(reservation);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_CANCELED"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void cancelReservationRejectsPastStartTime() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(1))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        doThrow(new BusinessRuleException("outside window")).when(cancellationPolicy)
                .assertCancellationAllowed(reservation);

        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.cancel(reservation.getId(), "late"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("outside window");
        }
    }

    @Test
    void approveReservationTransitionsToConfirmed() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.PENDING)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(999L)).thenReturn(Optional.of(userBuilder().withId(999L).build()));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.approve(reservation.getId(), 999L);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(notificationService).notifyReservationApproved(reservation);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_APPROVED"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void approveReservationRejectsNonPendingStatus() {
        Reservation reservation = reservationBuilder().withStatus(ReservationStatus.CONFIRMED).build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.approve(reservation.getId(), 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Only pending");
        }
    }

    @Test
    void markCheckInRegistersAttendeeWithinWindow() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withEnd(LocalDateTime.now().plusHours(1))
                .withQrCode("QR-CHECK")
                .withAttendees(2)
                .build();
        reservation.setAttendeeRecords(new ArrayList<>());
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder()
                    .withQrCode("QR-CHECK")
                    .withFirstName("Alice")
                    .withLastName("Brown")
                    .withAttendeeId("ID-1")
                    .build();

            ReservationDTO result = service.markCheckIn(reservation.getId(), request);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
            assertThat(reservation.getAttendeeRecords()).hasSize(1);
            ReservationAttendee attendee = reservation.getAttendeeRecords().get(0);
            assertThat(attendee.getIdNumber()).isEqualTo("ID-1");
            verify(reservationRepository).save(reservation);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_CHECKED_IN"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void markCheckIn_allows_when_already_checked_in_and_capacity_remaining() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CHECKED_IN)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withEnd(LocalDateTime.now().plusHours(1))
                .withQrCode("QR-CHECK")
                .withAttendees(2)
                .withAttendeeRecords(new ArrayList<>(List.of(attendeeBuilder().withIdNumber("ID-1").build())))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder()
                    .withQrCode("QR-CHECK")
                    .withFirstName("Bob")
                    .withLastName("Smith")
                    .withAttendeeId("ID-2")
                    .build();

            ReservationDTO result = service.markCheckIn(reservation.getId(), request);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
            assertThat(reservation.getAttendeeRecords()).hasSize(2);
        }
    }

    @Test
    void update_allows_same_qr_when_updating_same_reservation() {
        User owner = userBuilder().withId(6060L).build();
        Space space = spaceBuilder().withId(7070L).build();
        Reservation existing = reservationBuilder()
                .withId(8080L)
                .withUser(owner)
                .withSpace(space)
                .withQrCode("SAME-QR")
                .build();
        when(reservationRepository.findById(8080L)).thenReturn(Optional.of(existing));
        // Simular una reserva existente con el mismo QR (la misma entidad debe ser ignorada por validateQrCode)
        when(reservationRepository.findAll()).thenReturn(List.of(existing));
        when(reservationRepository.save(existing)).thenReturn(existing);

        ReservationDTO patch = reservationDtoBuilder()
                .withUserId(owner.getId())
                .withSpaceId(space.getId())
                .withQrCode("SAME-QR")
                .build();

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            ReservationDTO out = service.update(8080L, patch);
            assertThat(out).isNotNull();
            // no excepción por QR duplicado cuando es la misma reserva
            assertThat(existing.getQrCode()).isEqualTo("SAME-QR");
        }
    }

    @Test
    void markCheckInRejectsPendingReservation() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.PENDING)
                .withStart(LocalDateTime.now().plusHours(1))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder().build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("QR code is not yet active");
        }
    }

    @Test
    void markNoShowTransitionsFromConfirmed() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusHours(2))
                .build();
        reservation.setCheckinAt(null);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            ReservationDTO result = service.markNoShow(reservation.getId());
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_MARKED_NO_SHOW"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void markNoShowRejectsFutureStartTime() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(2))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.markNoShow(reservation.getId()))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("after the reservation start time");
        }
    }

    @Test
    void deleteSoftDeletesReservationBasedOnStatus() {
        Reservation canceled = reservationBuilder()
                .withStatus(ReservationStatus.CANCELED)
                .withUser(userBuilder().withId(555L).build())
                .build();
        when(reservationRepository.findById(canceled.getId())).thenReturn(Optional.of(canceled));
        when(reservationRepository.save(canceled)).thenReturn(canceled);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(555L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            service.delete(canceled.getId());
            assertThat(canceled.getDeletedAt()).isNotNull();
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(555L, UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(auditLogService, times(1)).logEvent(any(), eq("RESERVATION_SOFT_DELETED"),
                    eq(canceled.getId().toString()), any());
        }

        clearInvocations(auditLogService);

        Reservation active = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withUser(userBuilder().withId(111L).build())
                .build();
        when(reservationRepository.findById(active.getId())).thenReturn(Optional.of(active));
        when(reservationRepository.save(active)).thenReturn(active);

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            service.delete(active.getId());
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(auditLogService, times(1)).logEvent(any(), eq("RESERVATION_SOFT_DELETED"),
                    eq(active.getId().toString()), any());
        }
    }

    @Test
    void hardDeleteRemovesReservationWhenStatusAllows() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CANCELED)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.ADMIN))).thenAnswer(invocation -> null);

            service.hardDelete(reservation.getId());
            verify(reservationRepository).delete(reservation);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_HARD_DELETED"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void hardDeleteAllowsAdminRegardlessOfStatus() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.ADMIN)).thenAnswer(invocation -> null);

            // Con la lógica actual, ADMIN puede eliminar cualquier reserva (sin validar estado)
            service.hardDelete(reservation.getId());
            verify(reservationRepository).delete(reservation);
            verify(auditLogService).logEvent(any(), eq("RESERVATION_HARD_DELETED"), eq(reservation.getId().toString()), any());
        }
    }

    @Test
    void findByIdThrowsWhenReservationDeleted() {
        Reservation reservation = reservationBuilder()
                .withDeletedAt(LocalDateTime.now())
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Long deletedOwnerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(deletedOwnerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.findById(reservation.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

        @Test
        void findByIdThrowsWhenReservationNotFound() {
                when(reservationRepository.findById(99999L)).thenReturn(Optional.empty());

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(any(), any(), any())).thenAnswer(inv -> null);
                        assertThatThrownBy(() -> service.findById(99999L))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Reservation with id 99999 not found");
                }
        }

    @Test
    void createFailsWhenUserIdMissing() {
        ReservationDTO request = reservationDtoBuilder().withUserId(null).build();
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(null, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);
            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("User identifier is required");
        }
    }

        @Test
        void createFailsWhenUserNotFound() {
                var dto = reservationDtoBuilder().withUserId(12345L).build();
                when(userRepository.findById(12345L)).thenReturn(Optional.empty());

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(12345L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(inv -> null);

                        assertThatThrownBy(() -> service.create(dto))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("User with id 12345 not found");
                }
        }

    @Test
    void createFailsWhenSpaceNotAvailable() {
        ReservationDTO request = reservationDtoBuilder().build();
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(userBuilder().build()));
        when(spaceRepository.findById(request.getSpaceId())).thenReturn(Optional.of(spaceBuilder().withActive(false).build()));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(request.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Space is not available");
        }
    }

        @Test
        void createFailsWhenSpaceIsDeleted() {
                ReservationDTO request = reservationDtoBuilder().build();
                var deletedSpace = spaceBuilder().build();
                deletedSpace.setDeletedAt(LocalDateTime.now());

                when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(userBuilder().build()));
                when(spaceRepository.findById(request.getSpaceId())).thenReturn(Optional.of(deletedSpace));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(request.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN))
                                        .thenAnswer(invocation -> null);

                        assertThatThrownBy(() -> service.create(request))
                                        .isInstanceOf(BusinessRuleException.class)
                                        .hasMessageContaining("Space is not available");
                }
        }

        @Test
        void createFailsWhenSpaceNotFound() {
                var dto = reservationDtoBuilder().withSpaceId(54321L).build();
                when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(userBuilder().build()));
                when(spaceRepository.findById(54321L)).thenReturn(Optional.empty());

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(dto.getUserId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(inv -> null);

                        assertThatThrownBy(() -> service.create(dto))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Space with id 54321 not found");
                }
        }

    @Test
    void markCheckInRejectsDuplicateAttendee() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(5))
                .withQrCode("QR-CHECK")
                .withAttendeeRecords(List.of(attendeeBuilder().withIdNumber("DUP").build()))
                .withAttendees(1)
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationCheckInRequest request = checkInRequestBuilder().withQrCode("QR-CHECK").withAttendeeId("DUP").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Reservation already reached the maximum number of attendees");
        }
    }

    @Test
    void toDtoOrdersAttendeeRecordsByCheckInTime() {
        ReservationAttendee first = attendeeBuilder().withId(1L).withCheckInAt(LocalDateTime.now().minusMinutes(5)).build();
        ReservationAttendee second = attendeeBuilder().withId(2L).withCheckInAt(LocalDateTime.now().minusMinutes(2)).build();
        Reservation reservation = reservationBuilder()
                .withAttendeeRecords(List.of(second, first))
                .withNotifications(List.of(notificationBuilder().withId(7L).build()))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        Long orderingOwnerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(orderingOwnerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            ReservationDTO dto = service.findById(reservation.getId());
            assertThat(dto.getAttendeeRecords())
                    .extracting(record -> tuple(record.getId(), record.getCheckInAt()))
                    .containsExactly(tuple(1L, first.getCheckInAt()), tuple(2L, second.getCheckInAt()));
        }
    }

    @Test
    void create_withRequiresApproval_forcesPendingStatus() {
        var user = userBuilder().withId(11L).build();
        var space = spaceBuilder().withId(22L).withRequiresApproval(true).build();
        var dto = reservationDtoBuilder()
                .withUserId(user.getId())
                .withSpaceId(space.getId())
                .withStatus(ReservationStatus.CONFIRMED)
                .withQrCode("QR-X")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(user.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            ReservationDTO created = service.create(dto);
            assertThat(created.getStatus()).isEqualTo(ReservationStatus.PENDING);
        }
    }

    @Test
    void create_withRequestedStatusNull_defaultsToConfirmed() {
        var user = userBuilder().withId(12L).build();
        var space = spaceBuilder().withId(23L).withRequiresApproval(false).build();
        var dto = reservationDtoBuilder()
                .withUserId(user.getId())
                .withSpaceId(space.getId())
                .withStatus(null)
                .withQrCode("QR-Y")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(user.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

                        ReservationDTO created = service.create(dto);
                        // Política actual: inicia en PENDING aunque no se especifique estado
                        assertThat(created.getStatus()).isEqualTo(ReservationStatus.PENDING);
        }
    }

    @Test
    void create_withPreApprovalOnApprovalRequiredSpace_throws() {
        var user = userBuilder().withId(13L).build();
        var space = spaceBuilder().withId(24L).withRequiresApproval(true).build();
        var dto = reservationDtoBuilder()
                .withUserId(user.getId())
                .withSpaceId(space.getId())
                .withApprovedByUserId(999L)
                .withQrCode("QR-Z")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(user.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("require approval");
        }
    }

    @Test
    void create_withBlankQr_throws() {
        var user = userBuilder().withId(14L).build();
        var space = spaceBuilder().withId(25L).build();
        var dto = reservationDtoBuilder()
                .withUserId(user.getId())
                .withSpaceId(space.getId())
                .withQrCode(" ")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(user.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("QR code is required");
        }
    }

    @Test
    void markCheckIn_rejectsNullRequest() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusMinutes(1))
                .withQrCode("QR-1")
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Check-in data is required");
        }
    }

    @Test
        void markCheckIn_rejectsWhenQrDoesNotMatch() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().minusMinutes(1))
                .withQrCode("QR-ABC")
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
                        ReservationCheckInRequest req = checkInRequestBuilder().withQrCode("DIFFERENT").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("does not match");
        }
    }

        @Test
        void markCheckIn_rejects_duplicate_attendee_when_capacity_available() {
                // Existing reservation with one attendee registered and capacity for 2
                Reservation existing = reservationBuilder()
                                .withStatus(ReservationStatus.CONFIRMED)
                                .withStart(LocalDateTime.now().minusMinutes(10))
                                .withQrCode("QR-DUP")
                                .withAttendees(2)
                                .withAttendeeRecords(new ArrayList<>(List.of(attendeeBuilder().withIdNumber("DUP").build())))
                                .build();
                when(reservationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);

                        ReservationCheckInRequest req = checkInRequestBuilder()
                                        .withQrCode("QR-DUP")
                                        .withAttendeeId("DUP")
                                        .withFirstName("Jane")
                                        .withLastName("Doe")
                                        .build();

                    assertThatThrownBy(() -> service.markCheckIn(existing.getId(), req))
                            .isInstanceOf(BusinessRuleException.class)
                            .hasMessageContaining("already been registered");
                }
        }

        @Test
        void markCheckIn_initializes_attendeeRecords_when_null() {
                // Force attendeeRecords to null to exercise initialization branch
                Reservation reservation = reservationBuilder()
                                .withStatus(ReservationStatus.CONFIRMED)
                                .withStart(LocalDateTime.now().minusMinutes(5))
                                .withEnd(LocalDateTime.now().plusHours(1))
                                .withQrCode("QR-INIT")
                                .withAttendees(3)
                                .build();
                reservation.setAttendeeRecords(null);
                when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
                when(reservationRepository.save(reservation)).thenAnswer(inv -> inv.getArgument(0));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);

                        ReservationCheckInRequest request = checkInRequestBuilder()
                                        .withQrCode("QR-INIT")
                                        .withFirstName("Nora")
                                        .withLastName("Jones")
                                        .withAttendeeId("ID-NEW")
                                        .build();

                        ReservationDTO dto = service.markCheckIn(reservation.getId(), request);
                        assertThat(dto.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
                        assertThat(reservation.getAttendeeRecords()).isNotNull();
                        assertThat(reservation.getAttendeeRecords()).hasSize(1);
                }
        }

        @Test
        void toDto_handles_both_null_checkin_times() {
                ReservationAttendee a1 = attendeeBuilder().withId(11L).withCheckInAt(null).build();
                ReservationAttendee a2 = attendeeBuilder().withId(22L).withCheckInAt(null).build();
                Reservation reservation = reservationBuilder()
                                .withAttendeeRecords(List.of(a1, a2))
                                .build();
                when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                                        .thenAnswer(inv -> null);

                        ReservationDTO dto = service.findById(reservation.getId());
                        assertThat(dto.getAttendeeRecords()).hasSize(2);
                        // When both are null, comparator returns 0; order should be preserved
                        assertThat(dto.getAttendeeRecords().get(0).getId()).isEqualTo(11L);
                        assertThat(dto.getAttendeeRecords().get(1).getId()).isEqualTo(22L);
                }
        }

        @Test
        void update_sets_notes_and_weatherCheck_when_present_only() {
                User owner = userBuilder().withId(707L).build();
                Space space = spaceBuilder().withId(808L).build();
                Reservation existing = reservationBuilder()
                                .withId(606L)
                                .withUser(owner)
                                .withSpace(space)
                                .withStart(LocalDateTime.now().plusHours(2))
                                .withEnd(LocalDateTime.now().plusHours(3))
                                .build();
                when(reservationRepository.findById(606L)).thenReturn(Optional.of(existing));
                when(reservationRepository.save(existing)).thenReturn(existing);

                ReservationDTO patch = reservationDtoBuilder()
                                .withUserId(owner.getId())
                                .withSpaceId(space.getId())
                                .withStart(null)
                                .withEnd(null)
                                .withQrCode(" ") // en blanco: no actualiza
                                .build();
                // set extras únicamente
                patch.setNotes("nuevas notas");
                patch.setWeatherCheck(new ObjectMapper().createObjectNode().put("ok", true));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(owner.getId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(invocation -> null);

                        ReservationDTO out = service.update(606L, patch);
                            assertThat(out).isNotNull();
                            assertThat(existing.getNotes()).isEqualTo("nuevas notas");
                            assertThat(existing.getWeatherCheck().get("ok").asBoolean()).isTrue();
                        // no cambió start/end ni qr
                        verify(reservationRepository).save(existing);
                        verify(auditLogService).logEvent(any(), eq("RESERVATION_UPDATED"), eq("606"), any());
                }
        }

        @Test
        void findById_allows_when_user_is_null_and_maps_null_userId() {
                Reservation reservation = reservationBuilder()
                                .withUser(null)
                                .build();
                when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        // la llamada interna pasará null como userId
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(null), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(invocation -> null);

                        ReservationDTO dto = service.findById(reservation.getId());
                        assertThat(dto.getUserId()).isNull();
                }
        }

    @Test
    void markCheckIn_rejectsBeforeWindowOpens() {
        Reservation reservation = reservationBuilder()
                .withStatus(ReservationStatus.CONFIRMED)
                .withStart(LocalDateTime.now().plusHours(1))
                .withQrCode("QR-TIME")
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN)).thenAnswer(inv -> null);
            ReservationCheckInRequest req = checkInRequestBuilder().withQrCode("QR-TIME").build();
            assertThatThrownBy(() -> service.markCheckIn(reservation.getId(), req))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be scanned yet");
        }
    }

    @Test
    void toDto_sortsWhenSomeCheckInTimesAreNull() {
        ReservationAttendee nullTime = attendeeBuilder().withId(1L).withCheckInAt(null).build();
        ReservationAttendee hasTime = attendeeBuilder().withId(2L).withCheckInAt(LocalDateTime.now()).build();
        Reservation reservation = reservationBuilder()
                .withAttendeeRecords(List.of(hasTime, nullTime))
                .build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
            mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                    .thenAnswer(inv -> null);
            ReservationDTO dto = service.findById(reservation.getId());
            assertThat(dto.getAttendeeRecords().get(0).getId()).isEqualTo(1L);
        }
    }

    @Test
    void recordAudit_handlesMissingAuthentication() {
        // Force SecurityUtils.getCurrentUserId to throw and ensure audit still logs
        Reservation reservation = reservationBuilder().build();
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(any())).thenReturn(Optional.of(userBuilder().build()));
        when(spaceRepository.findById(any())).thenReturn(Optional.of(spaceBuilder().build()));
        when(reservationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(any(), any(), any())).thenAnswer(inv -> null);
            mocked.when(SecurityUtils::getCurrentUserId).thenThrow(new AuthenticationCredentialsNotFoundException("no auth"));

            service.create(reservationDtoBuilder().withQrCode("REC-AUD").build());
            verify(auditLogService).logEvent(any(), any(), any(), any());
        }
    }

    @Test
    void toDto_maps_rating_and_approvedBy_ids() {
        // Build reservation with rating and approvedBy to ensure dto fields are populated
        var approver = userBuilder().withId(888L).build();
        var reservation = reservationBuilder()
                .withApprovedBy(approver)
                .build();
        // attach rating with id
        finalprojectprogramming.project.models.Rating rating = finalprojectprogramming.project.models.Rating.builder()
                .id(777L)
                .reservation(reservation)
                .user(reservation.getUser())
                .space(reservation.getSpace())
                .score(5)
                .comment("ok")
                .visible(true)
                .build();
        reservation.setRating(rating);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(ownerId), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);

            ReservationDTO dto = service.findById(reservation.getId());
            assertThat(dto.getApprovedByUserId()).isEqualTo(888L);
            assertThat(dto.getRatingId()).isEqualTo(777L);
        }
    }

        @Test
        void cancel_on_already_canceled_returns_same_and_does_not_notify() {
                Reservation reservation = reservationBuilder()
                                .withStatus(ReservationStatus.CANCELED)
                                .build();
                when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                                        .thenAnswer(inv -> null);

                        ReservationDTO out = service.cancel(reservation.getId(), "ignored");
                        assertThat(out.getStatus()).isEqualTo(ReservationStatus.CANCELED);
                        // notificationService should not be called when already canceled
                        verify(notificationService, never()).notifyReservationCanceled(any());
                        verify(reservationRepository, never()).save(any());
                }
        }

        @Test
        void create_withApproverId_when_space_does_not_require_approval_sets_approvedBy() {
                var user = userBuilder().withId(20L).build();
                var approver = userBuilder().withId(99L).build();
                var space = spaceBuilder().withId(30L).withRequiresApproval(false).build();
                var dto = reservationDtoBuilder().withUserId(20L).withSpaceId(30L).withApprovedByUserId(99L)
                                .withQrCode("QR-OK").build();

                when(userRepository.findById(20L)).thenReturn(Optional.of(user));
                when(userRepository.findById(99L)).thenReturn(Optional.of(approver));
                when(spaceRepository.findById(30L)).thenReturn(Optional.of(space));
                when(reservationRepository.findAll()).thenReturn(List.of());
                when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(20L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(inv -> null);

                        ReservationDTO created = service.create(dto);
                        assertThat(created.getApprovedByUserId()).isEqualTo(99L);
                }
        }

        @Test
        void createFailsWhenSpaceIdMissing() {
                var dto = reservationDtoBuilder().withSpaceId(null).build();
                when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(userBuilder().build()));
                try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                        mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(dto.getUserId()), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                        .thenAnswer(inv -> null);
                        assertThatThrownBy(() -> service.create(dto))
                                        .isInstanceOf(BusinessRuleException.class)
                                        .hasMessageContaining("Space identifier is required");
                }
        }

                @Test
                void create_rejects_when_start_time_less_than_60_minutes() {
                        var user = userBuilder().withId(100L).build();
                        var space = spaceBuilder().withId(200L).build();

                        var startCR = java.time.LocalDateTime.now(java.time.ZoneId.of("America/Costa_Rica")).plusMinutes(30);
                        var dto = reservationDtoBuilder()
                                        .withUserId(100L)
                                        .withSpaceId(200L)
                                        .withStart(startCR)
                                        .withEnd(startCR.plusHours(1))
                                        .withQrCode("QR-CR-30")
                                        .build();

                        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
                        when(spaceRepository.findById(200L)).thenReturn(Optional.of(space));
                        when(reservationRepository.findAll()).thenReturn(List.of());

                        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                                mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(100L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                                .thenAnswer(inv -> null);

                                assertThatThrownBy(() -> service.create(dto))
                                                .isInstanceOf(BusinessRuleException.class)
                                                .hasMessageContaining("60 minutos");
                        }
                }

                @Test
                void cancel_by_admin_skips_cancellation_policy() {
                        Reservation reservation = reservationBuilder()
                                        .withStatus(ReservationStatus.CONFIRMED)
                                        .withStart(LocalDateTime.now().plusHours(2))
                                        .build();
                        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
                        when(reservationRepository.save(reservation)).thenReturn(reservation);

                        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
                        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                                mocked.when(() -> SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN))
                                                .thenAnswer(inv -> null);
                                mocked.when(() -> SecurityUtils.hasAny(UserRole.ADMIN)).thenReturn(true);

                                ReservationDTO out = service.cancel(reservation.getId(), "porque sí");
                                assertThat(out.getStatus()).isEqualTo(ReservationStatus.CANCELED);
                                // Política no debe ser invocada por ADMIN
                                verify(cancellationPolicy, never()).assertCancellationAllowed(any());
                                verify(notificationService).notifyReservationCanceled(reservation);
                        }
                }

                @Test
                void recordAudit_swallows_exceptions_from_audit_service() {
                        var user = userBuilder().withId(310L).build();
                        var space = spaceBuilder().withId(410L).build();
                        var dto = reservationDtoBuilder()
                                        .withUserId(310L)
                                        .withSpaceId(410L)
                                        .withQrCode("QR-AUD-ERR")
                                        .withStart(LocalDateTime.now().plusHours(2))
                                        .withEnd(LocalDateTime.now().plusHours(3))
                                        .build();

                        when(userRepository.findById(310L)).thenReturn(Optional.of(user));
                        when(spaceRepository.findById(410L)).thenReturn(Optional.of(space));
                        when(reservationRepository.findAll()).thenReturn(List.of());
                        // Hacer que save devuelva la entidad con id para que audit tenga entityId
                        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
                                Reservation r = inv.getArgument(0);
                                r.setId(9999L);
                                return r;
                        });
                        // Forzar excepción en el servicio de auditoría
                        doThrow(new RuntimeException("db down")).when(auditLogService)
                                        .logEvent(any(), any(), any(), any());

                        try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
                                mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(310L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                                                .thenAnswer(inv -> null);
                                // La operación create no debe lanzar aunque falle audit
                                ReservationDTO created = service.create(dto);
                                assertThat(created.getId()).isEqualTo(9999L);
                        }
                }
}
